package com.kreativesquadz.hisabkitab.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kreativesquadz.hisabkitab.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
     fun getUserById(userId: Int): User?

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>


}
