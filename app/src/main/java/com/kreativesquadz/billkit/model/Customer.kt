package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val CustomerName: String,
    val ShopContactNumber: String,
    val GSTNo: String,
    val TotalSales: String,
    val Address: String,
    val CreditAmount: String
) : Serializable