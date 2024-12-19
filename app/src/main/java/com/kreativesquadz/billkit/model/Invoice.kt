package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

@Entity(
    tableName = "invoices",
    )
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId : Long = Config.userId,
    val invoiceId: Int,
    val invoiceNumber: String,
    val invoiceDate: String,
    val invoiceTime: String,
    val createdBy: String,
    val totalItems: Int,
    val subtotal: Double,
    val cashAmount: Double? = null,
    val onlineAmount: Double? = null,
    val creditAmount: Double? = null,
    val packageAmount: Double? = null,
    val otherChargesAmount: Double? = null,
    val customGstAmount: String? = null,
    val totalAmount: Double,
    val totalTax: Double?=null,
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
    )
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var invoiceId: Long,
    val orderId: Long?, // Add this field
    var itemName: String,
    val unitPrice: Double,
    var quantity: Int,
    var returnedQty: Int? = 0,
    var totalPrice: Double,
    val productMrp: Double?=null,
    val taxRate: Double,
    val productTaxType : String?=null,
    val isProduct : Int = 0,
) : Serializable

