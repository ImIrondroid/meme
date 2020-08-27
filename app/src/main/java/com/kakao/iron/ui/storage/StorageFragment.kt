package com.kakao.iron.ui.storage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.SimpleItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentStorageBinding
import com.kakao.iron.ui.base.BaseFragment
import com.kakao.iron.util.extension.showToast
import kotlinx.android.synthetic.main.fragment_storage.*
import kotlinx.android.synthetic.main.fragment_storage.fab
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class StorageFragment : BaseFragment<FragmentStorageBinding>() {

    private val mManageViewModel: ManageViewModel by sharedViewModel()

    private lateinit var mStorageAdapter: StorageAdapter
    private lateinit var mArrayAdapter: ArrayAdapter<String>

    override fun getLayoutId(): Int = R.layout.fragment_storage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mArrayAdapter = ArrayAdapter<String>(requireActivity(), R.layout.item_dropdown_search, resources.getStringArray(R.array.array_storgage_state))
        mStorageAdapter = StorageAdapter().apply {
            setOnItemSelectedListener { _, item, position ->
                when(mManageViewModel.stateWatcher.get()) {
                    ActionState.Normal -> { showShareDialog(item) }
                    ActionState.Add -> { mManageViewModel.onItemSelected(position) }
                    ActionState.Delete -> {
                        val dialog = MaterialDialog(requireActivity())
                        dialog.show {
                            title(R.string.text_delete)
                            icon(R.drawable.vector_emphasize)
                            cornerRadius(16f)
                            negativeButton(R.string.text_cancel_short) { requireActivity().showToast(R.string.text_cancel_long) }
                            positiveButton(R.string.text_confirm_short) {
                                mManageViewModel.onDelete(item)
                                requireContext().showToast(R.string.text_delete_complete)
                            }
                        }
                    }
                }
            }
        }
        mBinding.apply {
            viewModel = mManageViewModel
            rcvStorage.let {
                it.adapter = mStorageAdapter
                (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            dropdown.let {
                it.setAdapter(mArrayAdapter)
                it.onItemClickListener = itemClickListener
            }
        }

        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        fab.setOnClickListener {
            val dialog = MaterialDialog(it.context)
            dialog.show {
                title(R.string.text_register_message)
                icon(R.drawable.vector_save)
                cornerRadius(16f)
                negativeButton(R.string.text_cancel_short) {
                    context.showToast(R.string.text_cancel_long) }
                positiveButton(R.string.text_confirm_short) {
                    mManageViewModel.onAdd()
                    mBinding.dropdown.setText(getString(R.string.text_screen_default), false)
                    context.showToast(R.string.text_register_complete)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_FROM_STORAGE -> {
                    val query = data?.getStringExtra("query") ?: getString(R.string.text_default_query)
                    mManageViewModel.currentLabelQuery = query
                    mManageViewModel.onSelect(query)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //상세페이지에서 보관함 이동시 초기화
        val isSelectedAtLabelTab = tabLayout.getTabAt(3)?.isSelected ?: false
        if(isSelectedAtLabelTab.not()) {
            mManageViewModel.onStart()
        }
    }

    private val itemClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
            when(position) {
                0 -> mManageViewModel.onOptionChanged(ActionState.Normal)
                1 -> mManageViewModel.onOptionChanged(ActionState.Add)
                2 -> mManageViewModel.onOptionChanged(ActionState.Delete)
            }
        }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            when(tab?.position) {
                3 -> {
                    ViewCompat.setTransitionName(tab.view, getString(R.string.transitionName))
                    val option = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), tab.view, getString(R.string.transitionName))
                    nextActivityForResult(
                        kClass = LabelSearchActivity::class,
                        requestCode = REQUEST_CODE_FROM_STORAGE,
                        option = option
                    )
                }
            }
        }
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> {
                    mManageViewModel.currentTab = ManageViewModel.Tab.OtherTab
                    mManageViewModel.onItemSortChanged(StorageState.SortDescending)
                }
                1 -> {
                    mManageViewModel.currentTab = ManageViewModel.Tab.OtherTab
                    mManageViewModel.onItemSortChanged(StorageState.SortAscending)
                }
                2 -> {
                    mManageViewModel.currentTab = ManageViewModel.Tab.OtherTab
                    mManageViewModel.onItemSortChanged(StorageState.SortQuery)
                }
                3 -> {
                    mManageViewModel.currentTab = ManageViewModel.Tab.LabelTab
                    ViewCompat.setTransitionName(tab.view, getString(R.string.transitionName))
                    val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        tab.view,
                        getString(R.string.transitionName)
                    )
                    nextActivityForResult(
                        kClass = LabelSearchActivity::class,
                        requestCode = REQUEST_CODE_FROM_STORAGE,
                        option = option
                    )
                }
            }
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) { }
    }

    private fun showShareDialog(data: StorageData) {
        val existFragment = childFragmentManager.findFragmentByTag(BottomStorageFragment.TAG) as? BottomStorageFragment
        if(existFragment == null) {
            BottomStorageFragment().apply {
                this.arguments = Bundle().apply { putSerializable("data", data) }
            }.show(childFragmentManager, BottomStorageFragment.TAG)
        } else {
            if(!existFragment.showsDialog) {
                existFragment.show(childFragmentManager, BottomStorageFragment.TAG)
            }
        }
    }

    fun <T : Activity> nextActivityForResult(
        kClass: KClass<T>,
        requestCode: Int,
        singleTop: Boolean = false,
        option: ActivityOptionsCompat
    ) {
        startActivityForResult(Intent(context, kClass.java).apply {
            if (singleTop) {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }, requestCode, option.toBundle())
    }

    companion object {
        private const val REQUEST_CODE_FROM_STORAGE = 2000
    }
}