package com.example.baocaocuoiky.ui.home

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
import com.example.baocaocuoiky.data.model.RequestStatus
import com.example.baocaocuoiky.databinding.ItemMyPostBinding

class MyPostAdapter(
    private var items: List<Post>,
    private val onOpenDetail: (Post) -> Unit,
    private val onViewRequests: (Post) -> Unit,
    private val onEditPost: (Post) -> Unit,
    private val onDeletePost: (Post) -> Unit
) : RecyclerView.Adapter<MyPostAdapter.Holder>() {

    inner class Holder(val binding: ItemMyPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemMyPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val post = items[position]
        val ctx = holder.itemView.context

        with(holder.binding) {
            tvTitle.text = post.title
            tvCreatedDate.text = "Ngày đăng: ${formatDate(post.createdAt)}"

            Glide.with(ctx)
                .load(post.imageUrl.ifBlank { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgPost)

            // Nhãn hình thức
            if (post.postType == PostType.GIVE_AWAY) {
                tvTypeBadge.text = "0đ GIVE_AWAY"
                tvTypeBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.status_green_bg)
                )
            } else {
                tvTypeBadge.text = "TRAO ĐỔI EXCHANGE"
                tvTypeBadge.setTextColor(Color.parseColor("#E65100"))
                tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }

            // Đếm số yêu cầu thực tế của bài đăng này
            val postRequests = AppDataStore.requests.filter { it.postId == post.postId }
            val reqCount = maxOf(postRequests.size, post.requestCount)

            val isCompleted = post.postStatus == PostStatus.TAKEN || post.postStatus == PostStatus.EXCHANGED

            if (isCompleted) {
                tvStatusBadge.text = "ĐÃ HOÀN TẤT (${post.postStatus})"
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
                tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEF2F6"))

                val winnerName = postRequests
                    .firstOrNull { it.requestStatus == RequestStatus.ACCEPTED }
                    ?.requesterName ?: "sinh viên UTE"

                layoutRequestCountPill.visibility = View.GONE
                layoutActiveButtons.visibility = View.GONE
                tvCompletedRecipient.visibility = View.VISIBLE
                btnFinishedBadge.visibility = View.VISIBLE
                tvCompletedRecipient.text = "Đã trao cho bạn $winnerName"
            } else {
                tvStatusBadge.text = if (post.postStatus == PostStatus.AVAILABLE) "AVAILABLE" else "CHỜ DUYỆT"
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.status_green_bg)
                )

                layoutRequestCountPill.visibility = View.VISIBLE
                layoutActiveButtons.visibility = View.VISIBLE
                tvCompletedRecipient.visibility = View.GONE
                btnFinishedBadge.visibility = View.GONE

                tvRequestSummary.text = if (post.postType == PostType.GIVE_AWAY) {
                    "$reqCount yêu cầu nhận đồ"
                } else {
                    "$reqCount yêu cầu trao đổi"
                }
                btnViewRequests.text = "Xem yêu cầu ($reqCount)"
            }

            root.setOnClickListener { onOpenDetail(post) }
            btnViewRequests.setOnClickListener { onViewRequests(post) }
            btnEditPost.setOnClickListener { onEditPost(post) }
            btnDeletePost.setOnClickListener { onDeletePost(post) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Post>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatDate(createdAt: Long): String {
        val diffDays = (System.currentTimeMillis() - createdAt) / (1000L * 60 * 60 * 24)
        return when {
            diffDays <= 0L -> "Hôm nay"
            else -> "$diffDays ngày trước"
        }
    }
}
