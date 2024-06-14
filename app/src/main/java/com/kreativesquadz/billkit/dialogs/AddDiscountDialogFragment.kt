package com.kreativesquadz.billkit.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.customdialog.BaseDialogFragment
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.databinding.DialogFragmentAddDiscountBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat

class AddDiscountDialogFragment : BaseDialogFragment<DialogFragmentAddDiscountBinding>() {

    private val viewModel: DialogViewModel by activityViewModels()
    val df = DecimalFormat("#")


    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddDiscountBinding {
        return DialogFragmentAddDiscountBinding.inflate(inflater, container, false)
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

        viewModel.totalAmountLivedata.observe(viewLifecycleOwner){
            binding.totalAmountText.text = "Total Amount  $it"
            binding.totalAmount.text = it
        }
        viewModel.dialogText.observe(viewLifecycleOwner) { discountAmount ->
            handleDialogTextChange(discountAmount)
        }

        viewModel.percentage.observe(viewLifecycleOwner) { discountPercentage ->
            handlePercentageChange(discountPercentage)
        }

       // setupDebouncedListeners()
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

    private fun handleDialogTextChange(discountAmount: String) {
       df.roundingMode = RoundingMode.DOWN
        val actualAmount = viewModel.totalAmountLivedata.value?.replace(Config.CURRENCY, "")?.trim()?.toDoubleOrNull() ?: return

        if (discountAmount.isNotEmpty()) {
            val discount = discountAmount.toDoubleOrNull() ?: return
            if (discount > actualAmount) {
                Toast.makeText(requireContext(), "Discount amount cannot be greater than total amount", Toast.LENGTH_SHORT).show()
            } else {
                val newTotal = actualAmount - discount
                binding.totalAmount.text = Config.CURRENCY+df.format(newTotal)
            }
        } else {
            binding.totalAmount.text = Config.CURRENCY +df.format(actualAmount)
        }
    }

    private fun handlePercentageChange(discountPercentage: String) {
        df.roundingMode = RoundingMode.DOWN
        val actualAmount = viewModel.totalAmountLivedata.value?.replace(Config.CURRENCY, "")?.trim()?.toDoubleOrNull() ?: return
        if (discountPercentage.isNotEmpty()) {
            val percentage = discountPercentage.toDoubleOrNull() ?: return
            if (percentage > 100) {
                Toast.makeText(requireContext(), "Discount percentage cannot be greater than 100", Toast.LENGTH_SHORT).show()
            } else {
                val discountAmount = actualAmount * (percentage / 100)
                val newTotal = actualAmount - discountAmount
                binding.totalAmount.text = Config.CURRENCY+df.format(newTotal)
                viewModel.dialogText.value = df.format(discountAmount)
            }
        } else {
            binding.totalAmount.text = Config.CURRENCY+df.format(actualAmount)
            viewModel.dialogText.value = ""
        }
    }
    companion object {
        const val TAG = "CustomDialogFragment1"
    }


}
