package com.kreativesquadz.billkit.ui.dialogs.savedOrderDialogFrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class SavedOrderDialogViewModel  @Inject constructor(val savedOrderRepository: SavedOrderRepository) : ViewModel() {
    val dialogText = MutableLiveData<String>()

    val gstText = MutableLiveData<String>()
    val percentage = MutableLiveData<String>()
    val totalAmountLivedata = MutableLiveData<String>()
    val isApplied = MutableLiveData<Boolean>(false)
    private val _dismissDialog = MutableLiveData<Boolean>()
    val dismissDialog: LiveData<Boolean> get() = _dismissDialog
    private var isUpdating = false
    val df = DecimalFormat("#")

    fun getDiscountedText() {
        if (dialogText.value.isNullOrEmpty()){
            _dismissDialog.value = true
            return
        }
        isApplied.value = true
        _dismissDialog.value = true
    }
    fun onRemoveClicked() {
        isApplied.value = false
        dialogText.value = ""
        percentage.value = ""

    }
    fun onDismissHandled() {
        _dismissDialog.value = false // Reset the event after it has been handled
    }


    fun setTotalAmount(totalAmount: String){
        totalAmountLivedata.value = totalAmount
    }

    fun saveOrder(savedOrder: SavedOrder, item: List<InvoiceItem>) {
        viewModelScope.launch {
            savedOrderRepository.saveOrder(savedOrder,item)
        }
    }



}
