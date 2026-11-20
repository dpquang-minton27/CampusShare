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
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.*
import com.example.baocaocuoiky.databinding.ActivityPostDetailBinding
import com.example.baocaocuoiky.databinding.DialogSendRequestBinding

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("EXTRA_POST_ID").orEmpty()
        if (postId.isBlank()) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnFavorite.setOnClickListener {
            val saved = AppDataStore.toggleFavorite(postId)
            updateFavoriteIcon(saved)
            Toast.makeText(
                this,
                if (saved) "Đã lưu vào danh sách yêu thích" else "Đã bỏ yêu thích",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        renderPost()
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )
    }

    private fun renderPost() {
        val currentUser = SessionManager.getCurrentUser(this) ?: return
        val post = AppDataStore.posts.firstOrNull { it.postId == postId }
        if (post == null) {
            Toast.makeText(this, "Bài đăng không còn tồn tại!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateFavoriteIcon(AppDataStore.favoritePostIds.contains(post.postId))

        binding.tvTitle.text = post.title
        binding.tvCategory.text = "Danh mục: ${post.categoryName}"
        binding.tvCondition.text = "Tình trạng độ mới: ${post.conditionPercent}%"
        binding.tvLocation.text = "📍 Địa điểm giao nhận: ${post.pickupLocation}"
        binding.tvDescription.text = post.description
        binding.tvOwnerName.text = post.ownerName
        binding.tvOwnerClass.text = post.ownerClass.ifBlank { "Sinh viên ĐH SPKT Đà Nẵng" }
        binding.tvOwnerInitial.text = post.ownerName.firstOrNull()?.uppercase() ?: "S"

        if (post.postType == PostType.GIVE_AWAY) {
            binding.tvTypeBadge.text = "Cho tặng 0đ"
            binding.tvTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.status_green))
            binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_green_bg)
            )
            binding.layoutExchangeBox.visibility = View.GONE
        } else {
            binding.tvTypeBadge.text = "Trao đổi"
            binding.tvTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.primary_blue_light)
            )
            binding.layoutExchangeBox.visibility = View.VISIBLE
            binding.tvExchangeNote.text = post.exchangeNote.ifBlank { "Trao đổi đồ dùng học tập" }
        }

        binding.tvStatusBadge.text = when (post.postStatus) {
            PostStatus.AVAILABLE -> "Đang sẵn sàng"
            PostStatus.TAKEN -> "Đã có người nhận"
            PostStatus.EXCHANGED -> "Đã trao đổi"
            else -> "Đã ẩn"
        }

        Glide.with(this)
            .load(post.imageUrl.ifBlank { null })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.imgPostDetail)

        setupActionButtons(post, currentUser)
    }

    private fun setupActionButtons(post: Post, currentUser: User) {
        val isOwner = post.ownerId == currentUser.userId
        val myRequest = AppDataStore.requests.firstOrNull {
            it.postId == post.postId && it.requesterId == currentUser.userId
        }

        binding.btnSecondaryAction.visibility = View.GONE
        binding.tvRequestStatusBanner.visibility = View.GONE

        if (isOwner) {
            binding.tvRequestStatusBanner.visibility = View.VISIBLE
            binding.tvRequestStatusBanner.text = "Đây là bài đăng của bạn (${post.requestCount} lượt đăng ký)."
            binding.btnPrimaryAction.isEnabled = true
            binding.btnPrimaryAction.text = "XEM YÊU CẦU NHẬN ĐỒ"
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
                        binding.tvRequestStatusBanner.text = "⏳ Yêu cầu của bạn đang chờ chủ bài đăng duyệt."
                        binding.btnSecondaryAction.visibility = View.VISIBLE
                        binding.btnSecondaryAction.text = "Hủy yêu cầu"
                        binding.btnSecondaryAction.setOnClickListener {
                            AppDataStore.cancelRequest(myRequest.requestId)
                            Toast.makeText(this, "Đã hủy yêu cầu!", Toast.LENGTH_SHORT).show()
                            renderPost()
                        }
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "ĐANG CHỜ DUYỆT"
                    }
                    RequestStatus.ACCEPTED -> {
                        binding.tvRequestStatusBanner.text = "🎉 Chúc mừng! Bạn đã được chọn nhận món đồ này."
                        binding.btnPrimaryAction.isEnabled = true
                        binding.btnPrimaryAction.text = "LIÊN HỆ SĐT / ZALO"
                        binding.btnPrimaryAction.setOnClickListener {
                            ContactBottomSheet.show(this, myRequest, currentUser.userId)
                        }
                    }
                    RequestStatus.REJECTED -> {
                        binding.tvRequestStatusBanner.text = "Rất tiếc, món đồ đã được trao cho bạn khác."
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "KHÔNG ĐƯỢC CHỌN"
                    }
                    else -> {
                        binding.tvRequestStatusBanner.text = "Bạn đã hủy yêu cầu trước đó."
                        binding.btnPrimaryAction.isEnabled = false
                        binding.btnPrimaryAction.text = "ĐÃ HỦY YÊU CẦU"
                    }
                }
            }
            post.postStatus != PostStatus.AVAILABLE -> {
                binding.btnPrimaryAction.isEnabled = false
                binding.btnPrimaryAction.text = "MÓN ĐỒ ĐÃ HẾT"
            }
            else -> {
                binding.btnPrimaryAction.isEnabled = true
                binding.btnPrimaryAction.text = if (post.postType == PostType.GIVE_AWAY) {
                    "ĐĂNG KÝ NHẬN ĐỒ"
                } else {
                    "ĐỀ XUẤT TRAO ĐỔI"
                }
                binding.btnPrimaryAction.setOnClickListener {
                    showSendRequestDialog(post, currentUser)
                }
            }
        }
    }

    private fun showSendRequestDialog(post: Post, currentUser: User) {
        val dialogBinding = DialogSendRequestBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (post.postType == PostType.GIVE_AWAY) "Đăng ký nhận đồ" else "Đề xuất trao đổi")
            .setView(dialogBinding.root)
            .setNegativeButton("Đóng", null)
            .setPositiveButton("Gửi yêu cầu", null)
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
                    Toast.makeText(this, "Đã gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    renderPost()
                } else {
                    Toast.makeText(this, "Không thể gửi yêu cầu cho bài đăng này.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }
}