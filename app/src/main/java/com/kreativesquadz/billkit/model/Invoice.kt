package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "invoices",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["customerId"])]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Int,
    val invoiceNumber: String,
    val invoiceDate: String,
    val invoiceTime: String,
    val createdBy: String,
    val totalItems: Int,
    val subtotal: Double,
    val cashAmount: Double,
    val totalAmount: Double,
    val customerId: Long? = null,
    val isSynced: Int = 0,
    val invoiceItems: List<InvoiceItem>
) : Serializable

@Entity(
    tableName = "invoice_items",
    foreignKeys = [ForeignKey(
        entity = Invoice::class,
        parentColumns = ["id"],
        childColumns = ["invoiceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val itemName: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val taxRate: Double
) : Serializable

