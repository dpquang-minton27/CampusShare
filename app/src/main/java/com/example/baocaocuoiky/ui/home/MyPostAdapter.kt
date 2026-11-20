package com.example.baocaocuoiky.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

/**
 * Màn "Bài đăng của tôi" theo Figma 16:269.
 * Chỉ điều chỉnh cách hiển thị thẻ; không thay đổi nguồn dữ liệu hay callback nghiệp vụ.
 */
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

            // Figma: GIVE_AWAY xanh lá nền nhạt; EXCHANGE nền cam và viền cam.
            if (post.postType == PostType.GIVE_AWAY) {
                tvTypeBadge.setBackgroundResource(R.drawable.bg_my_posts_badge_pill)
                tvTypeBadge.backgroundTintList = null
                tvTypeBadge.text = "0đ GIVE_AWAY"
                tvTypeBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
            } else {
                tvTypeBadge.setBackgroundResource(R.drawable.bg_my_posts_exchange_pill)
                tvTypeBadge.backgroundTintList = null
                tvTypeBadge.text = "TRAO ĐỔI EXCHANGE"
                tvTypeBadge.setTextColor(Color.parseColor("#D84315"))
            }

            val postRequests = AppDataStore.requests.filter { it.postId == post.postId }
            val reqCount = maxOf(postRequests.size, post.requestCount)
            val isCompleted = post.postStatus == PostStatus.TAKEN ||
                post.postStatus == PostStatus.EXCHANGED

            // Figma: completed posts have a long status chip and the date below it.
            // Restore horizontal placement when a RecyclerView holder is recycled.
            layoutMetaRow.orientation = if (isCompleted) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            val dateLayout = tvCreatedDate.layoutParams as LinearLayout.LayoutParams
            val density = ctx.resources.displayMetrics.density
            dateLayout.marginStart = if (isCompleted) 0 else (8 * density).toInt()
            dateLayout.topMargin = if (isCompleted) (4 * density).toInt() else 0
            tvCreatedDate.layoutParams = dateLayout

            if (isCompleted) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_my_posts_gray_pill)
                tvStatusBadge.backgroundTintList = null
                tvStatusBadge.text = "ĐÃ HOÀN TẤT (${post.postStatus})"
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))

                val winnerName = postRequests
                    .firstOrNull { it.requestStatus == RequestStatus.ACCEPTED }
                    ?.requesterName ?: "sinh viên UTE"

                layoutRequestCountPill.visibility = View.GONE
                layoutActiveButtons.visibility = View.GONE
                tvCompletedRecipient.visibility = View.VISIBLE
                btnFinishedBadge.visibility = View.VISIBLE
                tvCompletedRecipient.text = "Đã trao cho bạn $winnerName"
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_my_posts_badge_pill)
                tvStatusBadge.backgroundTintList = null
                tvStatusBadge.text = if (post.postStatus == PostStatus.AVAILABLE) {
                    "AVAILABLE"
                } else {
                    "CHỜ DUYỆT"
                }
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))

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
