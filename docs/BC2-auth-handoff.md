# BC2 — Login, Register, Profile và session

## Phạm vi bàn giao

- Kotlin/XML, không thêm Firebase/Room khi nhóm chưa có quyết định trong mã nguồn.
- Đăng nhập, đăng ký (tự đăng nhập sau khi tạo tài khoản), xem email/mã người dùng, đổi tên, đăng xuất.
- Kiểm tra email, email trùng không phân biệt hoa thường, mật khẩu, xác nhận mật khẩu và tên trống.
- Form có trạng thái xử lý/lỗi; hồ sơ có phản hồi lưu; thiếu session đưa về Login.
- Home giữ khung của Trọng, hiển thị tên người dùng; mục Cá nhân mở Profile.
- Xóa back stack khi đăng nhập/đăng xuất để không Back vào màn cần đăng nhập sau logout.

Đây là **mock trong bộ nhớ phục vụ BC2**. Tài khoản mới, tên đã sửa và session mất khi tiến trình bị đóng; xoay màn hình không tạo lại repository. Không dùng thông tin thật. Không có xác thực máy chủ, khôi phục mật khẩu hay đồng bộ nhiều thiết bị. SHA-256 có salt chỉ tránh giữ mật khẩu rõ trong bộ nhớ mock, không phải giải pháp lưu mật khẩu cho bản thật.

## Tài khoản mẫu

| Email | Mật khẩu | ID |
| --- | --- | --- |
| an@sv.example.edu | Campus123 | u1 |
| binh@sv.example.edu | Campus123 | u2 |
| chi@sv.example.edu | Campus123 | u3 |

## Contract cho Trọng và Thắng

Chỉ lấy instance chung; không `FakeAuthRepository()` ở từng màn hình:

```kotlin
val repository = CampusShareData.authRepository
val user = repository.currentUser // User?; null nghĩa là chưa đăng nhập
val ownerId = user?.id

val unsubscribe = repository.observeAuthState { state ->
    when (state) {
        AuthState.SignedOut -> { /* đưa về Login */ }
        is AuthState.SignedIn -> { /* dùng state.user */ }
    }
}
// Gọi unsubscribe() khi màn hình không còn theo dõi (ví dụ onPause).
```

Model duy nhất: `domain.model.User(id, fullName, email)`. Các thao tác có kết quả `Result<User>`: `login(email,password)`, `register(fullName,email,password)`, `updateProfile(fullName)`; `logout()` xóa session. `observeAuthState` trả ngay trạng thái và báo lại sau login/register/update/logout. Trong bản mock, gọi các hàm và quản lý observer trên UI thread; Activity xóa callback đang chờ khi bị hủy. Khi thay bằng mạng ở BC3, chuyển việc gọi sang ViewModel bất đồng bộ và bổ sung cơ chế hủy yêu cầu.

Repo gốc chưa có `Models.kt`, `Repositories.kt` hoặc `CampusShareData.kt` từ thẻ fake-post. Khi làm tiếp thẻ đó, **mở rộng những file hiện có**, không dán thêm một `User`, `AuthRepository` hay `CampusShareData` trùng tên. Không thêm `loginAs()` để thay thế luồng kiểm tra mật khẩu trên UI.

## Kiểm tra trên máy Quang và máy một thành viên khác

1. Lấy code `develop`, Sync, chạy `app`. Khi chưa có session phải hiện Login.
2. Nhập email trống/sai; nhập mật khẩu sai: hiện lỗi và không vào Home.
3. Đăng nhập tài khoản An: Home chào đúng tên, bốn mục cũ vẫn chuyển nội dung.
4. Mở Cá nhân: đúng tên/email/ID. Lưu tên trống phải báo lỗi; lưu tên hợp lệ rồi về Home phải chào tên mới.
5. Xoay màn hình tại Login/Register/Profile: giữ chế độ và nội dung form, không crash.
6. Logout: hiện Login, bấm Back không được vào Home bằng session cũ.
7. Đăng ký email mới; thử mật khẩu ngắn, xác nhận khác nhau, email trùng (kể cả chữ hoa); chỉ dữ liệu hợp lệ được chấp nhận.
8. Logout rồi login bằng tài khoản vừa tạo. Tiếp theo login tài khoản Bình để kiểm tra tên/ID độc lập.
9. Đóng hẳn tiến trình rồi mở lại: cần login; tài khoản mới không còn theo giới hạn mock. Tài khoản mẫu vẫn dùng được.
10. Trọng hoặc Thắng pull `develop`, lặp lại luồng Login → Home → Profile → Logout và ghi kết quả/ảnh lên thẻ.

Chuyển **Testing** sau khi build và bàn giao code. Chỉ **Done** sau khi một thành viên khác xác nhận chạy trên `develop`; unit test không thay thế kiểm thử emulator.

## Lệnh kiểm tra

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

APK debug sau build: `app/build/outputs/apk/debug/app-debug.apk`.
