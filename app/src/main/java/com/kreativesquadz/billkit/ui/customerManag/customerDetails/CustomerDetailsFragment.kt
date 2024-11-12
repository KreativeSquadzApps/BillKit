package com.kreativesquadz.billkit.ui.customerManag.customerDetails

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericTabAdapter
import com.kreativesquadz.billkit.adapter.showCustomAlertDialog
import com.kreativesquadz.billkit.databinding.FragmentCustomerDetailsBinding
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.DialogData
import com.kreativesquadz.billkit.ui.bottomSheet.customerReceiveCredit.CustomerReceiveCreditBottomSheetFrag
import com.kreativesquadz.billkit.ui.customerManag.customerDetails.tab.billFrag.BillsFragment
import com.kreativesquadz.billkit.ui.customerManag.customerDetails.tab.creditDetailsFrag.CreditDetailsCustomerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerDetailsFragment : Fragment() {

    private var _binding: FragmentCustomerDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerDetailsViewModel by viewModels()
    private var customerId: String? = null

    // Use a SharedViewModel for communication between fragments
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private var customer  : Customer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve customerId from arguments

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListeners()
        setupTabs()
        observers()
    }

    private fun observers() {
        sharedViewModel.customer.observe(viewLifecycleOwner) { customer ->
            binding.customer = customer
            this.customer = customer
            this.customer?.creditAmount?.let {
                if (it == "0" || it.isEmpty() || it == "0.0"){
                    binding.ivDelete.alpha = 1.0f
                    binding.ivDelete.clearColorFilter()
                    binding.ivDelete.isEnabled = true
                    binding.ivDelete.isClickable = true
                }else{
                    binding.ivDelete.alpha = 0.5f
                    binding.ivDelete.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
                    binding.ivDelete.isEnabled = false
                    binding.ivDelete.isClickable = false
                }
            }
        }
    }

    private fun onClickListeners() {
        binding.btnAddCredit.setOnClickListener {
            customer?.let {
                val action = CustomerDetailsFragmentDirections.actionCustomerDetailsFragmentToAddCreditFragment(it)
                findNavController().navigate(action)
            }
        }
        binding.btnReceiveCredit.setOnClickListener {
            customer?.let {
                val customerReceiveCreditBottomSheetFrag = CustomerReceiveCreditBottomSheetFrag()
                customerReceiveCreditBottomSheetFrag.show(parentFragmentManager, customerReceiveCreditBottomSheetFrag.tag)
            }
    }
        binding.ivEdit.setOnClickListener {
            customer?.let {
                val action = CustomerDetailsFragmentDirections.actionCustomerDetailsFragmentToEditCustomerFragment()
                findNavController().navigate(action)
            }
        }

        binding.ivDelete.setOnClickListener {
            customer?.let {
                setupPopup(it.customerName){
                    deleteCustomer(it)
                }

            }
        }
    }

    private fun deleteCustomer(it : Customer) {
        viewModel.deleteCustomer(requireContext(),it.id)
        findNavController().popBackStack()
    }

    private fun setupPopup(name : String ,action: () -> Unit){
        val dialogData = DialogData(
            title = "Delete Customer",
            info = "Are you sure you want to Delete ${name} Customer ?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel"
        )

        showCustomAlertDialog(
            context = requireActivity(),
            dialogData = dialogData,
            positiveAction = {
                action()
            },
            negativeAction = {
                // Handle negative button action
                // E.g., dismiss the dialog
            }
        )
    }

    private fun setupTabs() {
        val fragments = listOf(BillsFragment(), CreditDetailsCustomerFragment())
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            tabText.text = when (position) {
                0 -> "Bills"
                1 -> "Credit Details"
                else -> ""
            }
            tab.customView = tabView
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
