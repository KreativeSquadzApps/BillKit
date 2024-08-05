package com.kreativesquadz.billkit.dialogs.savedOrderDialogFrag

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
import com.kreativesquadz.billkit.databinding.DialogFragmentSavedOrderBinding
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.tabTaxes.TaxesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class SavedOrderDialogFragment : BaseDialogFragment<DialogFragmentSavedOrderBinding>() {

    private val viewModel: SavedOrderDialogViewModel by activityViewModels()
    private val taxesViewModel: TaxesViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    val df = DecimalFormat("#")
    private var selectedGst : String ?= null


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentSavedOrderBinding {
        return DialogFragmentSavedOrderBinding.inflate(inflater, container, false)
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

        binding.saveOrder.setOnClickListener {
            val totalSum = sharedViewModel.getItemsList().sumOf { it.totalPrice }
            var orderName = "---"
            if(binding.etOrderName.text.toString().isNotEmpty()){
                orderName = binding.etOrderName.text.toString()
            }
            val savedOrder = SavedOrder(200, orderName, sharedViewModel.getItemsList(), totalSum.toString().toDouble(), System.currentTimeMillis())
            viewModel.saveOrder(savedOrder,sharedViewModel.getItemsList())
            //sharedViewModel.clearOrder()
            dismiss()
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }


    companion object {
        const val TAG = "SavedOrderDialogFragment"
    }


}
