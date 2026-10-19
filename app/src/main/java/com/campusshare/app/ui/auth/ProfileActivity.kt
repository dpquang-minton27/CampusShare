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
import com.campusshare.app.R
import com.campusshare.app.data.CampusShareData

class ProfileActivity : Activity() {
    private val repository = CampusShareData.authRepository
    private val handler = Handler(Looper.getMainLooper())
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val user = repository.currentUser
        if (user == null) { openLogin(); return }
        val name = findViewById<EditText>(R.id.profileName)
        name.setText(user.fullName)
        findViewById<TextView>(R.id.profileEmail).text = user.email
        findViewById<TextView>(R.id.profileId).text = getString(R.string.user_id, user.id)
        val save = findViewById<Button>(R.id.profileSave)
        val logout = findViewById<Button>(R.id.profileLogout)
        val back = findViewById<Button>(R.id.profileBack)
        val progress = findViewById<ProgressBar>(R.id.profileProgress)
        val message = findViewById<TextView>(R.id.profileMessage)
        save.setOnClickListener {
            if (!busy) {
                busy = true
                message.visibility = View.GONE
                val fullName = name.text.toString()
                listOf(name, save, logout, back).forEach { it.isEnabled = false }
                progress.visibility = View.VISIBLE
                handler.post {
                    val result = repository.updateProfile(fullName)
                    busy = false
                    listOf(name, save, logout, back).forEach { it.isEnabled = true }
                    progress.visibility = View.GONE
                    message.text = result.fold(
                        onSuccess = { getString(R.string.profile_saved) },
                        onFailure = { it.message ?: getString(R.string.auth_failed) }
                    )
                    message.visibility = View.VISIBLE
                }
            }
        }
        logout.setOnClickListener { repository.logout(); openLogin() }
        back.setOnClickListener { finish() }
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
