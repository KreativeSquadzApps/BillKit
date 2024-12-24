package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val adminId: Long,
    val name: String,
    val mailId: String,
    val password: String,
    val status: String,
    val role: String,
    val totalSalesCount: Int,
    val totalSalesAmount: Double,
    val permissions: String,
    val isSynced : Int = 0
) : Serializable
