package com.example.baocaocuoiky.ui.profile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.ProfileLocalStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.databinding.ActivityEditProfileBinding
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var pendingAvatarUri: Uri? = null

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                if (!ProfileLocalStore.grantAvatarRead(this, uri)) {
                    Toast.makeText(this, "Không thể giữ quyền đọc ảnh đã chọn.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                pendingAvatarUri = uri
                Glide.with(this).load(uri)
                    .placeholder(R.drawable.logo_ute)
                    .error(R.drawable.logo_ute)
                    .into(binding.imgAvatar)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Consistent edge-to-edge behavior on Android 15+ and older Android versions.
        // The ScrollView handles system-bar and keyboard insets exactly once.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureKeyboardAwareScrolling()
        // Avoid opening the keyboard immediately when entering the profile editor.
        binding.scrollEditProfile.requestFocus()

        val current = SessionManager.getCurrentUser(this)
        if (current == null) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.layoutAvatar.setOnClickListener {
            pickAvatarLauncher.launch(arrayOf("image/*"))
        }
        binding.edtFullName.setText(current.fullName)
        binding.edtClassName.setText(current.className)
        binding.edtContact.setText(current.phone.ifBlank { current.zaloPhone })
        binding.tvStudentId.text = current.studentId
        binding.tvEmail.text = current.email
        showAvatar(current.userId)

        binding.btnSave.setOnClickListener {
            val name = binding.edtFullName.text.toString().trim()
            val className = binding.edtClassName.text.toString().trim()
            val contact = binding.edtContact.text.toString().trim()

            if (name.length !in 2..80) {
                binding.edtFullName.error = "Họ tên phải từ 2 đến 80 ký tự"
                binding.edtFullName.requestFocus()
                return@setOnClickListener
            }
            if (className.isBlank()) {
                binding.edtClassName.error = "Vui lòng nhập Lớp / Khoa"
                binding.edtClassName.requestFocus()
                return@setOnClickListener
            }
            if (contact.isNotEmpty() && !Regex("^0[0-9]{9}$").matches(contact)) {
                binding.edtContact.error = "Số điện thoại gồm 10 chữ số và bắt đầu bằng 0"
                binding.edtContact.requestFocus()
                return@setOnClickListener
            }

            // Student ID and email must remain immutable as shown in Figma.
            val latest = SessionManager.getCurrentUser(this) ?: current
            val updated = latest.copy(
                fullName = name,
                className = className,
                phone = contact,
                zaloPhone = contact
            )
            AppDataStore.updateUser(updated)
            SessionManager.saveUser(this, updated)
            ProfileLocalStore.saveProfile(this, updated)
            pendingAvatarUri?.let {
                ProfileLocalStore.saveAvatar(this, updated.userId, it)
            }
            setResult(Activity.RESULT_OK)
            Toast.makeText(this, "Đã lưu thay đổi hồ sơ!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showAvatar(uid: String) {
        val uri = ProfileLocalStore.avatarUri(this, uid)
        Glide.with(this)
            .load(uri ?: R.drawable.logo_ute)
            .placeholder(R.drawable.logo_ute)
            .error(R.drawable.logo_ute)
            .into(binding.imgAvatar)
    }

    /**
     * Do not scale or compress the form when the IME opens. Instead, reserve
     * keyboard space as ScrollView padding so even the Save button can be
     * scrolled fully above the on-screen keyboard.
     *
     * ScrollView in XML has fitsSystemWindows=false to prevent applying
     * status/navigation insets a second time.
     */
    private fun configureKeyboardAwareScrolling() {
        val scroll = binding.scrollEditProfile
        val baseLeft = scroll.paddingLeft
        val baseTop = scroll.paddingTop
        val baseRight = scroll.paddingRight
        val baseBottom = scroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.updatePadding(
                left = baseLeft + safe.left,
                top = baseTop + safe.top,
                right = baseRight + safe.right,
                bottom = baseBottom + maxOf(safe.bottom, keyboard.bottom)
            )
            // Keep insets available for the Android view hierarchy.
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
    }
}
