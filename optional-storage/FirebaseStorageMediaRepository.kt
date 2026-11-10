// OPTIONAL FILE. NOT part of app source set until Firebase Storage/Blaze is approved.
// Copy to app/src/main/java/com/campusshare/app/data/firebase/ when enabling.
package com.campusshare.app.data.firebase

import android.net.Uri
import com.campusshare.app.data.MediaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageMediaRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : MediaRepository {
    override suspend fun uploadPostImage(postId: String, localUri: String): String {
        val uid = requireNotNull(auth.currentUser?.uid) { "Vui lòng đăng nhập." }
        require(postId.isNotBlank() && '/' !in postId)
        val uri = Uri.parse(localUri)
        require(uri.scheme == "content") { "Hãy dùng URI chọn bằng Android Photo Picker." }
        val path = "post-images/$uid/$postId/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(path)
        // Caller must verify file size/type before upload; Storage Rules also enforce size/type.
        ref.putFile(uri, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
        return ref.downloadUrl.await().toString()
    }
    override suspend fun deletePostImage(postId: String, storagePath: String) {
        val uid = requireNotNull(auth.currentUser?.uid)
        val requiredPrefix = "post-images/$uid/$postId/"
        require(storagePath.startsWith(requiredPrefix)) { "Không thể xóa ảnh ngoài bài đăng của mình." }
        storage.reference.child(storagePath).delete().await()
    }
}
