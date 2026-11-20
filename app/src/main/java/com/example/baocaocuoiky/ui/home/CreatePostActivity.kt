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

/**
 * Create/Edit Post aligned to Figma 06_Create_Post_Screen (16:140).
 * The existing Post.imageUrl remains the cover image; imageUrls stores up to 3
 * local prototype images. This app does not yet upload images to a backend.
 */
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUris: MutableList<String> = mutableListOf()
    private var conditionValue: Int = 85
    private var editingPostId: String? = null

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris.take(3).map { it.toString() }.toMutableList()
            refreshImagePreviews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.layoutPhotoPicker.setOnClickListener {
            pickImagesLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Preserved for compatibility; shortcut is hidden because it is not in Figma.
        binding.btnUseSamplePhoto.setOnClickListener {
            selectedImageUris = mutableListOf(DEFAULT_SAMPLE_IMAGE)
            refreshImagePreviews()
            Toast.makeText(this, "Đã chọn ảnh minh họa mẫu!", Toast.LENGTH_SHORT).show()
        }

        binding.chipGroupPostType.setOnCheckedChangeListener { _, checkedId ->
            binding.tilExchangeNote.visibility =
                if (checkedId == R.id.chipPostExchange) View.VISIBLE else View.GONE
            refreshGiveAwayCheck()
        }
        refreshGiveAwayCheck()

        binding.sliderPostCondition.addOnChangeListener { _, value, _ ->
            conditionValue = value.toInt()
            updateConditionLabel()
        }
        updateConditionLabel()

        editingPostId = intent.getStringExtra("EXTRA_POST_ID")
        editingPostId?.let(::loadExistingPost)

        binding.btnSubmitPost.setOnClickListener { savePost() }
    }

    private fun refreshGiveAwayCheck() {
        val isGiveAway = binding.chipPostGiveAway.isChecked
        binding.chipPostGiveAway.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (isGiveAway) R.drawable.post_ic_green_check_badge else 0, 0, 0, 0
        )
    }

    private fun updateConditionLabel() {
        binding.tvPostCondition.text = "Tình trạng đồ dùng (%)"
        binding.tvPostConditionValue.text = "Mới $conditionValue%"
    }

    private fun refreshImagePreviews() {
        val main = selectedImageUris.firstOrNull()
        if (main == null) {
            binding.imgPreview.visibility = View.GONE
            binding.layoutPhotoPlaceholder.visibility = View.VISIBLE
            Glide.with(this).clear(binding.imgPreview)
        } else {
            binding.layoutPhotoPlaceholder.visibility = View.GONE
            binding.imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(main).centerCrop().into(binding.imgPreview)
        }

        val extraImages = listOf(binding.imgPostThumb1, binding.imgPostThumb2)
        for ((index, thumbnail) in extraImages.withIndex()) {
            val next = selectedImageUris.getOrNull(index + 1)
            Glide.with(this).clear(thumbnail)
            if (next == null) {
                thumbnail.setImageDrawable(null)
            } else {
                Glide.with(this).load(next).centerCrop().into(thumbnail)
            }
        }
    }

    private fun loadExistingPost(postId: String) {
        val post = AppDataStore.posts.firstOrNull { it.postId == postId } ?: return
        binding.tvToolbarTitle.text = "CHỈNH SỬA BÀI ĐĂNG"
        binding.btnSubmitPost.text = "LƯU THAY ĐỔI"
        binding.edtTitle.setText(post.title)
        binding.edtDescription.setText(post.description)
        binding.edtLocation.setText(post.pickupLocation)

        // Slider uses 1% increments; existing values such as 88% are valid.
        conditionValue = post.conditionPercent.coerceIn(10, 100)
        binding.sliderPostCondition.value = conditionValue.toFloat()
        updateConditionLabel()

        selectedImageUris = (
            post.imageUrls.ifEmpty {
                post.imageUrl.takeIf { it.isNotBlank() }?.let { listOf(it) }.orEmpty()
            }
        ).take(3).toMutableList()
        refreshImagePreviews()

        when (post.categoryId) {
            "books" -> binding.chipPostBooks.isChecked = true
            "tech" -> binding.chipPostTech.isChecked = true
            "dorm" -> binding.chipPostDorm.isChecked = true
            "study" -> binding.chipPostStudy.isChecked = true // legacy storage ID
        }

        if (post.postType == PostType.EXCHANGE) {
            binding.chipPostExchange.isChecked = true
            binding.tilExchangeNote.visibility = View.VISIBLE
            binding.edtExchangeNote.setText(post.exchangeNote)
        } else {
            binding.chipPostGiveAway.isChecked = true
        }
        refreshGiveAwayCheck()
    }

    private fun savePost() {
        val currentUser = SessionManager.getCurrentUser(this) ?: run {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng tin.", Toast.LENGTH_SHORT).show()
            return
        }
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

        // Retain the existing IDs consumed by Home and Search to avoid breaking filters.
        val (catId, catName) = when (binding.chipGroupPostCat.checkedChipId) {
            R.id.chipPostTech -> "tech" to "Thiết bị điện tử"
            R.id.chipPostDorm -> "dorm" to "Đồ dùng KTX"
            R.id.chipPostStudy -> "study" to "Đồ cá nhân"
            else -> "books" to "Giáo trình"
        }

        val isExchange = binding.chipGroupPostType.checkedRadioButtonId == R.id.chipPostExchange
        val postType = if (isExchange) PostType.EXCHANGE else PostType.GIVE_AWAY
        val exchangeNote = binding.edtExchangeNote.text.toString().trim()
        if (isExchange && exchangeNote.isBlank()) {
            binding.edtExchangeNote.error = "Hãy nhập món đồ bạn muốn đổi!"
            return
        }

        // Current prototype uses an image URL or on-device URI; no cloud storage exists.
        val images = selectedImageUris.take(3).ifEmpty { listOf(DEFAULT_SAMPLE_IMAGE) }
        val coverImage = images.first()

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
                imageUrl = coverImage,
                imageUrls = images,
                postType = postType,
                postStatus = PostStatus.AVAILABLE,
                exchangeNote = exchangeNote
            )
            AppDataStore.posts.add(0, newPost)

            val updatedUser = currentUser.copy(greenPoints = currentUser.greenPoints + 10)
            AppDataStore.updateUser(updatedUser)
            SessionManager.saveUser(this, updatedUser)
            Toast.makeText(this, "Đăng tin thành công! +10 Điểm Sống Xanh", Toast.LENGTH_SHORT).show()
        } else {
            val index = AppDataStore.posts.indexOfFirst { it.postId == editingPostId }
            if (index != -1) {
                val old = AppDataStore.posts[index]
                AppDataStore.posts[index] = old.copy(
                    categoryId = catId,
                    categoryName = catName,
                    title = title,
                    description = description,
                    conditionPercent = conditionValue,
                    pickupLocation = location,
                    imageUrl = coverImage,
                    imageUrls = images,
                    postType = postType,
                    exchangeNote = exchangeNote
                )
                Toast.makeText(this, "Đã cập nhật bài đăng!", Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    private companion object {
        const val DEFAULT_SAMPLE_IMAGE =
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=600"
    }
}
