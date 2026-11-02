package com.campusshare.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.campusshare.app.R
import com.campusshare.app.databinding.ActivityProfileBinding
import com.campusshare.app.data.CampusShareData

class ProfileActivity : Activity() {
    private val repository = CampusShareData.authRepository
    private val handler = Handler(Looper.getMainLooper())
    private var busy = false
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user = repository.currentUser
        if (user == null) { openLogin(); return }
        binding.profileName.setText(user.fullName)
        binding.profileEmail.text = user.email
        binding.profileId.text = getString(R.string.user_id, user.id)
        binding.profileSave.setOnClickListener {
            if (!busy) {
                busy = true
                binding.profileMessage.visibility = View.GONE
                val fullName = binding.profileName.text.toString()
                listOf(binding.profileName, binding.profileSave, binding.profileLogout, binding.profileBack).forEach { it.isEnabled = false }
                binding.profileProgress.visibility = View.VISIBLE
                handler.post {
                    val result = repository.updateProfile(fullName)
                    busy = false
                    listOf(binding.profileName, binding.profileSave, binding.profileLogout, binding.profileBack).forEach { it.isEnabled = true }
                    binding.profileProgress.visibility = View.GONE
                    binding.profileMessage.text = result.fold(
                        onSuccess = { getString(R.string.profile_saved) },
                        onFailure = { it.message ?: getString(R.string.auth_failed) }
                    )
                    binding.profileMessage.visibility = View.VISIBLE
                }
            }
        }
        binding.profileLogout.setOnClickListener { repository.logout(); openLogin() }
        binding.profileBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        if (repository.currentUser == null) openLogin()
    }

    private fun openLogin() {
        startActivity(Intent(this, AuthActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

