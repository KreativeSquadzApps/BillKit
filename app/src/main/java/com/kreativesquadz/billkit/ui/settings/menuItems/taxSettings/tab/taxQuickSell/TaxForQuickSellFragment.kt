package com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.taxQuickSell

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.FragmentTaxForQuickSellBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.settings.TaxOption
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import com.kreativesquadz.billkit.utils.TaxType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  TaxForQuickSellFragment : Fragment() {
    private var  _binding: FragmentTaxForQuickSellBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<GST>
    private val viewModel: TaxForQuickSellViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var selectedPosition: Int = -1 // Keeps track of selected switch position
    private var selectedTaxValue: Double? = null
    private var selectedTaxOption: TaxOption = TaxOption.ExemptTax // Default value
    val itemList = TaxType.getList().map{ it.displayName }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initializeTaxSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxForQuickSellBinding.inflate(inflater, container, false)
        setupSpinnerTaxType()
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun observers(){
        viewModel.taxSettings.observe(viewLifecycleOwner ){ taxSettings ->
            taxSettings?.let {
                val selectedTaxOption = taxSettings.defaultTaxOption
                viewModel.gstTaxList.observe(viewLifecycleOwner) {
                    it.data?.let {
                        selectedPosition = it.indexOfFirst { it.taxAmount == taxSettings.selectedTaxPercentage.toDouble() }

                        if (selectedPosition != -1) {
                            // Ensure that the correct switch is checked if a GST is pre-selected
                            binding.recyclerView.post {
                                val preselectedHolder = binding.recyclerView.findViewHolderForAdapterPosition(selectedPosition)
                                if (preselectedHolder is GenericAdapter.ViewHolder<*>) {
                                    val preselectedBinding = preselectedHolder.binding as ViewDataBinding
                                    val preselectedSwitch = preselectedBinding.root.findViewById<SwitchCompat>(R.id.switchTax)
                                    preselectedSwitch.isChecked = true
                                    selectedTaxValue = it[selectedPosition].taxAmount
                                }
                            }
                        }
                        adapter.submitList(it)

                    }
                }
                // Set the selected spinner option based on the saved setting
                val position = when (selectedTaxOption) {
                    is TaxOption.PriceIncludesTax -> 0
                    is TaxOption.PriceExcludesTax -> 1
                    is TaxOption.ZeroRatedTax -> 2
                    is TaxOption.ExemptTax -> 3
                }
                when(position){
                    0 -> binding.recyclerView.visibility = View.VISIBLE
                    1 -> binding.recyclerView.visibility = View.VISIBLE
                    2 -> binding.recyclerView.visibility = View.GONE
                    3 -> binding.recyclerView.visibility = View.GONE
                }
                binding.dropdownTaxType.setText(itemList[position])
            }

        }




    }

    private fun onClickListeners(){
        binding.btnAdd.setOnClickListener {
            val position = when (selectedTaxOption) {
                is TaxOption.PriceIncludesTax -> 0
                is TaxOption.PriceExcludesTax -> 1
                is TaxOption.ZeroRatedTax -> 2
                is TaxOption.ExemptTax -> 3
            }
            when(position){
                0 -> {
                    if (selectedTaxValue == null){
                        Toast.makeText(requireContext(), "Please select a tax value", Toast.LENGTH_SHORT).show()
                    }else{
                        viewModel.setDefaultTaxOption(selectedTaxOption, selectedTaxValue?.toFloat())
                    }
                }
                1 -> {
                    if (selectedTaxValue == null){
                        Toast.makeText(requireContext(), "Please select a tax value", Toast.LENGTH_SHORT).show()
                    } else{
                        viewModel.setDefaultTaxOption(selectedTaxOption, selectedTaxValue?.toFloat())
                    }
                }
                2 -> {
                    viewModel.setDefaultTaxOption(selectedTaxOption, null)
                }
                3 -> {
                    viewModel.setDefaultTaxOption(selectedTaxOption, null)
            }
            }

        }
    }

    private fun setupSpinnerTaxType() {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownTaxType.setAdapter(adapterStockUnit)
        //binding.dropdownTaxType.setText(itemList[0],false)

        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            when(selectedItem){
                "Price includes Tax" -> {
                    selectedTaxOption = TaxOption.PriceIncludesTax
                    binding.recyclerView.visibility = View.VISIBLE
                }
                "Price is without Tax" -> {
                    selectedTaxOption = TaxOption.PriceExcludesTax
                    binding.recyclerView.visibility = View.VISIBLE

                }
                "Zero Rated Tax" -> {
                    selectedTaxOption = TaxOption.ZeroRatedTax
                    binding.recyclerView.visibility = View.GONE

                }
                "Exempt Tax" -> {
                    selectedTaxOption = TaxOption.ExemptTax
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }

    }
    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.gstTaxList.value?.data ?: emptyList(),
            object : OnItemClickListener<GST> {
                override fun onItemClick(item: GST) {
                    val gstPosition = viewModel.gstTaxList.value?.data?.indexOf(item) ?: -1
                    val currentHolder = binding.recyclerView.findViewHolderForAdapterPosition(gstPosition)
                    if (currentHolder is GenericAdapter.ViewHolder<*>) {
                        val currentBinding = currentHolder.binding as ViewDataBinding
                        val currentSwitch = currentBinding.root.findViewById<SwitchCompat>(R.id.switchTax)

                        if (selectedPosition == gstPosition) {
                            // If the current item is already selected, unselect it
                            currentSwitch.isChecked = false
                            selectedPosition = -1 // Reset the selection
                        } else {
                            // Unselect previously selected switch if exists
                            if (selectedPosition != -1) {
                                val previousHolder = binding.recyclerView.findViewHolderForAdapterPosition(selectedPosition)
                                if (previousHolder is GenericAdapter.ViewHolder<*>) {
                                    val previousBinding = previousHolder.binding as ViewDataBinding
                                    val previousSwitch = previousBinding.root.findViewById<SwitchCompat>(R.id.switchTax)
                                    previousSwitch.isChecked = false // Uncheck the previous switch
                                }
                            }

                            // Set the current switch as selected
                            currentSwitch.isChecked = true
                            selectedPosition = gstPosition   // Update the selected position
                            selectedTaxValue = item.taxAmount
//                            viewModel.updateDefaultTax(item.copy(productCount = item.taxAmount.toInt()))
                        }
                    }
                }

            },
            R.layout.item_tax_add_products,
            BR.gst // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

}