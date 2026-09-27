# CampusShare

**CampusShare** là ứng dụng Android cho sinh viên trong cùng trường đăng tin cho tặng hoặc trao đổi đồ dùng còn sử dụng được. Đây là đồ án môn **Lập trình trên điện thoại di động** của nhóm 3 sinh viên.

## Phạm vi MVP

- Đăng ký, đăng nhập, đăng xuất và hồ sơ cơ bản.
- Xem danh sách, chi tiết và danh mục đồ dùng.
- Tạo, sửa, xóa, quản lý bài đăng cá nhân và hình ảnh ở mức cơ bản.
- Tìm kiếm theo từ khóa; lọc theo danh mục, loại tin và trạng thái.
- Lưu hoặc bỏ yêu thích.
- Gửi yêu cầu nhận đồ kèm lời nhắn; xem yêu cầu đã gửi và nhận được.
- Chủ bài đăng chấp nhận hoặc từ chối yêu cầu; trạng thái bài đăng được cập nhật nhất quán.

Luồng demo chính: **A đăng tin → B tìm và mở chi tiết → B gửi yêu cầu → A xử lý → bài đăng đổi trạng thái**. Không cho tự yêu cầu tin của mình, gửi yêu cầu trùng đang chờ hoặc gửi yêu cầu cho tin không còn khả dụng.

Phiên bản đầu chưa triển khai thanh toán, giao hàng, bản đồ/GPS, chat thời gian thực, AI hay hệ thống đánh giá phức tạp.

## Công nghệ và kiến trúc dự kiến

- Android Studio, Kotlin và giao diện XML.
- Luồng kiến trúc: **UI → ViewModel → Repository → Data Source → Database/API**.
- Các model dùng chung: User, Post, Category, Favorite, Request.
- Cả nhóm cần chốt tại BC1: dùng **Room/SQLite hoặc Firebase**, tên trường chủ bài đăng (chỉ chọn một trong `ownerId` và `userId`), enum trạng thái, repository interface, route `postId` và dữ liệu mẫu.
- Figma cho wireframe/prototype; Trello cho phân công và tiến độ.

Không tự ý đổi model hoặc interface dùng chung sau khi nhóm đã chốt mà chưa thông báo cho hai thành viên còn lại.

## Phân công nhóm

| Thành viên | Module chính | Bàn giao |
| --- | --- | --- |
| Dương Phước Quang (TV1) | Data & Authentication | Model/schema, repository, dữ liệu mẫu, tài khoản, phiên đăng nhập, quyền truy cập và tính nhất quán dữ liệu |
| Nguyễn Vương Trọng (TV2) | Post & Search | Home, Create/Edit/Delete Post, My Posts, danh mục, tìm kiếm/lọc và ảnh |
| Nguyễn Tấn Thắng (TV3) | Detail & Interaction | Post Detail, Favorite, Send Request, Sent/Received Requests, Accept/Reject và trạng thái tương tác |

Cả nhóm cùng thống nhất MVP/kiến trúc/UI, tích hợp nhánh, review, kiểm thử, sửa lỗi, slide và demo. Mỗi người làm cả UI, ViewModel và logic trong module của mình.

## Mốc báo cáo

| Mốc | Ngày báo cáo | Đầu ra |
| --- | --- | --- |
| BC1 | 29/09/2026 | Chốt đề tài, MVP, model/schema, phân công, wireframe, kế hoạch Git/Trello và slide |
| BC2 | 13/10/2026 | Prototype chạy trên emulator: Login → Home → Detail, Create Post, Favorite/Request bằng dữ liệu mẫu; danh sách màn hình, slide và Trello |
| BC3 | 10/11/2026 | Giao diện MVP, navigation ổn định; ưu tiên CRUD, Search, Favorite và Request với dữ liệu thật, demo hai tài khoản |
| Cuối kỳ | Chưa công bố | App hoàn chỉnh, luồng A đăng → B yêu cầu → A xử lý, kiểm thử, APK, README, slide và demo |

Mốc tích hợp nội bộ: **I1 khoảng 20/10** (Login → Home → Detail), **I2 khoảng 30/10** (Create → My Posts → Home → Detail, Search), **I3 khoảng 06/11** (Favorite/Request và luồng hai tài khoản). Hạn chi tiết nằm trên [Trello của nhóm](https://trello.com/b/P7UgBWl8/campusshare-d%E1%BB%B1-%C3%A1n-nh%C3%B3m-10).

## Quy trình Git dự kiến

- `main`: bản ổn định để báo cáo/nộp.
- `develop`: nhánh tích hợp chung.
- Nhánh chức năng theo module, ví dụ `feature/auth-data`, `feature/post-search`, `feature/detail-request`.
- Mỗi thành viên làm trên nhánh chức năng, tạo pull request và được ít nhất một người khác kiểm tra trước khi ghép vào `develop`. Chỉ đưa bản đã thử trên emulator vào `main`.

## Trạng thái repository

Repository mới có README và `.gitignore` cho Android. **Chưa có mã nguồn ứng dụng Android.** Các chức năng và mốc trên là kế hoạch, không phải tính năng đã hoàn thành. Khi project Android Studio được đẩy lên, nhóm sẽ bổ sung hướng dẫn mở/chạy app và kết quả kiểm thử.
