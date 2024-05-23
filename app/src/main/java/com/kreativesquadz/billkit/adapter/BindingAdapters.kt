package com.kreativesquadz.billkit.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("android:text")
fun setDoubleToText(view: TextView, value: Double) {
    view.text = value.toString()
}
@BindingAdapter("android:text")
fun setIntToText(view: TextView, value: Int) {
    view.text = value.toString()
}