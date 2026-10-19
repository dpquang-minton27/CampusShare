package com.campusshare.app.data

import com.campusshare.app.domain.model.Post

interface PostRepository {
    fun observePosts(onChange: (List<Post>) -> Unit): () -> Unit
    fun getPostById(postId: String): Post?
    fun addPost(post: Post)
}

class FakePostRepository(
    initialPosts: List<Post>
) : PostRepository {

    private val posts = initialPosts.toMutableList()
    private val observers = mutableSetOf<(List<Post>) -> Unit>()

    override fun observePosts(
        onChange: (List<Post>) -> Unit
    ): () -> Unit {
        observers.add(onChange)
        onChange(posts.toList())

        return {
            observers.remove(onChange)
            Unit
        }
    }

    override fun getPostById(postId: String): Post? {
        return posts.find { it.id == postId }
    }

    override fun addPost(post: Post) {
        require(posts.none { it.id == post.id }) {
            "Mã bài đăng đã tồn tại: ${post.id}"
        }

        posts.add(0, post)
        val snapshot = posts.toList()
        observers.toList().forEach { it(snapshot) }
    }
}