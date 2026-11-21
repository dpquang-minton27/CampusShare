package com.example.baocaocuoiky.ui.detail

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.model.ExchangeRequest
import com.example.baocaocuoiky.data.model.RequestStatus
import com.example.baocaocuoiky.databinding.ItemRequestBinding

class RequestAdapter(
    private var items: List<ExchangeRequest>,
    private var isReceivedTab: Boolean,
    private val onOpenPost: (ExchangeRequest) -> Unit,
    private val onAccept: (ExchangeRequest) -> Unit,
    private val onReject: (ExchangeRequest) -> Unit,
    private val onCancel: (ExchangeRequest) -> Unit,
    private val onContact: (ExchangeRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.Holder>() {

    inner class Holder(val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val req = items[position]
        val ctx = holder.itemView.context

        with(holder.binding) {
            tvPostTitle.text = req.postTitle
            tvRequestMessage.text = "“${req.message}”"
            tvCounterpartInfo.text = if (isReceivedTab) {
                "Người xin nhận: ${req.requesterName} (${req.requesterStudentId})"
            } else {
                "Người đăng: ${req.ownerName}"
            }

            Glide.with(ctx)
                .load(req.postImageUrl.ifBlank { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgPostThumb)

            // Cập nhật thanh tiến trình 3 bước chuẩn Figma
            val green = ContextCompat.getColor(ctx, R.color.status_green)
            val greenBg = ContextCompat.getColor(ctx, R.color.status_green_bg)
            val red = ContextCompat.getColor(ctx, R.color.status_red)
            val gray = ContextCompat.getColor(ctx, R.color.text_secondary)
            val grayBg = ContextCompat.getColor(ctx, R.color.bg_screen)

            when (req.requestStatus) {
                RequestStatus.PENDING -> {
                    tvStep3.text = "3. Chờ kết quả"
                    tvStep3.setTextColor(gray)
                    tvStep3.backgroundTintList = ColorStateList.valueOf(grayBg)
                    lineStep23.setBackgroundColor(ContextCompat.getColor(ctx, R.color.border_gray))
                }
                RequestStatus.ACCEPTED -> {
                    tvStep2.setTextColor(green)
                    tvStep2.backgroundTintList = ColorStateList.valueOf(greenBg)
                    lineStep23.setBackgroundColor(green)
                    tvStep3.text = "3. Được nhận"
                    tvStep3.setTextColor(green)
                    tvStep3.backgroundTintList = ColorStateList.valueOf(greenBg)
                }
                RequestStatus.REJECTED -> {
                    lineStep23.setBackgroundColor(red)
                    tvStep3.text = "3. Từ chối"
                    tvStep3.setTextColor(red)
                    tvStep3.backgroundTintList = ColorStateList.valueOf(grayBg)
                }
                RequestStatus.CANCELLED -> {
                    lineStep23.setBackgroundColor(gray)
                    tvStep3.text = "3. Đã hủy"
                    tvStep3.setTextColor(gray)
                    tvStep3.backgroundTintList = ColorStateList.valueOf(grayBg)
                }
            }

            layoutOwnerActions.visibility = if (isReceivedTab && req.requestStatus == RequestStatus.PENDING) {
                View.VISIBLE
            } else {
                View.GONE
            }

            btnCancelRequest.visibility = if (!isReceivedTab && req.requestStatus == RequestStatus.PENDING) {
                View.VISIBLE
            } else {
                View.GONE
            }

            btnContactPartner.visibility = if (req.requestStatus == RequestStatus.ACCEPTED) {
                View.VISIBLE
            } else {
                View.GONE
            }

            root.setOnClickListener { onOpenPost(req) }
            btnAccept.setOnClickListener { onAccept(req) }
            btnReject.setOnClickListener { onReject(req) }
            btnCancelRequest.setOnClickListener { onCancel(req) }
            btnContactPartner.setOnClickListener { onContact(req) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ExchangeRequest>, receivedMode: Boolean) {
        items = newItems
        isReceivedTab = receivedMode
        notifyDataSetChanged()
    }
}