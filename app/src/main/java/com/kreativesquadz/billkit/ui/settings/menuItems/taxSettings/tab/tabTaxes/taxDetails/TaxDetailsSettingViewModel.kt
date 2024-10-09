package com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.tabTaxes.taxDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.worker.DeleteGstWorker
import com.kreativesquadz.billkit.worker.UpdateCompanyDetailsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxDetailsSettingViewModel @Inject constructor(val workManager: WorkManager ,
                                                     val inventoryRepository: InventoryRepository,
                                                     val gstTaxRepository: GstTaxRepository) : ViewModel() {
    private val _productTax = MutableLiveData<Double>()

    val productsForUserByTax: LiveData<List<Product>> = _productTax.switchMap { tax ->
        inventoryRepository.getProductsForUserByTax(tax)
    }

    fun setProductTax(tax: Double) {
        _productTax.value = tax
    }

    fun deleteGST(id: Int) {
        viewModelScope.launch {
            gstTaxRepository.deleteGSTById(id)
            deleteGstTax(id)
        }
    }

    private fun deleteGstTax(id: Int) {
        val data = Data.Builder()
        .putInt("id", id)
        .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteGstWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "deleteGstTax",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

}