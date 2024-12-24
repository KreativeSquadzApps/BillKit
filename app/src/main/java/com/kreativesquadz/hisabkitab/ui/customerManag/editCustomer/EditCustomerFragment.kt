package com.kreativesquadz.hisabkitab.ui.customerManag.editCustomer

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.hisabkitab.databinding.FragmentEditCustomerBinding
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.CustomerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditCustomerFragment : Fragment() {
    private var _binding: FragmentEditCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditCustomerViewModel by viewModels()
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCustomerBinding.inflate(inflater , container,false)
        onClickListeners()
        observers()
        return binding.root
    }

    private fun onClickListeners(){
        binding.btnUpdate.setOnClickListener {
            if (binding.etShopContactNumber.text.toString().isNotEmpty()){
                viewModel.addCustomerObj(getCustomerData(),requireContext())

            }else{
                Toast.makeText(requireContext(),"Please enter contact number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCustomerData(): Customer? {
        customer?.let {
            return Customer(it.id,binding.etCustomerName.text.toString(),
                binding.etShopContactNumber.text.toString(),
                binding.etGSTNo.text.toString(),
                it.totalSales,
                binding.etAddress.text.toString(),
                it.creditAmount,
                binding.etCustomerMark.text.toString()
                , binding.etCustomerOther.text.toString() ,it.isSynced)
        }
        return null
    }

    private fun observers(){
        sharedViewModel.customer.observe(viewLifecycleOwner) { customer ->
            binding.customer = customer
            this.customer = customer
        }
        viewModel.customerStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Customer Updated successfully", Toast.LENGTH_SHORT)
                    .show()
                customer?.let {
                    sharedViewModel.setCustomer(it.id.toString())
                }
                findNavController().popBackStack()
            }

        }

    }
}