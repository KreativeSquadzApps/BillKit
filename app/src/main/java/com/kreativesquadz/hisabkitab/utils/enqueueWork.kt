package com.kreativesquadz.hisabkitab.utils

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker

inline fun <reified T : Worker> enqueueWork(
    workManager: WorkManager,
    workName: String,
    inputData: Data,
    existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.APPEND,
    networkType: NetworkType = NetworkType.CONNECTED
) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(networkType)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<T>() // No need to pass the class type
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()

    workManager.enqueueUniqueWork(workName, existingWorkPolicy, syncWorkRequest)
}

