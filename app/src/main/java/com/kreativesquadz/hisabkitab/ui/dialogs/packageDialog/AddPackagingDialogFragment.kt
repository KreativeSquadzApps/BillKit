package com.kreativesquadz.hisabkitab.ui.dialogs.packageDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.customdialog.BaseDialogFragment
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericSpinnerAdapter
import com.kreativesquadz.hisabkitab.databinding.DialogFragmentAddPackagingBinding
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.billSettings.BillSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class AddPackagingDialogFragment : BaseDialogFragment<DialogFragmentAddPackagingBinding>() {

    private val viewModel: AddPackagingDialogViewModel by activityViewModels()
    private val billSettingsViewModel: BillSettingsViewModel by activityViewModels()
    private var selectedPackaging : String ?= null
    val df = DecimalFormat("#")

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentAddPackagingBinding {
        return DialogFragmentAddPackagingBinding.inflate(inflater, container, false)
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

//        viewModel.dialogText.observe(viewLifecycleOwner) { packageAmount ->
//            handleDialogTextChange(packageAmount)
//        }
        binding.btnUpdate.setOnClickListener {
            if (selectedPackaging != null){
                viewModel.packagingText.value = selectedPackaging
                viewModel.getPackagingsText()
                dismiss()
            }else{
                Toast.makeText(requireContext(), "Please select a packaging", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

//    private fun handleDialogTextChange(packageAmount: String) {
//       df.roundingMode = RoundingMode.DOWN
//        if (packageAmount.isNotEmpty()) {
//            viewModel.packagingText.value = df.format(packageAmount.toInt())
//        }
//    }

    companion object {
        const val TAG = "AddPackagingDialogFragment"
    }

    private fun setupSpinner() {
        val adapter  = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            liveDataItems = billSettingsViewModel.getPackagingListLivedata()
        )
        binding.dropdownPackaging.setAdapter(adapter)
        binding.dropdownPackaging.setOnItemClickListener { _, _, position, _ ->
            selectedPackaging = adapter.getItem(position).toString()
        }
    }
}
