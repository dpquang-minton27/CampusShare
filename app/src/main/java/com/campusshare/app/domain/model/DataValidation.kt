package com.campusshare.app.domain.model

import java.util.Locale

object DataValidation {
    fun email(raw: String): String {
        val value = raw.trim().lowercase(Locale.ROOT)
        require(value.length <= 254 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value)) {
            "Email không hợp lệ."
        }
        return value
    }
    fun name(raw: String): String = raw.trim().also {
        require(it.length in 2..80) { "Họ tên phải dài từ 2 đến 80 ký tự." }
    }
    fun password(raw: String) {
        require(raw.length >= 8) { "Mật khẩu phải có ít nhất 8 ký tự." }
    }
    fun profile(input: ProfileEdit): ProfileEdit = input.copy(
        fullName = name(input.fullName),
        phone = input.phone?.trim()?.takeIf { it.isNotEmpty() }?.also {
            require(it.length in 8..20 && it.all { c -> c.isDigit() || c in "+- ()" }) {
                "Số điện thoại không hợp lệ."
            }
        },
        zaloUrl = input.zaloUrl?.trim()?.takeIf { it.isNotEmpty() }?.also {
            require(it.length <= 200 && (it.startsWith("https://") || it.startsWith("http://"))) {
                "Địa chỉ Zalo phải là liên kết HTTP(S)."
            }
        }
    )
    fun post(draft: PostDraft): PostDraft = draft.copy(
        categoryId = draft.categoryId.trim(),
        title = draft.title.trim(),
        description = draft.description.trim(),
        meetingLocation = draft.meetingLocation.trim(),
        exchangeNote = draft.exchangeNote?.trim()?.takeIf { it.isNotBlank() }
    ).also {
        require(it.categoryId.isNotBlank()) { "Hãy chọn danh mục." }
        require(it.title.length in 3..100) { "Tiêu đề phải dài từ 3 đến 100 ký tự." }
        require(it.description.length in 5..2000) { "Mô tả phải dài từ 5 đến 2000 ký tự." }
        require(it.meetingLocation.length in 2..200) { "Địa điểm phải dài từ 2 đến 200 ký tự." }
        require(it.conditionPercent in 0..100) { "Độ mới phải thuộc 0–100%." }
        require(it.imageUrls.size <= 3 && it.imageUrls.all { url -> url.startsWith("https://") }) {
            "Tối đa 3 ảnh HTTPS; ảnh cục bộ phải được upload trước khi lưu Post."
        }
        require((it.exchangeNote?.length ?: 0) <= 300)
        if (it.type == PostType.EXCHANGE) require(!it.exchangeNote.isNullOrBlank()) {
            "Hãy mô tả đồ dùng mong muốn trao đổi."
        }
    }
    fun requestMessage(raw: String) = raw.trim().also {
        require(it.length in 1..200) { "Lời nhắn phải dài từ 1 đến 200 ký tự." }
    }
    fun requestId(postId: String, requesterId: String) = "${postId}__${requesterId}"
    fun favoriteId(userId: String, postId: String) = "${userId}__${postId}"
}
