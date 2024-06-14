package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users_settings")
data class UserSetting(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val isdiscount: Int = 0,
    val email: String,
    val password: String,
    val isSynced: Int = 0
) : Serializable
