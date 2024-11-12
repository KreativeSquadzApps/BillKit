package com.kreativesquadz.billkit.ui.daybook

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.paging.map
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.bluetooth.AndroidBluetoothConnection
import com.kreativesquadz.billkit.bluetooth.ConnectionError
import com.kreativesquadz.billkit.databinding.FragmentDayBookBinding
import com.kreativesquadz.billkit.model.Invoice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DayBookFrag : Fragment() {
    private var _binding: FragmentDayBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DayBookViewModel by viewModels()
    private var selectedDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = viewModel.getSelectedDate()
        val selectedCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate // Use selectedDate here
            set(Calendar.HOUR_OF_DAY, 0) // Set time to midnight (start of the day)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = selectedCalendar.timeInMillis // Start of the selected day
        selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = selectedCalendar.timeInMillis - 1
        viewModel.getInvoices(startOfDay, endOfDay)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayBookBinding.inflate(inflater, container, false)
        binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)

        observers()
        onClickListener()
        return binding.root
    }
    private fun observers(){
        viewModel.invoices.observe(viewLifecycleOwner) {
            var totalSales = 0.0
            var totalSalesCredit = 0.0
            var totalSalesCreditCount = 0
            var totalSalesCash = 0.0
            var totalSalesOnline = 0.0
            it.forEach { invoice ->
                totalSales += invoice.totalAmount
                totalSalesCredit += invoice.creditAmount ?: 0.0
                if (invoice.creditAmount != null && invoice.creditAmount != 0.0) {
                    totalSalesCreditCount++
                }
                totalSalesCash += invoice.cashAmount ?: 0.0
                totalSalesOnline += invoice.onlineAmount ?: 0.0
                setInvoiceTable(invoice)
            }
            binding.tvSalesCredit.text = Config.CURRENCY +" "+totalSalesCredit
            binding.tvSalesCreditCount.text = totalSalesCreditCount.toString()

            binding.tvTotalSales.text = Config.CURRENCY +" "+totalSales
            binding.tvTotalSalesCount.text = it.size.toString()

            binding.tvTotalPaymentIn.text = Config.CURRENCY +" "+ totalSalesCash.plus(totalSalesOnline)
            binding.tvTotalCashIn.text = totalSalesCash.toString()
            binding.tvTotalOnlineIn.text = totalSalesOnline.toString()

        }


    }

    private fun setInvoiceTable(invoice :Invoice){
        val tableRow = TableRow(requireContext())
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        val textViewParams = TableRow.LayoutParams(
            0, // width will be set by weight
            TableRow.LayoutParams.MATCH_PARENT
        ).apply {
            weight = 1f // Each TextView takes equal space
        }
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color_main)
        val textSize = resources.getDimension(R.dimen.txt_14sp)
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.table_border)
        val backgroundLast = ContextCompat.getDrawable(requireContext(), R.drawable.table_border_green)
        fun createTextView(text: String): TextView {
            return TextView(requireContext()).apply {
                layoutParams = textViewParams
                this.text = text
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setBackgroundDrawable(background)
                gravity = Gravity.CENTER
            }
        }
        fun createTextViewLast(text: String): TextView {
            return TextView(requireContext()).apply {
                layoutParams = textViewParams
                this.text = text
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setBackgroundDrawable(backgroundLast)
                gravity = Gravity.CENTER
            }
        }
        var customerName = "--"
        if (invoice.customerId != null) {
            customerName = viewModel.getCustomerById(invoice.customerId.toString()).customerName

        }
        tableRow.addView(createTextView("Sale"))
        tableRow.addView(createTextView(invoice.invoiceNumber.toString()))
        tableRow.addView(createTextView(invoice.createdBy.replace("Created By","")))
        tableRow.addView(createTextView(customerName))
        tableRow.addView(createTextView(invoice.totalAmount.toString()))
        tableRow.addView(createTextViewLast(invoice.cashAmount.toString()))
        binding.invoiceTableLayout.addView(tableRow)
    }

    private fun onClickListener(){
        binding.calenderView.setOnClickListener {
            setCurrentDateOnCalendar(binding.tvDate)
        }
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
            for (i in binding.invoiceTableLayout.childCount - 1 downTo 1) {
                binding.invoiceTableLayout.removeViewAt(i)
            }
            val startOfDay = selectedCalendar.timeInMillis
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = selectedCalendar.timeInMillis - 1
            viewModel.getInvoices(startOfDay, endOfDay)
            binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedTimestamp)

        }, year, month, day)
        datePickerDialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}