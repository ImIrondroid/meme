package com.kakao.iron.ui.detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentDialogBinding
import javax.inject.Singleton

@Singleton
class LoadingFragment : DialogFragment() {

    private lateinit var mBinding: FragmentDialogBinding

    override fun onStart() {
        super.onStart()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dialog, container, false)
        return mBinding.root
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
    }
}