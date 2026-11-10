package com.campusshare.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.campusshare.app.R
import com.campusshare.app.data.CampusShareData
import com.campusshare.app.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val model: ProfileViewModel by viewModels {
        ProfileViewModel.factory(CampusShareData.authRepository, CampusShareData.userRepository)
    }
    private var lastRenderedVersion = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CampusShareData.authRepository.currentUser == null) { openLogin(); return }
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.profileSave.setOnClickListener {
            model.save(binding.profileName.text.toString(), binding.profilePhone.text.toString(),
                binding.profileZalo.text.toString())
        }
        binding.profileSendVerification.setOnClickListener { model.sendVerification() }
        binding.profileCheckVerification.setOnClickListener { model.checkVerification() }
        binding.profileLogout.setOnClickListener { CampusShareData.authRepository.logout(); openLogin() }
        binding.profileBack.setOnClickListener { finish() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    if (state.user != null && state.version != lastRenderedVersion) {
                        lastRenderedVersion = state.version
                        binding.profileName.setText(state.user.fullName)
                        binding.profilePhone.setText(state.user.phone.orEmpty())
                        binding.profileZalo.setText(state.user.zaloUrl.orEmpty())
                        binding.profileEmail.text = state.user.email
                        binding.profileId.text = getString(R.string.user_id, state.user.id)
                    }
                    binding.profileProgress.visibility = if (state.loading) View.VISIBLE else View.GONE
                    listOf(binding.profileSave, binding.profileSendVerification,
                        binding.profileCheckVerification).forEach { it.isEnabled = !state.loading }
                    binding.profileVerifyStatus.text = if (state.emailVerified)
                        getString(R.string.email_verified) else getString(R.string.email_unverified)
                    binding.profileMessage.text = (state.error ?: state.message).orEmpty()
                    binding.profileMessage.visibility =
                        if (state.error == null && state.message == null) View.GONE else View.VISIBLE
                }
            }
        }
    }
    private fun openLogin() {
        startActivity(Intent(this, AuthActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        ))
        finish()
    }
}
