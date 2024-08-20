package com.kreativesquadz.billkit.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

@Entity(tableName = "pdf_settings")
data class PdfSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = Config.userId,
    val pdfCompanyInfo: String = "0 1 1 0",
    val pdfItemTable: String = "0 0 0 0",
    val pdfFooter: String = "Thank You",
    val isSynced: Int = 0
) : Serializable {

    fun isContentEqual(other: PdfSettings): Boolean {
        return this.pdfCompanyInfo == other.pdfCompanyInfo &&
                this.pdfItemTable == other.pdfItemTable &&
                this.pdfFooter == other.pdfFooter
    }
}
