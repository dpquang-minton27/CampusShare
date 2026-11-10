# BC3 testing & acceptance checklist

## Domain/Fake (đã chạy trong môi trường tạo overlay)

1. `python3 tools/validate_overlay.py` — XML/JSON/package/static checks.
2. `python3 seed/validate_seed.py` — dữ liệu mẫu & winner consistency.
3. `bash tools/check_domain_offline.sh` — compiler Kotlin + Fake Auth/Post/Request smoke.

## Android (CHƯA chạy ở môi trường tạo overlay)

- `gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` (Windows) hoặc `./gradlew ...`.
- Run Android Emulator API >= 26, kiểm tra: Login/Register/Profile, xoay màn, thoát/relaunch, logout Back, email verification banner.
- Firebase mode: tải cấu hình, Sync, build, xác thực 2 email và chạy cross-device.
- Confirm User public/private data separation using Firebase Rules.

## Emulator Rules tests (CHƯA chạy ở môi trường tạo overlay)

`rules-tests/rules.test.mjs` kiểm tra owner-only Post edits, Post HIDDEN, userContacts privacy, email verified restriction, valid request creation, atomic ACCEPT. Cần thêm trường hợp stress/race và thử contactShares trong quá trình nghiệm thu.

## Transaction/Request test matrix

| ID | Tình huống | Kỳ vọng |
|---|---|---|
| R01 | Gửi Request vào AVAILABLE | PENDING |
| R02 | Xin Post của mình | Bị từ chối |
| R03 | Gửi cùng (post,uid) 2 lần | Không tạo trùng |
| R04 | Hủy PENDING | CANCELLED |
| R05 | Người khác reject | Permission denied |
| R06 | Owner reject | REJECTED, Post vẫn AVAILABLE |
| R07 | Owner accept | Post TAKEN/EXCHANGED, Request ACCEPTED |
| R08 | Hai request cùng được accept | Đúng 1 winner |
| R09 | Có PENDING thua sau acceptance | Effective REJECTED, reconciliation bền vững |
| R10 | Mạng offline trong transaction | Không tạo 2 winner; UI báo lỗi |
| R11 | Request vào TAKEN | Bị từ chối |
| R12 | Đọc contactShares khi chưa ACCEPTED | Permission denied |

## Đầu ra nghiệm thu

- Ảnh hoặc video hai thiết bị đồng bộ bài đăng, yêu cầu và trạng thái.
- Lệnh chạy, phiên bản SDK, commit trên develop, các vấn đề mở.
- Chỉ đánh dấu Done khi Trọng hoặc Thắng xác nhận có thể `git pull origin develop` và gọi Repository theo tài liệu.
