package com.kreativesquadz.hisabkitab.ui.settings.menuItems.billSettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.settings.Packaging
import com.kreativesquadz.hisabkitab.repository.BillSettingsRepository
import com.kreativesquadz.hisabkitab.worker.DeletePackagingWorker
import com.kreativesquadz.hisabkitab.worker.SyncPackagingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillSettingsViewModel @Inject constructor(val repository: BillSettingsRepository,
                                                val workManager: WorkManager)
    : ViewModel() {
    lateinit var packagingList : LiveData<Resource<List<Packaging>>>
    private var _packagingStatus = MutableLiveData<Boolean>()
    val packagingStatus: LiveData<Boolean> get() = _packagingStatus

    fun getPackagingList(){
        packagingList = repository.loadAllPackagingList(Config.userId.toInt())
    }
    fun getPackagingListLivedata(): LiveData<List<String>> {
        packagingList = repository.loadAllPackagingList(Config.userId.toInt())
        return packagingList.map {
            it.data?.map { it.packagingAmount.toString()} ?: emptyList()
        }
    }
    fun addPackagingObj(packaging: Packaging) {
        viewModelScope.launch {
            _packagingStatus.value = repository.addPackaging(packaging).value
            schedulePackagingSync()
        }
    }

    private fun schedulePackagingSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPackagingWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "SyncPackagingWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    fun deletePackaging(id: Int) {
        viewModelScope.launch {
            repository.deletePackagingById(id)
            syncDeletePackagingWorker(id)
        }
    }

    private fun syncDeletePackagingWorker(id: Int) {
        val data = Data.Builder()
            .putInt("id", id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeletePackagingWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "syncDeletePackagingWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

}