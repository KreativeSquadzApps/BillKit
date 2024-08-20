package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.model.settings.PdfSettings

@Dao
interface UserSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userSetting: UserSetting)

    @Query("SELECT * FROM users_settings WHERE userId = :userId")
     fun getUserById(userId: Long): LiveData<UserSetting>


     @Query("SELECT * FROM pdf_settings WHERE userId = :userId")
     fun getUserPdfSettingsById(userId: Long): PdfSettings?

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertPdfSettings(pdfSettings: PdfSettings) : Long



    @Query("UPDATE pdf_settings SET pdfCompanyInfo = :pdfCompanyInfo, pdfItemTable = :pdfItemTable,pdfFooter = :pdfFooter WHERE userId = :userId")
    suspend fun updatePdfSettings(userId: Long, pdfCompanyInfo: String, pdfItemTable: String, pdfFooter: String)
}
