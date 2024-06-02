package com.kreativesquadz.billkit.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kreativesquadz.billkit.Dao.CompanyDetailsDao
import com.kreativesquadz.billkit.Dao.CustomerDao
import com.kreativesquadz.billkit.Dao.InventoryDao
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Dao.UserDao
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.User
import kotlin.concurrent.Volatile


@Database(entities = [Customer::class, Invoice::class ,CompanyDetails::class,InvoiceItem::class, User::class,Category::class], version = 32, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class AppDatabase  : RoomDatabase() {
    companion object{
        private val DATABASE_NAME = "billKit_database"
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

    abstract fun customerDao() : CustomerDao
    abstract fun invoiceDao() : InvoiceDao
    abstract fun companyDetailsDao() : CompanyDetailsDao

    abstract fun inventoryDao() : InventoryDao
    abstract fun userDao(): UserDao


}