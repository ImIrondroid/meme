package com.kakao.iron.ui.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentBottomStorageBinding
import com.kakao.iron.util.KakaoLinkProvider
import kotlinx.android.synthetic.main.fragment_bottom_storage.*
import java.io.File
import java.lang.StringBuilder
import javax.inject.Singleton

@Singleton
class BottomStorageFragment : BottomSheetDialogFragment() {

    private lateinit var mBinding: FragmentBottomStorageBinding

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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_storage, container, false)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments!=null) {
            val data: StorageData = arguments?.getSerializable("data") as StorageData
            mBinding.let { it ->
                val extractText = replaceLine(data.text)
                it.Text.text = when(TextUtils.equals("[]", extractText)) {
                    true -> getString(R.string.text_nothing)
                    false -> extractText
                }

                val labelText = replace(data.label)
                it.Label.text = when(TextUtils.equals("[]", labelText)) {
                    true -> getString(R.string.text_nothing)
                    false -> labelText
                }

                it.share.setOnClickListener { view ->
                    KakaoLinkProvider.sendKakaoLink(view.context, data)
                    dismiss()
                }
            }

            when(data.form) {
                0 -> {
                    val set = ConstraintSet()
                    val ratio = String.format("%d: %d", data.imageWidth, data.imageHeight)
                    Glide
                        .with(requireActivity())
                        .load(data.imageUrl)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(image)
                    set.apply {
                        clone(bottom_constraint)
                        setDimensionRatio(card_image.id, ratio)
                        setDimensionRatio(image.id, ratio)
                        applyTo(bottom_constraint)
                    }
                }
                1 -> {
                    Glide
                        .with(requireActivity())
                        .load(File(data.filePath))
                        .error(R.drawable.ic_launcher_foreground)
                        .into(image)
                }
            }
        }

        onFadeIn()

        cancel.setOnClickListener {
            onFadeOut()
        }
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

    private fun replace(list: List<String>): String {
        return list.toString()
            .replace("[^\uAC00-\uD7A3]".toRegex(), " ")
            .split(" ")
            .filter { it.isNotBlank() && it.isNotEmpty() }
            .toString()
    }

    private fun replaceLine(list: List<String>): String {
        val map = linkedMapOf<String, Int>()
        list.forEach {
            if(map[it] == null) map[it] = 1
            else map[it] = map[it]?.plus(1) as Int
        }
        return map.keys
            .map { it.replace("[^\uAC00-\uD7A3]".toRegex(), " ").trim() }
            .filter { it.length <= 16 && it.isNotEmpty() }
            .toString()
    }

    companion object {
        const val TAG = "BottomStorageFragment"
    }
}