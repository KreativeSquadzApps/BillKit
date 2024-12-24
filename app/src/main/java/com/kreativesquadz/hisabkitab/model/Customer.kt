package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val shopContactNumber: String,
    val gstNo: String,
    val totalSales: String,
    val address: String,
    val creditAmount: String,
    val customerMark : String? = null,
    val customerOther : String? = null,
    val isSynced: Int = 0,
    ) : Serializable