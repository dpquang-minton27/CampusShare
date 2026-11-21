package com.example.baocaocuoiky.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ItemAdminPostBinding

class AdminPostAdapter(
    private var items: List<Post>,
    private val onOpenDetail: (Post) -> Unit,
    private val onApprove: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<AdminPostAdapter.Holder>() {

    inner class Holder(val binding: ItemAdminPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemAdminPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val post = items[position]
        val ctx = holder.itemView.context
        val owner = AppDataStore.registeredAccounts.firstOrNull { it.first.userId == post.ownerId }?.first

        with(holder.binding) {
            tvTitle.text = post.title
            tvOwnerInfo.text = if (owner != null) {
                "${post.ownerName} • ${owner.studentId}"
            } else {
                post.ownerName
            }

            Glide.with(ctx)
                .load(post.imageUrl.ifBlank { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgPost)

            if (post.postType == PostType.GIVE_AWAY) {
                tvTypeBadge.text = "0Đ GIVE_AWAY"
                tvTypeBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.status_green_bg)
                )
            } else {
                tvTypeBadge.text = "TRAO ĐỔI"
                tvTypeBadge.setTextColor(Color.parseColor("#E65100"))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }

            if (post.postStatus == PostStatus.PENDING) {
                tvStatusBadge.text = "CHỜ DUYỆT"
                tvStatusBadge.setTextColor(Color.parseColor("#D97706"))
                tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                btnApprove.visibility = View.VISIBLE
            } else {
                tvStatusBadge.text = post.postStatus
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.status_green_bg)
                )
                btnApprove.visibility = View.GONE
            }

            root.setOnClickListener { onOpenDetail(post) }
            btnApprove.setOnClickListener { onApprove(post) }
            btnDelete.setOnClickListener { onDelete(post) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Post>) {
        items = newItems
        notifyDataSetChanged()
    }
}
