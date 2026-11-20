package com.example.baocaocuoiky.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.databinding.ItemAdminPostBinding

class AdminPostAdapter(
    private var items: List<Post>,
    private val onOpenDetail: (Post) -> Unit,
    private val onToggleModerate: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<AdminPostAdapter.Holder>() {

    inner class Holder(val binding: ItemAdminPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemAdminPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val post = items[position]
        val ctx = holder.itemView.context

        with(holder.binding) {
            tvTitle.text = post.title
            tvOwnerMeta.text = "Người đăng: ${post.ownerName} • ${post.categoryName}"

            Glide.with(ctx)
                .load(post.imageUrl.ifBlank { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgThumb)

            when (post.postStatus) {
                PostStatus.AVAILABLE -> {
                    tvStatus.text = "Trạng thái: Đang hiển thị"
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                    btnToggleModerate.text = "Ẩn vi phạm"
                }
                PostStatus.HIDDEN -> {
                    tvStatus.text = "Trạng thái: Đang bị Admin ẩn"
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_red))
                    btnToggleModerate.text = "Mở khóa tin"
                }
                else -> {
                    tvStatus.text = "Trạng thái: Đã hoàn tất giao dịch"
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.primary_blue))
                    btnToggleModerate.text = "Ẩn tin"
                }
            }

            root.setOnClickListener { onOpenDetail(post) }
            btnToggleModerate.setOnClickListener { onToggleModerate(post) }
            btnDeletePost.setOnClickListener { onDelete(post) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Post>) {
        items = newItems
        notifyDataSetChanged()
    }
}