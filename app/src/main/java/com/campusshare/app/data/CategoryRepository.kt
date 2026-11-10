package com.campusshare.app.data

import com.campusshare.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<Category>>
}
