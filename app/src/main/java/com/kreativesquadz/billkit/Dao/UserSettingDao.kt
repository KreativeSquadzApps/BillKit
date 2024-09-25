package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.model.settings.InvoicePrinterSettings
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup

@Dao
interface UserSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userSetting: UserSetting)

    @Query("SELECT * FROM users_settings WHERE userId = :userId")
     fun getUserById(userId: Long): LiveData<UserSetting>


    @Query("SELECT * FROM users_settings WHERE userId = :userId")
     fun getUserSettingById(userId: Long): UserSetting





     @Query("SELECT * FROM pdf_settings WHERE userId = :userId")
     fun getUserPdfSettingsById(userId: Long): PdfSettings?

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertPdfSettings(pdfSettings: PdfSettings) : Long

    @Query("UPDATE pdf_settings SET pdfCompanyInfo = :pdfCompanyInfo, pdfItemTable = :pdfItemTable,pdfFooter = :pdfFooter WHERE userId = :userId")
    suspend fun updatePdfSettings(userId: Long, pdfCompanyInfo: String, pdfItemTable: String, pdfFooter: String)



     @Query("SELECT * FROM invoice_printer_settings WHERE userId = :userId")
     fun getInvoicePrinterSettingsById(userId: Long): InvoicePrinterSettings?

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoicePrinterSettings(invoicePrinterSettings: InvoicePrinterSettings) : Long

    @Query("UPDATE invoice_printer_settings SET printerCompanyInfo = :printerCompanyInfo, printerItemTable = :printerItemTable,printerFooter = :printerFooter WHERE userId = :userId")
    suspend fun updateInvoicePrinterSettings(userId: Long, printerCompanyInfo: String, printerItemTable: String, printerFooter: String)





     @Query("SELECT * FROM thermal_printer_setup WHERE userId = :userId")
     fun getUserPrinterSettingsById(userId: Long): ThermalPrinterSetup?

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertPrinterSettings(thermalPrinterSetup: ThermalPrinterSetup) : Long



    @Query("UPDATE thermal_printer_setup SET printerSize = :printerSize, printerMode = :printerMode, fontSize = :fontSize, enableAutoPrint = :enableAutoPrint, openCashDrawer = :openCashDrawer, disconnectAfterPrint = :disconnectAfterPrint, autoCutAfterPrint = :autoCutAfterPrint, defaultPrinterAddress = :defaultPrinterAddress , defaultPrinterName = :defaultPrinterName WHERE userId = :userId")
    suspend fun updatePrinterSettings(userId: Long, printerSize : String, printerMode : String, fontSize : String, enableAutoPrint : Boolean, openCashDrawer : Boolean, disconnectAfterPrint : Boolean, autoCutAfterPrint : Boolean, defaultPrinterAddress : String, defaultPrinterName : String)



}
