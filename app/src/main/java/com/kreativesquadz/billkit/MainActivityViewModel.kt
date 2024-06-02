package com.kreativesquadz.billkit
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.User
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(val settingsRepository: SettingsRepository)
  : ViewModel() {

    lateinit var companyDetailsMain : LiveData<Resource<CompanyDetails>>


//     fun addUser()  {
//        viewModelScope.launch {
//            val user = User("1".toLong(),"Admin","admin@gmail.com","Admin")
//            userRepository.addUser(user)
//        }
//         getUser()
//     }
//    fun getUser() {
//        viewModelScope.launch {
//            val user = userRepository.getUserById("1".toLong())
//            println(user)
//        }
//        addInventory()
//
//    }
//
//    fun addInventory() {
//        val category = Category(userId = "1".toLong(), categoryName = "Shoes")
//        viewModelScope.launch {
//            inventoryRepository.addCategory(category)
//        }
//        getInventory()
//    }
//
//    fun getInventory() {
//        viewModelScope.launch {
//            val category = inventoryRepository.getCategoriesForUser("1".toLong())
//            println(category)
//        }
//    }

    fun getCompanyDetails() : LiveData<Resource<CompanyDetails>> {
        viewModelScope.launch {
            companyDetailsMain = settingsRepository.loadCompanyDetails(Config.userId)
        }
        return companyDetailsMain
    }

}