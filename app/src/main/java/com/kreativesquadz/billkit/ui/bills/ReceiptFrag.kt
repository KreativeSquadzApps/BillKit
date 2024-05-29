package com.kreativesquadz.billkit.ui.bills

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.databinding.FragmentReceiptBinding
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptFrag : Fragment() {
    var _binding: FragmentReceiptBinding? = null
    val binding get() = _binding!!
    private val viewModel: ReceiptViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    val invoiceId by lazy {
        arguments?.getString("invoiceId")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
      val invoice= viewModel.getInvoiceDetails(invoiceId!!)
        invoice.observe(viewLifecycleOwner) {
             println("Invoice : $it")
        }
        viewModel.getCompanyDetails().observe(viewLifecycleOwner){
            println("Company Details : $it")
        }


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}