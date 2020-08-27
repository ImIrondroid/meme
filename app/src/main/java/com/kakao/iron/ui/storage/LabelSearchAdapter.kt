package com.kakao.iron.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kakao.iron.R
import com.kakao.iron.util.OnItemSelectedListener
import java.util.*

class LabelSearchAdapter(
    labelList: MutableList<String>
) : RecyclerView.Adapter<LabelSearchAdapter.ViewHolder>(), Filterable {

    var mLabelList: MutableList<String> = mutableListOf()
    var mLabelListAll: MutableList<String> = mutableListOf()

    init {
        mLabelList.addAll(labelList)
        mLabelListAll.addAll(labelList)
    }

    fun setData(list: List<String>) {
        mLabelList.clear()
        mLabelList.addAll(list)
        mLabelListAll.clear()
        mLabelListAll.addAll(list)
    }

    override fun getItemId(position: Int): Long {
        return mLabelList[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_search_storage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.labelText.text = mLabelList[position]
    }

    override fun getItemCount(): Int {
        return mLabelList.size
    }

    private var onItemSelectedListener : OnItemSelectedListener<String>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<String>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun getFilter(): Filter = newFilter

    private var newFilter: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val filteredList = mutableListOf<String>()
            when(charSequence.isEmpty()) {
                true -> filteredList.addAll(mLabelListAll)
                false -> {
                    for (label in mLabelListAll) {
                        if (label.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT))) {
                            filteredList.add(label)
                        }
                    }
                }
            }
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            mLabelList.clear()
            mLabelList.addAll((filterResults.values as List<String>))
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val labelText: TextView = itemView.findViewById(R.id.label)
        init {
            itemView.setOnClickListener {
                onItemSelectedListener?.invoke(it, mLabelList[adapterPosition], adapterPosition)
            }
        }
    }
}