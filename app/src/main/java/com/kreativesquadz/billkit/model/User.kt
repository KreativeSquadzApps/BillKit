package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val username: String,
    val email: String,
    val password: String
) : Serializable
