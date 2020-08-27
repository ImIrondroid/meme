package com.kakao.iron.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kakao.iron.R
import com.kakao.iron.util.OnItemSelectedListener
import java.util.*

class HistorySearchAdapter(
    historyList: MutableList<HistoryData>
) : RecyclerView.Adapter<HistorySearchAdapter.ViewHolder>(), Filterable {

    var mHistoryList: MutableList<HistoryData> = mutableListOf()
    var mHistoryListAll: MutableList<HistoryData> = mutableListOf()

    init {
        mHistoryList.addAll(historyList)
        mHistoryListAll.addAll(historyList)
    }

    fun setData(list: MutableList<HistoryData>) {
        mHistoryList.clear()
        mHistoryList.addAll(list)
        mHistoryListAll.clear()
        mHistoryListAll.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nowTime = Date(System.currentTimeMillis()).time
        holder.queryText.text = mHistoryList[position].query
        holder.timeText.text = getTimeDiff((nowTime - mHistoryList[position].time))
    }

    override fun getItemCount(): Int {
        return mHistoryList.size
    }

    private var onItemSelectedListener : OnItemSelectedListener<HistoryData>? = null
    private var onItemRemovedListener : OnItemSelectedListener<HistoryData>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<HistoryData>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    fun setOnItemRemovedListener(onItemRemovedListener: OnItemSelectedListener<HistoryData>) {
        this.onItemRemovedListener = onItemRemovedListener
    }

    override fun getFilter(): Filter = newFilter

    private var newFilter: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val filteredList: MutableList<HistoryData> = mutableListOf()
            when(charSequence.isEmpty()) {
                true -> filteredList.addAll(mHistoryListAll)
                false -> {
                    for (history in mHistoryListAll) {
                        if (history.query.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT))) {
                            filteredList.add(history)
                        }
                    }
                }
            }
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            mHistoryList.clear()
            mHistoryList.addAll((filterResults.values as List<HistoryData>))
            notifyDataSetChanged()
        }
    }

    private fun getTimeDiff(time: Long): String {
        val sec = time/1000
        return when {
            sec<=0 -> "0분 전"
            sec<3600 -> "${sec/60}분 전"
            sec<3600*24 -> "${sec/3600}시간 전"
            sec<3600*24*30 -> "${sec/(3600*24)}일 전"
            else -> "오래 전"
        }
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val queryText: TextView = itemView.findViewById(R.id.query)
        val timeText: TextView = itemView.findViewById(R.id.time)
        val group: ConstraintLayout = itemView.findViewById(R.id.group)
        val delete: ImageView = itemView.findViewById(R.id.delete)
        init {
            group.setOnClickListener {
                onItemSelectedListener?.invoke(it, mHistoryList[absoluteAdapterPosition], absoluteAdapterPosition)
            }
            delete.setOnClickListener {
                onItemRemovedListener?.invoke(it, mHistoryList[absoluteAdapterPosition], absoluteAdapterPosition)
            }
        }
    }
}