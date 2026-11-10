# CampusShare BC3 — Bộ code của Dương Phước Quang

## Mục tiêu

Phần BC3 của Dương Phước Quang gồm Data Contract v2, Auth/Profile, repository Mock và Firebase, phân quyền và transaction duyệt yêu cầu. Các màn hình Auth/Profile dùng ViewBinding. Nhóm tiếp tục tích hợp giao diện theo phân công trên `develop`.

## Chạy và kiểm thử

1. Mở thư mục gốc của repository bằng Android Studio, cài SDK 36, Build Tools 36.0.0 và chọn JDK 17.
2. Chạy cấu hình `app` ở chế độ Mock. Các tài khoản mẫu trong README dùng để kiểm tra đăng nhập và hồ sơ.
3. Chạy `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Trên Windows, dùng `gradlew.bat`.
4. Xem [cấu hình Firebase](docs/BC3-FIREBASE-SETUP.md) và [hướng dẫn chạy Rules tests](rules-tests/README.md) trước khi thử trên Firebase. Thêm `google-services.json` riêng trên máy phát triển.

## Phần mã được thay thế

- `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`: bổ sung Firebase, Lifecycle, Flow, tùy chọn Mock/Firebase.
- `domain/model/User.kt`, `PostModels.kt`: mở rộng Data Contract.
- `data/AuthRepository.kt`, `PostRepository.kt`, `FakeAuthRepository.kt`, `CampusShareData.kt`: thay thế API BC2 trong khi giữ tương thích với MainActivity.
- `ui/auth/` và hai layout `activity_auth.xml`, `activity_profile.xml`: sử dụng ViewModel và View Binding.
- `res/values/auth_strings.xml`: chuỗi màn hình Auth/Profile.
- `app/src/test/.../FakeAuthRepositoryTest.kt`: cập nhật kiểm thử BC2 theo API mới.

## Phần hoàn toàn mới

- `data/CategoryRepository.kt`, `UserRepository.kt`, `RequestRepository.kt`, `FavoriteRepository.kt`, `ContactShareRepository.kt`.
- `data/fake/`: shared fake store và dữ liệu mẫu để Trọng/Thắng không phải chờ Firebase.
- `data/firebase/`: Auth/Firestore repositories, mappers và snapshot listeners.
- `ui/auth/AuthViewModel.kt`, `ProfileViewModel.kt`.
- `CampusShareApplication.kt` làm điểm khởi tạo dependencies duy nhất.
- `firebase/` (Rules và indexes), `rules-tests/`, `seed/`, `tools/`, `docs/BC3-*`.

## Không đụng vào phạm vi của thành viên khác

- **Không thay `MainActivity.kt`**: Thắng sẽ sở hữu navigation cuối cùng; file BC2 tiếp tục dùng `CampusShareData.authRepository.currentUser`.
- **Không tạo/sửa `ui/posts/`, `ui/search/`**: Trọng.
- **Không tạo/sửa `ui/detail/`, `ui/requests/`, `ui/favorites/`**: Thắng.
- **Không sửa `activity_main.xml`, tài nguyên điều hướng, icon UI của hai thành viên.**

## Hai chế độ chạy

| Mode | Gradle | Cấu hình | Ý nghĩa |
|---|---|---|---|
| Mock | `:app:assembleDebug` | Không cần google-services.json | Phát triển UI, fake CRUD / Request |
| Firebase thật | `:app:assembleDebug -PcampusshareBackend=firebase` | Cần `app/google-services.json` | Data cross-device qua Internet |
| Firebase Emulator | `:app:assembleDebug -PcampusshareBackend=firebase -PcampusshareEmulator=true` | Cần JSON config; emulator chạy localhost máy tính | Kiểm tra Auth, Firestore và Rules qua `10.0.2.2` từ Android Emulator |

## Mức độ kiểm chứng

- Gradle chạy build Mock, unit test và lint trên dự án chỉ gồm phần Quang.
- Firestore Emulator kiểm tra quyền chủ tin và hai lượt duyệt cạnh tranh cho cả Cho tặng và Trao đổi. Một yêu cầu được chấp nhận và yêu cầu thua chuyển thành REJECTED.
- Cần cấu hình dự án Firebase của nhóm để kiểm thử xác thực và dữ liệu trên hai thiết bị.

## Lưu ý bảo mật

- Không commit mật khẩu thật, keystore, service account, token, `google-services.json` (theo quy ước của nhóm), hay dữ liệu cá nhân thật.
- `users/{uid}` chỉ gồm tên/ảnh; `userContacts/{uid}` là riêng tư. Hai bên phải chủ động chia sẻ qua `contactShares` sau ACCEPTED; đây không phải cơ chế tự lộ phone/Zalo cho mọi người.
- Một ACCEPT có hai bước: atomic winner selection (Post + selected Request), rồi retryable reconciliation các Request thua. Không mô tả bước thứ hai là atomic với bước đầu.
- Cơ chế ảnh chưa kết nối Firebase Storage. Danh sách ảnh trong Post có thể rỗng khi test; yêu cầu upload ảnh thật phải được chốt riêng về billing và Storage Rules.
