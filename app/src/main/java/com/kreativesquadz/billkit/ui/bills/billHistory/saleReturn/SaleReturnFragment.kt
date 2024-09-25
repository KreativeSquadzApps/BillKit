package com.kreativesquadz.billkit.ui.bills.billHistory.saleReturn

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.adapter.SalesReturnAdapter
import com.kreativesquadz.billkit.databinding.FragmentSaleReturnBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.interfaces.OnItemListListener
import com.kreativesquadz.billkit.interfaces.OnToastShow
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleReturnFragment : Fragment() {
    private val viewModel: SaleReturnViewModel by activityViewModels()
    private var _binding: FragmentSaleReturnBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SalesReturnAdapter<InvoiceItem>
    val returnMode = listOf("CreditNote", "Refund")
    val returnModeOption = listOf("Cash", "Online")
    var isRefund = false
    var total = 0.0
    var finaltotal = 0.0
    var tax = 0.0
    var itemList = mutableListOf<InvoiceItem>()
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchInvoiceItems(invoice!!.id)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleReturnBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        bindings()
        observers()
        setupSpinnerReturnMode(returnMode)
        setupSpinnerReturnModeOption(returnModeOption)
        return binding.root
    }
    fun bindings(){
        binding.invoice = invoice
        binding.returnMode = "Save Credit Note"
        binding.amount =  "${Config.CURRENCY} 0"
        binding.totalGst = "${Config.CURRENCY} 0"
        binding.totalAmount = "${Config.CURRENCY} 0"
    }


   fun observers(){
       val invoice = viewModel.getInvoiceDetails(invoice?.id.toString())
       invoice.observe(viewLifecycleOwner) {
           binding.invoice = it
           Log.e("rrrrrrr",it.toString())

       }
       viewModel.invoiceItems.observe(viewLifecycleOwner){
           setupRecyclerView(it)
       }
   }

    fun onClickListeners(){
        binding.btnAdd.setOnClickListener{

            invoice?.let {
                val creditNote = CreditNote(
                    invoiceId = it.id,
                    invoiceNumber = it.invoiceNumber,
                    createdBy = it.createdBy,
                    dateTime = it.invoiceDate,
                    status = "Active",
                    amount = total,
                    totalAmount = finaltotal,
                    userId = Config.userId,
                    invoiceItems = itemList
                )

                viewModel.generateCreditNote(requireContext(),creditNote,isRefund)
                findNavController().popBackStack()
            }
        }
    }
    private fun setupSpinnerReturnMode(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownReturnMode.setAdapter(adapterStockUnit)
        binding.dropdownReturnMode.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            if (selectedItem.equals("Refund")){
                binding.isRefund = true
                binding.returnMode = "REFUND"
                isRefund = true
            }else{
                binding.isRefund = false
                binding.returnMode = "Save Credit Note"
                isRefund = false

            }
        }
    }

    private fun setupSpinnerReturnModeOption(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownReturnModeOption.setAdapter(adapterStockUnit)
        binding.dropdownReturnModeOption.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)

        }
    }

    private fun setupRecyclerView(list : List<InvoiceItem>?) {
        adapter = SalesReturnAdapter(
            list ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    // Handle item click
                }
            }, object : OnItemListListener<InvoiceItem> {
                override fun onItemList(item: InvoiceItem) {
                    itemList.removeAll { it.id == item.id }

    // Add the new item
    itemList.add(item)

    // Calculate totals (optimized)
     total = 0.0
     tax = 0.0
    for (it in itemList) {
        total += it.unitPrice * it.returnedQty!!
        tax += it.taxRate
    }
     finaltotal = total + tax

    // Update UI (no changes needed here)
    binding.amount = "${Config.CURRENCY} $total"
    binding.totalGst = "${Config.CURRENCY} $tax"
    binding.totalAmount = "${Config.CURRENCY} ${finaltotal}"
                }
            },

             object : OnToastShow {
                override fun showToast(msg: String) {
                    Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
                }

            },R.layout.item_sale_return,
            BR.item
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.isNestedScrollingEnabled = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}