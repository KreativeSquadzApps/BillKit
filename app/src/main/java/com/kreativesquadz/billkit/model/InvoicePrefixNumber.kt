package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config

@Entity(tableName = "invoice_prefix_number")
data class InvoicePrefixNumber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId : Long  = Config.userId,
    val invoicePrefix: String,
    val invoiceNumber: Long
)
