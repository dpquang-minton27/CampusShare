package com.example.baocaocuoiky.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baocaocuoiky.databinding.ItemAdminSimpleBinding

data class AdminRowItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
)

class AdminSimpleAdapter(
    private var items: List<AdminRowItem>,
    private val onItemClick: (AdminRowItem) -> Unit
) : RecyclerView.Adapter<AdminSimpleAdapter.Holder>() {

    inner class Holder(val binding: ItemAdminSimpleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemAdminSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = items[position]
        with(holder.binding) {
            tvItemTitle.text = row.title
            tvItemSubtitle.text = row.subtitle
            tvItemBadge.text = row.badge
            root.setOnClickListener { onItemClick(row) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminRowItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}