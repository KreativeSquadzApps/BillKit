package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

data class DayBook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val invoiceNumber: String,
    val createdBy: String,
    val customerName: String,
    val totalAmount: Double,
    val cashAmount: Double? = null,
)



