package com.kreativesquadz.hisabkitab.ui.bottomSheet.customerReceiveCredit

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.databinding.FragmentCustomerReceiveCreditBottomSheetBinding
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.CustomerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerReceiveCreditBottomSheetFrag : BottomSheetDialogFragment() {
    var _binding: FragmentCustomerReceiveCreditBottomSheetBinding? = null
    val binding get() = _binding!!
    val viewModel: CustomerReceiveCreditBottomSheetViewModel by activityViewModels()
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private val customerCredit: Customer? by lazy {
        sharedViewModel.customer.value
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // Get the display height
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels

            // Set the height of the BottomSheet to be half of the screen height
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (screenHeight * 0.5).toInt() // Set to half of the screen height
            bottomSheet.layoutParams = layoutParams

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_top)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerReceiveCreditBottomSheetBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListeners()
        observers()
    }

    private fun onClickListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
        binding.btnCash.setOnClickListener {
            if (binding.etCreditAmount.text.toString().isNotEmpty()){
                customerCredit?.let {
                    viewModel.updateCreditAmount(requireContext(),it.id,binding.etCreditAmount.text.toString().toDouble(),"Cash")
                    sharedViewModel.setCustomer(it.id.toString())

                }
            }else{
                binding.etCreditAmount.error = "Please enter credit amount"
                binding.etCreditAmount.requestFocus()
            }
            dismiss()
        }
        binding.btnOnline.setOnClickListener {
            if (binding.etCreditAmount.text.toString().isNotEmpty()){
                customerCredit?.let {
                    viewModel.updateCreditAmount(requireContext(),it.id,binding.etCreditAmount.text.toString().toDouble(),"Online")
                    sharedViewModel.setCustomer(it.id.toString())

                }
            }else{
                binding.etCreditAmount.error = "Please enter credit amount"
                binding.etCreditAmount.requestFocus()
            }
            dismiss()
        }

        binding.btnWaveOf.setOnClickListener {
            if (binding.etCreditAmount.text.toString().isNotEmpty()){
                customerCredit?.let {
                    viewModel.updateCreditAmount(requireContext(),it.id,binding.etCreditAmount.text.toString().toDouble(),"Waive Off")
                    sharedViewModel.setCustomer(it.id.toString())

                }
            }else{
                binding.etCreditAmount.error = "Please enter credit amount"
                binding.etCreditAmount.requestFocus()
            }
            dismiss()
        }

    }

    private fun observers(){
        sharedViewModel.customer.observe(viewLifecycleOwner) { customer ->
            binding.customer = customer
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}