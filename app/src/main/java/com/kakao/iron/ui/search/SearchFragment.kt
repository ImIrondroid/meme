package com.kakao.iron.ui.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.SimpleItemAnimator
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.fileChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bumptech.glide.Glide
import com.kakao.iron.R
import com.kakao.iron.databinding.FragmentSearchBinding
import com.kakao.iron.ui.base.BaseFragment
import com.kakao.iron.ui.detail.DetailActivity
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import kotlin.reflect.KClass

class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val mSearchViewModel: SearchViewModel by sharedViewModel()

    private lateinit var mSearchAdapter: SearchAdapter

    override fun getLayoutId(): Int = R.layout.fragment_search

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSearchAdapter = SearchAdapter().apply {
            setOnItemSelectedListener { _, item, _ ->
                nextActivityForResult(
                    kClass = DetailActivity::class,
                    bundle = Bundle().apply {
                        val searchedQuery = mSearchViewModel.searchQuery + getString(R.string.suffix)
                        putString("query", searchedQuery)
                        putParcelable("data", item)
                    },
                    requestCode = MainActivity.REQUEST_CODE_FROM_MAIN,
                    clearTop = true
                )
            }
            addDataRefreshListener {
                when (it) {
                    true -> { notify.visibility = View.VISIBLE }
                    false -> { notify.visibility = View.INVISIBLE }
                }
            }
        }

        mBinding.rcvImage.let {
            it.adapter = mSearchAdapter
            (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        search.setOnClickListener { it: View ->
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), it, it.transitionName)
            nextActivityForResult(
                kClass = HistorySearchActivity::class,
                requestCode = REQUEST_CODE_TO_HISTORY,
                option = option
            )
        }

        history.setOnClickListener {
            fileChooser()
        }

        launch {
            mSearchAdapter.dataRefreshFlow.collect {
                rcv_image.scrollToPosition(0)
            }
        }
    }

    @ExperimentalPagingApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_TO_HISTORY -> {
                    launch {
                        val query = data?.getStringExtra("query") ?: getString(R.string.text_default_query)
                        search.setText(query)
                        mSearchViewModel.onSearch(query).collect {
                            mSearchAdapter.submitData(it)
                        }
                    }
                }
            }
        }
    }

    private fun fileChooser() = runWithPermissions(Permission.READ_EXTERNAL_STORAGE) {
        val context = requireContext()
        val dialog = MaterialDialog(context)
        MaterialDialog(context).show {
            fileChooser(context, context.externalCacheDir) { _: MaterialDialog, file: File ->
                val subFile = File(file.path)
                val message = String(subFile.readBytes())
                dialog.show {
                    title(text = file.name)
                    message(text = message)
                    cornerRadius(16f)
                    positiveButton(R.string.text_confirm_short)
                }
            }
            negativeButton(R.string.text_cancel_log)
            positiveButton(R.string.text_confirm_log)
            lifecycleOwner(this@SearchFragment)
        }
    }

    fun <T : Activity> nextActivityForResult(
        kClass: KClass<T>,
        bundle: Bundle? = null,
        requestCode: Int,
        clearTop: Boolean = false
    ) {
        startActivityForResult(Intent(context, kClass.java).apply {
            bundle?.let(this::putExtras)
            if (clearTop) {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }, requestCode)
    }

    fun <T : Activity> nextActivityForResult(
        kClass: KClass<T>,
        bundle: Bundle? = null,
        requestCode: Int,
        option: ActivityOptionsCompat
    ) {
        startActivityForResult(Intent(context, kClass.java).apply {
            bundle?.let(this::putExtras)
        }, requestCode, option.toBundle())
    }

    companion object {
        const val REQUEST_CODE_TO_HISTORY = 3000
    }
}