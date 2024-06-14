// BackPressHandler.kt

package com.kreativesquadz.billkit.utils

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

fun Fragment.addBackPressHandler(
    lifecycleOwner: LifecycleOwner,
    shouldAllowBack: () -> Boolean,

) {
    requireActivity().onBackPressedDispatcher.addCallback(lifecycleOwner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (shouldAllowBack()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    })
}
