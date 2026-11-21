package com.example.baocaocuoiky.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ItemPostGridBinding

class PostGridAdapter(
    private var items: List<Post>,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = items[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvTitle.text = post.title
            tvCondition.text = "Tình trạng: ${post.conditionPercent}%"
            tvOwner.text = "Người đăng: ${post.ownerName}"
            tvTime.text = "Thời gian: ${formatRelativeTime(post.createdAt)}"

            tvStatusBadge.text = if (post.postStatus == PostStatus.AVAILABLE) "AVAILABLE" else post.postStatus

            if (post.postType == PostType.GIVE_AWAY) {
                tvTypeBadge.text = "0đ GIVE_AWAY"
                tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.status_green))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.status_green_bg)
                )
            } else {
                tvTypeBadge.text = "TRAO ĐỔI EXCHANGE"
                tvTypeBadge.setTextColor(Color.parseColor("#E65100"))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor("#FFF3E0")
                )
            }

            Glide.with(context)
                .load(post.imageUrl.ifBlank { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imgPost)

            root.setOnClickListener { onItemClick(post) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Post>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatRelativeTime(createdAt: Long): String {
        val diffMinutes = ((System.currentTimeMillis() - createdAt) / 60000L).coerceAtLeast(1L)
        return when {
            diffMinutes < 60 -> "$diffMinutes phút trước"
            diffMinutes < 1440 -> "${diffMinutes / 60} giờ trước"
            else -> "${diffMinutes / 1440} ngày trước"
        }
    }
}
