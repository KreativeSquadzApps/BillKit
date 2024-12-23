package com.kreativesquadz.billkit.ui.bills.billHistory.invoice

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.showCustomAlertDialog
import com.kreativesquadz.billkit.databinding.FragmentInvoiceBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.DialogData
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel

class InvoiceFragment : Fragment() {
    var _binding: FragmentInvoiceBinding? = null
    val binding get() = _binding!!
    private val viewModel: InvoiceViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<InvoiceItem>
    var isTaxAvailable = false
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }
    var invoiceSend : Invoice? = null

    override fun onResume() {
        super.onResume()
        viewModel.getInvoiceDetails(invoice?.id.toString())
        viewModel.fetchInvoiceItems(invoice!!.invoiceId.toLong())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListeners()
        observers()
     }

    fun observers(){
          viewModel.invoice.observe(viewLifecycleOwner) {
              binding.invoice = it
              binding.isCustomerAvailable = it?.customerId != null
              binding.customer = viewModel.getCustomerById(it.customerId.toString())
              val firstValue = it.customGstAmount?.split("|")?.first()
              binding.tvCustomGst.text = "Custom Gst : Rs "+firstValue?.toDouble()
              binding.tvDiscount.text = "Discount : Rs "+it.discount?.toDouble()
              binding.tvCreditNote.text = "Credit Note : Rs "+it.creditNoteAmount?.toDouble()
              invoiceSend = it
          }

        viewModel.invoiceItems.observe(viewLifecycleOwner){
            var totalQty = 0.0
            it?.forEach {
                  if(it.taxRate > 0){
                      isTaxAvailable = true
                  }
                totalQty += it.quantity
              }

            binding.tvTotalQty.setText("Total Qty : "+totalQty.toInt().toString())
            binding.istTaxAvalaible = isTaxAvailable
              setupRecyclerView(it)
          }
      }

    private  fun onClickListeners() {
        binding.btnEdit.setOnClickListener{
            invoiceSend?.let {
                val action = InvoiceFragmentDirections.actionInvoiceFragmentToEditBillDetailsFrag(it)
                findNavController().navigate(action)
            }
        }
        binding.btnReceipt.setOnClickListener {
            val action = InvoiceFragmentDirections.actionInvoiceFragmentToReceiptFrag(invoice?.invoiceId.toString(),Config.InvoiceFragmentToReceiptFragment)
            findNavController().navigate(action)
        }

        binding.btnSaleReturn.setOnClickListener {
            invoice?.let {
                val action = InvoiceFragmentDirections.actionInvoiceFragmentToSaleReturnFragment(it)
                findNavController().navigate(action)
            }
        }
        binding.btnCancel.setOnClickListener {
            setupPopup()
        }
    }
    private fun setupPopup(){
        val dialogData = DialogData(
            title = "Cancel Invoice",
            info = "Are you sure you want to Cancel this invoice ${invoice?.invoiceNumber} ?",
            positiveButtonText = "Cancel Invoice",
            negativeButtonText = "Cancel"
        )
        showCustomAlertDialog(
            context = requireActivity(),
            dialogData = dialogData,
            positiveAction = {
                invoice?.id?.let {
                    viewModel.updateInvoiceStatus(requireContext(),"Cancelled",it.toInt(),invoice?.customerId,invoice?.creditAmount,
                        invoice?.invoiceNumber!!,invoice?.invoiceId!!.toInt())
                     val result = Bundle().apply {
                        putBoolean("dataUpdated", true)  // Indicate that data is updated
                     }
                    setFragmentResult("requestKey", result)

                }
            },
            negativeAction = {
                // Handle negative button action
                // E.g., dismiss the dialog
            }
        )
    }

    private fun setupRecyclerView(list : List<InvoiceItem>?) {
        adapter = GenericAdapter(
            list ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    // Handle item click
                }
            },
            R.layout.item_invoice_item,
            BR.item // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val itemHeight = resources.getDimensionPixelSize(R.dimen._25dp) // set this to your item height
        val params = binding.recyclerView.layoutParams
        params.height = itemHeight * adapter.itemCount
        binding.recyclerView.layoutParams = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}