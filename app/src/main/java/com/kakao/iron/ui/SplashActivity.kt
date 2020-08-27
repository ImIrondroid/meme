package com.kakao.iron.ui

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.kakao.iron.R
import com.kakao.iron.databinding.ActivitySplashBinding
import com.kakao.iron.ui.base.BaseActivity
import com.kakao.iron.ui.search.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun getLayoutId(): Int = R.layout.activity_splash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doBounceAnimation()
        launch {
            delay(SPLASH_TIME)
            toMain()
        }
    }

    private fun toMain() {
        nextActivity(kClass = MainActivity::class, clearTask = true)
    }

    private fun doBounceAnimation() {
        mBinding.let {
            it.TopTitle1.animation = AnimationUtils.loadAnimation(this, R.anim.anim_bounce)
            it.TopTitle2.animation = AnimationUtils.loadAnimation(this, R.anim.anim_bounce)
            it.TopTitle3.animation = AnimationUtils.loadAnimation(this, R.anim.anim_bounce)
            it.TopTitle4.animation = AnimationUtils.loadAnimation(this, R.anim.anim_bounce)
            it.TopTitle1.animate()
            it.TopTitle2.animate()
            it.TopTitle3.animate()
            it.TopTitle4.animate()
        }
    }

    companion object {
        private const val SPLASH_TIME = 3000L
    }
}