package com.kreativesquadz.hisabkitab.utils

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