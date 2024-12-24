package com.kreativesquadz.hisabkitab.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "gst_table")
data class GST(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val taxAmount: Double,
    val taxType: String,
    var productCount: Int ,
    val isSynced: Int = 0
) : Serializable
