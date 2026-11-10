package com.campusshare.app.data

import com.campusshare.app.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(userId: String): Flow<List<Favorite>>
    fun observeIsFavorite(postId: String): Flow<Boolean>
    suspend fun setFavorite(postId: String, saved: Boolean)
}
