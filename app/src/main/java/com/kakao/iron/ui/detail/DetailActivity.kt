package com.kakao.iron.ui.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import androidx.constraintlayout.widget.ConstraintSet
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.tasks.Tasks
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
import com.kakao.iron.databinding.ActivityDetailBinding
import com.kakao.iron.ui.base.BaseActivity
import com.kakao.iron.ui.search.SearchData
import com.kakao.iron.util.coroutine.Dispatchers
import com.kakao.iron.util.extension.showToast
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.*
import okio.buffer
import okio.sink
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.*

class DetailActivity : BaseActivity<ActivityDetailBinding>() {

    private val dispatchers: Dispatchers by inject()

    private val mSaveViewModel: SaveViewModel by viewModel()

    private lateinit var context: Context
    private lateinit var mQuery: String
    private lateinit var mData: SearchData

    override fun getLayoutId(): Int = R.layout.activity_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFullImage()

        val data =
        save.setOnClickListener {
            val dialog = MaterialDialog(context)
            dialog.show {
                title(R.string.text_store_message)
                icon(R.drawable.vector_save)
                cornerRadius(16f)
                negativeButton(R.string.text_cancel_short) { showToast(R.string.text_cancel_long) }
                positiveButton(R.string.text_confirm_short) { onSave() }
            }
        }

        back.setOnClickListener {
            finish()
        }

        next.setOnClickListener {
            val dialog = MaterialDialog(context)
            dialog.show {
                title(R.string.text_store_move)
                icon(R.drawable.vector_next)
                cornerRadius(16f)
                negativeButton(R.string.text_cancel_short) { showToast(R.string.text_cancel_long) }
                positiveButton(R.string.text_confirm_short) {
                    setResult(RESULT_OK_FROM_DETAIL)
                    finish()
                }
            }
        }

        news.setOnClickListener {
            showShareDialog(mData.documentUrl)
        }
    }

    override fun init() {
        context = this
        mQuery = intent
            ?.getStringExtra("query") ?: getString(R.string.text_default_query)
        mData = intent
            ?.getParcelableExtra("data") as? SearchData
            ?: SearchData()
    }

    override fun onBind() {
        mBinding.data = mData
    }

    private fun showShareDialog(docUrl: String) {
        val existFragment = supportFragmentManager.findFragmentByTag(BottomNewsFragment.TAG) as? BottomNewsFragment
        if(existFragment == null) {
            BottomNewsFragment().apply {
                this.arguments = Bundle().apply { putString("url", docUrl) }
            }.show(supportFragmentManager, BottomNewsFragment.TAG)
        } else {
            if(!existFragment.showsDialog) {
                existFragment.show(supportFragmentManager, BottomNewsFragment.TAG)
            }
        }
    }

    private fun loadFullImage() {
        Picasso
            .get()
            .load(mData.imageUrl)
            .into(detail_image)
        val set = ConstraintSet()
        val ratio = String.format("%d: %d", mData.imageWidth, mData.imageHeight)
        set.let {
            it.clone(detail_constraint)
            it.setDimensionRatio(card_image.id, ratio)
            it.setDimensionRatio(detail_image.id, ratio)
            it.applyTo(detail_constraint)
        }
    }

    private fun onSave() {
        launch {

            //동일한 이미지 저장되어있는지 체크
            val isEmpty = mSaveViewModel.checkForDuplicate(mQuery, mData)
            if(isEmpty.not()) {
                //이미 동일한 이미지가 있다면 종료
                showToast(R.string.text_store_already_done)
                return@launch
            }

            showLoading()

            var mBitmap: Bitmap? = null
            Picasso
                .get()
                .load(mData.imageUrl)
                .into(object: Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        mBitmap = bitmap
                    }
                })

            when(mBitmap) {
                null -> { showToast(R.string.text_store_failed) }
                else -> {
                    val textList = processRecognize(mBitmap!!)
                    val labelList = processLabeling(mBitmap!!)
                    mSaveViewModel.onInsert(
                        form = 0,
                        query = mQuery,
                        data = mData,
                        textList = textList,
                        labelList = labelList
                    )
                    showToast(R.string.text_store_complete)
                }
            }

            //이미지를 불러올 수 있는지 체크
            /*val filePath: String? = saveImage(mData.imageUrl)
            when(filePath.isNullOrEmpty()) {
                true -> {
                    //404
                    showToast(R.string.text_image_cant_load)
                    return@launch
                }
                false -> showLoading()
            }

            val bitmap: Bitmap? = getBitmap(filePath)*/

            hideLoading()
        }
    }

    //////////////////////////////////// 이미지 저장 및 확인 ///////////////////////////////////////

    private fun saveImage(imageUrl: String): String? {
        var mBitmap: Bitmap? = null
        Picasso
            .get()
            .load(imageUrl)
            .into(object: Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) { mBitmap = bitmap }
            })
        return when(mBitmap == null) {
            true -> null
            false -> {
                runCatching {
                    val nowTime = Date(System.currentTimeMillis()).time
                    val fileName = "image_${nowTime}.jpg"
                    val filePath = cacheDir.path + "/" + fileName
                    File(filePath).also {
                        it.createNewFile()
                        it.sink().buffer().use { sink ->
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            mBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            sink.write(byteArrayOutputStream.toByteArray())
                        }
                    }
                    filePath
                }.getOrNull()
            }
        }
    }

    private suspend fun getBitmap(filePath: String): Bitmap? {
        return withContext(dispatchers.single()) {
            runCatching {
                BitmapFactory.decodeFile(filePath)
            }.getOrNull()
        }
    }

    //////////////////////////////////// ML KIT ///////////////////////////////////////

    private suspend fun processRecognize(bitmap: Bitmap): List<String> {
        val originalList = mutableListOf<String>()
        val resultList = mutableListOf<String>()
        withContext(dispatchers.io()) {
            val text = textRecognize(bitmap)
            text.textBlocks.forEach {
                originalList.add(it.text)
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
        withContext(dispatchers.io()) {
            val labels = imageLabeling(bitmap)
            labels.forEach {
                val subText = it.text
                if(subText.length >= TEXT_MINIMUM_LENGTH) {
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
            .getClient(LanguageIdentificationOptions.Builder()
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

    companion object {
        const val TEXT_MINIMUM_LENGTH = 2
        const val RESULT_OK_FROM_DETAIL = 3002
    }
}
