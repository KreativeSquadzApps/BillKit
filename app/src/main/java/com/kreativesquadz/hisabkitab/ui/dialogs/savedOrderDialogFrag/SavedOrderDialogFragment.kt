package com.kreativesquadz.hisabkitab.ui.dialogs.savedOrderDialogFrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.example.customdialog.BaseDialogFragment
import com.kreativesquadz.hisabkitab.databinding.DialogFragmentSavedOrderBinding
import com.kreativesquadz.hisabkitab.model.SavedOrder
import com.kreativesquadz.hisabkitab.ui.home.tab.SharedViewModel
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings.tab.tabTaxes.TaxesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class SavedOrderDialogFragment : BaseDialogFragment<DialogFragmentSavedOrderBinding>() {

    private val viewModel: SavedOrderDialogViewModel by activityViewModels()
    private val taxesViewModel: TaxesViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    val df = DecimalFormat("#")


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
            sharedViewModel.clearItemsList()
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
