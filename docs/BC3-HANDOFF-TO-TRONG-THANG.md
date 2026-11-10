# Bàn giao API Quang -> Trọng và Thắng

## Sau khi Quang push code lên develop

```bash
git switch develop
git pull --ff-only origin develop
```

**Trọng** không thêm model Post khác. Dùng `com.campusshare.app.domain.model.PostDraft`, `Post` và `CampusShareData.postRepository`. Tại ViewModel:

```kotlin
val repository = CampusShareData.postRepository
val listFlow = repository.observeAvailablePosts()
viewModelScope.launch {
    val id = repository.createPost(PostDraft(
        categoryId = "books", title = "Sách Giải tích 1",
        description = "Giáo trình còn sử dụng tốt",
        type = PostType.GIVE_AWAY,
        meetingLocation = "Thư viện trường"
    ))
}
```

Không đặt ảnh `content://` local trực tiếp vào `imageUrls`; Firebase Post chỉ nhận HTTPS URL. Trước khi Storage được duyệt có thể để danh sách ảnh rỗng hoặc ảnh HTTPS demo được quyền sử dụng.

**Thắng** dùng `CampusShareData.requestRepository`, `favoriteRepository`, `postRepository`. Tại ViewModel, thao tác phải chạy trong `viewModelScope.launch`, xử lý exception và chờ Flow thay đổi:

```kotlin
val postFlow = CampusShareData.postRepository.observePost(postId)
val sentFlow = CampusShareData.requestRepository.observeSent(uid)
viewModelScope.launch { CampusShareData.requestRepository.sendRequest(postId, "Em xin nhận đồ") }
viewModelScope.launch { CampusShareData.requestRepository.acceptRequest(requestId) }
```

Thắng không tự gọi Firestore `update("status", "ACCEPTED")`. Quy tắc chọn winner nằm trong Repository + Rules. Đối với request PENDING, dùng `request.effectiveStatus(post)` để xử lý yêu cầu thua chưa reconcile. Chỉ hiển thị phone/Zalo nếu `contactShareRepository.getOtherPartyContact(requestId)` trả dữ liệu, sau khi người dùng bên kia chủ động chia sẻ.

## `MainActivity` và điều hướng

File `MainActivity.kt` vẫn thuộc Thắng. Quang không sửa file này; gọi `CampusShareData.authRepository.currentUser` và `observeAuthState` tương thích BC2. Các màn Post của Trọng, Detail/Requests của Thắng sẽ tự nối sau bằng navigation do Thắng tích hợp.

## Không cần biết Firebase trong ViewModel

Quang chọn implementation qua `CampusShareApplication` -> `CampusShareData.initialize()` dựa vào `BuildConfig.USE_FIREBASE`. Trọng và Thắng chỉ sử dụng interface `PostRepository`, `RequestRepository`, v.v. Hai chế độ Mock/Firebase không thay đổi tên API.

## Giới hạn cần biết

- Mock data chỉ RAM, reset khi tắt tiến trình; Firebase data lưu cloud.
- Firebase login có thể thành công nhưng chưa verified email, khi đó Rules từ chối tạo Post/Request; UI phải hiển thị hướng dẫn.
- Không có upload ảnh thật trong phần Quang này; Trọng đảm nhiệm Photo Picker UI, Quang sẽ hỗ trợ Storage integration sau khi quyết định billing.
- Không có API xóa Post vật lý; `hidePost` thực hiện soft hide để không mất Request history.
- `contactShares` là opt-in sau ACCEPT, không tự tiết lộ thông tin cá nhân.
