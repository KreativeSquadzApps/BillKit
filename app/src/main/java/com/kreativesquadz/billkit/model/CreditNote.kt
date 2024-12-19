package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "credit_notes")
data class CreditNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Long,
    val invoiceNumber: String,
    val createdBy: String,
    val dateTime: String,
    val status: String,
    val amount: Double,
    val totalAmount: Double,
    val isSynced: Int = 0,
    val userId: Long,
    var invoiceItems: List<InvoiceItem>
) : Serializable
