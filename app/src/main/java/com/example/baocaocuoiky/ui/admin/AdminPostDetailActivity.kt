package com.example.baocaocuoiky.ui.admin

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ActivityAdminPostDetailBinding
import com.example.baocaocuoiky.databinding.DialogAdminRejectBinding

class AdminPostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostDetailBinding
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("EXTRA_POST_ID") ?: ""
        binding.btnBack.setOnClickListener { finish() }

        val post = AppDataStore.posts.firstOrNull { it.postId == postId }
        if (post == null) {
            finish()
            return
        }

        val owner = AppDataStore.registeredAccounts.firstOrNull { it.first.userId == post.ownerId }?.first

        binding.tvDetailTitle.text = post.title
        binding.tvDetailType.text = if (post.postType == PostType.GIVE_AWAY) "0Đ GIVE_AWAY" else "TRAO ĐỔI"
        binding.tvDetailCondition.text = "Độ mới: ${post.conditionPercent}%"
        binding.tvDetailDesc.text = post.description
        binding.tvDetailLocation.text = "📍 Địa điểm giao nhận: ${post.pickupLocation}"

        binding.tvPosterName.text = post.ownerName
        binding.tvPosterInitial.text = post.ownerName.firstOrNull()?.uppercase() ?: "S"
        binding.tvPosterInfo.text = if (owner != null) {
            "MSV: ${owner.studentId} • Lớp ${owner.className} • 🌱 ${owner.greenPoints} Điểm"
        } else {
            "Sinh viên ĐH Sư phạm Kỹ thuật"
        }

        Glide.with(this)
            .load(post.imageUrl.ifBlank { null })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.imgDetailPost)

        if (post.postStatus == PostStatus.PENDING) {
            binding.tvTopStatus.text = "CHỜ DUYỆT"
            binding.tvTopStatus.setTextColor(Color.parseColor("#D97706"))
            binding.tvTopStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
            binding.btnDetailApprove.visibility = View.VISIBLE
        } else {
            binding.tvTopStatus.text = post.postStatus
            binding.tvTopStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))
            binding.tvTopStatus.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_green_bg)
            )
            binding.btnDetailApprove.visibility = View.GONE
        }

        binding.btnDetailApprove.setOnClickListener {
            AppDataStore.adminApprovePost(post.postId)
            Toast.makeText(this, "Đã duyệt hiển thị bài đăng!", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnDetailReject.setOnClickListener {
            val dialogBinding = DialogAdminRejectBinding.inflate(layoutInflater)
            dialogBinding.tvRejectSubtitle.text = "Chọn lý do gỡ bài của sinh viên ${post.ownerName}:"
            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialogBinding.btnCancelReject.setOnClickListener { dialog.dismiss() }
            dialogBinding.btnConfirmReject.setOnClickListener {
                AppDataStore.adminDeletePost(post.postId)
                Toast.makeText(this, "Đã gỡ bài đăng vi phạm!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }
            dialog.show()
        }
    }
}
