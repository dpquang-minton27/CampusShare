# Ownership và Git — duy nhất branch develop

## Phạm vi

| Vùng code | Phụ trách chính |
|---|---|
| `domain/model/` | Quang |
| `data/` (contract, fake, firebase, DI) | Quang; Trọng/Thắng cùng xác nhận API |
| `ui/auth/`, `res/layout/activity_auth.xml`, `activity_profile.xml`, `auth_strings.xml` | Quang |
| `ui/posts/`, `ui/search/` | Trọng |
| `ui/detail/`, `ui/favorites/`, `ui/requests/`, Navigation/MainActivity | Thắng |
| `build.gradle.kts`, `app/build.gradle.kts`, `AndroidManifest.xml` | Quang quản lý thay đổi chung sau khi thông báo nhóm |
| `firebase/`, `seed/`, `rules-tests/` | Quang; Thắng review request/permissions |

## Trình tự đẩy code trên một branch duy nhất

```bash
git switch develop
git pull --ff-only origin develop
git status --short
# thay code trong folder được giao, build/test, kiểm tra diff
git diff --check
git add app/src/main/java/com/campusshare/app/domain/model
git add app/src/main/java/com/campusshare/app/data
git add app/src/main/java/com/campusshare/app/ui/auth
git add app/src/main/java/com/campusshare/app/CampusShareApplication.kt
git add app/src/main/res/layout/activity_auth.xml app/src/main/res/layout/activity_profile.xml
git add app/src/main/res/values/auth_strings.xml
git add app/src/main/AndroidManifest.xml app/build.gradle.kts build.gradle.kts
git add firebase firebase.json seed rules-tests docs/BC3-* tools QUANG-BC3-START-HERE.md
git diff --cached --name-only
git commit -m "feat(data): implement BC3 contracts, fake and Firebase repositories"
git push origin develop
```

Không dùng `git add .` khi máy có file Firebase config hoặc thay đổi chưa bàn giao của thành viên khác. Không dùng `git push --force`. Nếu `git push` bị từ chối do có commit mới, dùng `git fetch origin`, xem `git log --oneline --left-right --graph HEAD...origin/develop`, sau đó `git pull --rebase origin develop` nếu sạch và giải quyết conflict; thống nhất khi conflict là file chung. Chỉ push lại sau khi build/test.

## Quy tắc tuần tự trên develop

**Cổng 1:** Quang push model/contract/fake, nhóm kéo và cùng compile. Trọng/Thắng chưa sửa các file của Quang.

**Cổng 2:** Quang push Firebase/Auth/Profile và cấu hình. Trọng/Thắng kéo các contract đã ổn định, làm UI qua interface.

**Cổng 3:** Thắng bắt đầu kết nối UI Request qua `RequestRepository`, Trọng nối Post qua `PostRepository`. Nếu cần thêm hàm vào interface, thông báo Quang trước khi sửa.

**Cổng 4:** Khi tất cả màn hình hoạt động, cả nhóm chạy một vòng kiểm thử trên hai thiết bị trước khi trình bày.
