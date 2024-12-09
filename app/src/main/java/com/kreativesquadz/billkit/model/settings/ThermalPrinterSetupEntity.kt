package com.kreativesquadz.billkit.model.settings

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.billkit.Config
import java.io.Serializable

@Entity(tableName = "thermal_printer_setup")
data class ThermalPrinterSetup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val printerSize: String = "80MM",
    val printerMode: String = "CONFIG-1",
    val fontSize: String = "Medium",
    val enableAutoPrint: Boolean = false,
    val openCashDrawer: Boolean = false,
    val disconnectAfterPrint: Boolean = false,
    val autoCutAfterPrint: Boolean = true,
    val defaultPrinterName: String = "Default",
    var defaultPrinterAddress: String = "",
    val userId: Long = Config.userId,
    val isSynced: Int = 0
) : Serializable {

    fun isContentEqual(other: ThermalPrinterSetup): Boolean {
        return this.printerSize == other.printerSize &&
                this.printerMode == other.printerMode &&
                this.fontSize == other.fontSize &&
                this.enableAutoPrint == other.enableAutoPrint &&
                this.openCashDrawer == other.openCashDrawer &&
                this.disconnectAfterPrint == other.disconnectAfterPrint &&
                this.autoCutAfterPrint == other.autoCutAfterPrint }
}