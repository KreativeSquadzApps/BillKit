package com.kreativesquadz.billkit.ui.dialogs.otherChargesDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.example.customdialog.BaseDialogFragment
import com.kreativesquadz.billkit.databinding.DialogFragmentAddOtherChargesBinding
import com.kreativesquadz.billkit.databinding.DialogFragmentAddPackagingBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class AddOtherChargesDialogFragment : BaseDialogFragment<DialogFragmentAddOtherChargesBinding>() {

    private val viewModel: AddOtherChargesDialogViewModel by activityViewModels()
    val df = DecimalFormat("#")

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddOtherChargesBinding {
        return DialogFragmentAddOtherChargesBinding.inflate(inflater, container, false)
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

    private fun handleDialogTextChange(otherChargesAmount: String) {
       df.roundingMode = RoundingMode.DOWN
        if (otherChargesAmount.isNotEmpty()) {
            viewModel.otherChargesText.value = df.format(otherChargesAmount.toInt())
        }
    }

    companion object {
        const val TAG = "AddOtherChargesDialogFragment"
    }


}
