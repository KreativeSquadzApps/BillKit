package com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.taxQuickSell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.settings.TaxOption
import com.kreativesquadz.billkit.model.settings.TaxSettings
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaxForQuickSellViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                              val gstTaxRepository: GstTaxRepository
) : ViewModel() {
    val gstTaxList: LiveData<Resource<List<GST>>> = gstTaxRepository.loadAllgstTax(Config.userId.toInt())
    val taxSettings: LiveData<TaxSettings> = gstTaxRepository.getTaxSettings()

    fun initializeTaxSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val taxSettings = gstTaxRepository.getTaxSettingsObj()
            if (taxSettings == null) {
                // Insert default tax settings if no settings exist
                val defaultTaxSettings = TaxSettings(
                    defaultTaxOption = TaxOption.ExemptTax, // Default value
                    selectedTaxPercentage = 0.0f // Default percentage
                )
                gstTaxRepository.saveTaxSettings(defaultTaxSettings)
            }
        }
    }
    fun setDefaultTaxOption(taxOption: TaxOption,tax : Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTaxSettings = TaxSettings(
                defaultTaxOption = taxOption,
                selectedTaxPercentage = tax ?: 0.0f
            )
            gstTaxRepository.saveTaxSettings(newTaxSettings)
        }
    }

}