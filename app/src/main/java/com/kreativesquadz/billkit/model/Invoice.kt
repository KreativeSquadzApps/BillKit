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
    val totalGst: Double,
    val customerId: Long? = null,
    val isSynced: Int = 0,
    val creditNoteAmount: Int = 0,
    val creditNoteId: Int? = 0,
    val discount : Int? = null,
    val status : String,
    val invoiceItems: List<InvoiceItem>?=null
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
    @PrimaryKey(autoGenerate = true)  val id: Long = 0,
    val invoiceId: Long,
    var itemName: String,
    val unitPrice: Double,
    var quantity: Int,
    var returnedQty: Int?=0,
    var totalPrice: Double,
    val taxRate: Double
) : Serializable

