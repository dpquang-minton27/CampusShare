package com.campusshare.app.data

import com.campusshare.app.domain.model.User

/** Shared for every screen. Do not construct a repository per Activity. */
object CampusShareData {
    val users = listOf(
        User("u1", "Nguyễn An", "an@sv.example.edu"),
        User("u2", "Trần Bình", "binh@sv.example.edu"),
        User("u3", "Lê Chi", "chi@sv.example.edu")
    )
    val authRepository: AuthRepository = FakeAuthRepository(users)
}
