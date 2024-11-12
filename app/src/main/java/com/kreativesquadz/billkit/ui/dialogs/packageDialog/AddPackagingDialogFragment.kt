package com.kreativesquadz.billkit.ui.dialogs.packageDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.customdialog.BaseDialogFragment
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.DialogFragmentAddGstBinding
import com.kreativesquadz.billkit.databinding.DialogFragmentAddPackagingBinding
import com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.tabTaxes.TaxesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class AddPackagingDialogFragment : BaseDialogFragment<DialogFragmentAddPackagingBinding>() {

    private val viewModel: AddPackagingDialogViewModel by activityViewModels()
    val df = DecimalFormat("#")

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddPackagingBinding {
        return DialogFragmentAddPackagingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        // Observe the dismissDialog LiveData
        viewModel.dismissDialog.observe(this) { shouldDismiss ->
            if (shouldDismiss) {
                dismiss()
                viewModel.onDismissHandled() // Reset the event after handling it
            }
        }

        viewModel.dialogText.observe(viewLifecycleOwner) { packageAmount ->
            handleDialogTextChange(packageAmount)
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

    private fun handleDialogTextChange(packageAmount: String) {
       df.roundingMode = RoundingMode.DOWN
        if (packageAmount.isNotEmpty()) {
            viewModel.packagingText.value = df.format(packageAmount.toInt())
        }
    }

    companion object {
        const val TAG = "AddPackagingDialogFragment"
    }


}