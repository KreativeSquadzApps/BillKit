package com.kreativesquadz.billkit.repository

import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Dao.StaffDao
import com.kreativesquadz.billkit.Dao.UserDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.model.User
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.model.request.LoginRequest
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */



class UserSessionRepository @Inject constructor(private val db: AppDatabase) {
   private val userSessionDao = db.userSessionDao()

   // val userSession: LiveData<UserSession> = userSessionDao.getUserSession()

    suspend fun insertUserSession(userSession: UserSession) {
        userSessionDao.insertUserSession(userSession)
    }

    suspend fun clearSession() {
        userSessionDao.clearSession()
    }


}