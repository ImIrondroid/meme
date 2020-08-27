package com.kakao.iron.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.iron.R
import com.kakao.iron.databinding.ActivityMainBinding
import com.kakao.iron.ui.base.BaseActivity
import com.kakao.iron.ui.camera.CameraViewModel
import com.kakao.iron.ui.camera.FileState
import com.kakao.iron.ui.detail.DetailActivity.Companion.RESULT_OK_FROM_DETAIL
import com.kakao.iron.ui.storage.ActionState
import com.kakao.iron.ui.storage.ManageViewModel
import com.kakao.iron.util.KakaoLinkProvider
import com.kakao.iron.util.extension.showToast
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mSearchViewModel: SearchViewModel by viewModel()
    private val mManageViewModel: ManageViewModel by viewModel()
    private val mCameraViewModel: CameraViewModel by viewModel()

    private lateinit var mPagerAdapter: FragmentAdapter
    private lateinit var context: Context

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this

        setUpPagerWithTab()
    }

    override fun onStart() {
        super.onStart()
        if(intent.action == Intent.ACTION_VIEW) {
            val eventCode = intent.data?.getQueryParameter("event") ?: 0
            if(eventCode == KakaoLinkProvider.KAKAO_EVENT_CODE) {
                tabLayout.post {
                    Log.e("onStart", eventCode.toString())
                    tabLayout.selectTab(tabLayout.getTabAt(2), true)
                }
            }
        }
    }

    private fun setUpPagerWithTab() {
        val tabIconList = listOf(R.drawable.vector_search, R.drawable.vector_camera, R.drawable.vector_storage, R.drawable.vector_special)
        tabLayout.addOnTabSelectedListener(tabSelectedListener)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.setIcon(tabIconList[position])
        }.attach()
    }

    override fun init() {
        mPagerAdapter = FragmentAdapter(this)
    }

    override fun onBind() {
        pager.let {
            it.adapter = mPagerAdapter
            it.offscreenPageLimit = 4
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK_FROM_DETAIL) {
            tabLayout.post {
                tabLayout.selectTab(tabLayout.getTabAt(2), true)
            }
        }
    }

    override fun onBackPressed() {
        val isMainTab = tabLayout.getTabAt(0)?.isSelected ?: false
        when(isMainTab) {
            false -> {
                val isCameraTab = tabLayout.getTabAt(1)?.isSelected ?: false
                when(isCameraTab) {
                    true -> {
                        when(mCameraViewModel.stateWatcher.get() != FileState.Normal) {
                            true -> {
                                val dialog = MaterialDialog(this)
                                dialog.show {
                                    title(R.string.text_back)
                                    icon(R.drawable.vector_emphasize)
                                    cornerRadius(16f)
                                    negativeButton(R.string.text_cancel_short) { showToast(R.string.text_cancel_long) }
                                    positiveButton(R.string.text_confirm_short) { mCameraViewModel.onCancel() }
                                }
                            }
                            false -> {
                                tabLayout.selectTab(tabLayout.getTabAt(0), true)
                            }
                        }
                    }
                    false -> {
                        //StorageTab
                        when(mManageViewModel.stateWatcher.get() != ActionState.Normal) {
                            true -> {
                                val dialog = MaterialDialog(this)
                                dialog.show {
                                    title(R.string.text_back)
                                    icon(R.drawable.vector_emphasize)
                                    cornerRadius(16f)
                                    negativeButton(R.string.text_cancel_short) { showToast(R.string.text_cancel_long) }
                                    positiveButton(R.string.text_confirm_short) { mManageViewModel.onCancel() }
                                }
                            }
                            false -> {
                                tabLayout.selectTab(tabLayout.getTabAt(0), true)
                            }
                        }
                    }
                }
            }
            true -> {
                val dialog = MaterialDialog(this)
                dialog.show {
                    title(R.string.text_quit)
                    icon(R.drawable.vector_emphasize)
                    cornerRadius(16f)
                    negativeButton(R.string.text_cancel_short) { showToast(R.string.text_cancel_long) }
                    positiveButton(R.string.text_confirm_short) { super.onBackPressed() }
                }
            }
        }
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabSelected(tab: TabLayout.Tab?) {
            mManageViewModel.onCancel()
            mCameraViewModel.onCancel()
        }
    }

    companion object {
        const val REQUEST_CODE_FROM_MAIN = 1000
    }
}