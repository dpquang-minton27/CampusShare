package com.campusshare.app.data

import android.content.Context
import com.campusshare.app.BuildConfig
import com.campusshare.app.data.fake.*
import com.campusshare.app.data.firebase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The single dependency container shared by ALL screens.
 * Existing MainActivity can keep calling CampusShareData.authRepository.currentUser.
 */
object CampusShareData {
    private data class Services(
        val auth: AuthRepository,
        val users: UserRepository,
        val categories: CategoryRepository,
        val posts: PostRepository,
        val favorites: FavoriteRepository,
        val requests: RequestRepository,
        val contacts: ContactShareRepository,
        val media: MediaRepository
    )
    @Volatile private var services: Services? = null

    @Synchronized fun initialize(context: Context) {
        if (services != null) return
        // FirebaseInitProvider initializes Firebase automatically when google-services is applied.
        services = if (BuildConfig.USE_FIREBASE) {
            val authSdk = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            if (BuildConfig.USE_FIREBASE_EMULATOR) {
                // Android Emulator routes 10.0.2.2 to the computer's localhost.
                // Call these BEFORE any Firebase Auth/Firestore request.
                authSdk.useEmulator("10.0.2.2", 9099)
                firestore.useEmulator("10.0.2.2", 8080)
            }
            Services(
                auth = FirebaseAuthRepository(authSdk, firestore),
                users = FirebaseUserRepository(authSdk, firestore),
                categories = FirebaseCategoryRepository(firestore),
                posts = FirebasePostRepository(authSdk, firestore),
                favorites = FirebaseFavoriteRepository(authSdk, firestore),
                requests = FirebaseRequestRepository(authSdk, firestore),
                contacts = FirebaseContactShareRepository(authSdk, firestore),
                media = DisabledMediaRepository()
            )
        } else {
            val auth = FakeAuthRepository(SeedData.users)
            val state = FakeStore()
            Services(
                auth = auth,
                users = FakeUserRepository(auth),
                categories = FakeCategoryRepository(),
                posts = FakePostRepository(state, auth),
                favorites = FakeFavoriteRepository(state, auth),
                requests = FakeRequestRepository(state, auth),
                contacts = FakeContactShareRepository(auth, state),
                media = DisabledMediaRepository()
            )
        }
    }
    private fun get() = checkNotNull(services) { "CampusShareApplication must initialize CampusShareData." }
    val authRepository: AuthRepository get() = get().auth
    val userRepository: UserRepository get() = get().users
    val categoryRepository: CategoryRepository get() = get().categories
    val postRepository: PostRepository get() = get().posts
    val favoriteRepository: FavoriteRepository get() = get().favorites
    val requestRepository: RequestRepository get() = get().requests
    val contactShareRepository: ContactShareRepository get() = get().contacts
    val mediaRepository: MediaRepository get() = get().media

    // Reference fixtures. The Firebase project does NOT use these as authentic user UIDs.
    val users get() = SeedData.users
    val categories get() = SeedData.categories
    val posts get() = SeedData.posts
}
