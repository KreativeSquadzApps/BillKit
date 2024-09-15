package com.kreativesquadz.billkit.ui.customerManag.customerDetails.tab.creditDetailsFrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.MergedCreditDetail
import com.kreativesquadz.billkit.repository.CreditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditDetailsCustomerViewModel @Inject constructor(private val repository: CreditRepository)  : ViewModel() {
    private val _customerId = MutableLiveData<Long>()
    val customerId: LiveData<Long> get() = _customerId

    private val _mergedCreditDetails = MutableLiveData<List<MergedCreditDetail>>()
    val mergedCreditDetails: LiveData<List<MergedCreditDetail>> get() = _mergedCreditDetails

    fun setCustomerId(id: Long) {
        _customerId.value = id
        fetchMergedCreditDetails(id)
    }

    private fun fetchMergedCreditDetails(id: Long) {
        viewModelScope.launch {
            repository.getMergedCreditDetails(id)
                .onEach { mergedDetails ->
                    _mergedCreditDetails.value = mergedDetails
                }
                .launchIn(viewModelScope)
        }
    }
}

