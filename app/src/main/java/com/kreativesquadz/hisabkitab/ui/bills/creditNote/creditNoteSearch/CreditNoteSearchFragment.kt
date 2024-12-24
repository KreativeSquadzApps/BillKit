package com.kreativesquadz.hisabkitab.ui.bills.creditNote.creditNoteSearch

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentCreditNoteSearchBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.CreditNote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditNoteSearchFragment : Fragment() {
    private var _binding: FragmentCreditNoteSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreditNoteSearchViewModel by viewModels()
    private lateinit var adapter: GenericAdapter<CreditNote>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getcreditNoteList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditNoteSearchBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        setupRecyclerView()
        return binding.root
    }

    private fun onClickListeners() {
        binding.etCreditNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.creditNoteList.value?.data?.let {
                    adapter.submitList(it.filter { creditNote ->
                        creditNote.invoiceNumber.contains(s.toString(), ignoreCase = true)})
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observers() {
        viewModel.creditNoteList.observe(viewLifecycleOwner){
            it.data?.let {
                adapter.submitList(it.sortedByDescending{ it.dateTime })
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
             emptyList(),
            object : OnItemClickListener<CreditNote> {
                override fun onItemClick(item: CreditNote) {
                }
            },
            R.layout.item_credit_note,
            BR.creditNote
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

}