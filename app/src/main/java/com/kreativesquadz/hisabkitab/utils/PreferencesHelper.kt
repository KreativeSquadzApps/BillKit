package com.kreativesquadz.hisabkitab.utils

import android.content.Context

class PreferencesHelper(context: Context) {
    private val preferences = context.getSharedPreferences("InvoicePreferences", Context.MODE_PRIVATE)

    fun saveSelectedDate(timestamp: Long) {
        preferences.edit().putLong("selected_date", timestamp).apply()
    }

    fun getSelectedDate(): Long {
        return preferences.getLong("selected_date", System.currentTimeMillis())
    }
    fun removeSelectedDate() {
        preferences.edit().remove("selected_date").apply()
    }

     fun saveSelectedDateCreditNote(timestamp: Long) {
        preferences.edit().putLong("selected_date_crreditnote", timestamp).apply()
    }

    fun getSelectedDateCreditNote(): Long {
        return preferences.getLong("selected_date_crreditnote", System.currentTimeMillis())
    }

    fun removeSelectedDateCreditNote() {
        preferences.edit().remove("selected_date_crreditnote").apply()
    }
}