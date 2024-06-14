package com.kreativesquadz.billkit.adapter

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@BindingAdapter("android:text")
fun setDoubleToText(view: TextView, value: Double) {
    view.text = value.toString()

}
@BindingAdapter("android:text")
fun setIntToText(view: TextView, value: Int) {
    view.text = value.toString()
}
@BindingAdapter("splitText")
fun splitText(view: TextView, text: String) {
    // This splits the text at '0' and joins it back together with spaces
    val splitText = text.split(' ').get(0)
    view.text = splitText
}
@BindingAdapter("splitText2")
fun splitText2(view: TextView, text: String) {
    // This splits the text at '0' and joins it back together with spaces
    if (text == "Returned") {
        view.visibility =   View.VISIBLE
    } else {
        view.visibility =   View.GONE
    }
}

@BindingAdapter("timestampToDateTime")
fun timestampToDateTime(view: TextView, timestamp: String?) {
    if (timestamp != null) {
        try {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.getDefault())
            view.text = format.format(date)
        } catch (e: Exception) {
            view.text = ""
        }
    } else {
        view.text = ""
    }


}