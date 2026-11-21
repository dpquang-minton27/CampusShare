package com.example.baocaocuoiky.ui.detail

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.ExchangeRequest
import com.example.baocaocuoiky.databinding.ActivityRequestsBinding
import com.google.android.material.tabs.TabLayout

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding
    private lateinit var adapter: RequestAdapter
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = SessionManager.getCurrentUser(this)?.userId ?: run {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        adapter = RequestAdapter(
            items = emptyList(),
            isReceivedTab = false,
            onOpenPost = { req ->
                startActivity(Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("EXTRA_POST_ID", req.postId)
                })
            },
            onAccept = { req -> confirmAccept(req, uid) },
            onReject = { req ->
                AppDataStore.rejectRequest(req.requestId)
                Toast.makeText(this, "Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show()
                loadRequests(uid)
            },
            onCancel = { req ->
                AppDataStore.cancelRequest(req.requestId)
                Toast.makeText(this, "Đã hủy yêu cầu", Toast.LENGTH_SHORT).show()
                loadRequests(uid)
            },
            onContact = { req -> ContactBottomSheet.show(this, req, uid) }
        )

        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter

        currentTab = intent.getIntExtra("EXTRA_INITIAL_TAB", 0)
        binding.tabLayoutRequests.getTabAt(currentTab)?.select()

        binding.tabLayoutRequests.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                loadRequests(uid)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        loadRequests(uid)
    }

    private fun loadRequests(uid: String) {
        val isReceived = currentTab == 1
        val filtered = if (isReceived) {
            AppDataStore.requests.filter { it.ownerId == uid }
        } else {
            AppDataStore.requests.filter { it.requesterId == uid }
        }
        adapter.updateData(filtered, isReceived)
        binding.tvEmptyRequests.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmAccept(req: ExchangeRequest, uid: String) {
        AlertDialog.Builder(this)
            .setTitle("Chấp nhận người nhận này?")
            .setMessage("Hệ thống sẽ chốt giao dịch cho ${req.requesterName}, tự động từ chối các yêu cầu còn lại và cộng cho bạn +20 Điểm Sống Xanh.")
            .setNegativeButton("Quay lại", null)
            .setPositiveButton("Đồng ý") { _, _ ->
                if (AppDataStore.acceptRequest(req.requestId)) {
                    // Đồng bộ lại điểm Sống Xanh mới vào SessionManager
                    val updatedMe = AppDataStore.registeredAccounts.firstOrNull { it.first.userId == uid }?.first
                    if (updatedMe != null) {
                        SessionManager.saveUser(this, updatedMe)
                    }
                    Toast.makeText(this, "Đã chốt giao dịch! +20 Điểm Sống Xanh", Toast.LENGTH_LONG).show()
                    loadRequests(uid)
                }
            }
            .show()
    }
}
