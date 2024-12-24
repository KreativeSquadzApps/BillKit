package com.kreativesquadz.hisabkitab.repository

import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.model.UserSession
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