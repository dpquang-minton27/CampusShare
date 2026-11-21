package com.example.baocaocuoiky.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.baocaocuoiky.data.model.ExchangeRequest
import com.example.baocaocuoiky.databinding.BottomSheetContactBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

object ContactBottomSheet {

    fun show(context: Context, request: ExchangeRequest, currentUserId: String) {
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetContactBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        val isOwner = request.ownerId == currentUserId
        val partnerName = if (isOwner) request.requesterName else request.ownerName
        val partnerPhone = if (isOwner) "0905888999" else request.ownerPhone.ifBlank { "0905123456" }

        binding.tvContactRole.text = if (isOwner) {
            "Người nhận được duyệt • Điểm hẹn: ${request.pickupLocation}"
        } else {
            "Chủ bài đăng đã duyệt • Điểm hẹn: ${request.pickupLocation}"
        }
        binding.tvContactName.text = "Sinh viên: $partnerName"
        binding.tvContactPhone.text = "Số điện thoại: $partnerPhone"
        binding.tvContactZalo.text = "Zalo liên hệ: $partnerPhone"

        binding.btnCopyPhone.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("SĐT CampusShare", partnerPhone))
            Toast.makeText(context, "Đã sao chép SĐT / Zalo: $partnerPhone", Toast.LENGTH_SHORT).show()
        }

        binding.btnCallPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$partnerPhone"))
            context.startActivity(intent)
        }

        dialog.show()
    }
}