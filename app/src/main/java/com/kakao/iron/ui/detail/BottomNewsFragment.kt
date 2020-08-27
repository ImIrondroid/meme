package com.kakao.iron.ui.detail

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentBottomNewsBinding
import kotlinx.android.synthetic.main.fragment_bottom_news.*
import javax.inject.Singleton

@Singleton
class BottomNewsFragment : BottomSheetDialogFragment() {

    private lateinit var mBinding: FragmentBottomNewsBinding

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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_news, container, false)
        return mBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val docUrl = arguments?.getString("url") ?: "empty"
        if(docUrl.isNotEmpty()) {
            webView.let { web ->
                web.webViewClient = WebViewClient()
                web.settings.let {
                    it.javaScriptEnabled = true
                    it.useWideViewPort = true
                    it.useWideViewPort = true
                    it.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                }
                web.loadUrl(docUrl)
            }
        }

        back.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "BottomNewsFragment"
    }
}
