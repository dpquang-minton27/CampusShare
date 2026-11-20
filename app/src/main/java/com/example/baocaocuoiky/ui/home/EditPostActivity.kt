package com.example.baocaocuoiky.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.Post
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.ActivityEditPostBinding

/** Full-screen editor matching Figma 60:389; no change to CreatePostActivity. */
class EditPostActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_POST_ID = "EXTRA_POST_ID"
    }

    private lateinit var binding: ActivityEditPostBinding
    private lateinit var postId: String
    private var imageUrl: String = ""
    private var isExchange: Boolean = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // The current app uses in-memory posts. Persist URI access where the provider allows it;
            // an actual multi-device backend will need a media upload instead of saving content://.
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some pickers provide temporary access instead.
            }
            imageUrl = uri.toString()
            renderImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra(EXTRA_POST_ID).orEmpty()
        if (postId.isBlank()) {
            Toast.makeText(this, "Thiếu mã bài đăng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val post = AppDataStore.posts.firstOrNull { it.postId == postId }
        val uid = SessionManager.getCurrentUser(this)?.userId
        if (post == null || uid == null || post.ownerId != uid) {
            Toast.makeText(this, "Không thể chỉnh sửa bài đăng này", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.layoutPhotoPicker.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnGiveAway.setOnClickListener { isExchange = false; updateTypeStyle() }
        binding.btnExchange.setOnClickListener { isExchange = true; updateTypeStyle() }
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, _ -> updateCategoryStyle() }
        binding.sliderCondition.addOnChangeListener { _, value, _ ->
            binding.tvCondition.text = "Mới ${value.toInt()}%"
        }
        loadPost(post)
        binding.btnSavePost.setOnClickListener { saveChanges() }
    }

    private fun loadPost(post: Post) = with(binding) {
        edtTitle.setText(post.title)
        edtDescription.setText(post.description)
        edtLocation.setText(post.pickupLocation)
        edtExchangeNote.setText(post.exchangeNote)
        imageUrl = post.imageUrl
        renderImage()
        sliderCondition.value = post.conditionPercent.coerceIn(0, 100).toFloat()
        tvCondition.text = "Mới ${post.conditionPercent.coerceIn(0, 100)}%"
        // 'study' is a real legacy category in this repository; do not silently relabel it.
        chipStudyLegacy.visibility = if (post.categoryId == "study") View.VISIBLE else View.GONE
        chipGroupCategory.check(when (post.categoryId) {
            "tech" -> R.id.chipTech
            "dorm" -> R.id.chipDorm
            "personal" -> R.id.chipPersonal
            "study" -> R.id.chipStudyLegacy
            else -> R.id.chipBooks
        })
        isExchange = post.postType == PostType.EXCHANGE
        updateTypeStyle()
        updateCategoryStyle()
    }

    private fun renderImage() {
        binding.tvPhotoEmpty.visibility = if (imageUrl.isBlank()) View.VISIBLE else View.GONE
        binding.imgPreview.visibility = if (imageUrl.isBlank()) View.INVISIBLE else View.VISIBLE
        if (imageUrl.isNotBlank()) Glide.with(this).load(imageUrl).into(binding.imgPreview)
        else Glide.with(this).clear(binding.imgPreview)
    }

    private fun updateCategoryStyle() {
        val active = ContextCompat.getColor(this, R.color.cs_edit_blue)
        val inactive = ContextCompat.getColor(this, R.color.cs_edit_ink)
        val white = ContextCompat.getColor(this, R.color.white)
        val gray = ContextCompat.getColor(this, R.color.cs_edit_chip)
        listOf(binding.chipBooks, binding.chipTech, binding.chipDorm,
            binding.chipPersonal, binding.chipStudyLegacy).forEach { chip ->
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(if (chip.isChecked) active else gray))
            chip.setTextColor(if (chip.isChecked) white else inactive)
        }
    }

    private fun updateTypeStyle() {
        binding.tilExchangeNote.visibility = if (isExchange) View.VISIBLE else View.GONE
        binding.btnGiveAway.setBackgroundResource(if (isExchange) R.drawable.cs_edit_gray_pill else R.drawable.cs_edit_green_pill)
        binding.btnGiveAway.setTextColor(ContextCompat.getColor(this,
            if (isExchange) R.color.cs_edit_ink else R.color.cs_edit_green))
        binding.btnGiveAway.text = if (isExchange) "Cho tặng 0đ\n(GIVE_AWAY)" else "✓  Cho tặng 0đ\n    (GIVE_AWAY)"
        binding.btnExchange.setBackgroundResource(if (isExchange) R.drawable.cs_edit_blue_pill else R.drawable.cs_edit_outline_pill)
        binding.btnExchange.setTextColor(ContextCompat.getColor(this,
            if (isExchange) R.color.white else R.color.cs_edit_ink))
    }

    private fun saveChanges() {
        val uid = SessionManager.getCurrentUser(this)?.userId
        val current = AppDataStore.posts.firstOrNull { it.postId == postId }
        if (uid == null || current == null || current.ownerId != uid) {
            Toast.makeText(this, "Bài đăng không còn khả dụng để sửa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val location = binding.edtLocation.text.toString().trim()
        if (title.length < 3) { binding.edtTitle.error = "Tiêu đề tối thiểu 3 ký tự"; return }
        if (description.length < 5) { binding.edtDescription.error = "Mô tả tối thiểu 5 ký tự"; return }
        if (location.isBlank()) { binding.edtLocation.error = "Vui lòng nhập địa điểm"; return }
        val category = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipTech -> "tech" to "Thiết bị điện tử"
            R.id.chipDorm -> "dorm" to "Đồ dùng KTX"
            R.id.chipPersonal -> "personal" to "Đồ cá nhân"
            R.id.chipStudyLegacy -> "study" to "Dụng cụ học tập"
            else -> "books" to "Giáo trình"
        }
        val exchange = isExchange
        val exchangeNote = binding.edtExchangeNote.text.toString().trim()
        if (exchange && exchangeNote.isBlank()) {
            binding.edtExchangeNote.error = "Nhập món đồ muốn trao đổi"
            return
        }
        val index = AppDataStore.posts.indexOfFirst { it.postId == postId && it.ownerId == uid }
        if (index == -1) { Toast.makeText(this, "Không tìm thấy bài đăng", Toast.LENGTH_SHORT).show(); return }
        AppDataStore.posts[index] = current.copy(
            title = title,
            categoryId = category.first,
            categoryName = category.second,
            postType = if (exchange) PostType.EXCHANGE else PostType.GIVE_AWAY,
            exchangeNote = if (exchange) exchangeNote else "",
            conditionPercent = binding.sliderCondition.value.toInt(),
            description = description,
            pickupLocation = location,
            imageUrl = imageUrl
        )
        Toast.makeText(this, "Đã cập nhật bài đăng!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}
