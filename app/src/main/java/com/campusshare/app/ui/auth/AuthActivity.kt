package com.campusshare.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.campusshare.app.BuildConfig
import com.campusshare.app.MainActivity
import com.campusshare.app.R
import com.campusshare.app.data.CampusShareData
import com.campusshare.app.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val model: AuthViewModel by viewModels {
        AuthViewModel.factory(CampusShareData.authRepository)
    }
    private var registering = false
    private var alreadyNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CampusShareData.authRepository.currentUser != null) { openHome(); return }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registering = savedInstanceState?.getBoolean("registering") ?: false
        binding.authDemo.setText(if (BuildConfig.USE_FIREBASE) R.string.auth_firebase_info else R.string.demo_accounts)
        renderMode()

        binding.authSwitch.setOnClickListener {
            registering = !registering
            model.clearMessage()
            binding.authConfirmation.text.clear()
            renderMode()
        }
        binding.authSubmit.setOnClickListener {
            if (registering && binding.authPassword.text.toString() != binding.authConfirmation.text.toString()) {
                binding.authError.setText(R.string.password_mismatch)
                binding.authError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            model.submit(
                registering,
                binding.authName.text.toString(),
                binding.authEmail.text.toString(),
                binding.authPassword.text.toString()
            )
        }
        binding.authReset.setOnClickListener { model.resetPassword(binding.authEmail.text.toString()) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    if (state.authenticated) {
                        openHome()
                        return@collect
                    }
                    binding.authProgress.visibility = if (state.busy) View.VISIBLE else View.GONE
                    listOf(
                        binding.authName, binding.authEmail, binding.authPassword,
                        binding.authConfirmation, binding.authSubmit, binding.authSwitch,
                        binding.authReset
                    ).forEach { it.isEnabled = !state.busy }
                    val message = state.error ?: state.information
                    binding.authError.text = message.orEmpty()
                    binding.authError.visibility = if (message == null) View.GONE else View.VISIBLE
                }
            }
        }
    }
    private fun renderMode() {
        binding.authTitle.setText(if (registering) R.string.register_title else R.string.login_title)
        binding.authName.visibility = if (registering) View.VISIBLE else View.GONE
        binding.authConfirmation.visibility = if (registering) View.VISIBLE else View.GONE
        binding.authSubmit.setText(if (registering) R.string.register_title else R.string.login_title)
        binding.authSwitch.setText(if (registering) R.string.switch_login else R.string.switch_register)
        binding.authReset.visibility = if (registering) View.GONE else View.VISIBLE
    }
    private fun openHome() {
        if (alreadyNavigated) return
        alreadyNavigated = true
        startActivity(Intent(this, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        ))
        finish()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("registering", registering)
        super.onSaveInstanceState(outState)
    }
}
