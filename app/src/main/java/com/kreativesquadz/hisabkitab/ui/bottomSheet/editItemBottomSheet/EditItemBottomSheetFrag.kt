package com.kreativesquadz.hisabkitab.ui.bottomSheet.editItemBottomSheet

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kreativesquadz.hisabkitab.databinding.FragmentEditItemBottomSheetBinding
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.settings.TaxOption
import com.kreativesquadz.hisabkitab.ui.home.tab.SharedViewModel
import com.kreativesquadz.hisabkitab.utils.TaxType
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditItemBottomSheetFrag(var item: InvoiceItem) : BottomSheetDialogFragment() {
    var _binding: FragmentEditItemBottomSheetBinding? = null
    val binding get() = _binding!!
    val viewModel: EditItemBottomSheetViewModel by activityViewModels()
    val sharedViewModel: SharedViewModel by activityViewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditItemBottomSheetBinding.inflate(inflater, container, false)
        binding.invoiceItem = item
        binding.invoiceName = item.itemName.split("(")[0]
        return binding.root
    }
//    override fun getTheme(): Int {
//        return R.style.CustomBottomSheetDialogTheme
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListeners()
        observers()
    }
    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // Calculate the height of the ActionBar
            val actionBarHeight = getActionBarHeight()

            // Get the display height
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels

            // Set the height of the BottomSheet to be the screen height minus the ActionBar height
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = screenHeight - actionBarHeight - 300
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    private fun getActionBarHeight(): Int {
        var actionBarHeight = 0
        val styledAttributes = requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        actionBarHeight = styledAttributes.getDimensionPixelSize(0, 0)
        styledAttributes.recycle()
        return actionBarHeight
    }





    private fun onClickListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        binding.btnupdate.setOnClickListener {
            var totalprice = item.unitPrice * binding.etQty.text.toString().toDouble()
            if (item.isProduct == 1){
                item.productTaxType?.let { taxTypeString ->
                    val taxType = TaxType.fromString(taxTypeString)
                    taxType?.let { type ->
                        when (type) {
                            TaxType.PriceIncludesTax -> {
                            }
                            TaxType.PriceWithoutTax -> {
                                Toast.makeText(requireContext(), "${item.taxRate}", Toast.LENGTH_SHORT).show()
                                if (item.taxRate != 0.0 ){
                                    val productTax =   item.unitPrice.times(item.taxRate).div(100)
                                    totalprice += (productTax * binding.etQty.text.toString().toInt())
                                }

                                // Handle PriceWithoutTax
                            }
                            TaxType.ZeroRatedTax -> {

                                // Handle ZeroRatedTax
                            }
                            TaxType.ExemptTax -> {
                                // Handle ExemptTax
                            }
                        }
                    }
                }
            }
            else{
                val selectedTaxPercentage = sharedViewModel.taxSettings.value?.selectedTaxPercentage
                sharedViewModel.taxSettings.value?.defaultTaxOption?.let {
                    if (it == TaxOption.ExemptTax){
                    }
                    if (it == TaxOption.PriceIncludesTax){
                    }
                    if (it == TaxOption.PriceExcludesTax){
                        selectedTaxPercentage?.let {
                            val productTax =  item.unitPrice.times(it).div(100)
                            totalprice += (productTax * binding.etQty.text.toString().toInt())
                        }
                    }
                    if (it == TaxOption.ZeroRatedTax){
                    }

                }
            }

            val itemName = binding.etItemName.text.toString() + " ( " + binding.etPrice.text.toString() + " )"+ " X " + binding.etQty.text.toString().toInt()
            sharedViewModel.updateItemAt(item,item.copy(itemName = itemName, unitPrice = binding.etPrice.text.toString().toDouble() , quantity = binding.etQty.text.toString().toInt() , totalPrice = totalprice))
            dismiss()
        }
        binding.txtMinus.setOnClickListener {
            if (binding.etQty.text.isEmpty()){
                binding.etQty.setText("1")
            }else{
                var qty = binding.etQty.text.toString().toInt()
                if (qty > 1){
                    qty--
                    binding.etQty.setText(qty.toString())
                }
            }
        }
        binding.txtPlus.setOnClickListener {
            if (binding.etQty.text.isEmpty()){
                binding.etQty.setText("1")
            }else{
                var qty = binding.etQty.text.toString().toInt()
                qty++
                binding.etQty.setText(qty.toString())
            }

        }
        binding.btnDelete.setOnClickListener {
            sharedViewModel.removeItem(item)
            dismiss()
        }

    }

    private fun observers(){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}