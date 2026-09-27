# CampusShare

Ứng dụng Android viết bằng Kotlin để sinh viên đăng tin cho tặng hoặc trao đổi đồ dùng trong trường.

## Các phần mã nguồn

- **Data & Authentication — Dương Phước Quang:** model, repository, đăng ký/đăng nhập, hồ sơ và quyền truy cập.
- **Post & Search — Nguyễn Vương Trọng:** danh sách, tạo/sửa/xóa bài đăng, bài của tôi, tìm kiếm và lọc.
- **Detail & Interaction — Nguyễn Tấn Thắng:** chi tiết bài đăng, yêu thích, gửi/nhận yêu cầu, chấp nhận/từ chối.

Mỗi phần gồm giao diện, ViewModel và logic liên quan. Các model chung dự kiến là `User`, `Post`, `Category`, `Favorite`, `Request`. Nhóm sẽ thống nhất interface và kiểu trạng thái trước khi viết các phần phụ thuộc. Kiến trúc dự kiến: `UI → ViewModel → Repository → Data Source`. Công nghệ lưu trữ (Room hoặc Firebase) sẽ được chốt trước khi triển khai data layer thật.

## Cách nhóm làm việc với Git

- `main`: mã nguồn đã kiểm tra và chạy ổn định.
- `develop`: mã nguồn tích hợp từ các thành viên.
- `feature/auth-data`: phần của Quang.
- `feature/post-search`: phần của Trọng.
- `feature/detail-request`: phần của Thắng.

Quang đưa project Android Studio khởi tạo chạy được lên `develop` trước. Sau đó mỗi người tạo nhánh chức năng từ `develop`, commit phần việc của mình và mở pull request về `develop`. Một thành viên khác kiểm tra trước khi merge. Khi bản tích hợp chạy ổn định trên emulator, nhóm mới đưa vào `main`. Không commit `local.properties`, khóa ký ứng dụng hoặc thông tin bí mật.

## Mở và chạy ứng dụng

**Chưa có mã nguồn Android trong repository.** Hiện repository chỉ có README và `.gitignore` cho Android. Sau khi project được đẩy lên, nhóm sẽ bổ sung phiên bản Android Studio, cách cấu hình dữ liệu và các bước chạy trên emulator.
