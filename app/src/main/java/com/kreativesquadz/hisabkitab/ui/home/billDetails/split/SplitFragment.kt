package com.kreativesquadz.hisabkitab.ui.home.billDetails.split

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.databinding.FragmentSplitBinding
import com.kreativesquadz.hisabkitab.interfaces.FragmentBaseFunctions
import com.kreativesquadz.hisabkitab.ui.dialogs.DialogViewModel
import com.kreativesquadz.hisabkitab.ui.dialogs.gstDialogFragment.AddGstDialogViewModel
import com.kreativesquadz.hisabkitab.ui.home.billDetails.BillDetailsViewModel
import com.kreativesquadz.hisabkitab.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class SplitFragment : Fragment(),FragmentBaseFunctions {
    private var _binding: FragmentSplitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplitViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val billDetailsViewModel: BillDetailsViewModel by activityViewModels()
    private val dialogViewModel: DialogViewModel by activityViewModels()
    private val dialogGstViewModel: AddGstDialogViewModel by activityViewModels()
    private var isCustomerSelected = false
    val df = DecimalFormat("#")
    var invoicePrefixNumber = ""
    var invoicePrefix = ""
    var customGstAmount : String ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplitBinding.inflate(inflater, container, false)
        binding.sharedViewModel = sharedViewModel
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.actualAmount.value = sharedViewModel.totalLivedata.value?.replace(Config.CURRENCY, "")?.trim()?.toDoubleOrNull() ?: 0.0
        binding.totalAmount.text = sharedViewModel.totalLivedata.value
        binding.etCredit.setText(Config.CURRENCY + df.format(viewModel.actualAmount.value))
        observers()
        onClickListener()


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun observers() {
        sharedViewModel.loadCompanyDetails().observe(viewLifecycleOwner){
            it?.data?.let {
                invoicePrefixNumber = it.InvoicePrefix + it.InvoiceNumber
                invoicePrefix = it.InvoicePrefix
            }
        }
        sharedViewModel.isCustomerSelected.observe(viewLifecycleOwner){
            it?.let {
                isCustomerSelected = it
            }
        }

        binding.etCash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val amount = s?.toString()?.toDoubleOrNull() ?: 0.0
                viewModel.setCashAmount(amount)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etOnline.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val amount = s?.toString()?.toDoubleOrNull() ?: 0.0
                viewModel.setOnlineAmount(amount)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.totalAmount.observe(viewLifecycleOwner, Observer { totalAmount ->
            // Update UI with the remaining amount
            binding.etCredit.setText(Config.CURRENCY + df.format(totalAmount))
        })

        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        })

        billDetailsViewModel.invoiceId.observe(viewLifecycleOwner){
            it?.let {
               // val action = BillDetailsFragDirections.actionBillDetailsFragToReceiptFrag(billDetailsViewModel.invoiceId.value.toString(),Config.BillDetailsFragmentToReceiptFragment)
                val action = SplitFragmentDirections.actionSplitFragmentToReceiptFrag(billDetailsViewModel.invoiceId.value.toString(),Config.BillDetailsFragmentToReceiptFragment)
                findNavController().navigate(action)
                billDetailsViewModel.clearInvoiceStatus()
                sharedViewModel.clearOrder()
                dialogViewModel.onRemoveClicked()
                dialogGstViewModel.onRemoveClicked()
            }
        }
        dialogGstViewModel.isApplied.observe(viewLifecycleOwner) {
            if (it == true) {
                val customGstAmountApplied = dialogGstViewModel.gstText.value.toString().substringBefore("|")
                //val customGstRateApplied = dialogGstViewModel.gstText.value.toString().substringAfter("|")
                sharedViewModel.addGst(customGstAmountApplied)
                customGstAmount = dialogGstViewModel.gstText.value.toString()
            }else{
                sharedViewModel.removeGst()
                customGstAmount = null
            }
        }


    }

    override fun onClickListener() {
       binding.saveBill.setOnClickListener {
           if (binding.etCredit.text.toString().trim().isEmpty() || binding.etCredit.text.toString().equals((Config.CURRENCY)+"0")){
               billDetailsViewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
                   invoice = sharedViewModel.getInvoice(onlineAmount = viewModel.onlineAmount.value, creditAmount = binding.etCredit.text.toString().replace(Config.CURRENCY, "").trim().toDoubleOrNull() , cashAmount = viewModel.cashAmount.value,customGstAmount,invoicePrefixNumber),
                   items =  sharedViewModel.getItemsList(),
                   creditNoteId =  sharedViewModel.getCreditNote()?.id)
               billDetailsViewModel.updateInvoicePrefixNumber(invoicePrefix)
           }else{
               if (isCustomerSelected){
                   billDetailsViewModel.insertInvoiceWithItems( isSavedOrderIdExist = sharedViewModel.isSavedOrderIdExist(),
                       invoice = sharedViewModel.getInvoice(onlineAmount = viewModel.onlineAmount.value, creditAmount = binding.etCredit.text.toString().replace(Config.CURRENCY, "").trim().toDoubleOrNull() , cashAmount = viewModel.cashAmount.value,customGstAmount,invoicePrefixNumber),
                       items =  sharedViewModel.getItemsList(),
                       creditNoteId =  sharedViewModel.getCreditNote()?.id)
                   billDetailsViewModel.updateInvoicePrefixNumber(invoicePrefix)
               }else{
                   Toast.makeText(requireContext(),"Please select a customer",Toast.LENGTH_SHORT).show()
               }
           }


       }
    }
}