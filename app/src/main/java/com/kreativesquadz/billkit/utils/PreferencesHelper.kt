package com.kreativesquadz.billkit.utils

import android.content.Context

class PreferencesHelper(context: Context) {
    private val preferences = context.getSharedPreferences("InvoicePreferences", Context.MODE_PRIVATE)

    fun saveSelectedDate(timestamp: Long) {
        preferences.edit().putLong("selected_date", timestamp).apply()
    }

    fun getSelectedDate(): Long {
        return preferences.getLong("selected_date", System.currentTimeMillis())
    }

     fun saveSelectedDateCreditNote(timestamp: Long) {
        preferences.edit().putLong("selected_date_crreditnote", timestamp).apply()
    }

    fun getSelectedDateCreditNote(): Long {
        return preferences.getLong("selected_date_crreditnote", System.currentTimeMillis())
    }


}
