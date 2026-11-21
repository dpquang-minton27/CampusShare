package com.example.baocaocuoiky.ui.detail

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.ExchangeRequest
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.data.model.RequestStatus
import com.example.baocaocuoiky.databinding.ActivityPostRequestsBinding
import com.example.baocaocuoiky.databinding.DialogAcceptSuccessBinding
import com.example.baocaocuoiky.databinding.DialogRejectRequestBinding

class PostRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostRequestsBinding
    private lateinit var adapter: PostRequestAdapter
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("EXTRA_POST_ID") ?: ""
        binding.btnBack.setOnClickListener { finish() }

        adapter = PostRequestAdapter(
            items = emptyList(),
            onReject = { req -> handleReject(req) },
            onAccept = { req -> confirmAccept(req) }
        )
        binding.rvPostRequests.layoutManager = LinearLayoutManager(this)
        binding.rvPostRequests.adapter = adapter

        loadData()
    }

    private fun loadData() {
        val post = AppDataStore.posts.firstOrNull { it.postId == postId }
        if (post == null) {
            finish()
            return
        }

        binding.tvPostTitle.text = post.title
        Glide.with(this)
            .load(post.imageUrl.ifBlank { null })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.imgPostThumb)

        if (post.postType == PostType.GIVE_AWAY) {
            binding.tvTypePrefix.visibility = View.VISIBLE
            binding.tvTypePrefix.text = "0Đ"
            binding.tvTypeLabel.text = "GIVE_AWAY"
            binding.tvTypeLabel.setTextColor(ContextCompat.getColor(this, R.color.status_green))
            binding.layoutTypeBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_green_bg)
            )
        } else {
            binding.tvTypePrefix.visibility = View.GONE
            binding.tvTypeLabel.text = "TRAO ĐỔI EXCHANGE"
            binding.tvTypeLabel.setTextColor(Color.parseColor("#E65100"))
            binding.layoutTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
        }

        val list = AppDataStore.requests
            .filter { it.postId == postId }
            .sortedByDescending { it.createdAt }

        val pendingCount = list.count { it.requestStatus == RequestStatus.PENDING }
        binding.tvPendingCount.text = "$pendingCount yêu cầu đang chờ"

        adapter.updateData(list)
        binding.tvEmptyRequests.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun handleReject(req: ExchangeRequest) {
        val dialogBinding = DialogRejectRequestBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Từ chối yêu cầu")
            .setMessage("Nhập lý do từ chối gửi tới ${req.requesterName}:")
            .setView(dialogBinding.root)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xác nhận từ chối") { _, _ ->
                val reason = dialogBinding.edtReason.text.toString().trim()
                if (reason.isBlank()) {
                    Toast.makeText(this, "Vui lòng nhập lý do từ chối!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val idx = AppDataStore.requests.indexOfFirst { it.requestId == req.requestId }
                if (idx != -1) {
                    AppDataStore.requests[idx] = req.copy(
                        requestStatus = RequestStatus.REJECTED,
                        rejectReason = reason
                    )
                    Toast.makeText(this, "Đã từ chối yêu cầu!", Toast.LENGTH_SHORT).show()
                    loadData()
                }
            }
            .show()
    }

    private fun confirmAccept(req: ExchangeRequest) {
        // 1. Chấp nhận yêu cầu được chọn và tự động từ chối các yêu cầu còn lại
        AppDataStore.requests.forEachIndexed { index, item ->
            if (item.postId == postId) {
                if (item.requestId == req.requestId) {
                    AppDataStore.requests[index] = item.copy(requestStatus = RequestStatus.ACCEPTED)
                } else if (item.requestStatus == RequestStatus.PENDING) {
                    AppDataStore.requests[index] = item.copy(
                        requestStatus = RequestStatus.REJECTED,
                        rejectReason = "Chủ bài đăng đã chọn trao cho sinh viên khác"
                    )
                }
            }
        }

        // 2. Chuyển trạng thái bài đăng sang ĐÃ HOÀN TẤT (TAKEN / EXCHANGED)
        var postTitle = "món đồ"
        val postIdx = AppDataStore.posts.indexOfFirst { it.postId == postId }
        if (postIdx != -1) {
            val oldPost = AppDataStore.posts[postIdx]
            postTitle = oldPost.title
            val doneStatus = if (oldPost.postType == PostType.GIVE_AWAY) {
                PostStatus.TAKEN
            } else {
                PostStatus.EXCHANGED
            }
            AppDataStore.posts[postIdx] = oldPost.copy(postStatus = doneStatus)
        }

        // 3. Hiển thị hộp thoại Xác Nhận Trao Đồ Thành Công
        val requester = AppDataStore.registeredAccounts
            .firstOrNull { it.first.userId == req.requesterId }?.first
        val zaloPhone = requester?.zaloPhone?.ifBlank { requester.phone } ?: "0796658054"
        val shortName = req.requesterName.trim().substringAfterLast(" ")

        val dialogBinding = DialogAcceptSuccessBinding.inflate(layoutInflater)
        dialogBinding.tvSuccessBody.text =
            "Bạn đã chấp nhận yêu cầu của ${req.requesterName}. Trạng thái bài đăng \"$postTitle\" đã tự động cập nhật sang ĐÃ HOÀN TẤT"
        dialogBinding.tvZaloNotice.text =
            "Số Zalo của bạn $shortName: $zaloPhone\nHãy liên hệ để hẹn thời gian, địa điểm bàn giao đồ nhé!"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCloseAndReturn.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
