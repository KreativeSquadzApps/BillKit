package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "invoices",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoice_number: String,
    val invoice_date: String,
    val invoice_time: String,
    val created_by: String,
    val total_items: Int,
    val subtotal: Double,
    val cash_amount: Double,
    val total_amount: Double,
    val customerId: Long? = null,
    val invoice_items: List<InvoiceItem>

) : Serializable


@Entity(
    tableName = "invoice_items",
    foreignKeys = [ForeignKey(
        entity = Invoice::class,
        parentColumns = ["id"],
        childColumns = ["invoice_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoice_id: Long,
    val item_name: String,
    val unit_price: Double,
    val quantity: Int,
    val total_price: Double,
    val tax_rate: Double
): Serializable