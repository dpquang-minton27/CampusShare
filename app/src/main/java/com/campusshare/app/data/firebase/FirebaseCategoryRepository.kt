package com.campusshare.app.data.firebase

import com.campusshare.app.data.CategoryRepository
import com.campusshare.app.domain.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseCategoryRepository(private val db: FirebaseFirestore) : CategoryRepository {
    override fun observeActiveCategories(): Flow<List<Category>> =
        db.collection("categories").whereEqualTo("active", true).observe().map { snapshot ->
            snapshot.documents.map { it.toCategory() }.sortedBy { it.sortOrder }
        }
}
