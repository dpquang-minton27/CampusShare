# CampusShare

Ứng dụng Android Kotlin/XML cho sinh viên cho tặng hoặc trao đổi đồ dùng trong trường. Bản BC2 bổ sung đăng nhập, đăng ký, hồ sơ và session mock vào khung điều hướng BC1. Chưa kết nối backend thật; các màn bài đăng/tương tác vẫn là khung mẫu.

Tài khoản demo: `an@sv.example.edu`, `binh@sv.example.edu`, `chi@sv.example.edu`; mật khẩu chung `Campus123`. Dữ liệu và session chỉ giữ trong bộ nhớ tiến trình. Xem [hướng dẫn bàn giao và kiểm thử BC2](docs/BC2-auth-handoff.md).

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
5. Đăng nhập bằng tài khoản demo hoặc đăng ký một tài khoản thử. Kiểm tra **Trang chủ**, **Tìm kiếm**, **Đăng tin**, **Yêu cầu**, và **Cá nhân**. Mục Cá nhân mở hồ sơ để sửa tên/đăng xuất. Chụp ảnh làm bằng chứng kiểm thử.

Nếu Gradle Sync lỗi, kiểm tra **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK = 17**, SDK Platform 36 và kết nối Internet. Có thể chạy `./gradlew :app:assembleDebug` trên macOS/Linux hoặc `gradlew.bat :app:assembleDebug` trên Windows sau khi cài SDK/JDK.

## Cấu trúc và quy ước

- `app/src/main/java/com/campusshare/app/`: mã Kotlin; `res/layout/`: giao diện XML; `res/values/`: chuỗi, màu, theme.
- `MainActivity` giữ khung BC1 và kiểm tra session. `ui/auth` có các màn đăng nhập/đăng ký/hồ sơ; `data` có contract và mock auth; `domain/model/User.kt` là model chung. Khi phát triển tiếp đặt các module ở `ui/posts`, `ui/detail`; không dồn nghiệp vụ vào `MainActivity`.
- Nhóm dự kiến luồng `UI → ViewModel → Repository → Data Source`. Model chung dự kiến: `User`, `Post`, `Category`, `Favorite`, `Request`. Thống nhất data contract và chọn Firebase/Room trước khi thêm dependency; BC1 chỉ cần Android SDK.
- Cấu hình: AGP 8.13.2, Gradle 8.13, Kotlin 2.2.20, compile SDK 36, min SDK 26, JDK 17.

## Phân công và quy tắc Git

Repository chỉ dùng hai nhánh:

- `main`: bản nhóm đã kiểm tra và có thể trình bày.
- `develop`: nơi ba thành viên cùng đưa code và tích hợp trong quá trình làm.

| Thành viên | Phần code |
| --- | --- |
| Dương Phước Quang | Data, đăng nhập, hồ sơ |
| Nguyễn Vương Trọng | Bài đăng, tìm kiếm |
| Nguyễn Tấn Thắng | Chi tiết, yêu thích, yêu cầu |

Mỗi thành viên clone repository và chuyển sang `develop` bằng `git switch develop`. Trước khi sửa code, dùng `git pull origin develop` để lấy thay đổi mới nhất. Trao đổi trong nhóm để tránh hai người sửa cùng một file; nếu cần sửa file chung thì thống nhất người phụ trách trước. Làm xong phần việc, kiểm tra app, `git add` đúng các file đã sửa, `git commit -m "Mô tả thay đổi"` và `git push origin develop`. Nếu bị từ chối push do nhánh đã thay đổi, `git pull --rebase origin develop`, xử lý xung đột rồi push lại; không dùng force push.

Sau khi nhóm kiểm tra bản tích hợp trên emulator, đưa `develop` vào `main` qua pull request để mọi người xem lại. Không commit `local.properties`, keystore, token hoặc `google-services.json`.

**BC1 hoàn tất sau khi Quang chạy app trên emulator và Trọng, Thắng clone + chạy lại được.**
