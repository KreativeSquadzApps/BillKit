package com.kreativesquadz.hisabkitab
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.InvoicePrefixNumber
import com.kreativesquadz.hisabkitab.model.LoginResponse
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.GstTaxRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.LoginRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                                val inventoryRepository: InventoryRepository,
                                                val billHistoryRepository: BillHistoryRepository,
                                                val customerManagRepository: CustomerManagRepository,
                                                val gstTaxRepository: GstTaxRepository,
                                                val loginRepository: LoginRepository
)
  : ViewModel() {

    lateinit var companyDetailsMain : LiveData<Resource<CompanyDetails>>
    val _loginResponse = MutableLiveData<LoginResponse>()
     val loginResponse : LiveData<LoginResponse> = _loginResponse
    private val _invoicePrefixNumberList = MutableLiveData<Resource<List<InvoicePrefixNumber>>>()
    var invoicePrefixNumberList: LiveData<Resource<List<InvoicePrefixNumber>>> = _invoicePrefixNumberList
    lateinit var companyDetails: LiveData<Resource<CompanyDetails>>
    lateinit var gstTax : LiveData<Resource<List<GST>>>
    lateinit var customer : LiveData<Resource<List<Customer>>>



    fun getSesssion() {
           val loginSession = loginRepository.getUserSessions()
            if (loginSession != null){
                if (loginSession.staffId != null){
                    _loginResponse.value = loginRepository.getSession(null, loginSession.staffId.toLong())
                }else if (loginSession.userId != null){
                    _loginResponse.value = loginRepository.getSession(loginSession.userId, null)
                }
                companyDetails = settingsRepository.loadCompanyDetails(Config.userId)
                invoicePrefixNumberList = settingsRepository.loadInvoicePrefixNumberList(Config.userId)
                gstTax = gstTaxRepository.loadAllgstTax(Config.userId.toInt())
                customer = customerManagRepository.loadAllCustomers()

            }else{
                _loginResponse.value = loginRepository.getSession(null, null)
            }


    }
  }