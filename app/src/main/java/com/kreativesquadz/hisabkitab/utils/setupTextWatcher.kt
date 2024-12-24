package com.kreativesquadz.hisabkitab.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.kreativesquadz.hisabkitab.interfaces.OnTextChangedCallback

fun setupTextWatcher(
    editText: EditText,
    callback: OnTextChangedCallback
) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            callback.onTextChanged()
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}