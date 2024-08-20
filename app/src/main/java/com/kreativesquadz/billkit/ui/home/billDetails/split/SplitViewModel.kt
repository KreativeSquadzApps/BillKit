package com.kreativesquadz.billkit.ui.home.billDetails.split

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitViewModel @Inject constructor() : ViewModel() {


    private val _cashAmount = MutableLiveData<Double>()
    val cashAmount: LiveData<Double> get() = _cashAmount

    private val _onlineAmount = MutableLiveData<Double>()
    val onlineAmount: LiveData<Double> get() = _onlineAmount

    private val _totalAmount = MutableLiveData<Double>()
    val totalAmount: LiveData<Double> get() = _totalAmount

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> get() = _toastMessage

    val actualAmount = MutableLiveData<Double>() // This should be set to the total invoice amount

    init {
        _cashAmount.value = 0.0
        _onlineAmount.value = 0.0
    }

    // Call this method when cash amount changes
    fun setCashAmount(amount: Double) {
        _cashAmount.value = amount
        validateAndCalculateTotal()
    }

    // Call this method when online amount changes
    fun setOnlineAmount(amount: Double) {
        _onlineAmount.value = amount
        validateAndCalculateTotal()
    }

    private fun validateAndCalculateTotal() {
        val cash = _cashAmount.value ?: 0.0
        val online = _onlineAmount.value ?: 0.0
        val total = cash + online
        val actual = actualAmount.value ?: 0.0

        if (total > actual) {
            _toastMessage.value = "Amount cannot be greater than total amount"
        } else {
            _totalAmount.value = actual - total
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }



}