package com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.tab.creditDetailsFrag

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentCreditDetailsCustomerBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.MergedCreditDetail
import com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.CustomerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreditDetailsCustomerFragment : Fragment() {
    private var _binding: FragmentCreditDetailsCustomerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<MergedCreditDetail>
    private val viewModel: CreditDetailsCustomerViewModel by viewModels()
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditDetailsCustomerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun observers() {
        sharedViewModel.customer.observe(viewLifecycleOwner) { customer ->
            customer?.let {
                viewModel.setCustomerId(customer.id)
            }
        }

        viewModel.mergedCreditDetails.observe(viewLifecycleOwner) { mergedDetails ->
            adapter.submitList(mergedDetails)  // Update the adapter's list
        }
    }


    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            items = emptyList(),  // Initially empty, will be updated via Flow
            listener = object : OnItemClickListener<MergedCreditDetail> {
                override fun onItemClick(item: MergedCreditDetail) {

                    // Handle item click here
                }
            },
            layoutResId = R.layout.item_merged_credit_detail,  // Use your item layout
            bindVariableId = BR.mergedCreditDetail  // Bind to the variable in the layout
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun onClickListeners() {
    }

}