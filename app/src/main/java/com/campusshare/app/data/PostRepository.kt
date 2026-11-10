package com.campusshare.app.data

import com.campusshare.app.domain.model.Post
import com.campusshare.app.domain.model.PostDraft
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observeAvailablePosts(): Flow<List<Post>>
    fun observeMyPosts(ownerId: String): Flow<List<Post>>
    fun observePost(postId: String): Flow<Post?>
    suspend fun createPost(draft: PostDraft): String
    suspend fun editPost(postId: String, draft: PostDraft)
    suspend fun hidePost(postId: String)
}
