package com.example.baocaocuoiky.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import com.example.baocaocuoiky.databinding.ActivityMyPostsBinding
import com.example.baocaocuoiky.ui.detail.PostDetailActivity
import com.example.baocaocuoiky.ui.detail.RequestsActivity

class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var adapter: MyPostAdapter
    private var selectedTab = 0 // 0: Đang hiển thị, 1: Chờ duyệt, 2: Đã hoàn tất

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val openCreate = View.OnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        binding.btnAddPostTop.setOnClickListener(openCreate)
        binding.fabAddPost.setOnClickListener(openCreate)

        adapter = MyPostAdapter(
            items = emptyList(),
            onOpenDetail = { post ->
                startActivity(Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("EXTRA_POST_ID", post.postId)
                })
            },
            onViewRequests = { post ->
                startActivity(Intent(this, com.example.baocaocuoiky.ui.detail.PostRequestsActivity::class.java).apply {
                    putExtra("EXTRA_POST_ID", post.postId)
                })
            },
            onEditPost = { post -> showEditPostDialog(post) },
            onDeletePost = { post -> confirmDeletePost(post) }
        )

        binding.rvMyPosts.layoutManager = LinearLayoutManager(this)
        binding.rvMyPosts.adapter = adapter

        binding.tabActive.setOnClickListener { switchTab(0) }
        binding.tabPending.setOnClickListener { switchTab(1) }
        binding.tabCompleted.setOnClickListener { switchTab(2) }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun switchTab(tabIndex: Int) {
        selectedTab = tabIndex
        updateTabStyles()
        refreshList()
    }

    private fun updateTabStyles() {
        styleTab(binding.tabActive, binding.tvTabActiveLabel, binding.tvTabActiveCount, selectedTab == 0)
        styleTab(binding.tabPending, binding.tvTabPendingLabel, binding.tvTabPendingCount, selectedTab == 1)
        styleTab(binding.tabCompleted, binding.tvTabCompletedLabel, binding.tvTabCompletedCount, selectedTab == 2)
    }

    private fun styleTab(container: View, label: TextView, badge: TextView, active: Boolean) {
        val blue = ContextCompat.getColor(this, R.color.primary_blue)
        val white = ContextCompat.getColor(this, R.color.white)

        if (active) {
            container.setBackgroundResource(R.drawable.bg_pill_blue)
            label.setTextColor(white)
            badge.setBackgroundResource(R.drawable.bg_circle_white_border)
            badge.setTextColor(blue)
        } else {
            container.setBackgroundResource(R.drawable.bg_pill_outline)
            label.setTextColor(blue)
            badge.setBackgroundResource(R.drawable.bg_circle_light_blue)
            badge.setTextColor(blue)
        }
    }

    private fun refreshList() {
        val uid = SessionManager.getCurrentUser(this)?.userId ?: return
        val myAll = AppDataStore.posts.filter { it.ownerId == uid }

        val activeList = myAll.filter { it.postStatus == PostStatus.AVAILABLE }
        val completedList = myAll.filter {
            it.postStatus == PostStatus.TAKEN || it.postStatus == PostStatus.EXCHANGED
        }
        val pendingList = myAll.filter {
            it.postStatus != PostStatus.AVAILABLE &&
                it.postStatus != PostStatus.TAKEN &&
                it.postStatus != PostStatus.EXCHANGED
        }

        binding.tvTabActiveCount.text = activeList.size.toString()
        binding.tvTabPendingCount.text = pendingList.size.toString()
        binding.tvTabCompletedCount.text = completedList.size.toString()

        val showing = when (selectedTab) {
            0 -> activeList
            1 -> pendingList
            else -> completedList
        }

        adapter.updateData(showing)
        binding.tvEmptyMyPosts.visibility = if (showing.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEditPostDialog(post: Post) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val edtTitle = EditText(this).apply {
            hint = "Tiêu đề món đồ"
            setText(post.title)
        }
        val edtLocation = EditText(this).apply {
            hint = "Địa điểm giao nhận"
            setText(post.pickupLocation)
        }
        val edtDesc = EditText(this).apply {
            hint = "Mô tả chi tiết"
            setText(post.description)
        }
        container.addView(edtTitle)
        container.addView(edtLocation)
        container.addView(edtDesc)

        AlertDialog.Builder(this)
            .setTitle("Chỉnh Sửa Bài Đăng")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = edtTitle.text.toString().trim()
                if (newTitle.isBlank()) {
                    Toast.makeText(this, "Tiêu đề không được để trống!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val idx = AppDataStore.posts.indexOfFirst { it.postId == post.postId }
                if (idx != -1) {
                    AppDataStore.posts[idx] = post.copy(
                        title = newTitle,
                        pickupLocation = edtLocation.text.toString().trim().ifBlank { post.pickupLocation },
                        description = edtDesc.text.toString().trim().ifBlank { post.description }
                    )
                    Toast.makeText(this, "Đã cập nhật bài đăng!", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
            }
            .show()
    }

    private fun confirmDeletePost(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Xóa bài đăng?")
            .setMessage("Bạn chắc chắn muốn xóa \"${post.title}\" không?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                AppDataStore.adminDeletePost(post.postId)
                Toast.makeText(this, "Đã xóa bài đăng!", Toast.LENGTH_SHORT).show()
                refreshList()
            }
            .show()
    }
}
