package com.example.baocaocuoiky.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.model.User
import com.example.baocaocuoiky.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private var items: List<User>,
    private val onToggleLock: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.Holder>() {

    inner class Holder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = items[position]
        val ctx = holder.itemView.context
        val blue = ContextCompat.getColor(ctx, R.color.primary_blue)
        val red = ContextCompat.getColor(ctx, R.color.status_red)
        val green = Color.parseColor("#16A34A")

        with(holder.binding) {
            tvUserInitial.text = user.fullName.firstOrNull()?.uppercase() ?: "S"
            tvUserName.text = user.fullName
            tvUserDetail.text = "MSV: ${user.studentId} • Lớp ${user.className}"
            tvGreenPoints.text = "🌱 ${user.greenPoints} Điểm"

            if (user.isLocked) {
                tvUserInitial.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
                tvUserInitial.setTextColor(Color.parseColor("#6B7280"))
                tvAccountStatus.text = "● Đã khóa tài khoản"
                tvAccountStatus.setTextColor(red)

                btnToggleLock.text = "Mở khóa"
                btnToggleLock.setTextColor(Color.WHITE)
                btnToggleLock.backgroundTintList = ColorStateList.valueOf(blue)
                btnToggleLock.strokeColor = ColorStateList.valueOf(blue)
            } else {
                tvUserInitial.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0F2FE"))
                tvUserInitial.setTextColor(blue)
                tvAccountStatus.text = "● Đang hoạt động"
                tvAccountStatus.setTextColor(green)

                btnToggleLock.text = "Khóa TK"
                btnToggleLock.setTextColor(red)
                btnToggleLock.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                btnToggleLock.strokeColor = ColorStateList.valueOf(red)
            }

            btnToggleLock.setOnClickListener { onToggleLock(user) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<User>) {
        items = newItems
        notifyDataSetChanged()
    }
}
