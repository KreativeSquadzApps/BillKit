package com.kreativesquadz.hisabkitab.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.databinding.DataBindingUtil
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.databinding.DialogCustomAlertBinding
import com.kreativesquadz.hisabkitab.model.DialogData

fun showCustomAlertDialog(
    context: Context,
    dialogData: DialogData,
    positiveAction: () -> Unit,
    negativeAction: () -> Unit
) {
    // Inflate the custom view with DataBinding
    val binding: DialogCustomAlertBinding = DataBindingUtil.inflate(
        (context as Activity).layoutInflater,
        R.layout.dialog_custom_alert,
        null,
        false
    )

    // Bind the dialogData object to the layout
    binding.dialogData = dialogData

    // Create the AlertDialog
    val dialog = AlertDialog.Builder(context)
        .setView(binding.root)
        .setCancelable(false) // Disable dismissal on outside touch
        .create()

    // Handle positive button click
    binding.btnPositive.setOnClickListener {
        positiveAction()
        dialog.dismiss()
    }

    // Handle negative button click
    binding.btnNegative.setOnClickListener {
        negativeAction()
        dialog.dismiss()
    }

    // Show the dialog
    dialog.show()
}
