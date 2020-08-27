package com.kakao.iron.ui.camera

import android.app.Activity
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentCameraBinding
import com.kakao.iron.ui.base.BaseFragment
import com.kakao.iron.util.extension.showToast
import kotlinx.android.synthetic.main.fragment_camera.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import javax.inject.Singleton

@Singleton
class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    private val mCameraViewModel: CameraViewModel by sharedViewModel()

    private lateinit var mCameraAdapter: CameraAdapter
    private lateinit var mArrayAdapter: ArrayAdapter<String>

    override fun getLayoutId(): Int = R.layout.fragment_camera

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         val storagePath = requireActivity().cacheDir.path
        mCameraViewModel.setUpFiles(storagePath)
        mArrayAdapter = ArrayAdapter<String>(requireActivity(), R.layout.item_dropdown_search, resources.getStringArray(R.array.array_camera_state))
        mCameraAdapter = CameraAdapter().apply {
            setOnItemSelectedListener { _, item, position ->
                when(mCameraViewModel.stateWatcher.get()) {
                    FileState.Normal -> showShareDialog(item.filePath)
                    FileState.Delete -> showDeleteDialog(position)
                }
            }
        }

        mBinding.let {
            it.viewModel = mCameraViewModel
            it.rcvCamera.let { view ->
                view.adapter = mCameraAdapter
                (view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            it.dropdown.let { view ->
                view.setAdapter(mArrayAdapter)
                view.onItemClickListener = itemClickListener
            }
        }

        add.setOnClickListener {
            mCameraViewModel.onCancel()
            startActivityForResult(Intent(this.context, CameraActivity::class.java), CAMERA_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CAMERA_CODE) {
                val storagePath = requireActivity().cacheDir.path
                mCameraViewModel.setUpFiles(storagePath)
            }
        }
    }

    private fun showShareDialog(path: String) {
        val existFragment = requireFragmentManager().findFragmentByTag(BottomCameraFragment.TAG) as? BottomCameraFragment
        if(existFragment == null) {
            BottomCameraFragment().apply {
                this.arguments = Bundle().apply { putString("path", path) }
            }.show(requireFragmentManager(), BottomCameraFragment.TAG)
        } else {
            if(!existFragment.showsDialog) {
                existFragment.show(requireFragmentManager(), BottomCameraFragment.TAG)
            }
        }
    }

    private fun showDeleteDialog(position: Int) {
        val dialog = MaterialDialog(requireActivity())
        dialog.show {
            title(R.string.text_delete)
            icon(R.drawable.vector_emphasize)
            cornerRadius(16f)
            negativeButton(R.string.text_cancel_short) { requireActivity().showToast(R.string.text_cancel_long) }
            positiveButton(R.string.text_confirm_short) {
                mCameraViewModel.onDelete(position)
                requireContext().showToast(R.string.text_delete_complete)
            }
        }
    }

    private val itemClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
            val storagePath = requireContext().cacheDir.path
            when(position) {
                0 -> mCameraViewModel.onOptionChanged(storagePath, FileState.Normal)
                1 -> mCameraViewModel.onOptionChanged(storagePath, FileState.Delete)
            }
        }

    companion object {
        const val CAMERA_CODE = 4000
    }
}
