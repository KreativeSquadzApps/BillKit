package com.kreativesquadz.billkit.ui.home.billDetails

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentBillDetailsBinding
import com.kreativesquadz.billkit.ui.dialogs.AddDiscountDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.DialogViewModel
import com.kreativesquadz.billkit.ui.dialogs.gstDialogFragment.AddGstDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.gstDialogFragment.AddGstDialogViewModel
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.ui.home.billDetails.creditNoteBottomSheet.CreditNoteBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.customerBottomSheet.CustomerAddBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet.EditItemBottomSheetFrag
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat


@AndroidEntryPoint
class BillDetailsFrag : Fragment() {
    private val viewModel: BillDetailsViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val dialogViewModel: DialogViewModel by activityViewModels()
    private val dialogGstViewModel: AddGstDialogViewModel by activityViewModels()
    private var _binding : FragmentBillDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<InvoiceItem>
    val df = DecimalFormat("#")
    var isCustomerSelected = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUserSettings()
        sharedViewModel.getTotalAmount()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillDetailsBinding.inflate(inflater, container, false)
        binding.sharedViewModel = sharedViewModel
        binding.isCustomerSelected = sharedViewModel.isCustomerSelected.value
        binding.isDiscountApplied = sharedViewModel.isDiscountApplied.value
        binding.isCreditNoteApplied = sharedViewModel.isCreditNoteApplied.value
        binding.lifecycleOwner = viewLifecycleOwner
        init()
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun init(){
        binding.isGST = true

    }

    private fun onClickListeners(){
        binding.btnCash.setOnClickListener {
                      viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
                                          invoice = sharedViewModel.getInvoice(onlineAmount = 0.0, creditAmount = 0.0, cashAmount = sharedViewModel.getTotalAmountDouble()),
                                         items =  sharedViewModel.getItemsList(),
                                         creditNoteId =  sharedViewModel.getCreditNote()?.id,
                                         context =  requireContext())
        }

