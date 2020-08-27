package com.kakao.iron.ui.storage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.kakao.iron.R
import com.kakao.iron.databinding.ActivityLabelSearchBinding
import com.kakao.iron.ui.base.BaseActivity
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import kotlinx.android.synthetic.main.activity_label_search.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LabelSearchActivity : BaseActivity<ActivityLabelSearchBinding>() {

    private val mManageViewModel: ManageViewModel by viewModel()

    private lateinit var labelSearchAdapter: LabelSearchAdapter
    private lateinit var mFlowLayoutManager: FlowLayoutManager

    override fun getLayoutId(): Int = R.layout.activity_label_search

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mManageViewModel.labelList.observe(this, Observer { data ->
            when(data.isNotEmpty()) {
                true -> {
                    top_text.post { top_text.visibility = View.VISIBLE }
                    search_text.post { search_text.visibility = View.INVISIBLE }
                }
                false -> {
                    top_text.post { top_text.visibility = View.INVISIBLE }
                    search_text.post { search_text.visibility = View.VISIBLE }
                }
            }
            labelSearchAdapter.setData(data)
        })
        mManageViewModel.queryResult.observe(this, Observer {
            labelSearchAdapter.filter.filter(it)
        })
        mManageViewModel.topList.observe(this, Observer {
            rcv_search_top.layoutManager = mFlowLayoutManager
            rcv_search_top.adapter = LabelTopSearchAdapter(it).apply {
                setOnItemSelectedListener { _, query, _ ->
                    val intent = Intent()
                    intent.putExtra("query", query)
                    setResult(Activity.RESULT_OK, intent)
                    onBackPressed()
                }
            }
            (rcv_search_top.adapter as LabelTopSearchAdapter).notifyDataSetChanged()
        })
    }

    override fun init() {
        labelSearchAdapter = LabelSearchAdapter(
            mManageViewModel.labelList.value ?: mutableListOf()
        ).apply {
            setOnItemSelectedListener { _, query, _ ->
                val intent = Intent()
                intent.putExtra("query",query)
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
            }
        }
        mFlowLayoutManager = FlowLayoutManager()
        mFlowLayoutManager.isAutoMeasureEnabled = true
    }

    override fun onBind() {
        mBinding.apply {
            viewModel = mManageViewModel
            rcvSearchLabel.adapter = labelSearchAdapter
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu!!.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView

        menu.findItem(R.id.action_search).setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                onBackPressed()
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val intent = Intent()
                intent.putExtra("query",searchView.query.toString())
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                launch {
                    mManageViewModel.queryChannel.send(newText)
                }
                return false
            }
        })
        menuItem.expandActionView()
        searchView.setQuery("",true)
        return super.onCreateOptionsMenu(menu)
    }
}