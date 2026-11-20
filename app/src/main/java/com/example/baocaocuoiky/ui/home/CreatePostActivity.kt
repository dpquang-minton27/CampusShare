package com.example.baocaocuoiky.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ActivityCreatePostBinding
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUri: String = ""
    private var conditionValue: Int = 90
    private var editingPostId: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            showPreviewImage(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.layoutPhotoPicker.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnUseSamplePhoto.setOnClickListener {
            selectedImageUri = "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=600"
            showPreviewImage(selectedImageUri)
            Toast.makeText(this, "Đã gắn ảnh minh họa mẫu!", Toast.LENGTH_SHORT).show()
        }

        binding.chipGroupPostType.setOnCheckedStateChangeListener { _, checkedIds ->
            val isExchange = checkedIds.firstOrNull() == R.id.chipPostExchange
            binding.tilExchangeNote.visibility = if (isExchange) View.VISIBLE else View.GONE
        }

        binding.sliderPostCondition.addOnChangeListener { _, value, _ ->
            conditionValue = value.toInt()
            binding.tvPostCondition.text = "Tình trạng độ mới: $conditionValue%"
        }

        // Kiểm tra nếu mở ở chế độ Chỉnh sửa
        editingPostId = intent.getStringExtra("EXTRA_POST_ID")
        if (editingPostId != null) {
            loadExistingPost(editingPostId!!)
        }

        binding.btnSubmitPost.setOnClickListener { savePost() }
    }

    private fun showPreviewImage(uriString: String) {
        binding.layoutPhotoPlaceholder.visibility = View.GONE
        binding.imgPreview.visibility = View.VISIBLE
        Glide.with(this).load(uriString).into(binding.imgPreview)
    }

    private fun loadExistingPost(postId: String) {
        val post = AppDataStore.posts.firstOrNull { it.postId == postId } ?: return
        binding.tvToolbarTitle.text = "Chỉnh Sửa Bài Đăng"
        binding.btnSubmitPost.text = "LƯU THAY ĐỔI"

        binding.edtTitle.setText(post.title)
        binding.edtDescription.setText(post.description)
        binding.edtLocation.setText(post.pickupLocation)
        conditionValue = post.conditionPercent
        binding.sliderPostCondition.value = conditionValue.toFloat()
        binding.tvPostCondition.text = "Tình trạng độ mới: $conditionValue%"

        if (post.imageUrl.isNotBlank()) {
            selectedImageUri = post.imageUrl
            showPreviewImage(selectedImageUri)
        }

        when (post.categoryId) {
            "books" -> binding.chipPostBooks.isChecked = true
            "tech" -> binding.chipPostTech.isChecked = true
            "dorm" -> binding.chipPostDorm.isChecked = true
            "study" -> binding.chipPostStudy.isChecked = true
        }

        if (post.postType == PostType.EXCHANGE) {
            binding.chipPostExchange.isChecked = true
            binding.tilExchangeNote.visibility = View.VISIBLE
            binding.edtExchangeNote.setText(post.exchangeNote)
        }
    }

    private fun savePost() {
        val currentUser = SessionManager.getCurrentUser(this) ?: return
        val title = binding.edtTitle.text.toString().trim()
        val location = binding.edtLocation.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        if (title.length < 3) {
            binding.edtTitle.error = "Tên món đồ phải từ 3 ký tự!"
            return
        }
        if (location.isBlank()) {
            binding.edtLocation.error = "Nhập địa điểm giao nhận!"
            return
        }
        if (description.length < 5) {
            binding.edtDescription.error = "Mô tả tối thiểu 5 ký tự!"
            return
        }

        val (catId, catName) = when (binding.chipGroupPostCat.checkedChipId) {
            R.id.chipPostTech -> "tech" to "Thiết bị điện tử"
            R.id.chipPostDorm -> "dorm" to "Đồ dùng KTX"
            R.id.chipPostStudy -> "study" to "Dụng cụ học tập"
            else -> "books" to "Giáo trình"
        }

        val isExchange = binding.chipGroupPostType.checkedChipId == R.id.chipPostExchange
        val postType = if (isExchange) PostType.EXCHANGE else PostType.GIVE_AWAY
        val exchangeNote = binding.edtExchangeNote.text.toString().trim()

        if (isExchange && exchangeNote.isBlank()) {
            binding.edtExchangeNote.error = "Hãy nhập món đồ bạn muốn đổi!"
            return
        }

        if (editingPostId == null) {
            val newPost = Post(
                postId = "p_${UUID.randomUUID().toString().take(6)}",
                ownerId = currentUser.userId,
                ownerName = currentUser.fullName,
                ownerClass = currentUser.className,
                ownerPhone = currentUser.phone,
                categoryId = catId,
                categoryName = catName,
                title = title,
                description = description,
                conditionPercent = conditionValue,
                pickupLocation = location,
                imageUrl = selectedImageUri.ifBlank {
                    "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=600"
                },
                postType = postType,
                postStatus = PostStatus.AVAILABLE,
                exchangeNote = exchangeNote
            )
            AppDataStore.posts.add(0, newPost)

            // Cộng 10 Điểm Sống Xanh cho sinh viên khi đăng bài mới
            val updatedUser = currentUser.copy(greenPoints = currentUser.greenPoints + 10)
            AppDataStore.updateUser(updatedUser)
            SessionManager.saveUser(this, updatedUser)

            Toast.makeText(this, "Đăng tin thành công! +10 Điểm Sống Xanh", Toast.LENGTH_SHORT).show()
        } else {
            val idx = AppDataStore.posts.indexOfFirst { it.postId == editingPostId }
            if (idx != -1) {
                val old = AppDataStore.posts[idx]
                AppDataStore.posts[idx] = old.copy(
                    categoryId = catId,
                    categoryName = catName,
                    title = title,
                    description = description,
                    conditionPercent = conditionValue,
                    pickupLocation = location,
                    imageUrl = selectedImageUri.ifBlank { old.imageUrl },
                    postType = postType,
                    exchangeNote = exchangeNote
                )
                Toast.makeText(this, "Đã cập nhật bài đăng!", Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }
}