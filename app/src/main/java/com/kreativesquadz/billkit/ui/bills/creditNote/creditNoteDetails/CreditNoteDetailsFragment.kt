package com.kreativesquadz.billkit.ui.bills.creditNote.creditNoteDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentCreditNoteDetailsBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.InvoiceItem

class CreditNoteDetailsFragment : Fragment() {
    var _binding: FragmentCreditNoteDetailsBinding? = null
    val binding get() = _binding!!
    private val viewModel: CreditNoteDetailsViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<InvoiceItem>
    val invoiceId by lazy {
        arguments?.getString("invoiceId")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCreditNote(invoiceId!!.toLong())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditNoteDetailsBinding.inflate(inflater, container, false)
        observers()
        onClicklisteners()
        return binding.root
    }

    fun observers(){
        viewModel.creditNote.observe(viewLifecycleOwner){
            it.let {
                binding.creditNote = it
            }
        }
        viewModel.itemsList.observe(viewLifecycleOwner){
            it?.let {
                val list = it.filter { it.returnedQty!! > 0 }
                setupRecyclerView(list)
                binding.totalItem = list.size.toString()
            }

        }
    }

    fun onClicklisteners(){

    }

    private fun setupRecyclerView(list : List<InvoiceItem>?) {
        adapter = GenericAdapter(
            list ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    // Handle item click
                }
            },
            R.layout.item_invoice_item_credit_note,
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