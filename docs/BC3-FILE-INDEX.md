# Danh mục đầy đủ các file thuộc phần Quang

Mỗi đường dẫn bên dưới là một file có nội dung thực trong overlay, không phải lời hứa sẽ viết.
Không bao gồm `MainActivity.kt`, `ui/posts/`, `ui/search/`, `ui/detail/`, `ui/requests/`, `ui/favorites/`.

| File | Phạm vi |
|---|---|
| `.github/workflows/android-ci.yml` | Mock CI on develop push (Quang/shared configuration) |
| `QUANG-BC3-START-HERE.md` | Quang-owned integration file |
| `app/build.gradle.kts` | Shared Android configuration owned by Quang |
| `app/src/main/AndroidManifest.xml` | Shared Android configuration owned by Quang |
| `app/src/main/java/com/campusshare/app/CampusShareApplication.kt` | Quang-owned integration file |
| `app/src/main/java/com/campusshare/app/data/AuthRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/CampusShareData.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/CategoryRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/ContactShareRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/FakeAuthRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/FavoriteRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/MediaRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/PostRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/RequestRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/UserRepository.kt` | Data contract / dependency container (Quang) |
| `app/src/main/java/com/campusshare/app/data/fake/FakeRepositories.kt` | Fake repository / fixture (Quang) |
| `app/src/main/java/com/campusshare/app/data/fake/SeedData.kt` | Fake repository / fixture (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseAuthRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseCategoryRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseContactShareRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseFavoriteRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebasePostRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseRequestRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirebaseUserRepository.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirestoreFlow.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/data/firebase/FirestoreMappers.kt` | Firebase repository / mapper (Quang) |
| `app/src/main/java/com/campusshare/app/domain/model/DataValidation.kt` | Domain model / validation (Quang) |
| `app/src/main/java/com/campusshare/app/domain/model/PostModels.kt` | Domain model / validation (Quang) |
| `app/src/main/java/com/campusshare/app/domain/model/User.kt` | Domain model / validation (Quang) |
| `app/src/main/java/com/campusshare/app/ui/auth/AuthActivity.kt` | Auth/Profile UI + ViewModel (Quang) |
| `app/src/main/java/com/campusshare/app/ui/auth/AuthViewModel.kt` | Auth/Profile UI + ViewModel (Quang) |
| `app/src/main/java/com/campusshare/app/ui/auth/ProfileActivity.kt` | Auth/Profile UI + ViewModel (Quang) |
| `app/src/main/java/com/campusshare/app/ui/auth/ProfileViewModel.kt` | Auth/Profile UI + ViewModel (Quang) |
| `app/src/main/res/layout/activity_auth.xml` | Auth/Profile XML resources (Quang) |
| `app/src/main/res/layout/activity_profile.xml` | Auth/Profile XML resources (Quang) |
| `app/src/main/res/values/auth_strings.xml` | Auth/Profile XML resources (Quang) |
| `app/src/test/java/com/campusshare/app/data/FakeAuthRepositoryTest.kt` | Unit tests (Quang) |
| `app/src/test/java/com/campusshare/app/data/FakeWorkflowTest.kt` | Unit tests (Quang) |
| `build.gradle.kts` | Shared Android configuration owned by Quang |
| `docs/BC3-DATA-CONTRACT-V2.md` | Data contract/decision/handoff documentation (Quang) |
| `docs/BC3-FIREBASE-SETUP.md` | Data contract/decision/handoff documentation (Quang) |
| `docs/BC3-HANDOFF-TO-TRONG-THANG.md` | Data contract/decision/handoff documentation (Quang) |
| `docs/BC3-OWNERSHIP-DEVELOP.md` | Data contract/decision/handoff documentation (Quang) |
| `docs/BC3-SECURITY-AND-STATE.md` | Data contract/decision/handoff documentation (Quang) |
| `docs/BC3-TESTPLAN.md` | Data contract/decision/handoff documentation (Quang) |
| `firebase/firestore.indexes.json` | Firestore Rules/indexes/emulator config (Quang) |
| `firebase/firestore.rules` | Firestore Rules/indexes/emulator config (Quang) |
| `firebase.json` | Firestore Rules/indexes/emulator config (Quang) |
| `optional-storage/FirebaseStorageMediaRepository.kt` | Storage option; NOT integrated yet (Quang) |
| `optional-storage/README.md` | Storage option; NOT integrated yet (Quang) |
| `optional-storage/storage.rules` | Storage option; NOT integrated yet (Quang) |
| `rules-tests/README.md` | Rules emulator tests (Quang) |
| `rules-tests/package.json` | Rules emulator tests (Quang) |
| `rules-tests/rules.test.mjs` | Rules emulator tests (Quang) |
| `seed/README.md` | Emulator synthetic database and validator (Quang) |
| `seed/import_to_emulators.mjs` | Emulator synthetic database and validator (Quang) |
| `seed/package.json` | Emulator synthetic database and validator (Quang) |
| `seed/seed.v2.json` | Emulator synthetic database and validator (Quang) |
| `seed/validate_seed.py` | Emulator synthetic database and validator (Quang) |
| `tools/apply_overlay.py` | Local validation and safe overlay tool (Quang) |
| `tools/check_domain_offline.sh` | Local validation and safe overlay tool (Quang) |
| `tools/offline_smoke.kt` | Local validation and safe overlay tool (Quang) |
| `tools/validate_overlay.py` | Local validation and safe overlay tool (Quang) |
