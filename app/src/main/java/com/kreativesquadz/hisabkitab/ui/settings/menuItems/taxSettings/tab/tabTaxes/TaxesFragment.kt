package com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings.tab.tabTaxes

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.adapter.GenericSpinnerAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentTaxesBinding
import com.kreativesquadz.hisabkitab.interfaces.FragmentBaseFunctions
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings.TaxSettingsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaxesFragment : Fragment(), FragmentBaseFunctions {
    private val viewModel: TaxesViewModel by viewModels()
    private var _binding: FragmentTaxesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<GST>
    private var selectedTaxTpe = "GST"
    val taxTypeList = listOf("GST",
        "SGST",
        "CGST",
        "IGST",
        "VAT")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getGstTax()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxesBinding.inflate(inflater, container, false)
        setupSpinner()
        setupRecyclerView()
        onClickListener()
        observers()
        return binding.root
    }


    override fun observers() {
        viewModel.gstTax.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
            }
        }

        viewModel.gstStatus.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.getGstTax()
            }
        }
    }
    override fun onClickListener() {
        binding.btnAdd.setOnClickListener {
            if (binding.etTaxValue.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter Tax Percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (binding.etTaxValue.text.toString().toDouble() <= 0) {
                Toast.makeText(requireContext(), "Please enter valid Tax Percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else{
                val gst = GST(userId = Config.userId.toInt(),
                    taxType = selectedTaxTpe ,
                    taxAmount = binding.etTaxValue.text.toString().toDouble(),
                    productCount = 0,
                    isSynced = 0)
                val enteredTaxAmount = binding.etTaxValue.text.toString().toDouble()

                if (viewModel.gstTax.value?.data?.any { it.taxAmount == enteredTaxAmount } == true) {
                    Toast.makeText(requireContext(), "Tax already exists", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addGstObj(gst, requireContext())
                }
            }

        }
    }

    private fun setupSpinner() {
        val adapter  = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = taxTypeList
        )
        binding.dropdownTaxType.setAdapter(adapter)
        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            selectedTaxTpe = selectedItem ?: "GST"
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.gstTax.value?.data ?: emptyList(),
            object : OnItemClickListener<GST> {
                override fun onItemClick(item: GST) {
                    val action = TaxSettingsFragmentDirections.actionTaxSettingsFragmentToTaxDetailsSettingFragment(item)
                    findNavController().navigate(action)
                }
            },
            R.layout.item_tax,
            BR.gst // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}