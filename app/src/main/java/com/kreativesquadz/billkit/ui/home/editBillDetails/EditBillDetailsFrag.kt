package com.kreativesquadz.billkit.ui.home.editBillDetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentEditBillDetailsBinding
import com.kreativesquadz.billkit.ui.dialogs.AddDiscountDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.DialogViewModel
import com.kreativesquadz.billkit.ui.dialogs.gstDialogFragment.AddGstDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.gstDialogFragment.AddGstDialogViewModel
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.ui.bottomSheet.creditNoteBottomSheet.CreditNoteBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.customerBottomSheet.CustomerAddBottomSheetFrag
import com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet.EditItemBottomSheetFrag
import com.kreativesquadz.billkit.ui.dialogs.packageDialog.AddPackagingDialogViewModel
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat


@AndroidEntryPoint
class EditBillDetailsFrag : Fragment() {
    private var _binding : FragmentEditBillDetailsBinding? = null
    private val viewModel: EditBillDetailsViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val dialogViewModel: DialogViewModel by activityViewModels()
    private val dialogGstViewModel: AddGstDialogViewModel by activityViewModels()
    private val dialogPackagingViewModel: AddPackagingDialogViewModel by activityViewModels()
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<InvoiceItem>
    val df = DecimalFormat("#")
    var isCustomerSelected = false
    var invoicePrefixNumber = ""
    var invoicePrefix = ""
    var customGstAmount : String ? = null
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUserSettings()
        sharedViewModel.fetchInvoiceItems(invoice!!.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBillDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.invoice = invoice

        binding.customer  = sharedViewModel.selectedCustomer.value
        init()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun init(){
        binding.isGST = true
        binding.isCustomerSelected = false
        btnUpdateState()

    }

    private fun onClickListeners(){
        binding.btnupdate.setOnClickListener{
//            viewModel.updateInvoice(sharedViewModel.getInvoice(onlineAmount = viewModel.onlineAmount.value, creditAmount = binding.etCredit.text.toString().replace(Config.CURRENCY, "").trim().toDoubleOrNull() , cashAmount = viewModel.cashAmount.value,customGstAmount,invoicePrefixNumber),sharedViewModel.getCreditNote()?.id)
            findNavController().popBackStack()
        }
//        binding.btnCash.setOnClickListener {
//                      viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
//                                          invoice = sharedViewModel.getInvoice(onlineAmount = 0.0, creditAmount = 0.0, cashAmount = sharedViewModel.getTotalAmountDouble(),0.0 ,customGstAmount,invoicePrefixNumber),
//                                         items =  sharedViewModel.getItemsList(),
//                                         creditNoteId =  sharedViewModel.getCreditNote()?.id,
//                                         context =  requireContext())
//
//
//        }
//
//        binding.btnOnline.setOnClickListener{
//                        viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
//                                          invoice = sharedViewModel.getInvoice(onlineAmount = sharedViewModel.getTotalAmountDouble(), creditAmount = 0.0, cashAmount = 0.0,0.0,customGstAmount,invoicePrefixNumber),
//                                         items =  sharedViewModel.getItemsList(),
//                                         creditNoteId =  sharedViewModel.getCreditNote()?.id,
//                                         context =  requireContext())
//
//        }
//
//        binding.btnCredit.setOnClickListener{
//            if (isCustomerSelected){
//                viewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
//                    invoice = sharedViewModel.getInvoice(onlineAmount = 0.0, creditAmount = sharedViewModel.getTotalAmountDouble(), cashAmount = 0.0,0.0,customGstAmount,invoicePrefixNumber),
//                    items =  sharedViewModel.getItemsList(),
//                    creditNoteId =  sharedViewModel.getCreditNote()?.id,
//                    context =  requireContext())
//
//            }else{
//                Toast.makeText(requireContext(), "Please select customer", Toast.LENGTH_SHORT).show()
//            }
//
//        }
//
//        binding.btnSplit.setOnClickListener {
////            val action = EditBillDetailsFragDirections.actionBillDetailsFragToSplitFragment()
////            findNavController().navigate(action)
//        }

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
            btnUpdateStateCustomer()
        }
         binding.removeCreditNote.setOnClickListener {
             sharedViewModel.removeCreditNote()
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

        if (invoice?.cashAmount!=0.0 && invoice?.onlineAmount != 0.0 || invoice?.creditAmount != 0.0 ){
            binding.btnSplit.setBackgroundResource(R.drawable.corner_four_green)
            binding.btnCash.setBackgroundResource(R.drawable.corner_four_grey)
            binding.btnOnline.setBackgroundResource(R.drawable.corner_four_grey)
            binding.btnCredit.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtSplit.setTextColor(resources.getColor(R.color.text_color_heading_reverse))
        }else{
            binding.btnSplit.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtSplit.setTextColor(resources.getColor(R.color.text_color_main))

        }

        if (invoice?.cashAmount!=0.0 && invoice?.onlineAmount == 0.0  && invoice?.creditAmount == 0.0) {
            binding.btnCash.setBackgroundResource(R.drawable.corner_four_green)
            binding.txtCash.setTextColor(resources.getColor(R.color.text_color_heading_reverse))

        }else{
            binding.btnCash.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtCash.setTextColor(resources.getColor(R.color.text_color_main))

        }

        if (invoice?.onlineAmount!=0.0 && invoice?.cashAmount == 0.0  && invoice?.creditAmount == 0.0) {
            binding.btnOnline.setBackgroundResource(R.drawable.corner_four_green)
            binding.txtOnline.setTextColor(resources.getColor(R.color.text_color_heading_reverse))

        }else{
            binding.btnOnline.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtOnline.setTextColor(resources.getColor(R.color.text_color_main))

        }

        if (invoice?.creditAmount!=0.0 && invoice?.cashAmount == 0.0  && invoice?.onlineAmount == 0.0) {
            binding.btnCredit.setBackgroundResource(R.drawable.corner_four_green)
            binding.btnSplit.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtCredit.setTextColor(resources.getColor(R.color.text_color_main))
            binding.txtSplit.setTextColor(resources.getColor(R.color.text_color_heading_reverse))

        }else{
            binding.btnCredit.setBackgroundResource(R.drawable.corner_four_grey)
            binding.txtCredit.setTextColor(resources.getColor(R.color.text_color_main))

        }
        invoice?.customerId?.let { customerId ->
            binding.isCustomerSelected = true
            viewModel.getCustomerById(customerId.toString()).observe(viewLifecycleOwner) { customer ->
                binding.customer = customer
                sharedViewModel.updateSelectedCustomer(customer)

            }

        }
        sharedViewModel.invoiceItems.observe(viewLifecycleOwner){
            it?.let {
                sharedViewModel.list = it.toMutableList()
                setupRecyclerView(sharedViewModel.list)
                Log.e("list",sharedViewModel.list.toString())
                //sharedViewModel.setItemsList(it)
                sharedViewModel.getTotalAmount()
                binding.amountTotalTax.text =  "Total Tax : " + sharedViewModel.getTotalTax()
                binding.itemsCount.text = "Items : "+ sharedViewModel.getInvoiceItemCount()
                btnUpdateState()
            }

        }

//        sharedViewModel.items.observe(viewLifecycleOwner){
//            sharedViewModel.list = it.toMutableList()
//            setupRecyclerView(sharedViewModel.list)
//            sharedViewModel.getTotalAmount()
//            binding.amountTotalTax.text =  "Total Tax : " + sharedViewModel.getTotalTax()
//            binding.itemsCount.text = "Items : "+ sharedViewModel.getInvoiceItemCount()
//            btnUpdateState()
//        }
        viewModel.invoiceId.observe(viewLifecycleOwner){
            it?.let {

                viewModel.clearInvoiceStatus()
                sharedViewModel.clearOrder()
                dialogViewModel.onRemoveClicked()
                dialogGstViewModel.onRemoveClicked()
            }
        }

        sharedViewModel.isCustomerSelected.observe(viewLifecycleOwner) { isCustomerSelected ->
            binding.isCustomerSelected = isCustomerSelected
            btnUpdateStateCustomer()
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
            btnUpdateStateCustomer()
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
            binding.amountTotalTax.text = sharedViewModel.getTotalTax()
            btnUpdateState()
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
                val customGstAmountApplied = dialogGstViewModel.gstText.value.toString().substringBefore("|")
                //val customGstRateApplied = dialogGstViewModel.gstText.value.toString().substringAfter("|")
                binding.gstAppliedAmount = customGstAmountApplied
                sharedViewModel.addGst(customGstAmountApplied)
                customGstAmount = dialogGstViewModel.gstText.value.toString()
            }else{
                sharedViewModel.removeGst()
                customGstAmount = null
            }
        }
        dialogPackagingViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                val packageAmountApplied = dialogPackagingViewModel.packagingText.value.toString()
                binding.packagingAppliedAmount = packageAmountApplied
                sharedViewModel.addPackage(packageAmountApplied)
            }else{
                sharedViewModel.removePackage()
            }
        }
    }



    private fun setupRecyclerView(invoiceItem: List<InvoiceItem>) {
        adapter = GenericAdapter(
            invoiceItem,
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

    private fun btnUpdateState() {

        if (invoice?.totalAmount == sharedViewModel.getTotalAmountDouble() || invoice?.customerId == sharedViewModel.selectedCustomer.value?.id ){
            binding.btnupdate.setCardBackgroundColor(resources.getColor(R.color.lite_grey_200))
            binding.btnupdate.isEnabled = false
        }else{
            binding.btnupdate.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            binding.btnupdate.isEnabled = true
        }

    }
    private fun btnUpdateStateCustomer() {
        if(invoice?.customerId != sharedViewModel.selectedCustomer.value?.id){
            binding.btnupdate.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            binding.btnupdate.isEnabled = true
        }else{
            binding.btnupdate.setCardBackgroundColor(resources.getColor(R.color.lite_grey_200))
            binding.btnupdate.isEnabled = false


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.list.clear()
        sharedViewModel.clearOrder()
        sharedViewModel.clearItemsList()
        dialogViewModel.onRemoveClicked()
        sharedViewModel._invoiceItems.value = null
        _binding = null
    }
}