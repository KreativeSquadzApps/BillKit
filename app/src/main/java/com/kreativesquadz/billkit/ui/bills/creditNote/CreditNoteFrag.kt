package com.kreativesquadz.billkit.ui.bills.creditNote

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentCreditNoteBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditNoteFrag : Fragment() {
     var _bindings : FragmentCreditNoteBinding?=null
     val binding get() =  _bindings!!
    private val viewModel: CreditNoteViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<CreditNote>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCreditNotes()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindings = FragmentCreditNoteBinding.inflate(inflater,container,false)
        setupRecyclerView()
        observers()
        return binding.root
    }


   fun observers(){
        viewModel.creditNoteList.observe(viewLifecycleOwner){
            it.data?.let {
                adapter.submitList(it)
            }
        }

   }
    private fun setupRecyclerView() {
        adapter = GenericAdapter(
              emptyList(),
            object : OnItemClickListener<CreditNote> {
                override fun onItemClick(item: CreditNote) {
                        Log.e("CreditNote" , item.toString())
                    // Handle item click
                }
            },
            R.layout.item_credit_note,
            BR.creditNote // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }


}