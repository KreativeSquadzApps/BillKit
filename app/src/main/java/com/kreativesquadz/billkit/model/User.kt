package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId : Int,
    val username: String,
    val password: String,
    val email: String,
    val token: String,
    val role: String
)

