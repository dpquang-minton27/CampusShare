package com.campusshare.app.data

import com.campusshare.app.domain.model.ProfileEdit
import com.campusshare.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getMyProfile(): User
    suspend fun saveMyProfile(edit: ProfileEdit): User
    fun observePublicUser(uid: String): Flow<User?>
}
