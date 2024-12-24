package com.kreativesquadz.hisabkitab.ui.dialogs.otherChargesDialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat

class AddOtherChargesDialogViewModel : ViewModel() {
    val dialogText = MutableLiveData<String>()
    val otherChargesText = MutableLiveData<String>()
    val isApplied = MutableLiveData<Boolean>(false)
    private val _dismissDialog = MutableLiveData<Boolean>()
    val dismissDialog: LiveData<Boolean> get() = _dismissDialog
    private var isUpdating = false
    val df = DecimalFormat("#")

    fun getOtherChargeText() {
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

    }
    fun onDismissHandled() {
        _dismissDialog.value = false // Reset the event after it has been handled
    }

}
