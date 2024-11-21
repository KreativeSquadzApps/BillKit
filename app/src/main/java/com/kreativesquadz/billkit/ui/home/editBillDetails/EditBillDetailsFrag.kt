package com.kreativesquadz.billkit.ui.home.editBillDetails

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
import com.kreativesquadz.billkit.ui.dialogs.packageDialog.AddPackagingDialogFragment
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
    var customGstAmount : String ? = null
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }
    var cashAmount : Double = 0.0
    var onlineAmount : Double = 0.0
    var creditAmount : Double = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUserSettings()
        sharedViewModel.fetchInvoiceItems(invoice!!.invoiceId.toLong())
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
        invoice?.discount?.let {
            if (it !=0 ){
                dialogViewModel.isApplied.value = true
            }
        }
         invoice?.customGstAmount?.let {
             dialogGstViewModel.isApplied.value = true
        }

         invoice?.packageAmount?.let {
             dialogPackagingViewModel.isApplied.value = true
        }
        invoice?.creditNoteId?.let {
           val  creditNote = viewModel.getCreditNoteById(it.toLong())
            if (creditNote.value?.totalAmount != 0.0 && creditNote.value?.totalAmount != null){
                sharedViewModel.addCreditNote(creditNote.value)
                sharedViewModel._isCreditNoteApplied.value  = true
            }
        }
        invoice?.cashAmount?.let {
            cashAmount = it
        }
        invoice?.onlineAmount?.let {
            onlineAmount = it
        }
        invoice?.creditAmount?.let {
            creditAmount = it
        }
    }

    private fun onClickListeners(){
        binding.btnupdate.setOnClickListener{
            viewModel.updateInvoiceWithItems(sharedViewModel.getInvoice(onlineAmount = onlineAmount, creditAmount = creditAmount.toString().replace(Config.CURRENCY, "").trim().toDoubleOrNull() , cashAmount = cashAmount,customGstAmount,invoice?.invoiceNumber!!)
                .copy(id = invoice!!.id,isSynced = 1, invoiceDate = invoice!!.invoiceDate, invoiceTime = invoice!!.invoiceTime , invoiceId = invoice!!.invoiceId  )
                ,sharedViewModel.list,
                invoice!!.id)
        }


        binding.btnSplit.setOnClickListener {
//            val action = EditBillDetailsFragDirections.actionBillDetailsFragToSplitFragment()
//            findNavController().navigate(action)
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
            dialogViewModel.onRemoveClicked()
        }

        binding.removeGst.setOnClickListener{
            sharedViewModel.removeGst()
            dialogGstViewModel.onRemoveClicked()
        }
        binding.addPackaging.setOnClickListener {
            showAddPackagingDialog()
        }
        binding.removePack.setOnClickListener {
            sharedViewModel.removePackage()
            dialogPackagingViewModel.onRemoveClicked()

    }
    }
    private fun showAddPackagingDialog() {
        val dialog = AddPackagingDialogFragment()
        dialog.show(childFragmentManager, AddPackagingDialogFragment.TAG)
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

        Log.e("invoice",sharedViewModel.getTotalAmountDouble().toString())
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
                if (it.toInt() == 1){
                    sharedViewModel._invoiceItems.value = null
                    viewModel.clearInvoiceStatus()
                    sharedViewModel.list.clear()
                    sharedViewModel.clearOrder()
                    sharedViewModel.clearItemsList()
                    sharedViewModel.removeDiscount()
                    sharedViewModel.removeGst()
                    sharedViewModel.removePackage()
                    findNavController().popBackStack()
                }else{
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }

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

        sharedViewModel.isPackageApplied.observe(viewLifecycleOwner) { isPackagingApplied ->
            binding.isPackagingApplied = isPackagingApplied
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
                dialogViewModel.dialogText.value?.let {
                    if (it.equals("0") || it.equals("0.0")){
                        sharedViewModel.removeDiscount()
                        binding.isDiscountApplied  = false
                        return@observe
                    }else{
                        if(!it.equals("")){
                            sharedViewModel.addDiscount(it)
                            binding.discountedAmount = it
                            binding.isDiscountApplied  = true
                        }
                    }

                }
                invoice?.discount?.let {
                    if (it != 0){
                        sharedViewModel.addDiscount(invoice?.discount.toString())
                        binding.discountedAmount = invoice?.discount.toString()
                        binding.isDiscountApplied  = true
                    }
                }
            }else{
                sharedViewModel.removeDiscount()
                binding.isDiscountApplied  = false
            }
        }


        dialogGstViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                if (dialogGstViewModel.gstText.value != null){
                    val customGstAmountApplied = dialogGstViewModel.gstText.value.toString().substringBefore("|")
                    //val customGstRateApplied = dialogGstViewModel.gstText.value.toString().substringAfter("|")
                    binding.gstAppliedAmount = customGstAmountApplied
                    sharedViewModel.addGst(customGstAmountApplied)
                    customGstAmount = dialogGstViewModel.gstText.value.toString()
                }else{
                    val customGstAmountApplied = invoice?.customGstAmount.toString().substringBefore("|")
                    //val customGstRateApplied = dialogGstViewModel.gstText.value.toString().substringAfter("|")
                    binding.gstAppliedAmount = customGstAmountApplied
                    sharedViewModel.addGst(customGstAmountApplied)
                    customGstAmount = invoice?.customGstAmount.toString().substringBefore("|")
                }
            }else{
                    sharedViewModel.removeGst()
                    customGstAmount = null
            }
        }


        dialogPackagingViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                if (dialogPackagingViewModel.packagingText.value != null){
                    val packageAmountApplied = dialogPackagingViewModel.packagingText.value.toString()
                    binding.packagingAppliedAmount = packageAmountApplied
                    sharedViewModel.addPackage(packageAmountApplied)
                    binding.isPackagingApplied  = true
                }else{
                    val packageAmountApplied = invoice?.packageAmount.toString()
                    invoice?.packageAmount?.let {
                        binding.packagingAppliedAmount = packageAmountApplied
                        sharedViewModel.addPackage(packageAmountApplied)
                        binding.isPackagingApplied  = true
                    }
                }
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
                sharedViewModel.getTotalAmount()
                adapter.notifyItemRemoved(position)
                if (sharedViewModel.list.size == 0){
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
        if (invoice?.totalAmount == sharedViewModel.getTotalAmountDouble() ){
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
        viewModel.clearInvoiceStatus()
        sharedViewModel._invoiceItems.value = null
        sharedViewModel.removeGst()
        sharedViewModel.removeDiscount()
        sharedViewModel.removePackage()
        sharedViewModel.removeCreditNote()
        _binding = null
    }
}