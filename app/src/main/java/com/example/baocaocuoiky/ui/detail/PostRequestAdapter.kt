package com.example.baocaocuoiky.ui.detail

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.ExchangeRequest
import com.example.baocaocuoiky.data.model.RequestStatus
import com.example.baocaocuoiky.databinding.ItemPostRequestBinding

class PostRequestAdapter(
    private var items: List<ExchangeRequest>,
    private val onReject: (ExchangeRequest) -> Unit,
    private val onAccept: (ExchangeRequest) -> Unit
) : RecyclerView.Adapter<PostRequestAdapter.Holder>() {

    inner class Holder(val binding: ItemPostRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemPostRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val req = items[position]
        val ctx = holder.itemView.context
        val sender = AppDataStore.registeredAccounts.firstOrNull { it.first.userId == req.requesterId }?.first

        with(holder.binding) {
            tvRequesterName.text = req.requesterName
            tvRequesterInfo.text = if (sender != null) {
                "MSV:${sender.studentId} - Lớp ${sender.className}"
            } else {
                "Sinh viên UTE • Đã xác thực"
            }
            tvRequestTime.text = formatTime(req.createdAt)

            val rawMsg = req.message.ifBlank { "Mình muốn đăng ký nhận món đồ này ạ!" }
            tvRequestMessage.text = "$rawMsg (${rawMsg.length}/200)"

            if (req.requestStatus == RequestStatus.PENDING) {
                layoutActionButtons.visibility = View.VISIBLE
                tvDecisionStatus.visibility = View.GONE
            } else {
                layoutActionButtons.visibility = View.GONE
                tvDecisionStatus.visibility = View.VISIBLE
                if (req.requestStatus == RequestStatus.ACCEPTED) {
                    tvDecisionStatus.text = "✓ Đã chấp nhận yêu cầu này"
                    tvDecisionStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_green))
                    tvDecisionStatus.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.status_green_bg)
                    )
                } else {
                    val reasonNote = if (req.rejectReason.isNotBlank()) "\nLý do: ${req.rejectReason}" else ""
                    tvDecisionStatus.text = "✕ Đã từ chối yêu cầu$reasonNote"
                    tvDecisionStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_red))
                    tvDecisionStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                }
            }

            btnReject.setOnClickListener { onReject(req) }
            btnAccept.setOnClickListener { onAccept(req) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ExchangeRequest>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTime(createdAt: Long): String {
        val mins = ((System.currentTimeMillis() - createdAt) / 60000L).coerceAtLeast(1L)
        return when {
            mins < 60 -> "$mins phút trước"
            mins < 1440 -> "${mins / 60} giờ trước"
            else -> "${mins / 1440} ngày trước"
        }
    }
}
