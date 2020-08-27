package com.kakao.iron.ui.special

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.ExperimentalPagingApi
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentSpecialBinding
import com.kakao.iron.ui.base.BaseFragment
import com.kakao.iron.ui.storage.BottomStorageFragment
import com.kakao.iron.ui.storage.ManageViewModel
import com.kakao.iron.ui.storage.StorageData
import kotlinx.android.synthetic.main.fragment_special.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SpecialFragment : BaseFragment<FragmentSpecialBinding>() {

    private val mManageViewModel: ManageViewModel by sharedViewModel()

    private lateinit var mSpecialAdapter: SpecialAdapter

    override fun getLayoutId(): Int = R.layout.fragment_special

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSpecialAdapter = SpecialAdapter().apply {
            setOnItemSelectedListener { _, item, _ -> showShareDialog(item) }
            addDataRefreshListener {
                when (it) {
                    true -> {
                        rcv_special.visibility = View.INVISIBLE
                        _rcv_text.visibility = View.VISIBLE
                    }
                    false -> {
                        rcv_special.visibility = View.VISIBLE
                        _rcv_text.visibility = View.INVISIBLE
                    }
                }
            }
        }

        mBinding.apply {
            rcvSpecial.adapter = mSpecialAdapter
        }

        observeSpecialData()
    }

    @ExperimentalPagingApi
    private fun observeSpecialData() {
        mManageViewModel.specialList.observe(viewLifecycleOwner, Observer {
            launch {
                mSpecialAdapter.submitData(it)
            }
        })
        launch {
            mSpecialAdapter.dataRefreshFlow.collect {
                rcv_special.scrollToPosition(0)
            }
        }
    }

    private fun showShareDialog(data: StorageData) {
        val existFragment = requireFragmentManager().findFragmentByTag(BottomStorageFragment.TAG) as? BottomStorageFragment
        if(existFragment == null) {
            BottomStorageFragment().apply {
                this.arguments = Bundle().apply { putSerializable("data", data) }
            }.show(requireFragmentManager(), BottomStorageFragment.TAG)
        } else {
            if(!existFragment.showsDialog) {
                existFragment.show(requireFragmentManager(), BottomStorageFragment.TAG)
            }
        }
    }
}