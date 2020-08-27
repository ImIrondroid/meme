package com.kakao.iron.ui.camera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentBottomCameraBinding
import com.kakao.iron.ui.detail.DetailActivity
import com.kakao.iron.ui.detail.LoadingFragment
import com.kakao.iron.ui.detail.SaveViewModel
import com.kakao.iron.ui.search.SearchData
import com.kakao.iron.ui.storage.ManageViewModel
import com.kakao.iron.util.exifOrientationToDegrees
import com.kakao.iron.util.extension.showToast
import com.kakao.iron.util.rotate
import kotlinx.android.synthetic.main.fragment_bottom_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import javax.inject.Singleton

@Singleton
class BottomCameraFragment : BottomSheetDialogFragment() {

    private val mSaveViewModel: SaveViewModel by sharedViewModel()
    private val mManageViewModel: ManageViewModel by sharedViewModel()

    private lateinit var mBinding: FragmentBottomCameraBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as View).apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_camera, container, false)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments!=null) {
            val filePath: String = arguments?.getString("path") ?: "empty"
            mBinding.filePath = filePath

            save.setOnClickListener {
                val exif = ExifInterface(filePath)
                val exifOrientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val exifDegree: Int = exifOrientationToDegrees(exifOrientation)
                val bitmap: Bitmap = BitmapFactory.decodeFile(filePath)
                val rotateBitmap = rotate(bitmap, exifDegree.toFloat())
                lifecycleScope.launch {
                    onSave(
                        filePath = filePath,
                        bitmap = rotateBitmap
                    )
                    dismiss()
                }
            }
        } else {
            val fragment = this
            save.setOnClickListener {
                val dialog = MaterialDialog(requireActivity())
                dialog.show {
                    title(R.string.text_image_cant_load)
                    icon(R.drawable.vector_emphasize)
                    cornerRadius(16f)
                    positiveButton(R.string.text_confirm_short) {
                        dismiss()
                        fragment.dismiss()
                    }
                }
            }
        }

        onFadeIn()

        cancel.setOnClickListener {
            onFadeOut()
        }
    }

    private suspend fun onSave(filePath: String, bitmap: Bitmap) {
        showLoading()

        val textList = processRecognize(bitmap)
        val labelList = processLabeling(bitmap)
        Log.e("textList", textList.toString())
        Log.e("labelList", labelList.toString())
        val isEmpty = mSaveViewModel.checkForDuplicate(textList, labelList)
        when(isEmpty) {
            true -> {
                mSaveViewModel.onInsert(
                    filePath = filePath,
                    form = 1,
                    query = "camera" + getString(R.string.suffix),
                    data = SearchData(collection = "camera"),
                    textList = textList,
                    labelList = labelList
                )
                mManageViewModel.onStart()
                context?.showToast(R.string.text_store_complete)
            }
            false -> {
                context?.showToast(R.string.text_store_already_done)
            }
        }

        hideLoading()
    }

    private suspend fun processRecognize(bitmap: Bitmap): List<String> {
        val originalList = mutableListOf<String>()
        val resultList = mutableListOf<String>()
        withContext(Dispatchers.IO) {
            val text = textRecognize(bitmap)
            text.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    originalList.add(line.text)
                }
            }
            originalList.map {
                val result = identify(it)
                if (TextUtils.equals("en", result)) {
                    val translateWord = translate(it)
                    resultList.add(translateWord)
                }
            }
        }
        return resultList
    }

    private suspend fun processLabeling(bitmap: Bitmap): List<String> {
        val originalList = mutableListOf<String>()
        val resultList = mutableListOf<String>()
        withContext(Dispatchers.IO) {
            val labels = imageLabeling(bitmap)
            labels.forEach {
                val subText = it.text
                if(subText.length >= DetailActivity.TEXT_MINIMUM_LENGTH) {
                    originalList.add(subText)
                }
            }
            originalList.map {
                val result = identify(it)
                if (TextUtils.equals("en", result)) {
                    val translateWord = translate(it)
                    resultList.add(translateWord)
                }
            }
        }
        return resultList
    }

    private fun textRecognize(bitmap: Bitmap): Text {
        val image = InputImage.fromBitmap(bitmap, 0)
        val task = TextRecognition.getClient().process(image)
        return Tasks.await(task)
    }

    private fun imageLabeling(bitmap: Bitmap): List<ImageLabel> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val task = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS).process(image)
        return Tasks.await(task)
    }

    private fun identify(text: String): String {
        val languageIdentifier = LanguageIdentification
            .getClient(
                LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.4f)
                .build())
        val task = languageIdentifier.identifyLanguage(text)
        return Tasks.await(task)
    }

    private fun translate(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        val task = Translation.getClient(options)
            .downloadModelIfNeeded()
            .onSuccessTask {
                translator.translate(text)
            }
        return Tasks.await(task)
    }

    private fun onFadeIn() {
        group.let {
            it.visibility = View.GONE
            it.alpha = 0f
            it.visibility = View.VISIBLE
            it.animate().alpha(1f).duration = 500L
        }
    }

    private fun onFadeOut() {
        group.let {
            it.alpha = 1f
            it.animate().alpha(0f).setDuration(300L).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    group.visibility = View.GONE
                    dismiss()
                }
            })
        }
    }

    private fun showLoading() {
        val existFragment = requireFragmentManager()
            .findFragmentByTag(LoadingFragment.TAG) as? DialogFragment
        if (existFragment == null) {
            LoadingFragment()
                .show(requireFragmentManager(), LoadingFragment.TAG)
        } else {
            if (!existFragment.showsDialog) {
                existFragment
                    .show(requireFragmentManager(), LoadingFragment.TAG)
            }
        }
    }

    private fun hideLoading() {
        val existFragment = requireFragmentManager()
            .findFragmentByTag(LoadingFragment.TAG) as? LoadingFragment
            ?: return
        if (existFragment.showsDialog) {
            existFragment.dismiss()
        }
    }

    companion object {
        const val TAG = "BottomCameraFragment"
    }
}