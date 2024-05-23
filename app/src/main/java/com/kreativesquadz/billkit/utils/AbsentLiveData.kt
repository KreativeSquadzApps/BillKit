package com.kreativesquadz.billkit.utils

import androidx.lifecycle.LiveData

object AbsentLiveData : LiveData<Any?>() {

    init {
        postValue(null)
    }

    object Factory {
        fun <T> create(): LiveData<T> {
            return AbsentLiveData as LiveData<T>
        }
    }
}