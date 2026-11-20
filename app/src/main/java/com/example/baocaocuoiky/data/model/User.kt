package com.example.baocaocuoiky.data.model

data class User(
    val userId: String = "",
    val fullName: String = "",
    val studentId: String = "",       // MSSV (chuẩn Figma)
    val className: String = "",       // Lớp / Khoa (chuẩn Figma)
    val email: String = "",
    val phone: String = "",
    val zaloPhone: String = "",
    val greenPoints: Int = 100,       // Điểm Sống Xanh
    val role: String = "STUDENT"      // "STUDENT" hoặc "ADMIN"
)