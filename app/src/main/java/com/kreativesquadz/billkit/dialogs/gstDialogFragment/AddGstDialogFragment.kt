package com.kreativesquadz.billkit.dialogs.gstDialogFragment

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
import com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.tabTaxes.TaxesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class AddGstDialogFragment : BaseDialogFragment<DialogFragmentAddGstBinding>() {

    private val viewModel: AddGstDialogViewModel by activityViewModels()
    private val taxesViewModel: TaxesViewModel by activityViewModels()
    val df = DecimalFormat("#")
    private var selectedGst : String ?= null


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddGstBinding {
        return DialogFragmentAddGstBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setupSpinner()
        // Observe the dismissDialog LiveData
        viewModel.dismissDialog.observe(this) { shouldDismiss ->
            if (shouldDismiss) {
                dismiss()
                viewModel.onDismissHandled() // Reset the event after handling it
            }
        }

        viewModel.totalAmountLivedata.observe(viewLifecycleOwner){
            binding.totalAmountText.text = "Add Gst $it"
            binding.totalAmount.text = it
        }
        viewModel.dialogText.observe(viewLifecycleOwner) { gstAmount ->
            handleDialogTextChange(gstAmount)
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

    private fun handleDialogTextChange(gstAmount: String) {
       df.roundingMode = RoundingMode.DOWN
        val actualAmount = viewModel.totalAmountLivedata.value?.replace(Config.CURRENCY, "")?.trim()?.toDoubleOrNull() ?: return

        if (gstAmount.isNotEmpty()) {
            val gst = gstAmount.toDoubleOrNull() ?: return
            if (gst > actualAmount) {
                Toast.makeText(requireContext(), "Tax amount cannot be greater than total amount", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedGst !=null){
                    val newTotal = gst/100 * selectedGst!!.replace("%", "").trim().toDouble()
                    binding.totalAmount.text = Config.CURRENCY+df.format(newTotal)
                    viewModel.gstText.value = df.format(newTotal)
                }else{
                    Toast.makeText(requireContext(), "Please select a tax", Toast.LENGTH_SHORT).show()
                    binding.etProductMrp.setText("")
                }

            }
        } else {
            binding.totalAmount.text = Config.CURRENCY +df.format(actualAmount)
        }
    }

    companion object {
        const val TAG = "AddGstDialogFragment"
    }

    private fun setupSpinner() {
        val adapter  = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            liveDataItems = taxesViewModel.getTaxList()
        )
        binding.dropdownTax.setAdapter(adapter)
        binding.dropdownTax.setOnItemClickListener { _, _, position, _ ->
             selectedGst = adapter.getItem(position).toString()

        }
    }

}
