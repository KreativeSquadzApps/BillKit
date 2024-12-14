package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.settings.UserSetting
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import com.kreativesquadz.billkit.utils.prepareFilePart
import com.kreativesquadz.billkit.worker.AddCompanyDetailsWorker
import com.kreativesquadz.billkit.worker.SyncUserSettingWorker
import com.kreativesquadz.billkit.worker.UpdateCompanyDetailsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class TabInvoiceViewModel @Inject constructor( val workManager: WorkManager,
                                               val settingsRepository: SettingsRepository,
                                              val userSettingRepository: UserSettingRepository)
    : ViewModel() {
    private val _updateCompanyDetailsStatus = MutableLiveData<Boolean>()
    private val _userSetting = MutableLiveData<UserSetting>()
    var userSetting: LiveData<UserSetting> = _userSetting
    lateinit var companyDetails: LiveData<Resource<CompanyDetails>>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _isUploading = MutableLiveData<Boolean>()
    val isUploading: LiveData<Boolean> get() = _isUploading

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: MutableLiveData<Uri?> = _selectedImageUri

    fun uploadImage(userId: String, imageUri: Uri?, context: Context, companyDetails: CompanyDetails) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadStatus.value = try {
                val userIdPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())
                val imagePart = imageUri?.let { prepareFilePart("image", it, context) }

                val result = settingsRepository.uploadCompanyImage(userIdPart, imagePart)
                val invoiceId = result.getOrNull()?.invoiceId

                if (invoiceId == 200) {
                    val message = result.getOrNull()?.message ?: throw Exception("Missing success message")
                    companyDetails.BusinessImage = message
                    settingsRepository.update(companyDetails)
                    getCompanyDetailsTab()
                    true
                } else {
                    throw Exception("Image upload failed with: $invoiceId")
                }
            } catch (e: Exception) {
                Log.e("UploadImage", "Error uploading image", e) // Log the error for debugging
                false // Upload failed
            } finally {
                _isUploading.value = false
            }
        }
    }


    fun getCompanyDetailsTab(): LiveData<Resource<CompanyDetails>> {
        companyDetails = settingsRepository.loadCompanyDetails(Config.userId)
        return companyDetails
    }


    private fun syncCompanyDetails(companyDetails: CompanyDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settingsRepository.insertCompanyDetails(companyDetails)
                addCompanyDetailsSync()
            } catch (e: Exception) {
                // Handle any errors, possibly using retry mechanisms
            }
        }
    }

    fun updateCompanyDetailsSettings(companyDetails: CompanyDetails) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Update the repository with the new settings
                settingsRepository.update(companyDetails)
                updateCompanyDetailsSync()
               // getCompanyDetailsTab()
                _updateCompanyDetailsStatus.value = true
            } catch (e: Exception) {
                // Handle any errors that might occur
                // You could log the error or notify the user
            } finally {
                // Ensure the loading state is set to false even if an error occurs
                _isLoading.value = false
                _updateCompanyDetailsStatus.value = false
            }
        }
    }

    fun updateCompanyDetailsDb(companyDetails: CompanyDetails) {
        viewModelScope.launch {
            settingsRepository.update(companyDetails)
        }
    }

    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }

    fun updateDiscount(context: Context,userSetting: UserSetting){
        userSettingRepository.insert(userSetting)
            //scheduleSettingSync(context)
    }
    fun isCompanyDetailsUpdated(oldSettings: CompanyDetails, newSettings: CompanyDetails): Boolean {
        return !oldSettings.isContentEquals(newSettings)
    }


    private fun scheduleSettingSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncUserSettingWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "SyncUserSettingWorker",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }

    private fun updateCompanyDetailsSync() {
      val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

      val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateCompanyDetailsWorker>()
            .setConstraints(constraints)
            .build()

      workManager.enqueueUniqueWork(
            "updateSettingSync",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }

    private fun addCompanyDetailsSync() {
      val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

      val syncWorkRequest = OneTimeWorkRequestBuilder<AddCompanyDetailsWorker>()
            .setConstraints(constraints)
            .build()

      workManager.enqueueUniqueWork(
            "addCompanyDetailsSync",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }





    fun init(activityResultLauncher: ActivityResultLauncher<Intent>, permissionLauncher: ActivityResultLauncher<String>) {
        this.activityResultLauncher = activityResultLauncher
        this.permissionLauncher = permissionLauncher
    }

    fun onImageViewClick() {
        // Request permission if needed
        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }

    fun setImageUri(uri: Uri?) {
        if (uri != null) {
            _selectedImageUri.value = uri
        }
    }

    fun removeImageUri() {
        _selectedImageUri.value = null
    }


}