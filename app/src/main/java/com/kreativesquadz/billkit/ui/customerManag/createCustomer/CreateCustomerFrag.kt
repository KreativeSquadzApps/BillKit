package com.kreativesquadz.billkit.ui.customerManag.createCustomer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.databinding.FragmentCreateCustomerBinding
import com.kreativesquadz.billkit.model.Customer

class CreateCustomerFrag : Fragment() {
    var _binding: FragmentCreateCustomerBinding? = null
    val binding get() = _binding!!
    private val viewModel: CreateCustomerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCustomerBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.resetValue = ""
        observers()
        onClickListeners()
        return binding.root
    }

    private fun onClickListeners(){
        binding.btnSubmit.setOnClickListener {
            if (!binding.etShopContactNumber.text.toString().isEmpty()){
                viewModel.addCustomerObj(getCustomerData(),requireContext())
            }else{
                Toast.makeText(requireContext(),"Please enter contact number",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCustomerData(): Customer {
        return Customer(0,binding.etCustomerName.text.toString(),
                        binding.etShopContactNumber.text.toString(),
                        binding.etGSTNo.text.toString(),
                        binding.etTotalSales.text.toString(),
                        binding.etAddress.text.toString(),
                        binding.etCreditAmount.text.toString(),0)
    }




    private fun observers(){
        viewModel.customerStatus.observe(viewLifecycleOwner) {
            if (it) {
                binding.resetValue = ""
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}