package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreativesquadz.billkit.model.User
import com.kreativesquadz.billkit.model.UserSession

@Dao
interface UserSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(userSession: UserSession)

    @Query("SELECT * FROM userSession LIMIT 1")
    fun getUserSession(): UserSession?

    @Query("SELECT * FROM userSession LIMIT 1")
    fun getUserSession2(): LiveData<UserSession>

    @Query("DELETE FROM userSession")
    suspend fun clearSession()


}
