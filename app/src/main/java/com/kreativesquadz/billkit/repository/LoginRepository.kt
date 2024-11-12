package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Dao.StaffDao
import com.kreativesquadz.billkit.Dao.UserDao
import com.kreativesquadz.billkit.Dao.UserSessionDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.model.request.LoginRequest
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */



class LoginRepository @Inject constructor(private val db: AppDatabase) {
   private val userDao : UserDao = db.userDao()
    private val staffDao : StaffDao = db.staffDao()
    private val userSessionDao : UserSessionDao = db.userSessionDao()

    suspend fun login(username: String, password: String, isStaff: Boolean) {
        try {
            val response = ApiClient.getApiService().login(LoginRequest(username, password, isStaff))
            if (response != null && response.code == 200) {
                saveSession(loginResponse = response)
            } else {
                Log.e("LoginError", "Login failed with code: ${response?.code}")
                // Handle specific error response here, maybe show a message to the user
            }
        } catch (e: Exception) {
            Log.e("Exception", "An error occurred: ${e.localizedMessage}")
            // Handle network or other unexpected errors
        }
    }
    suspend fun logout() {
        userDao.deleteAllUsers()
        staffDao.deleteStaffList()
        clearSession()
    }
     suspend fun saveSession(loginResponse: LoginResponse) {
         loginResponse.user?.let {
             userDao.insert(it)
             insertUserSession(UserSession(0,loginResponse.user.userId,"Created By "+loginResponse.user.username,System.currentTimeMillis(),null))
         }
         loginResponse.staff?.let {
             staffDao.insertStaff(it)
             insertUserSession(UserSession(0,null,"Created By "+it.name,System.currentTimeMillis(),it.id.toInt()))

         }
     }
     fun getSession(userId: Int?, staffId: Long?) : LoginResponse? {
        userId?.let {
           return LoginResponse(200,userDao.getUserById(it),null)
            Log.e("usersssss",userDao.getUserById(it).toString())
        }
        staffId?.let {
           return LoginResponse(200,null,staffDao.selectStaffById(it))
        }
        return null
    }

    private suspend fun insertUserSession(userSession: UserSession) {
        userSessionDao.insertUserSession(userSession)
    }

    fun getUserSessions() : UserSession? {
       return userSessionDao.getUserSession()
    }

    fun getUserSessionsLiveData() : LiveData<UserSession> {
       return userSessionDao.getUserSession2()
    }


    suspend fun clearSession() {
        userSessionDao.clearSession()
    }
}