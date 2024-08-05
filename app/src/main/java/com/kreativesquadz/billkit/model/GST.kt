package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gst_table")
data class GST(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val taxAmount: Double,
    val taxType: String,
    val productCount: Int,
    val isSynced: Int = 0
)
