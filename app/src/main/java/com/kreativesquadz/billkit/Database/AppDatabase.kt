package com.kreativesquadz.billkit.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kreativesquadz.billkit.Dao.CompanyDetailsDao
import com.kreativesquadz.billkit.Dao.CreditDetailsDao
import com.kreativesquadz.billkit.Dao.CreditNoteDao
import com.kreativesquadz.billkit.Dao.CustomerDao
import com.kreativesquadz.billkit.Dao.GSTDao
import com.kreativesquadz.billkit.Dao.InventoryDao
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Dao.SavedOrderDao
import com.kreativesquadz.billkit.Dao.StaffDao
import com.kreativesquadz.billkit.Dao.UserDao
import com.kreativesquadz.billkit.Dao.UserSessionDao
import com.kreativesquadz.billkit.Dao.UserSettingDao
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.CustomerCreditDetail
import com.kreativesquadz.billkit.model.GST
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.InvoicePrefixNumber
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.model.SavedOrderEntity
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.model.User
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.model.settings.InvoicePrinterSettings
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import kotlin.concurrent.Volatile


@Database(entities = [User::class,Customer::class, Invoice::class ,CompanyDetails::class,InvoiceItem::class,
    UserSetting::class,Category::class, Product::class,CreditNote::class, Staff::class,UserSession::class,GST::class,
    SavedOrderEntity::class, PdfSettings::class,ThermalPrinterSetup::class, CustomerCreditDetail::class, InvoicePrinterSettings::class,
    InvoicePrefixNumber::class], version = 97, exportSchema = false)
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

}