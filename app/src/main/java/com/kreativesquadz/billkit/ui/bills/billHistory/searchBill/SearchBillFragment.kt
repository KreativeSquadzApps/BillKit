package com.kreativesquadz.billkit.ui.bills.billHistory.searchBill

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentSearchBillBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Invoice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchBillFragment : Fragment() {
    private var _binding: FragmentSearchBillBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchBillViewModel by viewModels()
    private lateinit var adapter: GenericAdapter<Invoice>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBillBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        setupRecyclerView()
        return binding.root
    }

    private fun onClickListeners() {
        binding.btnSearch.setOnClickListener{
            viewModel.searchBill(binding.etInvoice.text.toString())
        }
    }

    private fun observers() {
        viewModel.invoice.observe(viewLifecycleOwner) {
            if (it != null) {
                val mutableList = mutableListOf<Invoice>()
                mutableList.add(it)
                adapter.submitList(mutableList)
                Log.e("invoice",it.toString())
            }

        }
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            emptyList(),
            object : OnItemClickListener<Invoice> {
                override fun onItemClick(item: Invoice) {
                    val action = SearchBillFragmentDirections.actionSearchBillFragmentToInvoiceFragment(item, Config.BillDetailsFragmentToReceiptFragment)
                    findNavController().navigate(action)
                }
            },
            R.layout.item_bill_invoice_history,
            BR.invoice
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}