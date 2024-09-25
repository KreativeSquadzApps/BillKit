package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

@Entity(
    tableName = "companyDetails",
    indices = [Index(value = ["userId"], unique = true)]
)
data class CompanyDetails (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var userId : Long = Config.userId,
    var BusinessName: String = "",
    var BusinessImage: String  = "",
    var Place: String  = "",
    var ShopContactNumber: String  = "",
    var ShopEmail: String  = "",
    var GSTNo: String  = "",
    var FSSAINo: String? = "",
    var CurrencySymbol: String  = "",
    var InvoicePrefix: String  = "",
    var InvoiceNumber: Int  = 0,
): Serializable{
    fun isContentEquals(other: CompanyDetails): Boolean{
        return this.BusinessName == other.BusinessName && this.BusinessImage == other.BusinessImage && this.Place == other.Place
                && this.ShopContactNumber == other.ShopContactNumber && this.ShopEmail == other.ShopEmail
                && this.GSTNo == other.GSTNo && this.FSSAINo == other.FSSAINo &&
                this.CurrencySymbol == other.CurrencySymbol && this.InvoicePrefix == other.InvoicePrefix &&
                this.InvoiceNumber == other.InvoiceNumber
    }
}