package com.example.baocaocuoiky.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.databinding.ActivityAdminDashboardBinding
import com.example.baocaocuoiky.databinding.DialogAdminRejectBinding
import com.example.baocaocuoiky.ui.auth.LoginActivity

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var postAdapter: AdminPostAdapter
    private lateinit var userAdapter: AdminUserAdapter

    private var mainTab = 0   // 0: Bài đăng, 1: Sinh viên
    private var subFilter = 0 // Tab 0: (0=Chờ duyệt, 1=Tất cả, 2=Đang hiển thị) | Tab 1: (0=Tất cả, 1=Hoạt động, 2=Đã khóa)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postAdapter = AdminPostAdapter(
            items = emptyList(),
            onOpenDetail = { post ->
                startActivity(Intent(this, AdminPostDetailActivity::class.java).apply {
                    putExtra("EXTRA_POST_ID", post.postId)
                })
            },
            onApprove = { post ->
                AppDataStore.adminApprovePost(post.postId)
                Toast.makeText(this, "Đã duyệt bài \"${post.title}\"", Toast.LENGTH_SHORT).show()
                refreshUI()
            },
            onDelete = { post -> showRejectDialog(post) }
        )

        userAdapter = AdminUserAdapter(emptyList()) { user ->
            val lockedNow = AppDataStore.adminToggleLockUser(user.userId)
            val msg = if (lockedNow) "Đã khóa tài khoản ${user.fullName}" else "Đã mở khóa ${user.fullName}"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            refreshUI()
        }

        binding.rvAdminList.layoutManager = LinearLayoutManager(this)

        binding.navTabPosts.setOnClickListener { switchMainTab(0) }
        binding.navTabUsers.setOnClickListener { switchMainTab(1) }
        binding.cardStatPending.setOnClickListener {
            subFilter = 0
            refreshUI()
        }
        binding.cardStatStudents.setOnClickListener { switchMainTab(1) }

        binding.pillFilter1.setOnClickListener { subFilter = 0; refreshUI() }
        binding.pillFilter2.setOnClickListener { subFilter = 1; refreshUI() }
        binding.pillFilter3.setOnClickListener { subFilter = 2; refreshUI() }

        binding.edtAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refreshUI() }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAdminLogout.setOnClickListener {
            SessionManager.clearSession(this)
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        switchMainTab(0)
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun switchMainTab(tab: Int) {
        mainTab = tab
        subFilter = 0
        binding.edtAdminSearch.setText("")

        val blue = ContextCompat.getColor(this, R.color.primary_blue)
        val gray = ContextCompat.getColor(this, R.color.text_secondary)

        if (tab == 0) {
            binding.tvHeaderTitle.text = "Quản Trị Hệ Thống"
            binding.tvHeaderSubtitle.text = "CampusShare UTE"
            binding.layoutStatsGrid.visibility = View.VISIBLE
            binding.edtAdminSearch.hint = "Tìm bài đăng, người đăng, MSV..."

            binding.icNavPosts.imageTintList = ColorStateList.valueOf(blue)
            binding.tvNavPosts.setTextColor(blue)
            binding.icNavUsers.imageTintList = ColorStateList.valueOf(gray)
            binding.tvNavUsers.setTextColor(gray)

            binding.rvAdminList.adapter = postAdapter
        } else {
            binding.tvHeaderTitle.text = "Danh Sách Sinh Viên"
            binding.tvHeaderSubtitle.text = "Quản lý tài khoản & Điểm Sống Xanh UTE"
            binding.layoutStatsGrid.visibility = View.GONE
            binding.edtAdminSearch.hint = "Tìm họ tên, MSV hoặc lớp học phần..."

            binding.icNavUsers.imageTintList = ColorStateList.valueOf(blue)
            binding.tvNavUsers.setTextColor(blue)
            binding.icNavPosts.imageTintList = ColorStateList.valueOf(gray)
            binding.tvNavPosts.setTextColor(gray)

            binding.rvAdminList.adapter = userAdapter
        }
        refreshUI()
    }

    private fun refreshUI() {
        val allPosts = AppDataStore.posts
        val students = AppDataStore.registeredAccounts.map { it.first }.filter { it.role != "ADMIN" }
        val pendingPosts = allPosts.filter { it.postStatus == PostStatus.PENDING }
        val completedCount = allPosts.count {
            it.postStatus == PostStatus.TAKEN || it.postStatus == PostStatus.EXCHANGED
        }

        // Cập nhật 4 thẻ thống kê
        binding.tvStatTotalPosts.text = allPosts.size.toString()
        binding.tvStatPending.text = pendingPosts.size.toString()
        binding.tvStatStudents.text = students.size.toString()
        binding.tvStatCompleted.text = completedCount.toString()

        // Cập nhật style 3 nút lọc ngang
        stylePill(binding.pillFilter1, subFilter == 0)
        stylePill(binding.pillFilter2, subFilter == 1)
        stylePill(binding.pillFilter3, subFilter == 2)

        val query = binding.edtAdminSearch.text.toString().trim().lowercase()

        if (mainTab == 0) {
            binding.pillFilter1.text = "Chờ duyệt (${pendingPosts.size})"
            binding.pillFilter2.text = "Tất cả (${allPosts.size})"
            binding.pillFilter3.text = "Đang hiển thị"

            val byFilter = when (subFilter) {
                0 -> pendingPosts
                1 -> allPosts
                else -> allPosts.filter { it.postStatus == PostStatus.AVAILABLE }
            }
            val result = byFilter.filter {
                query.isEmpty() || it.title.lowercase().contains(query) || it.ownerName.lowercase().contains(query)
            }
            postAdapter.updateData(result)
            binding.tvAdminEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
        } else {
            val lockedUsers = students.filter { it.isLocked }
            binding.pillFilter1.text = "Tất cả (${students.size})"
            binding.pillFilter2.text = "Đang hoạt động"
            binding.pillFilter3.text = "Đã khóa (${lockedUsers.size})"

            val byFilter = when (subFilter) {
                0 -> students
                1 -> students.filter { !it.isLocked }
                else -> lockedUsers
            }
            val result = byFilter.filter {
                query.isEmpty() ||
                    it.fullName.lowercase().contains(query) ||
                    it.studentId.lowercase().contains(query) ||
                    it.className.lowercase().contains(query)
            }
            userAdapter.updateData(result)
            binding.tvAdminEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun stylePill(pill: TextView, active: Boolean) {
        val blue = ContextCompat.getColor(this, R.color.primary_blue)
        val white = ContextCompat.getColor(this, R.color.white)
        pill.setBackgroundResource(if (active) R.drawable.bg_pill_blue else R.drawable.bg_pill_outline)
        pill.setTextColor(if (active) white else blue)
    }

    private fun showRejectDialog(post: Post) {
        val dialogBinding = DialogAdminRejectBinding.inflate(layoutInflater)
        dialogBinding.tvRejectSubtitle.text = "Chọn hoặc nhập lý do gửi thông báo đến sinh viên ${post.ownerName}:"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelReject.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnConfirmReject.setOnClickListener {
            val selectedText = when (dialogBinding.rgRejectReasons.checkedRadioButtonId) {
                R.id.rbReason1 -> dialogBinding.rbReason1.text.toString()
                R.id.rbReason2 -> dialogBinding.rbReason2.text.toString()
                else -> dialogBinding.rbReason3.text.toString()
            }
            val note = dialogBinding.edtCustomReason.text.toString().trim()
            val finalReason = if (note.isNotEmpty()) "$selectedText ($note)" else selectedText

            AppDataStore.adminDeletePost(post.postId)
            Toast.makeText(this, "Đã gỡ bài: $finalReason", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            refreshUI()
        }

        dialog.show()
    }
}
