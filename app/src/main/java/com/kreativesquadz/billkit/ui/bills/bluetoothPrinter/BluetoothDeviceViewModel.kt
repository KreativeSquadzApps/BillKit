package com.kreativesquadz.billkit.ui.bills.bluetoothPrinter

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.BluetoothRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothDeviceViewModel @Inject constructor(private val repository: BluetoothRepository,
                                                   val settingsRepository: SettingsRepository,
                                                   val billHistoryRepository: BillHistoryRepository,
                                                   val customerManagRepository: CustomerManagRepository
) : ViewModel() {

    val pairedDevices = MutableLiveData<List<BluetoothDevice>>()
    val isConnected = MutableLiveData<Boolean>()
    val printStatus = MutableLiveData<String>()
    val connectionError = MutableLiveData<String>()
    val connectingDevice = MutableLiveData<BluetoothDevice?>()
    lateinit var companyDetails : LiveData<Resource<CompanyDetails>>
    private val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems

    fun scanDevices() {
        viewModelScope.launch {
            try {
                pairedDevices.postValue(repository.getPairedDevices())
            } catch (e: SecurityException) {
                connectionError.postValue(e.message)
            }
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            connectingDevice.postValue(device)
            try {
                val result = repository.connectToDevice(device)
                isConnected.postValue(result)
                connectingDevice.postValue(null)
            } catch (e: SecurityException) {
                connectionError.postValue(e.message)
                connectingDevice.postValue(null)
            }
        }
    }

    fun printData(data: String) {
        viewModelScope.launch {
            try {
                repository.printData(data)
                printStatus.postValue("Print Successful")
            } catch (e: SecurityException) {
                printStatus.postValue(e.message)
            }
        }
    }

    fun getCompanyDetailsRec(): LiveData<Resource<CompanyDetails>> {
        companyDetails = settingsRepository.loadCompanyDetails(Config.userId)
        return companyDetails
    }

    fun getCustomerById(id: String) : Customer {
        return customerManagRepository.getCustomer(id)
    }

    fun fetchInvoiceItems(invoiceId: Long) = viewModelScope.launch {
        try {
            val items = billHistoryRepository.getInvoiceItems(invoiceId)
            _invoiceItems.postValue(items)
        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }




    override fun onCleared() {
        super.onCleared()
        repository.closeConnection()
    }
}
