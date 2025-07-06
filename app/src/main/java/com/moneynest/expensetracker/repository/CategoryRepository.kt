package com.moneynest.expensetracker.repository

import com.moneynest.expensetracker.dao.CategoryDao
import com.moneynest.expensetracker.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(type: String): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun insertCategory(category: Category){
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category){
        return categoryDao.deleteCategory(category)
    }

}