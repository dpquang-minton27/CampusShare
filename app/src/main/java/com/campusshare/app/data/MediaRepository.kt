package com.campusshare.app.data

/** Optional feature contract for Trọng's Photo Picker UI. */
interface MediaRepository {
    /** localUri is a content:// URI from Android Photo Picker, not an HTTP URL. */
    suspend fun uploadPostImage(postId: String, localUri: String): String
    suspend fun deletePostImage(postId: String, storagePath: String)
}

/** Safe default: BC3 can run posts with 0 images while Storage billing is undecided. */
class DisabledMediaRepository : MediaRepository {
    override suspend fun uploadPostImage(postId: String, localUri: String): String =
        error("Firebase Storage is disabled. Approve Blaze & Storage Rules before enabling upload.")
    override suspend fun deletePostImage(postId: String, storagePath: String) = Unit
}
