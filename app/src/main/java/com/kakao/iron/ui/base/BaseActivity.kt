package com.kakao.iron.ui.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.kakao.iron.ui.detail.LoadingFragment
import com.kakao.iron.util.coroutine.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

abstract class BaseActivity<VDB : ViewDataBinding> :
    AppCompatActivity(), CoroutineScope {

    protected lateinit var mBinding: VDB

    protected val job: Job = Job()
    private val dispatchers: Dispatchers by inject()

    override val coroutineContext: CoroutineContext = dispatchers.main() + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        init()

        val mLayoutResId = getLayoutId()
        mBinding = DataBindingUtil.setContentView<VDB>(this, mLayoutResId)
        mBinding.apply {
            lifecycleOwner = this@BaseActivity
            executePendingBindings()
        }

        onBind()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    open fun init() {}
    open fun onBind() {}

    fun <T : Activity> nextActivity(
        kClass: KClass<T>,
        bundle: Bundle? = null,
        singleTop: Boolean = false,
        clearTask: Boolean = false
    ) {
        startActivity(Intent(this, kClass.java).apply {
            bundle?.let(this::putExtras)
            if (singleTop) {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            if (clearTask) {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        })
    }

    fun <T : Activity> nextActivity(
        kClass: KClass<T>,
        bundle: Bundle? = null,
        singleTop: Boolean = false,
        option: ActivityOptionsCompat
    ) {
        startActivity(Intent(this, kClass.java).apply {
            bundle?.let(this::putExtras)
            if (singleTop) {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }, option.toBundle())
    }

    fun <T : Activity> nextActivityForResult(
        kClass: KClass<T>,
        requestCode: Int,
        singleTop: Boolean = false,
        option: ActivityOptionsCompat
    ) {
        startActivityForResult(Intent(this, kClass.java).apply {
            if (singleTop) {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }, requestCode, option.toBundle())
    }

    fun <T : Activity> nextActivityForResult(
        kClass: KClass<T>,
        bundle: Bundle? = null,
        requestCode: Int,
        singleTop: Boolean = false,
        option: ActivityOptionsCompat
    ) {
        startActivityForResult(Intent(this, kClass.java).apply {
            bundle?.let(this::putExtras)
            if (singleTop) {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }, requestCode, option.toBundle())
    }

    fun showLoading() {
        val existFragment = supportFragmentManager
            .findFragmentByTag(LoadingFragment.TAG) as? DialogFragment
        if (existFragment == null) {
            LoadingFragment()
                .show(supportFragmentManager, LoadingFragment.TAG)
        } else {
            if (!existFragment.showsDialog) {
                existFragment
                    .show(supportFragmentManager, LoadingFragment.TAG)
            }
        }
    }

    fun hideLoading() {
        val existFragment = supportFragmentManager
            .findFragmentByTag(LoadingFragment.TAG) as? LoadingFragment
            ?: return
        if (existFragment.showsDialog) {
            existFragment.dismiss()
        }
    }
}