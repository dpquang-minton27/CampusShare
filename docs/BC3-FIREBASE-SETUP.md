# Hướng dẫn thiết lập Firebase/Android Studio (Quang)

## 1. Chạy Mock trước

Từ clone GitHub đã chuyển sang `develop`, sau khi copy overlay và Gradle Sync:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Lệnh này **không** áp dụng plugin Google Services cho app và **không** yêu cầu `app/google-services.json`. Dữ liệu BC2/BC3 trong RAM; tài khoản mock có trong `SeedData.kt`.

## 2. Tạo Firebase project thật

- Truy cập Firebase Console, tạo project cho nhóm và chọn đúng chủ sở hữu/quyền truy cập.
- Thêm Android app với `applicationId` chính xác `com.campusshare.app`.
- Tải `google-services.json` vào `app/google-services.json` trên máy Quang; những thành viên cần build online tự lấy cấu hình từ người phụ trách. Đã có `.gitignore` trong GitHub gốc.
- Bật Authentication -> Sign-in method -> Email/Password.
- Khởi tạo Cloud Firestore, chọn khu vực dữ liệu thích hợp và giữ Rules deny-all mặc định cho tới khi kiểm thử Rules hoàn tất.
- Thêm 4 document Category qua Firebase Console (hoặc một importer tin cậy trên dự án thử nghiệm, không phải chạy seed demo-u vào production).
- Trong Android Studio chọn JDK 17, Android SDK Platform 36 theo README hiện có.
- Nếu nhắc về billing, chú ý Cloud Storage và Cloud Functions có thể yêu cầu Blaze; **không bật trong bản này**.

## 3. Chạy online

```powershell
.\gradlew.bat :app:assembleDebug -PcampusshareBackend=firebase
```

App sử dụng `FirebaseAuthRepository`, `FirebasePostRepository`, `FirebaseRequestRepository`, `FirebaseFavoriteRepository` và các repository còn lại. Khi đăng ký, email verification được gửi nếu dịch vụ thành công. Vào Profile và bấm 'Kiểm tra trạng thái xác thực' để reload user và refresh ID token. Rules kiểm tra `email_verified` trước khi tạo Post hoặc Request.

## 4. Emulator kiểm thử backend trước production

Cài Firebase CLI: `npm install -g firebase-tools`. Ở project root chạy:

```sh
firebase emulators:start --only auth,firestore --project demo-campusshare
```

Trên Android Emulator build:

```powershell
.\gradlew.bat :app:assembleDebug -PcampusshareBackend=firebase -PcampusshareEmulator=true
```

`CampusShareData` gọi `useEmulator("10.0.2.2",9099)` và `useEmulator("10.0.2.2",8080)` trước request đầu tiên. Đường `10.0.2.2` chỉ dành cho Android Emulator; điện thoại vật lý cần LAN IP hoặc Firebase thật. Vẫn cần cấu hình Firebase app hợp lệ để plugin Google Services chạy.

Nạp dữ liệu mẫu theo `seed/README.md`. Kiểm thử Rules theo `rules-tests/README.md`. **Chưa** coi Rules đạt chuẩn nếu chưa chạy suite này.

## 5. Deploy Rules sau khi kiểm thử

Khi đã đăng nhập Firebase CLI bằng tài khoản được nhóm cấp quyền, chọn đúng project ID và chỉ deploy Firestore:

```sh
firebase use <FIREBASE_PROJECT_ID>
firebase deploy --only firestore:rules,firestore:indexes
```

Không deploy lên project khác hoặc dùng test-mode Rules `allow read,write: if true`. Không dùng Admin SDK trên điện thoại Android.

## 6. Demo hai thiết bị Internet

Tạo hai tài khoản email thật (không dùng email example.edu), xác thực email, sau đó A tạo Post AVAILABLE, B thấy tin, B gửi Request, A ACCEPT, B thấy Request ACCEPTED. Kiểm tra A/B đều có đúng quyền; bên C không thể ACCEPT Request hoặc gửi yêu cầu cho Post đã TAKEN. Tắt/mở lại hai app, kiểm chứng dữ liệu Firestore tồn tại. Cần ảnh chụp/video/log cho báo cáo.

## 7. Chưa triển khai

- Storage upload, Google Login/OTP, Cloud Functions, Room offline-first, notification push.
- Full-text search trên toàn Firestore và phân trang lớn.
- Verified student domain trường: nhóm phải xác nhận domain chính thức nếu muốn siết hơn `email_verified`.
