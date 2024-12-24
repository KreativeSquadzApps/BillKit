package com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.addCreditFrag

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.hisabkitab.databinding.FragmentAddCreditBinding
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.CustomerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCreditFragment : Fragment() {
    private var _binding: FragmentAddCreditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddCreditViewModel by viewModels()
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private var customerCredit: Customer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding  = FragmentAddCreditBinding.inflate(inflater, container, false)
        observers()
        onCLickListeners()
        return binding.root
    }


    private fun observers() {
        sharedViewModel.customer.observe(viewLifecycleOwner) { customer ->
            customer?.let {
                binding.customer = it
                customerCredit = it
            }
        }
    }

    private fun onCLickListeners() {
        binding.btnAddCredit.setOnClickListener {
            if (binding.etCreditAmount.text.toString().isNotEmpty()){
                customerCredit?.let {
                    viewModel.updateCreditAmount(it.id,binding.etCreditAmount.text.toString().toDouble(),"")
                    sharedViewModel.setCustomer(it.id.toString())
                    findNavController().popBackStack()

                }
            }else{
                binding.etCreditAmount.error = "Please enter credit amount"
                binding.etCreditAmount.requestFocus()
            }


        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}