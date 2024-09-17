package com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.databinding.FragmentEditItemBottomSheetBinding
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditItemBottomSheetFrag(var item: InvoiceItem) : BottomSheetDialogFragment() {
    var _binding: FragmentEditItemBottomSheetBinding? = null
    val binding get() = _binding!!
    val viewModel: EditItemBottomSheetViewModel by activityViewModels()
    val sharedViewModel: SharedViewModel by activityViewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
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
            layoutParams.height = screenHeight - actionBarHeight - 200
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_top)

        }
    }

    private fun getActionBarHeight(): Int {
        var actionBarHeight = 0
        val styledAttributes = requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        actionBarHeight = styledAttributes.getDimensionPixelSize(0, 0)
        styledAttributes.recycle()
        return actionBarHeight
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListeners()
        observers()
    }

    private fun onClickListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        binding.btnupdate.setOnClickListener {
            val totalprice = binding.etPrice.text.toString().toDouble() * binding.etQty.text.toString().toInt()
            val itemName = binding.etItemName.text.toString() + " ( " + binding.etPrice.text.toString() + " )"+ " X " + binding.etQty.text.toString().toInt()
            sharedViewModel.updateItemAt(item,item.copy(itemName = itemName, unitPrice = binding.etPrice.text.toString().toDouble() , quantity = binding.etQty.text.toString().toInt() , totalPrice = totalprice))
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