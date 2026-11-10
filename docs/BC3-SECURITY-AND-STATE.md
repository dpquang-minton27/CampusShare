# Bảo mật và trạng thái giao dịch — những gì đã/chưa đảm bảo

## Source of truth

Firestore `posts/{postId}.acceptedRequestId` xác định Request thắng. `Request.status == ACCEPTED` phải cùng transaction với cập nhật Post. Một Post chỉ được chuyển từ AVAILABLE sang TAKEN/EXCHANGED một lần theo cơ chế optimistic transaction retry.

## Vì sao cần hai mức xử lý

1. `FirebaseRequestRepository.acceptRequest(requestId)` đọc Post + Request, kiểm tra chủ bài, status và winner; một Firestore transaction ghi **2 document**. Security Rules dùng `getAfter` xác nhận cả hai cùng cập nhật.
2. Sau khi transaction commit, `reconcileRejected(postId)` lọc theo chủ tin, mã bài và trạng thái PENDING để tìm các Request còn lại và chuyển thành REJECTED theo các batch có thể retry. Nếu mất mạng ở bước hai, Post đã có winner duy nhất và Thắng phải dùng `effectiveStatus(request, post)` để hiện REJECTED cho các request thua.

**Không được tuyên bố** toàn bộ N Request thua bị từ chối atomically trong cùng transaction, vì chưa có backend server-side reconciliation. Nếu yêu cầu độ mạnh hơn, nâng cấp lên callable Cloud Functions và xem xét gói Blaze.

## Quy tắc quyền

- User profile công khai: không chứa phone, Zalo, email; chỉ có fullName/avatar.
- `userContacts` chỉ chính UID đọc/ghi; không có quy tắc đọc tự động từ ACCEPTED.
- `contactShares`: chính người gửi share mới được ghi; chỉ hai người trong một Request ACCEPTED đọc. Khi ACCEPT xong chưa có share sẵn, vì phải có sự đồng ý của mỗi bên.
- Post AVAILABLE được đọc bởi user đăng nhập; Post HIDDEN chỉ chủ tin đọc. Firestore `list` permission cũng bị giới hạn để không lộ bài ẩn qua query.
- Request chỉ requester hoặc post owner được đọc; update status theo hành động CANCELLED/REJECTED/ACCEPTED.
- Favorite chỉ chủ danh sách được đọc/ghi; ID cố định chống trùng.
- Category chỉ quản trị qua Console/Admin SDK, không cho Android client sửa.

## Rủi ro còn cần kiểm thử

- Quyền post/list đối với các query khác nhau: `status=AVAILABLE`, `ownerId=uid`, sai bộ lọc.
- REQUEST create và ACCEPT đồng thời.
- Hai mạng bị mất giữa lúc ghi; retry reconciliation.
- Đọc/ghi contactShares sau ACCEPT và khi không phải người tham gia.
- Email verified claim khi vừa click xác thực và token chưa refresh.
- Repeated favorite save (API idempotent).

Bộ kiểm thử `rules-tests/` đã chạy trên Firestore Emulator 1.22.0. Hai lần ACCEPT cạnh tranh có đúng một yêu cầu thắng trong cả luồng Cho tặng và Trao đổi. Truy vấn lọc theo chủ tin đọc được yêu cầu thua và cập nhật thành REJECTED. Cần kiểm tra riêng trên dự án Firebase của nhóm sau khi triển khai Rules và chỉ mục.
