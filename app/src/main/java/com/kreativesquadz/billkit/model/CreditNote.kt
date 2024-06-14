package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_notes")
data class CreditNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Long,
    val createdBy: String,
    val dateTime: String,
    val status: String,
    val amount: Double,
    val totalAmount: Double,
    val isSynced: Int = 0,
    val userId: Long,
    val invoiceItems: List<InvoiceItem>
)
