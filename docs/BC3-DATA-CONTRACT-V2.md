# CampusShare Data Contract v2 — chuẩn dùng chung cho cả ba thành viên

## Nguồn và thay đổi so với BC2

Báo cáo I xác định 5 entity: User, Category, Post, Request, Favorite và enum PostType/PostStatus/RequestStatus. Code develop v1 có tên Kotlin `User.id`, `User.fullName`, `Post.ownerId`, `Post.imageUrl`. v2 giữ `id/fullName/ownerId`, đổi `imageUrl` sang `imageUrls` và bổ sung dữ liệu phục vụ Figma/nhu cầu giao nhận.

Data source production: Firebase Auth (UID), Firestore (`Timestamp`, enum string). Domain: UTC epoch millis kiểu `Long`; UI chuyển sang ngày giờ địa phương. Không cho ViewModel chứa `DocumentSnapshot` hoặc FirebaseUser.

## Model / field

- `User(id,fullName,email,phone?,zaloUrl?,avatarUrl?,createdAtMillis,updatedAtMillis)`; trong Firebase phone/Zalo nằm tại `userContacts/{uid}`.
- `Category(id,name,iconKey,sortOrder,active)`; seed 4 ID: books/study/dorm/electronics.
- `Post(id,ownerId,categoryId,title,description,type,status,imageUrls,conditionPercent,meetingLocation,exchangeNote?,acceptedRequestId?,createdAtMillis,updatedAtMillis)`.
- `PostDraft(categoryId,title,description,type,imageUrls,conditionPercent,meetingLocation,exchangeNote?)` cho màn Create/Edit của Trọng. Không nhận ownerId/status/winner từ UI.
- `Request(id,postId,ownerId,requesterId,message,status,createdAtMillis,updatedAtMillis)`; ID=`postId + "__" + requesterId`.
- `Favorite(id,userId,postId,createdAtMillis)`; ID=`userId + "__" + postId`.
- `ProfileEdit(fullName,phone?,zaloUrl?)`, `UserContact(phone?,zaloUrl?)` là value object, không phải entity nghiệp vụ mới.

Enum: PostType=`GIVE_AWAY|EXCHANGE`; PostStatus=`AVAILABLE|TAKEN|EXCHANGED|HIDDEN`; RequestStatus=`PENDING|ACCEPTED|REJECTED|CANCELLED`.

## Validation

- User name 2–80, email định dạng cơ bản; production password >=8. Xác thực domain trường chưa chốt nên chưa khóa theo domain.
- Post title 3–100, description 5–2000, meetingLocation 2–200, conditionPercent 0–100, tối đa 3 HTTPS image URL; PostType.EXCHANGE yêu cầu exchangeNote.
- Request message 1–200, không tự xin tin của mình, chỉ xin khi Post AVAILABLE.
- `createPost` dùng auth.uid làm owner; không tin UID truyền từ UI.
- Post hard delete chưa hỗ trợ: `hidePost()` chuyển sang HIDDEN cho Post AVAILABLE. Không xóa lịch sử/Request.
- Sau ACCEPT, Post.status=TAKEN/EXCHANGED, acceptedRequestId cố định; Request thắng ACCEPTED. Các PENDING còn lại có `effectiveStatus` REJECTED dựa trên Post, và được persisting sau qua `reconcileRejected`.

## API giao cho Trọng

```kotlin
val posts = CampusShareData.postRepository
val categories = CampusShareData.categoryRepository
posts.observeAvailablePosts(): Flow<List<Post>>
posts.observeMyPosts(ownerId): Flow<List<Post>>
posts.observePost(postId): Flow<Post?>
posts.createPost(PostDraft(...)): String // suspend
posts.editPost(postId, PostDraft(...))    // suspend
posts.hidePost(postId)                   // suspend
categories.observeActiveCategories(): Flow<List<Category>>
```

Giao diện chỉ hiển thị dữ liệu lấy qua `Flow`. Không tự khởi tạo `FakePostRepository` ở Activity. Các thao tác mạng chạy trong `viewModelScope`, xử lý loading/error.

## API giao cho Thắng

```kotlin
val requests = CampusShareData.requestRepository
val favorites = CampusShareData.favoriteRepository
requests.observeSent(uid): Flow<List<Request>>
requests.observeReceived(uid): Flow<List<Request>>
requests.sendRequest(postId, message): String   // suspend
requests.cancelRequest(requestId)             // suspend
requests.rejectRequest(requestId)             // suspend
requests.acceptRequest(requestId)             // suspend
requests.reconcileRejected(postId)            // suspend / idempotent
favorites.observeFavorites(uid): Flow<List<Favorite>>
favorites.observeIsFavorite(postId): Flow<Boolean>
favorites.setFavorite(postId, true_or_false)  // suspend
```

UI không tự set status của Post/Request. Sau thao tác, UI chờ Repository trả kết quả và `Flow` phát trạng thái mới. `effectiveStatus` dùng để hiển thị yêu cầu thua khi quá trình đồng bộ REJECTED chưa hoàn tất.

## API Quang giữ

```kotlin
val auth = CampusShareData.authRepository
val userRepo = CampusShareData.userRepository
val user = auth.currentUser
val stop = auth.observeAuthState { state -> ... }
auth.signIn(email,password)          // suspend
auth.signUp(name,email,password)     // suspend
auth.updateMyProfile(ProfileEdit(...)) // suspend
userRepo.getMyProfile()             // suspend
userRepo.saveMyProfile(ProfileEdit(...)) // suspend
```

## Truy vấn Firestore và indexes

`posts(status==AVAILABLE).orderBy(createdAt desc)`; `posts(ownerId==uid).orderBy(createdAt desc)`; `requests(requesterId==uid)` và `requests(ownerId==uid)` orderBy createdAt; `requests(postId,status==PENDING)` phục vụ reconcile. Index file `firebase/firestore.indexes.json`.

Search theo từ khóa trong BC3: Firestore Standard không tự hỗ trợ full text; với dữ liệu demo Trọng có thể lọc cục bộ trên tập Post đã tải, nhưng phải ghi rõ không phải tìm kiếm toàn tập nếu phân trang. Các truy vấn production và giải pháp nâng cao sẽ được quyết định riêng.

## Tính nhất quán + quyền riêng tư

- Người chưa xác thực email có thể đăng nhập/xem profile nhưng Rules hạn chế tạo Post, Request (UI cung cấp gửi email xác thực và refresh token).
- `users` là hồ sơ công khai không chứa email/phone/Zalo. `userContacts` chỉ chủ tài khoản đọc. Sau ACCEPTED, mỗi bên **tự nguyện** tạo `contactShares` của chính mình để bên kia đọc; chưa có tự động cấp quyền/contact khi ACCEPT.
- Firestore transactions có thể retry và thất bại offline; không tạo hiệu ứng bên ngoài bên trong transaction lambda.
- `contactShares` không phải phân hệ chat; không cần Firebase Cloud Functions trong phạm vi nền tảng này.

## Versioning

`schemaVersion=2` trong seed; không dùng giả định có migration production từ dữ liệu BC2 vì BC2 chỉ lưu RAM. Khi cài phiên bản khác có database thật, yêu cầu migration sẽ được thiết kế sau.
