package com.kakao.iron.ui.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kakao.iron.ui.camera.CameraFragment
import com.kakao.iron.ui.special.SpecialFragment
import com.kakao.iron.ui.storage.StorageFragment

class FragmentAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val list =
        listOf<Fragment>(SearchFragment(), CameraFragment(), StorageFragment(), SpecialFragment())

    override fun getItemCount(): Int = MAX_FRAGMENT_SIZE

    override fun createFragment(position: Int): Fragment = list[position]

    companion object {
        const val MAX_FRAGMENT_SIZE = 4
    }
}

