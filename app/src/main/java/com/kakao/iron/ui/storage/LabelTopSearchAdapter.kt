package com.kakao.iron.ui.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakao.iron.R
import com.kakao.iron.util.OnItemSelectedListener
import java.util.*

class LabelTopSearchAdapter private constructor() : RecyclerView.Adapter<LabelTopSearchAdapter.LabelTopSearchViewHolder>() {

    private val items: MutableList<String>

    constructor(items: List<String>) : this() {
        this.items.addAll(items)
    }

    private var onItemSelectedListener: OnItemSelectedListener<String>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<String>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelTopSearchViewHolder {
        return LabelTopSearchViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_storage_top, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: LabelTopSearchViewHolder,
        position: Int
    ) {
        holder.queryText.text = items[position]
        holder.itemView.setOnClickListener {
            onItemSelectedListener?.invoke(it, items[position], position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    init {
        items = LinkedList()
    }

    inner class LabelTopSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val queryText: TextView = itemView.findViewById(R.id.query)
    }
}