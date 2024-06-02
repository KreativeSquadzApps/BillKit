package com.kreativesquadz.billkit.repository

import com.kreativesquadz.billkit.Dao.UserDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(val db: AppDatabase) {
    private val userDao: UserDao = db.userDao()
    suspend fun addUser(user: User) = userDao.insert(user)
    suspend fun getUserById(userId: Long) = userDao.getUserById(userId)
}