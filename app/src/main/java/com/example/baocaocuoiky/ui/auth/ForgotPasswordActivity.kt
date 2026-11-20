package com.example.baocaocuoiky.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var otpBoxes: List<EditText>
    private var timer: CountDownTimer? = null

    private var targetUserId: String = ""
    private var pendingNewPassword: String = ""
    private var generatedOtp: String = ""
    private var isOtpStep = false
    private var canResend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otpBoxes = listOf(
            binding.edtOtp1, binding.edtOtp2, binding.edtOtp3,
            binding.edtOtp4, binding.edtOtp5, binding.edtOtp6
        )
        setupOtpInputs()

        binding.btnSendOtp.setOnClickListener {
            validateAndGoToOtp()
        }

        binding.tvResendOtp.setOnClickListener {
            if (canResend) {
                sendOtpCode()
            }
        }

        binding.tvBackAction.setOnClickListener {
            if (isOtpStep) {
                showStep1()
            } else {
                finish()
            }
        }
    }

    private fun validateAndGoToOtp() {
        val input = binding.edtStudentIdOrEmail.text.toString().trim()
        val newPass = binding.edtNewPassword.text.toString()
        val confirmPass = binding.edtConfirmPassword.text.toString()

        if (input.isEmpty()) {
            binding.edtStudentIdOrEmail.error = "Vui lòng nhập MSSV hoặc Email UTE"
            return
        }
        val user = AppDataStore.findUserByStudentIdOrEmail(input)
        if (user == null) {
            binding.edtStudentIdOrEmail.error = "Không tìm thấy tài khoản sinh viên UTE này"
            return
        }
        if (newPass.length < 6) {
            binding.edtNewPassword.error = "Mật khẩu mới phải từ 6 ký tự trở lên"
            return
        }
        if (newPass != confirmPass) {
            binding.edtConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        targetUserId = user.userId
        pendingNewPassword = newPass
        binding.tvMaskedEmail.text = maskEmail(user.email)

        showStep2Otp()
        sendOtpCode()
    }

    private fun showStep1() {
        isOtpStep = false
        timer?.cancel()
        binding.layoutStep1Forgot.visibility = View.VISIBLE
        binding.layoutStep2Otp.visibility = View.GONE
        binding.tvBackAction.text = "← Quay lại Đăng nhập"
    }

    private fun showStep2Otp() {
        isOtpStep = true
        binding.layoutStep1Forgot.visibility = View.GONE
        binding.layoutStep2Otp.visibility = View.VISIBLE
        binding.tvBackAction.text = "← Quay lại"
        otpBoxes.forEach { it.setText("") }
        otpBoxes[0].requestFocus()
    }

    private fun sendOtpCode() {
        generatedOtp = (100000..999999).random().toString()
        // Hiển thị mã OTP qua Toast để dễ kiểm thử trên máy ảo
        Toast.makeText(this, "Mã OTP gửi tới email UTE: $generatedOtp", Toast.LENGTH_LONG).show()
        startCountdown()
    }

    private fun startCountdown() {
        canResend = false
        timer?.cancel()
        timer = object : CountDownTimer(45_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                binding.tvResendOtp.text = String.format("Gửi lại (00:%02d)", sec)
            }

            override fun onFinish() {
                canResend = true
                binding.tvResendOtp.text = "Gửi lại mã ngay"
            }
        }.start()
    }

    private fun setupOtpInputs() {
        otpBoxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < otpBoxes.lastIndex) {
                            otpBoxes[index + 1].requestFocus()
                        } else {
                            checkOtpAndReset()
                        }
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() &&
                    index > 0
                ) {
                    otpBoxes[index - 1].requestFocus()
                    otpBoxes[index - 1].setText("")
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun checkOtpAndReset() {
        val enteredOtp = otpBoxes.joinToString("") { it.text.toString() }
        if (enteredOtp.length < 6) return

        if (enteredOtp == generatedOtp) {
            AppDataStore.updatePassword(targetUserId, pendingNewPassword)
            Toast.makeText(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Mã OTP không đúng (Mã đúng: $generatedOtp)", Toast.LENGTH_SHORT).show()
            otpBoxes.forEach { it.setText("") }
            otpBoxes[0].requestFocus()
        }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        val prefix = parts[0]
        val visible = if (prefix.length > 7) prefix.take(7) else prefix.take(3)
        return "$visible******@${parts[1]}"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
