package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customerCreditDetails")
data class CustomerCreditDetail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val creditDate: String,
    val creditType: String, // "Manual" or "Invoice"
    val creditAmount: Double,
    val invoiceId: Long? = null // Nullable if it's manual
)