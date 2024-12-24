package com.kreativesquadz.hisabkitab.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users_settings")
data class UserSetting(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val isdiscount: Int = 0,
    val isQtyReverse: Int = 0,
    val email: String,
    val password: String,
    val isSynced: Int = 0
) : Serializable



