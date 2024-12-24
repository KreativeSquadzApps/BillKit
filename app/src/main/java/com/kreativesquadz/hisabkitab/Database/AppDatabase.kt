package com.kreativesquadz.hisabkitab.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kreativesquadz.hisabkitab.Dao.BillSettingsDao
import com.kreativesquadz.hisabkitab.Dao.CompanyDetailsDao
import com.kreativesquadz.hisabkitab.Dao.CreditDetailsDao
import com.kreativesquadz.hisabkitab.Dao.CreditNoteDao
import com.kreativesquadz.hisabkitab.Dao.CustomerDao
import com.kreativesquadz.hisabkitab.Dao.GSTDao
import com.kreativesquadz.hisabkitab.Dao.InventoryDao
import com.kreativesquadz.hisabkitab.Dao.InvoiceDao
import com.kreativesquadz.hisabkitab.Dao.SavedOrderDao
import com.kreativesquadz.hisabkitab.Dao.StaffDao
import com.kreativesquadz.hisabkitab.Dao.UserDao
import com.kreativesquadz.hisabkitab.Dao.UserSessionDao
import com.kreativesquadz.hisabkitab.Dao.UserSettingDao
import com.kreativesquadz.hisabkitab.model.Category
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.CustomerCreditDetail
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.InvoicePrefixNumber
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.model.SavedOrderEntity
import com.kreativesquadz.hisabkitab.model.Staff
import com.kreativesquadz.hisabkitab.model.User
import com.kreativesquadz.hisabkitab.model.UserSession
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import com.kreativesquadz.hisabkitab.model.settings.POSSettings
import com.kreativesquadz.hisabkitab.model.settings.Packaging
import com.kreativesquadz.hisabkitab.model.settings.PdfSettings
import com.kreativesquadz.hisabkitab.model.settings.TaxSettings
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import kotlin.concurrent.Volatile


@Database(entities = [User::class,Customer::class, Invoice::class ,CompanyDetails::class,InvoiceItem::class,
    UserSetting::class,Category::class, Product::class,CreditNote::class, Staff::class,UserSession::class, GST::class,
    SavedOrderEntity::class, PdfSettings::class,ThermalPrinterSetup::class, CustomerCreditDetail::class, InvoicePrinterSettings::class,
    InvoicePrefixNumber::class, TaxSettings::class, POSSettings::class,Packaging::class], version = 128, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class AppDatabase  : RoomDatabase() {
    companion object{
        private const val DATABASE_NAME = "billKit_database"
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context?): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = databaseBuilder(
                        context!!,
                        AppDatabase::class.java, DATABASE_NAME
                    ).fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }
    }
    abstract fun userSessionDao(): UserSessionDao
    abstract fun userDao(): UserDao
    abstract fun customerDao() : CustomerDao
    abstract fun invoiceDao() : InvoiceDao
    abstract fun companyDetailsDao() : CompanyDetailsDao
    abstract fun inventoryDao() : InventoryDao
    abstract fun userSettingDao(): UserSettingDao
    abstract fun creditNoteDao(): CreditNoteDao
    abstract fun staffDao(): StaffDao
    abstract fun gstDao(): GSTDao
    abstract fun savedOrderDao(): SavedOrderDao
    abstract fun creditDetailsDao(): CreditDetailsDao
    abstract fun billSettingsDao(): BillSettingsDao

}