        binding.btnOnline.setOnClickListener{
                        viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
                                          invoice = sharedViewModel.getInvoice(onlineAmount = sharedViewModel.getTotalAmountDouble(), creditAmount = 0.0, cashAmount = 0.0),
                                         items =  sharedViewModel.getItemsList(),
                                         creditNoteId =  sharedViewModel.getCreditNote()?.id,
                                         context =  requireContext())
        }

        binding.btnCredit.setOnClickListener{
            if (isCustomerSelected){
                viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
                    invoice = sharedViewModel.getInvoice(onlineAmount = 0.0, creditAmount = sharedViewModel.getTotalAmountDouble(), cashAmount = 0.0),
                    items =  sharedViewModel.getItemsList(),
                    creditNoteId =  sharedViewModel.getCreditNote()?.id,
                    context =  requireContext())
            }else{
                Toast.makeText(requireContext(), "Please select customer", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnSplit.setOnClickListener {
            val action = BillDetailsFragDirections.actionBillDetailsFragToSplitFragment()
            findNavController().navigate(action)
        }

        binding.addCustomer.setOnClickListener {
            val customerAddBottomSheetFrag = CustomerAddBottomSheetFrag()
            customerAddBottomSheetFrag.show(parentFragmentManager, "CustomerAddBottomSheetFrag")
        }
        binding.addCreditNote.setOnClickListener {
            val creditNoteBottomSheetFrag = CreditNoteBottomSheetFrag()
            creditNoteBottomSheetFrag.show(parentFragmentManager, "CustomerAddBottomSheetFrag")
        }

        binding.ivDeselectCustomer.setOnClickListener {
            sharedViewModel.updateDeselectCustomer()
        }
         binding.removeCreditNote.setOnClickListener {
             sharedViewModel.removeCreditNote()
        }


        binding.clearOrder.setOnClickListener {
            findNavController().popBackStack()
            sharedViewModel.clearOrder()
            dialogViewModel.onRemoveClicked()

        }

        binding.addDiscount.setOnClickListener {
            showAddDiscountDialog()
        }
        binding.addGst.setOnClickListener {
            showAddGstDialog()
        }

        binding.removeDiscount.setOnClickListener{
            sharedViewModel.removeDiscount()
        }

        binding.removeGst.setOnClickListener{
            sharedViewModel.removeGst()
        }




    }

    private fun showAddDiscountDialog() {
        val dialog = AddDiscountDialogFragment()
        dialog.show(childFragmentManager, AddDiscountDialogFragment.TAG)
        dialogViewModel.setTotalAmount(sharedViewModel.totalLivedata.value.toString())

    }
  private fun showAddGstDialog() {
        val dialog = AddGstDialogFragment()
        dialog.show(childFragmentManager, AddGstDialogFragment.TAG)
        dialogGstViewModel.setTotalAmount(sharedViewModel.totalLivedata.value.toString())
  }


    private fun observers(){
        sharedViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            sharedViewModel.getTotalAmount()
            binding.amountTotalTax.text =  "Total Tax : " + sharedViewModel.getTotalTax()
            binding.itemsCount.text = "Items : "+ sharedViewModel.getInvoiceItemCount()

        }

        viewModel.invoiceId.observe(viewLifecycleOwner){
            it?.let {
                val action = BillDetailsFragDirections.actionBillDetailsFragToReceiptFrag(viewModel.invoiceId.value.toString(),Config.BillDetailsFragmentToReceiptFragment)
                findNavController().navigate(action)
                viewModel.clearInvoiceStatus()
                sharedViewModel.clearOrder()
                dialogViewModel.onRemoveClicked()
                dialogGstViewModel.onRemoveClicked()
            }
        }

        sharedViewModel.isCustomerSelected.observe(viewLifecycleOwner) { isCustomerSelected ->
            binding.isCustomerSelected = isCustomerSelected
            this.isCustomerSelected = isCustomerSelected
        }

        sharedViewModel.isDiscountApplied.observe(viewLifecycleOwner) { isDiscountApplied ->
            binding.isDiscountApplied = isDiscountApplied
        }

        sharedViewModel.isGstApplied.observe(viewLifecycleOwner) { isGstApplied ->
            binding.isGSTApplied = isGstApplied
        }


        sharedViewModel.isCreditNoteApplied.observe(viewLifecycleOwner) { isCreditNoteApplied ->
            binding.isCreditNoteApplied = isCreditNoteApplied
        }

        sharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            binding.customer = customer
        }
        sharedViewModel.selectedCreditNote.observe(viewLifecycleOwner) { creditNote ->
            binding.creditNote = creditNote

        }
        viewModel.userSetting.observe(viewLifecycleOwner){
            it.let {
                if (it?.isdiscount==0){
                    binding.isDiscount = false
                }else{
                    binding.isDiscount = true
                }
            }
        }


        sharedViewModel.totalLivedata.observe(viewLifecycleOwner) { totalAmount ->
            binding.totalAmount.text = "Total Amount  :  "+totalAmount
            binding.amountTotal.text = "Amount : "+ (totalAmount.replace(Config.CURRENCY, "").toDouble()
                    - sharedViewModel.getTotalTax().replace(Config.CURRENCY, "").toDouble())
            //binding.amountTotal.text =  "Amount : " + totalAmount

        }

        dialogViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                sharedViewModel.addDiscount(dialogViewModel.dialogText.value.toString().substringAfter(" "))
                binding.discountedAmount = dialogViewModel.dialogText.value.toString().substringAfter(" ")
            }else{
                sharedViewModel.removeDiscount()
            }
        }
        dialogGstViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.gstAppliedAmount = dialogGstViewModel.gstText.value.toString().substringAfter(" ")
                sharedViewModel.addGst(dialogGstViewModel.gstText.value.toString().substringAfter(" "))
            }else{
                sharedViewModel.removeGst()
            }
        }

    }



    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            sharedViewModel.items.value ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    editItem(item)
                }
            },
            R.layout.item_invoice_details_bill,
            BR.item // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        setupSwipeToDelete(binding.recyclerView)
    }
    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                sharedViewModel.removeItemAt(position)
                if (sharedViewModel.items.value.isNullOrEmpty()){
                    findNavController().popBackStack()
                }



            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun editItem(item: InvoiceItem){
        val editItemBottomSheetFrag = EditItemBottomSheetFrag(item)
        editItemBottomSheetFrag.show(parentFragmentManager, "EditItemBottomSheetFrag")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}