package com.kreativesquadz.billkit.ui.bottomSheet.customerBottomSheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentCustomerBottomSheetBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.ui.home.billDetails.BillDetailsFragDirections
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerAddBottomSheetFrag : BottomSheetDialogFragment() {
    var _binding: FragmentCustomerBottomSheetBinding? = null
    val binding get() = _binding!!
    val viewModel: CustomerAddBottomSheetViewModel by activityViewModels()
    val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<Customer>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        viewModel.getCustomers()
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
            layoutParams.height = screenHeight - actionBarHeight
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
        _binding = FragmentCustomerBottomSheetBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        onClickListeners()
        observers()
    }

    private fun onClickListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        binding.addCustomer.setOnClickListener{
            dismiss()
            val action = BillDetailsFragDirections.actionBillDetailsFragToCreateCustomerFrag("bottomSheet")
            findNavController().navigate(action)
        }

        binding.etCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.customer.value?.data?.let {
                    adapter.submitList(it.filter { customer ->
                        customer.customerName.contains(s.toString(), ignoreCase = true) ||
                        customer.shopContactNumber.contains(s.toString(), ignoreCase = true)})
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observers(){
        viewModel.customer.observe(viewLifecycleOwner){
            it.data?.let {
                adapter.submitList(it)
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.customer.value?.data ?: emptyList(),
            object : OnItemClickListener<Customer> {
                override fun onItemClick(item: Customer) {
                    // Handle item click
                    sharedViewModel.updateSelectedCustomer(item)
                    dismiss()
                }
            },
            R.layout.item_customer,
            BR.customer // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}