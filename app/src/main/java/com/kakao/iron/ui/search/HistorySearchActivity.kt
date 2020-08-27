package com.kakao.iron.ui.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.kakao.iron.R
import com.kakao.iron.databinding.ActivityHistorySearchBinding
import com.kakao.iron.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_history_search.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistorySearchActivity: BaseActivity<ActivityHistorySearchBinding>() {

    private val mSearchViewModel: SearchViewModel by viewModel()

    private lateinit var searchAdapter: HistorySearchAdapter

    override fun getLayoutId(): Int = R.layout.activity_history_search

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSearchViewModel.historyList.observe(this, Observer { data ->
            when(data.size > 0) {
                true -> {
                    search_title.visibility = View.INVISIBLE
                    delete.visibility = View.VISIBLE
                }
                false -> {
                    search_title.visibility = View.VISIBLE
                    delete.visibility = View.INVISIBLE
                }
            }
            searchAdapter.let {
                it.setData(data)
                it.filter.filter(mSearchViewModel.searchQuery)
            }
        })
        mSearchViewModel.queryResult.observe(this, Observer {
            searchAdapter.filter.filter(it)
        })

        delete.setOnClickListener {
            mSearchViewModel.removeAll()
        }
    }

    override fun init() {
        searchAdapter = HistorySearchAdapter(mSearchViewModel.historyList.value ?: mutableListOf()).apply {
            setOnItemSelectedListener { _, item, _ ->
                val intent = Intent()
                intent.putExtra("query", item.query)
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
            }
            setOnItemRemovedListener { _, item, _ ->
                mSearchViewModel.remove(item.query)
            }
        }
    }

    override fun onBind() {
        mBinding.apply {
            recyclerView.adapter = searchAdapter
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu!!.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        ViewCompat.setTransitionName(searchView, getString(R.string.transitionName))

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
                intent.putExtra("query", searchView.query.toString())
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                launch {
                    mSearchViewModel.let {
                        it.searchQuery = newText
                        it.queryChannel.send(newText)
                    }
                }
                return false
            }
        })

        menuItem.expandActionView()
        searchView.setQuery("",true)
        return super.onCreateOptionsMenu(menu)
    }
}
