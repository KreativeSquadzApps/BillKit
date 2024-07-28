package com.kreativesquadz.billkit.Database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.Staff
import java.io.Serializable

class DataConverters : Serializable {

    @TypeConverter
    fun fromInvoiceItemList(invoiceItemList: List<InvoiceItem>): String {
        return Gson().toJson(invoiceItemList)
    }

    @TypeConverter
    fun toInvoiceItemList(json: String): List<InvoiceItem> {
        val type = object : TypeToken<List<InvoiceItem>>() {}.type
        return Gson().fromJson(json, type)
    }
    @TypeConverter
    fun fromInvoiceItem(invoiceItem: InvoiceItem): String {
        return Gson().toJson(invoiceItem)
    }

    @TypeConverter
    fun toInvoiceItem(json: String): InvoiceItem {
        return Gson().fromJson(json, InvoiceItem::class.java)
    }
    @TypeConverter
    fun fromDoubleToString(value: Double): String {
        return value.toString()
    }

    @TypeConverter
    fun fromStringToDouble(value: String): Double {
        return value.toDouble()
    }



    @TypeConverter
    fun fromIntToBoolean(value: Int): Boolean {
        return value != 0
    }

    @TypeConverter
    fun fromBooleanToInt(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun fromListToString(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun fromStringToArrayList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromStaffToString(staff: Staff): String {
        return Gson().toJson(staff)
    }

    @TypeConverter
    fun fromStringToStaff(staff: String): Staff {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(staff, type)
    }



}