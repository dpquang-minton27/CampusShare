package com.example.baocaocuoiky.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.databinding.ActivityAdminDashboardBinding
import com.example.baocaocuoiky.ui.detail.PostDetailActivity
import com.google.android.material.tabs.TabLayout

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var postAdapter: AdminPostAdapter
    private lateinit var simpleAdapter: AdminSimpleAdapter
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = SessionManager.getCurrentUser(this)
        if (currentUser == null || currentUser.role != "ADMIN") {
            goToLogin()
            return
        }

        binding.btnAdminLogout.setOnClickListener {
            SessionManager.clearSession(this)
            goToLogin()
        }

        postAdapter = AdminPostAdapter(
            items = emptyList(),
            onOpenDetail = { post ->
                startActivity(Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("EXTRA_POST_ID", post.postId)
                })
            },
            onToggleModerate = { post ->
                val updated = AppDataStore.adminToggleHidePost(post.postId)
                if (updated != null) {
                    val msg = if (updated.postStatus == PostStatus.HIDDEN) {
                        "Đã ẩn bài đăng vi phạm!"
                    } else {
                        "Đã mở khóa hiển thị bài đăng!"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    refreshAll()
                }
            },
            onDelete = { post ->
                AlertDialog.Builder(this)
                    .setTitle("Xóa vĩnh viễn bài đăng?")
                    .setMessage("Bạn chắc chắn muốn xóa \"${post.title}\" khỏi hệ thống?")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Xóa") { _, _ ->
                        AppDataStore.adminDeletePost(post.postId)
                        Toast.makeText(this, "Đã xóa bài đăng!", Toast.LENGTH_SHORT).show()
                        refreshAll()
                    }
                    .show()
            }
        )

        simpleAdapter = AdminSimpleAdapter(emptyList()) { row ->
            if (currentTab == 1) {
                val cat = AppDataStore.categories.firstOrNull { it.id == row.id } ?: return@AdminSimpleAdapter
                cat.isActive = !cat.isActive
                Toast.makeText(
                    this,
                    if (cat.isActive) "Đã kích hoạt danh mục ${cat.name}" else "Đã tạm khóa danh mục ${cat.name}",
                    Toast.LENGTH_SHORT
                ).show()
                refreshAll()
            }
        }

        binding.rvAdminContent.layoutManager = LinearLayoutManager(this)
        binding.rvAdminContent.adapter = postAdapter

        binding.tabLayoutAdmin.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                refreshAll()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.fabAddCategory.setOnClickListener { showAddCategoryDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
    }

    private fun refreshAll() {
        // Cập nhật 4 chỉ số thống kê KPI
        val totalPosts = AppDataStore.posts.size
        val completedCount = AppDataStore.posts.count {
            it.postStatus == PostStatus.TAKEN || it.postStatus == PostStatus.EXCHANGED
        }
        val hiddenCount = AppDataStore.posts.count { it.postStatus == PostStatus.HIDDEN }
        val studentGreenPoints = AppDataStore.registeredAccounts
            .filter { it.first.role == "STUDENT" }
            .sumOf { it.first.greenPoints }

        binding.tvStatTotalPosts.text = totalPosts.toString()
        binding.tvStatCompleted.text = completedCount.toString()
        binding.tvStatGreenPoints.text = studentGreenPoints.toString()
        binding.tvStatHidden.text = hiddenCount.toString()

        when (currentTab) {
            0 -> {
                binding.fabAddCategory.visibility = View.GONE
                binding.rvAdminContent.adapter = postAdapter
                postAdapter.updateData(AppDataStore.posts.toList())
            }
            1 -> {
                binding.fabAddCategory.visibility = View.VISIBLE
                binding.rvAdminContent.adapter = simpleAdapter
                val rows = AppDataStore.categories.map { cat ->
                    val count = AppDataStore.posts.count { it.categoryId == cat.id }
                    AdminRowItem(
                        id = cat.id,
                        title = "${cat.name} ($count tin)",
                        subtitle = "${cat.description} • Bấm để đổi trạng thái",
                        badge = if (cat.isActive) "Đang mở" else "Tạm khóa"
                    )
                }
                simpleAdapter.updateData(rows)
            }
            else -> {
                binding.fabAddCategory.visibility = View.GONE
                binding.rvAdminContent.adapter = simpleAdapter
                val rows = AppDataStore.registeredAccounts.map { (user, _) ->
                    AdminRowItem(
                        id = user.userId,
                        title = "${user.fullName} (${user.studentId})",
                        subtitle = "${user.className} • ${user.email}",
                        badge = "${user.greenPoints} điểm xanh"
                    )
                }
                simpleAdapter.updateData(rows)
            }
        }
    }

    private fun showAddCategoryDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val edtName = EditText(this).apply { hint = "Tên danh mục mới (VD: Đồng phục thể dục)" }
        val edtDesc = EditText(this).apply { hint = "Mô tả ngắn" }
        container.addView(edtName)
        container.addView(edtDesc)

        AlertDialog.Builder(this)
            .setTitle("Thêm Danh Mục Mới")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Thêm") { _, _ ->
                val name = edtName.text.toString().trim()
                if (name.length >= 2) {
                    AppDataStore.addCategory(name, edtDesc.text.toString())
                    Toast.makeText(this, "Đã thêm danh mục $name!", Toast.LENGTH_SHORT).show()
                    refreshAll()
                } else {
                    Toast.makeText(this, "Tên danh mục quá ngắn!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun goToLogin() {
        startActivity(Intent(this, com.example.baocaocuoiky.ui.auth.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}