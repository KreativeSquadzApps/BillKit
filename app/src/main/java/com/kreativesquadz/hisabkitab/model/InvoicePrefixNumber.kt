package com.kreativesquadz.hisabkitab.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.hisabkitab.Config

@Entity(tableName = "invoice_prefix_number")
data class InvoicePrefixNumber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId : Long  = Config.userId,
    val invoicePrefix: String,
    val invoiceNumber: Long
)
