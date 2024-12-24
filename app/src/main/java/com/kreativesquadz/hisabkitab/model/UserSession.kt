package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userSession")
data class UserSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int?,
    val sessionUser: String,
    val loginTime: Long,
    val staffId: Int?
)

