package com.kreativesquadz.billkit.ui.home.billDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentBillDetailsBinding
import com.kreativesquadz.billkit.dialogs.AddDiscountDialogFragment
import com.kreativesquadz.billkit.dialogs.DialogViewModel
import com.kreativesquadz.billkit.dialogs.gstDialogFragment.AddGstDialogFragment
import com.kreativesquadz.billkit.dialogs.gstDialogFragment.AddGstDialogViewModel
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.ui.home.billDetails.creditNoteBottomSheet.CreditNoteBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.customerBottomSheet.CustomerAddBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet.EditItemBottomSheetFrag
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
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
                                          invoice = sharedViewModel.getInvoice(),
                                         items =  sharedViewModel.getItemsList(),
                                         creditNoteId =  sharedViewModel.getCreditNote()?.id,
                                         context =  requireContext())
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
            binding.totalAmount.text = "Total Amount : "+totalAmount
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