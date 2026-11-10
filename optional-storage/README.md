# Firebase Cloud Storage — PHẦN TÙY CHỌN, CHƯA BẬT

Đây là mã đề xuất để sau khi nhóm CHỐT gói Blaze cho Storage; không được copy vào app source ngay lúc bàn giao Data Contract.

1. Chốt project Blaze, ngân sách/cảnh báo chi phí và quota; kiểm tra Storage bucket.
2. Kiểm thử `storage.rules` trên Firebase Storage Emulator, không deploy rules khi chưa test.
3. Trong `app/build.gradle.kts`, thêm `implementation("com.google.firebase:firebase-storage")` dưới Firebase BoM.
4. Copy file `FirebaseStorageMediaRepository.kt` vào `app/src/main/java/com/campusshare/app/data/firebase/`.
5. Trong nhánh `Firebase` của `CampusShareData.initialize`, thay `DisabledMediaRepository()` bằng `FirebaseStorageMediaRepository(authSdk, FirebaseStorage.getInstance())`. (Vẫn trên `develop`.)
6. Trọng lấy `Uri` từ Android Photo Picker; nếu source không thật sự là JPEG, chuyển đổi/nén sang JPEG có giới hạn kích thước trước khi gọi `uploadPostImage`.
7. `createPost(draft.copy(imageUrls=emptyList()))` trả postId, upload ảnh vào `post-images/{uid}/{postId}`, rồi `editPost(postId, draft.copy(imageUrls=uploadedUrls))`. Đây là quy trình hai giai đoạn, có thể tạo bài chưa ảnh/ảnh rác nếu lỗi; cần cleanup và retry.
8. Chỉ khi xử lý được retry, cleanup, quota và quyền đọc ảnh mới nghiệm thu chức năng upload thật.

Đối với MVP online khi chưa có Storage, các phần Firebase Auth/Firestore/Post/Request vẫn hoạt động; Post có thể có `imageUrls=[]` hoặc HTTPS demo hợp pháp.
