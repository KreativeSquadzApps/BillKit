package com.kreativesquadz.billkit.ui.home.billDetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentBillDetailsBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.ui.home.customerBottomSheet.CustomerAddBottomSheetFrag
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BillDetailsFrag : Fragment() {
    private val viewModel: BillDetailsViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding : FragmentBillDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<InvoiceItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillDetailsBinding.inflate(inflater, container, false)
        binding.sharedViewModel = sharedViewModel
        binding.isCustomerSelected = sharedViewModel.isCustomerSelected.value
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun onClickListeners(){
        binding.btnCash.setOnClickListener {
        viewModel.addInvoice(sharedViewModel.getInvoice(),requireContext())
        }

        binding.addCustomer.setOnClickListener {
            val customerAddBottomSheetFrag = CustomerAddBottomSheetFrag()
            customerAddBottomSheetFrag.show(parentFragmentManager, "CustomerAddBottomSheetFrag")
        }

        binding.ivDeselectCustomer.setOnClickListener {
            sharedViewModel.updateDeselectCustomer()
        }

    }


    private fun observers(){
        sharedViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        viewModel.invoiceApiStatus.observe(viewLifecycleOwner) {
            if(it == true){
                Log.e("Idddddd", viewModel.invoiceId.value.toString())
                val action = BillDetailsFragDirections.actionBillDetailsFragToReceiptFrag(viewModel.invoiceId.value.toString())
                findNavController().navigate(action)
            }
        }

        sharedViewModel.isCustomerSelected.observe(viewLifecycleOwner) { isCustomerSelected ->
            binding.isCustomerSelected = isCustomerSelected
        }

        sharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            binding.customer = customer
        }
    }


    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            sharedViewModel.items.value ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    // Handle item click
                }
            },
            R.layout.item_invoice_details_bill,
            BR.item // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}