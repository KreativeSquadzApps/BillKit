package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val userId: Long,
    val productName: String,
    val category: String?=null,
    val productPrice: Double?=null,
    val productCost: Double?=null,
    val productMrp: Double?=null,
    val productBarcode: String?=null,
    val productStockUnit: String?=null,
    val productTax: Double?=null,
    var productStock: Int?=null,
    val productDefaultQty: Int?=null,
    val isSynced: Int = 0,
) : Serializable
