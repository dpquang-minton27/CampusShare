package com.campusshare.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.campusshare.app.MainActivity
import com.campusshare.app.R
import com.campusshare.app.data.CampusShareData

class AuthActivity : Activity() {
    private val repository = CampusShareData.authRepository
    private val handler = Handler(Looper.getMainLooper())
    private var registering = false
    private var busy = false
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmation: EditText
    private lateinit var submit: Button
    private lateinit var switchMode: Button
    private lateinit var error: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (repository.currentUser != null) { openHome(); return }
        setContentView(R.layout.activity_auth)
        registering = savedInstanceState?.getBoolean("registering") ?: false
        name = findViewById(R.id.authName)
        email = findViewById(R.id.authEmail)
        password = findViewById(R.id.authPassword)
        confirmation = findViewById(R.id.authConfirmation)
        submit = findViewById(R.id.authSubmit)
        switchMode = findViewById(R.id.authSwitch)
        error = findViewById(R.id.authError)
        progress = findViewById(R.id.authProgress)
        renderMode()
        switchMode.setOnClickListener {
            registering = !registering
            error.visibility = View.GONE
            confirmation.text.clear()
            renderMode()
        }
        submit.setOnClickListener { submitForm() }
    }

    private fun renderMode() {
        findViewById<TextView>(R.id.authTitle).setText(if (registering) R.string.register_title else R.string.login_title)
        name.visibility = if (registering) View.VISIBLE else View.GONE
        confirmation.visibility = if (registering) View.VISIBLE else View.GONE
        submit.setText(if (registering) R.string.register_title else R.string.login_title)
        switchMode.setText(if (registering) R.string.switch_login else R.string.switch_register)
    }

    private fun submitForm() {
        if (busy) return
        error.visibility = View.GONE
        if (registering && password.text.toString() != confirmation.text.toString()) {
            showError(getString(R.string.password_mismatch)); return
        }
        val fullName = name.text.toString()
        val mail = email.text.toString()
        val secret = password.text.toString()
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
        listOf(name, email, password, confirmation, submit, switchMode).forEach { it.isEnabled = !value }
        progress.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        error.text = message
        error.visibility = View.VISIBLE
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
