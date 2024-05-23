package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companyDetails")
data class CompanyDetails (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var BusinessName: String,
    var Place: String,
    var ShopContactNumber: String,
    var ShopEmail: String,
    var GSTNo: String,
    var FSSAINo: String?,
    var CurrencySymbol: String,
    var InvoicePrefix: String,
    var InvoiceNumber: Int
)