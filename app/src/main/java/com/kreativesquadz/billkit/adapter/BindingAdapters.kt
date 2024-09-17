package com.kreativesquadz.billkit.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.paging.Config
import com.kreativesquadz.billkit.R
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
    val splitText = text.split('(').get(0)
    view.text = splitText
}
@BindingAdapter("returnedQty", "unitPrice")
fun setFormattedPrice(textView: TextView, returnedQty: Number, unitPrice: Number) {
    val total = returnedQty.toDouble() * unitPrice.toDouble()
    textView.text = String.format("%.2f", total)
}
@BindingAdapter("hideOnReturned")
fun hideOnReturned(view: TextView, text: String?) {
    if (text != null) {
        if (text == "Returned") {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}
@BindingAdapter("hideOnReturned2")
fun hideOnReturned2(view: TextView, text: String?) {
    if (text != null) {
        if (text == "Returned") {
        view.visibility =   View.VISIBLE
    } else {
        view.visibility =   View.GONE
    }
   }
}@BindingAdapter("hideOnReturnedCancel")
fun hideOnReturnedCancel(view: TextView, text: String?) {
    if (text != null) {
        if (text == "Cancelled") {
        view.visibility =   View.VISIBLE
    } else {
        view.visibility =   View.GONE
    }
    }
}
@BindingAdapter("hideOnZero")
fun hideOnZero(view: TextView, text: Int?) {
    if (text != null ) {
        if (text > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }else{
        view.visibility = View.GONE
    }
}

@BindingAdapter("isPriceStringEmpty")
fun isPriceStringEmpty(view: TextView, price: String?) {
    if (price.isNullOrEmpty()) {
        view.text = "${com.kreativesquadz.billkit.Config.CURRENCY} 0.0"
    } else {
        view.text = "${com.kreativesquadz.billkit.Config.CURRENCY} $price"
    }
}
@BindingAdapter("isActiveText")
fun isActiveText(view: TextView, status: String?) {
    if (status.equals("Active", ignoreCase = true)) {
        view.text = "Deactivate"
    } else {
        view.text = "Active"
    }
}
@BindingAdapter("isActiveVisible")
fun isActiveVisible(view: LinearLayout, status: String?) {
    if (status.equals("Active", ignoreCase = true)) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("rate")
fun rate(view: TextView, text: String) {
    // This splits the text at '0' and joins it back together with spaces
    val unitPrice = text.split(' ').get(0)
    val taxRate = text.split(' ').get(1)

    val  finalRate = unitPrice.toDouble() - unitPrice.toDouble() * taxRate.toDouble() / 100

    view.text = finalRate.toString()
}
@BindingAdapter("hideOnZeroDouble")
fun hideOnZeroDouble(view: TextView, text: Double?) {
    if (text != null) {
        if (text > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}
@BindingAdapter("hideView")
fun hideView(view: View, text: String?) {
    if (text != null) {
        if (text == "Returned" || text == "Cancelled" ) {
            view.visibility =   View.GONE
        } else {
            view.visibility =   View.VISIBLE
        }
    }

}@BindingAdapter("hideViewCancel")
fun hideViewCancel(view: View, text: String?) {
    if (text != null) {
        if (text == "Cancelled" ) {
            view.visibility =   View.GONE
        } else {
            view.visibility =   View.VISIBLE
        }
    }

}

@BindingAdapter("isSelected")
fun isSelected(textView: TextView, isSelected: Boolean?) {
    val context = textView.context
    val color = if (isSelected == true) {
        context.getColor(R.color.white) // Use your selected color resource
    } else {
        context.getColor(R.color.text_color_heading) // Use your default color resource
    }
    textView.setTextColor(color)
}

@BindingAdapter("isManual")
fun isManual(textView: TextView, manual: String) {
    val context = textView.context
    val color = if (manual.equals("Cash", ignoreCase = true)) {
        context.getColor(R.color.green) // Use your selected color resource
    }
    else if (manual.equals("Online", ignoreCase = true)) {
        context.getColor(R.color.green) // Use your selected color resource
    }
    else if (manual.equals("Waive Off", ignoreCase = true)) {
        context.getColor(R.color.green) // Use your selected color resource
    }
    else {
        context.getColor(R.color.red) // Use your default color resource
    }
    textView.setTextColor(color)
}
@BindingAdapter("isActive")
fun isActive(textView: CardView, manual: String) {
    val context = textView.context
    val color = if (manual.equals("Active", ignoreCase = true)) {
        context.getColor(R.color.green) // Use your selected color resource
    }
    else {
        context.getColor(R.color.red) // Use your default color resource
    }
    textView.setCardBackgroundColor(color)
}

@BindingAdapter("cardBackgroundColorSelected")
fun cardBackgroundColorSelected(cardView: androidx.cardview.widget.CardView, isSelected: Boolean?) {
    val context = cardView.context
    val color = if (isSelected == true) {
        context.getColor(R.color.colorPrimary) // Use your primary color resource
    } else {
        context.getColor(R.color.lite_grey_200) // Use your lite grey color resource
    }
    cardView.setCardBackgroundColor(color)
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
@BindingAdapter("timestampToDateTimeTxt")
fun timestampToDateTimeTxt(view: TextView, timestamp: String?) {
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
@BindingAdapter("timestampToTime")
fun timestampToTime(view: TextView, timestamp: String?) {
    if (timestamp != null) {
        try {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("HH:mm a", Locale.getDefault())
            view.text = "Time   " + format.format(date)
        } catch (e: Exception) {
            view.text = ""
        }
    } else {
        view.text = ""
    }
}
@BindingAdapter("currentDate")
fun currentDate(view: TextView, timestamp: String?) {

    val currentDateTime = Date()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(currentDateTime)
    view.text = formattedDate

}@BindingAdapter("dateStamp")
fun dateStamp(view: TextView, timestamp: String?) {
    if (timestamp != null) {
        try {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            view.text = format.format(date)
        } catch (e: Exception) {
            view.text = ""
        }
    } else {
        view.text = ""
    }

}
@BindingAdapter("timestampToDate")
fun timestampToDate(view: TextView, timestamp: String?) {
    if (timestamp != null) {
        try {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            view.text = "Date   "+ format.format(date)
        } catch (e: Exception) {
            view.text = ""
        }
    } else {
        view.text = ""
    }


}