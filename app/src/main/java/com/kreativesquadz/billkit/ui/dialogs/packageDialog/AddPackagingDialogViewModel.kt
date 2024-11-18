package com.kreativesquadz.billkit.ui.dialogs.packageDialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat

class AddPackagingDialogViewModel : ViewModel() {
    val dialogText = MutableLiveData<String>()
    val packagingText = MutableLiveData<String>()
    val isApplied = MutableLiveData<Boolean>(false)
    private val _dismissDialog = MutableLiveData<Boolean>()
    val dismissDialog: LiveData<Boolean> get() = _dismissDialog
    private var isUpdating = false
    val df = DecimalFormat("#")

    fun getPackagingsText() {
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
