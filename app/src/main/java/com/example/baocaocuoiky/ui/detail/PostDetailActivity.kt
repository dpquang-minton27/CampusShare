package com.example.baocaocuoiky.ui.detail

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.*
import com.example.baocaocuoiky.databinding.ActivityPostDetailBinding
import com.example.baocaocuoiky.databinding.DialogSendRequestBinding
import java.util.concurrent.TimeUnit

/** Screen source: Figma 08_Item_Detail_Screen (node 16:398). */
class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private var postId: String = ""
    private var galleryUrls: List<String> = emptyList()
    private var galleryIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        postId = intent.getStringExtra("EXTRA_POST_ID").orEmpty()
        if (postId.isBlank()) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnShare.setOnClickListener { sharePost() }
        binding.btnFavorite.setOnClickListener {
            val saved = AppDataStore.toggleFavorite(postId)
            updateFavoriteIcon(saved)
            Toast.makeText(
                this,
                if (saved) "\u0110\u00e3 l\u01b0u v\u00e0o danh s\u00e1ch y\u00eau th\u00edch" else "\u0110\u00e3 b\u1ecf y\u00eau th\u00edch",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Simple multi-image fallback without adding a new ViewPager dependency.
        binding.imgPostDetail.setOnClickListener {
            if (galleryUrls.size > 1) {
                galleryIndex = (galleryIndex + 1) % galleryUrls.size
                showGalleryImage()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        renderPost()
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) R.drawable.cs_detail_ic_heart_filled
            else R.drawable.cs_detail_ic_heart_outline
        )
        binding.btnFavorite.contentDescription =
            if (isFavorite) "B\u1ecf y\u00eau th\u00edch" else "Y\u00eau th\u00edch"
    }

    private fun renderPost() {
        val currentUser = SessionManager.getCurrentUser(this) ?: return
        val post = AppDataStore.posts.firstOrNull { it.postId == postId }
        if (post == null) {
            Toast.makeText(this, "B\u00e0i \u0111\u0103ng kh\u00f4ng c\u00f2n t\u1ed3n t\u1ea1i!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateFavoriteIcon(AppDataStore.favoritePostIds.contains(post.postId))
        binding.tvTitle.text = post.title
        binding.tvCategory.text = post.categoryName
        binding.tvCondition.text = "T\u00ecnh tr\u1ea1ng: M\u1edbi ${post.conditionPercent}%"
        binding.tvPostTime.text = formatPostedAt(post.createdAt)
        binding.tvLocation.text = "\u0110\u1ecba \u0111i\u1ec3m nh\u1eadn: ${post.pickupLocation}"
        binding.tvDescription.text = post.description
        binding.tvOwnerName.text = buildString {
            append(post.ownerName)
            if (post.ownerClass.isNotBlank()) append(" (${post.ownerClass})")
        }
        // Retain the old view ID but the class is merged into the Figma name row.
        binding.tvOwnerClass.text = post.ownerClass
        binding.tvOwnerClass.visibility = View.GONE
        binding.tvOwnerInitial.text = abbreviateName(post.ownerName)

        // The current User/Post models have no verifiedStudent, rating or reviewCount.
        // Do not display fabricated Figma demo values as real account facts.
        binding.tvOwnerVerified.visibility = View.GONE
        binding.tvOwnerRating.visibility = View.GONE

        if (post.postType == PostType.GIVE_AWAY) {
            binding.tvTypeBadge.text = "0\u0111 GIVE_AWAY (CHO T\u1eb6NG)"
            binding.tvTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.status_green))
            binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_green_bg)
            )
            binding.layoutExchangeBox.visibility = View.GONE
        } else {
            binding.tvTypeBadge.text = "EXCHANGE (TRAO \u0110\u1ed4I)"
            binding.tvTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.primary_blue_light)
            )
            binding.layoutExchangeBox.visibility = View.VISIBLE
            binding.tvExchangeNote.text = post.exchangeNote.ifBlank { "Trao \u0111\u1ed5i \u0111\u1ed3 d\u00f9ng h\u1ecdc t\u1eadp" }
        }
        binding.tvStatusBadge.text = when (post.postStatus) {
            PostStatus.AVAILABLE -> "AVAILABLE (C\u00d2N \u0110\u1ed2)"
            PostStatus.TAKEN -> "TAKEN (\u0110\u00c3 CHO)"
            PostStatus.EXCHANGED -> "EXCHANGED (\u0110\u00c3 TRAO)"
            else -> "HIDDEN (\u0110\u00c3 \u1ea8N)"
        }
        val available = post.postStatus == PostStatus.AVAILABLE
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(
            this, if (available) R.color.status_green else R.color.status_orange
        ))
        binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
            this, if (available) R.color.status_green_bg else R.color.status_orange_bg
        ))

        galleryUrls = (listOf(post.imageUrl) + post.imageUrls).filter { it.isNotBlank() }.distinct()
        galleryIndex = 0
        showGalleryImage()
        setupActionButtons(post, currentUser)
    }

    private fun showGalleryImage() {
        Glide.with(this)
            .load(galleryUrls.getOrNull(galleryIndex))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(binding.imgPostDetail)
        binding.tvGalleryIndicator.visibility = if (galleryUrls.size > 1) View.VISIBLE else View.GONE
        if (galleryUrls.size > 1) {
            binding.tvGalleryIndicator.text = "${galleryIndex + 1} / ${galleryUrls.size}"
        }
    }

    private fun sharePost() {
        val post = AppDataStore.posts.firstOrNull { it.postId == postId } ?: return
        val text = buildString {
            append(post.title)
            append("\n")
            append(post.description)
            if (post.pickupLocation.isNotBlank()) {
                append("\n\u0110\u1ecba \u0111i\u1ec3m nh\u1eadn: ")
                append(post.pickupLocation)
            }
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, post.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "Chia s\u1ebb b\u00e0i \u0111\u0103ng"))
    }

    private fun abbreviateName(fullName: String): String {
        val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.take(3).mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("").ifBlank { "SV" }
    }

    private fun formatPostedAt(timeMillis: Long): String {
        if (timeMillis <= 0L) return "\u0110\u0103ng g\u1ea7n \u0111\u00e2y"
        val age = (System.currentTimeMillis() - timeMillis).coerceAtLeast(0L)
        val min = TimeUnit.MILLISECONDS.toMinutes(age)
        val hour = TimeUnit.MILLISECONDS.toHours(age)
        val day = TimeUnit.MILLISECONDS.toDays(age)
        return when {
            min < 1L -> "\u0110\u0103ng v\u1eeba xong"
            min < 60L -> "\u0110\u0103ng $min ph\u00fat tr\u01b0\u1edbc"
            hour < 24L -> "\u0110\u0103ng $hour gi\u1edd tr\u01b0\u1edbc"
            day < 30L -> "\u0110\u0103ng $day ng\u00e0y tr\u01b0\u1edbc"
            else -> "\u0110\u0103ng h\u01a1n 1 th\u00e1ng tr\u01b0\u1edbc"
        }
    }

    private fun showSendIcon(show: Boolean) {
        binding.btnPrimaryAction.icon = if (show) {
            ContextCompat.getDrawable(this, R.drawable.cs_detail_ic_send)
        } else null
    }

    private fun setupActionButtons(post: Post, currentUser: User) {
        val isOwner = post.ownerId == currentUser.userId
        val myRequest = AppDataStore.requests.firstOrNull {
            it.postId == post.postId && it.requesterId == currentUser.userId
        }
        binding.btnSecondaryAction.visibility = View.GONE
        binding.tvRequestStatusBanner.visibility = View.GONE
        showSendIcon(false)

        if (isOwner) {
            binding.tvRequestStatusBanner.visibility = View.VISIBLE
            binding.tvRequestStatusBanner.text =
                "\u0110\u00e2y l\u00e0 b\u00e0i \u0111\u0103ng c\u1ee7a b\u1ea1n (${post.requestCount} l\u01b0\u1ee3t \u0111\u0103ng k\u00fd)."
            binding.btnPrimaryAction.isEnabled = true
            binding.btnPrimaryAction.text = "XEM Y\u00caU C\u1ea6U NH\u1eacN \u0110\u1ed2"
            binding.btnPrimaryAction.setOnClickListener {
                startActivity(Intent(this, RequestsActivity::class.java).apply {
                    putExtra("EXTRA_INITIAL_TAB", 1)
                })
            }
            return
        }

        when {
            myRequest != null -> {
                binding.tvRequestStatusBanner.visibility = View.VISIBLE
                when (myRequest.requestStatus) {
                    RequestStatus.PENDING -> {
                        binding.tvRequestStatusBanner.text =
                            "Y\u00eau c\u1ea7u c\u1ee7a b\u1ea1n \u0111ang ch\u1edd ch\u1ee7 b\u00e0i \u0111\u0103ng duy\u1ec7t."
                        binding.btnSecondaryAction.visibility = View.VISIBLE
                        binding.btnSecondaryAction.text = "H\u1ee7y y\u00eau c\u1ea7u"
                        binding.btnSecondaryAction.setOnClickListener {
                            AppDataStore.cancelRequest(myRequest.requestId)
                            Toast.makeText(this, "\u0110\u00e3 h\u1ee7y y\u00eau c\u1ea7u!", Toast.LENGTH_SHORT).show()
                            renderPost()
                        }
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "\u0110ANG CH\u1edc DUY\u1ec6T"
                    }
                    RequestStatus.ACCEPTED -> {
                        binding.tvRequestStatusBanner.text =
                            "Ch\u00fac m\u1eebng! B\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c ch\u1ecdn nh\u1eadn m\u00f3n \u0111\u1ed3 n\u00e0y."
                        binding.btnPrimaryAction.isEnabled = true
                        binding.btnPrimaryAction.text = "LI\u00caN H\u1ec6 S\u0110T / ZALO"
                        binding.btnPrimaryAction.setOnClickListener {
                            ContactBottomSheet.show(this, myRequest, currentUser.userId)
                        }
                    }
                    RequestStatus.REJECTED -> {
                        binding.tvRequestStatusBanner.text =
                            "R\u1ea5t ti\u1ebfc, m\u00f3n \u0111\u1ed3 \u0111\u00e3 \u0111\u01b0\u1ee3c trao cho b\u1ea1n kh\u00e1c."
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "KH\u00d4NG \u0110\u01af\u1ee2C CH\u1eccN"
                    }
                    else -> {
                        binding.tvRequestStatusBanner.text = "B\u1ea1n \u0111\u00e3 h\u1ee7y y\u00eau c\u1ea7u tr\u01b0\u1edbc \u0111\u00f3."
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "\u0110\u00c3 H\u1ee6Y Y\u00caU C\u1ea6U"
                    }
                }
            }
            post.postStatus != PostStatus.AVAILABLE -> {
                binding.btnPrimaryAction.isEnabled = false
                binding.btnPrimaryAction.text = "M\u00d3N \u0110\u1ed2 \u0110\u00c3 H\u1ebeT"
            }
            else -> {
                binding.btnPrimaryAction.isEnabled = true
                val giveaway = post.postType == PostType.GIVE_AWAY
                binding.btnPrimaryAction.text =
                    if (giveaway) "G\u1eecI Y\u00caU C\u1ea6U NH\u1eacN \u0110\u1ed2" else "\u0110\u1ec0 XU\u1ea4T TRAO \u0110\u1ed4I"
                showSendIcon(true)
                binding.btnPrimaryAction.setOnClickListener {
                    showSendRequestDialog(post, currentUser)
                }
            }
        }
    }

    private fun showSendRequestDialog(post: Post, currentUser: User) {
        val dialogBinding = DialogSendRequestBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (post.postType == PostType.GIVE_AWAY) "\u0110\u0103ng k\u00fd nh\u1eadn \u0111\u1ed3" else "\u0110\u1ec1 xu\u1ea5t trao \u0111\u1ed5i")
            .setView(dialogBinding.root)
            .setNegativeButton("\u0110\u00f3ng", null)
            .setPositiveButton("G\u1eedi y\u00eau c\u1ea7u", null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.isEnabled = false
            dialogBinding.edtRequestMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val len = s?.toString()?.trim()?.length ?: 0
                    dialogBinding.tvCharCount.text = "$len/200"
                    positiveBtn.isEnabled = len in 1..200
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
            positiveBtn.setOnClickListener {
                val msg = dialogBinding.edtRequestMessage.text.toString().trim()
                val created = AppDataStore.sendRequest(post, currentUser, msg)
                if (created != null) {
                    Toast.makeText(this, "\u0110\u00e3 g\u1eedi y\u00eau c\u1ea7u th\u00e0nh c\u00f4ng!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    renderPost()
                } else {
                    Toast.makeText(this, "Kh\u00f4ng th\u1ec3 g\u1eedi y\u00eau c\u1ea7u cho b\u00e0i \u0111\u0103ng n\u00e0y.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }
}
