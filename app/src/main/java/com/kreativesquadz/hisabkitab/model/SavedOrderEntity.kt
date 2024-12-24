package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_order")
data class SavedOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long = 0,
    val orderName: String,
    val totalAmount: Double,
    val date: Long
)