package com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.tabTaxes

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.GST
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.worker.SyncCustomerWorker
import com.kreativesquadz.billkit.worker.SyncGstWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TaxesViewModel @Inject constructor( val repository: GstTaxRepository) : ViewModel() {
    lateinit var gstTax : LiveData<Resource<List<GST>>>
    fun getGstTax(){
        gstTax = repository.loadAllgstTax(Config.userId.toInt())
    }
    fun getTaxList(): LiveData<List<String>> {
        gstTax = repository.loadAllgstTax(Config.userId.toInt())
        return gstTax.map {
            it.data?.map { it.taxAmount.toString()+"%"} ?: emptyList()
        }
    }

    private var _gstStatus = MutableLiveData<Boolean>()
    val gstStatus: LiveData<Boolean> get() = _gstStatus

    fun addGstObj(gst: GST, context: Context) {
        viewModelScope.launch {
            _gstStatus.value = repository.addGST(gst).value
            scheduleGstSync(context)
        }
    }

    fun scheduleGstSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncGstWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncGstWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }



}