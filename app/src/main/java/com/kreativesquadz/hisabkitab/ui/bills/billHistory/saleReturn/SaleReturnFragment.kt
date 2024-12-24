package com.kreativesquadz.hisabkitab.ui.bills.billHistory.saleReturn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericSpinnerAdapter
import com.kreativesquadz.hisabkitab.adapter.SalesReturnAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentSaleReturnBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.interfaces.OnItemListListener
import com.kreativesquadz.hisabkitab.interfaces.OnToastShow
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.utils.TaxType
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
    var finaltotals = 0.0
    var productTax = 0.0
    var itemList = mutableListOf<InvoiceItem>()
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onResume() {
        super.onResume()
        viewModel.getInvoiceDetails(invoice?.id.toString())
        viewModel.fetchInvoiceItems(invoice!!.invoiceId.toLong())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleReturnBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        bindings()
        setupSpinnerReturnMode(returnMode)
        setupSpinnerReturnModeOption(returnModeOption)
        return binding.root
    }
    fun bindings(){
        binding.invoice = invoice
        binding.returnMode = "Save Credit Note"
        binding.amount =  "${Config.CURRENCY} 0"
        binding.totalGst = "${Config.CURRENCY} 0"
        binding.totalAmount = "${Config.CURRENCY} $finaltotals"
        isButtonActive(finaltotals)
    }


   fun observers(){
       viewModel.invoice.observe(viewLifecycleOwner) {
           binding.invoice = it
       }
       viewModel.invoiceItems.observe(viewLifecycleOwner){
           setupRecyclerView(it)
       }
   }

    fun onClickListeners(){
        binding.btnAdd.setOnClickListener{
            if (finaltotals > 0.0){
                invoice?.let {
                    val creditNote = CreditNote(
                        invoiceId = it.invoiceId.toLong(),
                        invoiceNumber = it.invoiceNumber,
                        createdBy = it.createdBy,
                        dateTime = it.invoiceDate,
                        status = "Active",
                        amount = total,
                        totalAmount = finaltotals,
                        userId = Config.userId,
                        invoiceItems = itemList
                    )
                    viewModel.generateCreditNote(requireContext(),creditNote,isRefund){ isSuccess ->
                        if (isSuccess){
                            Toast.makeText(requireContext(),"Credit Note Saved",Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }

                    }
            }
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
                override fun onItemList(itemP: InvoiceItem) {
                    // Update the item list
                    itemList.removeAll { it.id == itemP.id }
                    itemList.add(itemP)

                    // Initialize accumulators
                    var total = 0.0
                    var totalProductTax = 0.0
                    var finalTotal = 0.0

                    // Loop through all items to calculate totals
                    itemList.forEach { item ->
                        val itemTotal = item.unitPrice * (item.returnedQty ?: 0)
                        var itemProductTax = if (item.taxRate > 0) {
                            item.unitPrice.times(item.taxRate).div(100)
                        } else {
                            0.0
                        }

                        var itemFinalTotal = itemTotal
                        if (item.isProduct == 1) {
                            item.productTaxType?.let { taxTypeString ->
                                TaxType.fromString(taxTypeString)?.let { taxType ->
                                    when (taxType) {
                                        TaxType.PriceIncludesTax -> {
                                        }
                                        TaxType.PriceWithoutTax -> {
                                            if (item.taxRate > 0) {
                                                itemFinalTotal += itemProductTax * (item.returnedQty ?: 0)
                                            }
                                        }
                                        TaxType.ZeroRatedTax, TaxType.ExemptTax -> {
                                            // No tax adjustments needed
                                        }
                                    }
                                }
                            }
                        }

                        // Accumulate totals
                        total += itemTotal
                        totalProductTax += if ((item.returnedQty ?: 0) > 0) itemProductTax * (item.returnedQty ?: 0) else 0.0
                        finalTotal += if ((item.returnedQty ?: 0) > 0) itemFinalTotal else 0.0
                    }

                    // Update binding with calculated values
                    binding.amount = "${Config.CURRENCY} $total"
                    binding.totalGst = "${Config.CURRENCY} $totalProductTax"
                    binding.totalAmount = "${Config.CURRENCY} $finalTotal"
                    finaltotals = finalTotal
                    this@SaleReturnFragment.total = total
                    isButtonActive(finalTotal)
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

    private fun isButtonActive(total : Double){
        if (total > 0.0){
            binding.isButtonActive = true
        }else{
            binding.isButtonActive = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}