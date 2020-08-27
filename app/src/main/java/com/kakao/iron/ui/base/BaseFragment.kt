package com.kakao.iron.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.kakao.iron.util.coroutine.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<VDB: ViewDataBinding>
    : Fragment(), CoroutineScope {

    protected lateinit var mBinding: VDB

    private val dispatchers: Dispatchers by inject()
    private val job: Job = Job()

    override val coroutineContext: CoroutineContext = dispatchers.main() + job

    @LayoutRes
    abstract fun getLayoutId() : Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        mBinding.apply {
            lifecycleOwner = this@BaseFragment
            executePendingBindings()
        }
        return mBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    open fun init() {}
    open fun onBind() {}
}