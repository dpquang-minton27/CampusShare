package com.example.baocaocuoiky.ui.detail

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ItemFavoritePostBinding

/** Dedicated horizontal card; keep PostGridAdapter unchanged for Home and Search. */
class FavoritePostAdapter(
    private val onOpenDetail: (Post) -> Unit,
    private val onRequest: (Post) -> Unit
) : ListAdapter<Post, FavoritePostAdapter.FavoriteViewHolder>(DIFF) {

    class FavoriteViewHolder(val binding: ItemFavoritePostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder =
        FavoriteViewHolder(
            ItemFavoritePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val post = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvFavTitle.text = post.title
        binding.tvFavCondition.text = context.getString(
            R.string.fav_condition_format, post.conditionPercent.coerceIn(0, 100)
        )

        val classLabel = post.ownerClass.trim()
        val cohort = Regex("^(?:K)?(2[0-9])(?=\\D|$)").find(classLabel)
            ?.groupValues?.get(1)?.let { "K$it" }
            ?: classLabel.takeIf { it.isNotEmpty() }
        val ownerName = post.ownerName.ifBlank { context.getString(R.string.fav_unknown_owner) }
        val ownerWithClass = if (cohort == null) ownerName else "$ownerName ($cohort)"
        binding.tvFavOwner.text = context.getString(R.string.fav_owner_format, ownerWithClass)

        binding.tvFavType.text = when (post.postType) {
            PostType.GIVE_AWAY -> "GIVE_AWAY"
            PostType.EXCHANGE -> "EXCHANGE"
            else -> post.postType
        }
        // Both sample chips display 0d in the supplied Figma frame. Exchange remains
        // a barter workflow; the chip must not be treated as a sale price.
        binding.tvFavPrice.setText(R.string.fav_zero_price)

        val available = post.postStatus == PostStatus.AVAILABLE
        binding.tvFavStatus.text = post.postStatus
        binding.tvFavStatus.setTextColor(ContextCompat.getColor(
            context, if (available) R.color.status_green else R.color.status_orange
        ))
        binding.tvFavStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
            context, if (available) R.color.status_green_bg else R.color.status_orange_bg
        ))

        val cover = post.imageUrl.takeIf { it.isNotBlank() }
            ?: post.imageUrls.firstOrNull { it.isNotBlank() }
        Glide.with(binding.imgFavPost)
            .load(cover)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(binding.imgFavPost)

        val viewerId = SessionManager.getCurrentUser(context)?.userId
        val ownPost = viewerId != null && post.ownerId == viewerId
        val alreadyRequested = viewerId != null && AppDataStore.requests.any {
            it.postId == post.postId && it.requesterId == viewerId
        }
        val canRequest = available && viewerId != null && !ownPost && !alreadyRequested
        binding.btnFavRequest.text = when {
            viewerId == null -> context.getString(R.string.fav_sign_in)
            ownPost -> context.getString(R.string.fav_my_post)
            alreadyRequested -> context.getString(R.string.fav_requested)
            !available -> context.getString(R.string.fav_unavailable)
            else -> context.getString(R.string.fav_request)
        }
        binding.btnFavRequest.isEnabled = canRequest
        binding.btnFavRequest.setOnClickListener { if (canRequest) onRequest(post) }
        binding.root.setOnClickListener { onOpenDetail(post) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
        }
    }
}
