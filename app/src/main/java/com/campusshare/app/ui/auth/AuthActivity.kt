package com.campusshare.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.campusshare.app.MainActivity
import com.campusshare.app.R
import com.campusshare.app.databinding.ActivityAuthBinding
import com.campusshare.app.data.CampusShareData

class AuthActivity : Activity() {
    private val repository = CampusShareData.authRepository
    private val handler = Handler(Looper.getMainLooper())
    private var registering = false
    private var busy = false
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (repository.currentUser != null) { openHome(); return }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registering = savedInstanceState?.getBoolean("registering") ?: false
        renderMode()
        binding.authSwitch.setOnClickListener {
            registering = !registering
            binding.authError.visibility = View.GONE
            binding.authConfirmation.text.clear()
            renderMode()
        }
        binding.authSubmit.setOnClickListener { submitForm() }
    }

    private fun renderMode() {
        binding.authTitle.setText(if (registering) R.string.register_title else R.string.login_title)
        binding.authName.visibility = if (registering) View.VISIBLE else View.GONE
        binding.authConfirmation.visibility = if (registering) View.VISIBLE else View.GONE
        binding.authSubmit.setText(if (registering) R.string.register_title else R.string.login_title)
        binding.authSwitch.setText(if (registering) R.string.switch_login else R.string.switch_register)
    }

    private fun submitForm() {
        if (busy) return
        binding.authError.visibility = View.GONE
        if (registering && binding.authPassword.text.toString() != binding.authConfirmation.text.toString()) {
            showError(getString(R.string.password_mismatch)); return
        }
        val fullName = binding.authName.text.toString()
        val mail = binding.authEmail.text.toString()
        val secret = binding.authPassword.text.toString()
        val register = registering
        setBusy(true)
        // Queue one UI-thread operation so the loading state is rendered. Fake calls are local/fast.
        // A remote repository must be called asynchronously in the BC3 ViewModel.
        handler.post {
            val result = if (register) repository.register(fullName, mail, secret) else repository.login(mail, secret)
            setBusy(false)
            result.fold(onSuccess = { openHome() }, onFailure = {
                showError(it.message ?: getString(R.string.auth_failed))
            })
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        listOf(binding.authName, binding.authEmail, binding.authPassword, binding.authConfirmation, binding.authSubmit, binding.authSwitch).forEach { it.isEnabled = !value }
        binding.authProgress.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.authError.text = message
        binding.authError.visibility = View.VISIBLE
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("registering", registering)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

