package com.kreativesquadz.hisabkitab.ui.bills.billHistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapterPagination
import com.kreativesquadz.hisabkitab.adapter.GenericDiffCallback
import com.kreativesquadz.hisabkitab.databinding.FragmentBillHistoryBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.Invoice
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BillHistoryFrag : Fragment() {
    private var _binding: FragmentBillHistoryBinding ?= null
    private val viewModel: BillHistoryViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapterPagination<Invoice>
    private val target by lazy {
        arguments?.getString("target")
    }
    private var createdBy = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = viewModel.getSelectedDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis - 1
        viewModel.fetchAllInvoices(startOfDay, endOfDay)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillHistoryBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observers()
        onClickListener()
        parentFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { _, bundle ->
            val dataUpdated = bundle.getBoolean("dataUpdated", false)
            if (dataUpdated) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = viewModel.getSelectedDate()
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                }
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.timeInMillis - 1
                viewModel.fetchAllInvoices(startOfDay, endOfDay)
            }
        }
    }

    private fun observers(){
        val selectedDate = viewModel.getSelectedDate()
        binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)

        viewModel.loginResponse.observe(viewLifecycleOwner) { userSession->
            createdBy = userSession.sessionUser
        }
        // Observe invoicesList (Resource<List<Invoice>>)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.invoicesList.collect { resource ->
                    Log.d("TAG", " : ${resource.data}")
                    if(resource.data == null)
                    {
                        binding.progressBar.visibility = View.VISIBLE
                    }else{
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.invoices.collectLatest { pagingData ->
                pagingData.let { it1 ->
                    if (target.isNullOrEmpty()){
                        adapter.submitData(it1)

                    }else{
                        adapter.submitData(it1.filter { it.createdBy == target })
                    }

                }

            }
        }



    }

    private fun onClickListener(){
        binding.calenderView.setOnClickListener {
            setCurrentDateOnCalendar(binding.tvDate)
        }
        binding.searchInvoice.setOnClickListener {
            val action = BillHistoryFragDirections.actionBillHistoryFragToSearchBillFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        val diffCallback = GenericDiffCallback<Invoice>(
            areItemsSame = { oldItem, newItem -> oldItem.invoiceId == newItem.invoiceId },
            areContentsSame = { oldItem, newItem -> oldItem == newItem }
        )
        adapter = GenericAdapterPagination(
            object : OnItemClickListener<Invoice> {
                override fun onItemClick(item: Invoice) {
                    Log.e("TAG", "onItemClick: $item")  
                     val action = BillHistoryFragDirections.actionBillHistoryFragToInvoiceFragment(item,Config.BillDetailsFragmentToReceiptFragment)
                    findNavController().navigate(action)
                }
            },
            R.layout.item_bill_invoice_history,
            BR.invoice // Variable ID generated by data binding
            ,diffCallback
        )
        binding.billHistoryRecyclerView.adapter = adapter
        binding.billHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
    }



    private fun setCurrentDateOnCalendar(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog and set initial date to current date
        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date
            val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
            textView.text = selectedDate
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedTimestamp = selectedCalendar.timeInMillis
            viewModel.saveSelectedDate(selectedTimestamp)

            val startOfDay = selectedCalendar.timeInMillis
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = selectedCalendar.timeInMillis - 1
            viewModel.getPagedInvoicesFromDb(startOfDay, endOfDay)
            binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedTimestamp)

        }, year, month, day)
        datePickerDialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


