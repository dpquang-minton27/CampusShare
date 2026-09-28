# CampusShare

Ứng dụng Android Kotlin/XML cho sinh viên cho tặng hoặc trao đổi đồ dùng trong trường. Nhánh `develop` hiện có project khởi tạo BC1 với năm mục điều hướng mẫu; chưa kết nối dữ liệu hoặc triển khai chức năng nghiệp vụ.

## Mở và chạy

1. Cài Android Studio, Android SDK Platform 36 và JDK 17 (Gradle JDK trong Android Studio có thể chọn **Embedded JDK 17**). Máy cần Internet ở lần đồng bộ Gradle đầu tiên.
2. Clone repository (cần quyền truy cập), sau đó chuyển sang nhánh `develop`:
   ```bash
   git clone https://github.com/dpquang-minton27/CampusShare.git
   cd CampusShare
   git switch develop
   ```
3. Trong Android Studio chọn **File → Open**, chọn thư mục `CampusShare` chứa `settings.gradle.kts`, và chờ **Gradle Sync** hoàn tất. Nếu được hỏi SDK, chọn SDK đã cài; Android Studio tự tạo `local.properties` riêng cho máy.
4. Trong **Device Manager**, tạo một emulator API 26 trở lên (khuyến nghị API 35 hoặc 36), khởi động máy ảo, chọn cấu hình `app` rồi bấm **Run ▶**.
5. Xác nhận màn hình **CampusShare** mở và lần lượt bấm **Trang chủ**, **Tìm kiếm**, **Đăng tin**, **Yêu cầu**, **Cá nhân** để thấy nội dung mẫu thay đổi. Chụp ảnh màn hình làm bằng chứng BC1.

Nếu Gradle Sync lỗi, kiểm tra **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK = 17**, SDK Platform 36 và kết nối Internet. Có thể chạy `./gradlew :app:assembleDebug` trên macOS/Linux hoặc `gradlew.bat :app:assembleDebug` trên Windows sau khi cài SDK/JDK.

## Cấu trúc và quy ước

- `app/src/main/java/com/campusshare/app/`: mã Kotlin; `res/layout/`: giao diện XML; `res/values/`: chuỗi, màu, theme.
- `MainActivity` là khung BC1. Khi phát triển chức năng, đặt code theo `ui/auth`, `ui/posts`, `ui/detail`, `data`, `domain` dưới package `com.campusshare.app`. Mỗi phần gồm UI, ViewModel và logic liên quan; không dồn code nghiệp vụ vào `MainActivity`.
- Nhóm dự kiến luồng `UI → ViewModel → Repository → Data Source`. Model chung dự kiến: `User`, `Post`, `Category`, `Favorite`, `Request`. Thống nhất data contract và chọn Firebase/Room trước khi thêm dependency; BC1 chỉ cần Android SDK.
- Cấu hình: AGP 8.13.2, Gradle 8.13, Kotlin 2.2.20, compile SDK 36, min SDK 26, JDK 17.

## Phân công và nhánh Git

| Thành viên | Phần code | Nhánh |
| --- | --- | --- |
| Dương Phước Quang | Data, đăng nhập, hồ sơ | `feature/auth-data` |
| Nguyễn Vương Trọng | Bài đăng, tìm kiếm | `feature/post-search` |
| Nguyễn Tấn Thắng | Chi tiết, yêu thích, yêu cầu | `feature/detail-request` |

`main` giữ bản chạy ổn định; `develop` nhận pull request đã review. Từng người bắt đầu từ `develop`, làm trên nhánh của mình, commit theo phần việc và mở pull request về `develop`. Người khác review, kiểm tra Gradle Sync/chạy app trước khi merge. Khi bản tích hợp chạy ổn định, mới merge `develop` vào `main`.

Nếu nhánh chức năng chưa tồn tại, tạo từ `develop` với `git switch -c feature/auth-data` (thay tên nhánh theo bảng), rồi `git push -u origin feature/auth-data`. Trước khi bắt đầu công việc mới, cập nhật `develop` bằng `git pull`. Không commit `local.properties`, keystore, token hoặc `google-services.json`.

**BC1 hoàn tất sau khi Quang chạy app trên emulator và Trọng, Thắng clone + chạy lại được.**
