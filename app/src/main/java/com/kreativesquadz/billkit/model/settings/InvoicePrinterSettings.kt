package com.kreativesquadz.billkit.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

@Entity(tableName = "invoice_printer_settings")
data class InvoicePrinterSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = Config.userId,
    val printerCompanyInfo: String = "0 1 1 0",
    val printerItemTable: String = "1 0 1 1",
    val printerFooter: String = "Thank You",
    val isSynced: Int = 0
) : Serializable {

    fun isContentEqual(other: InvoicePrinterSettings): Boolean {
        return this.printerCompanyInfo == other.printerCompanyInfo &&
                this.printerItemTable == other.printerItemTable &&
                this.printerFooter == other.printerFooter
    }
}
