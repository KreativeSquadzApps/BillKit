package com.kreativesquadz.billkit.ui.home.tab.quickSale

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.databinding.FragmentQuickSaleBinding
import com.kreativesquadz.billkit.model.settings.TaxOption
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import com.kreativesquadz.billkit.utils.TaxType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickSaleFragment : Fragment() {
    private var _binding: FragmentQuickSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuickSaleViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var selectedTaxOption: TaxOption = TaxOption.ExemptTax // Default value
    private var selectedTaxValue: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.initializeTaxSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickSaleBinding.inflate(inflater, container, false)
        binding.sharedViewModels = sharedViewModel
        observers()
        onClickListener()

        return binding.root
    }

    private fun onClickListener() {
        binding.btnAddItem.setOnClickListener {
            Log.d("TAG", "observersoooo: $selectedTaxValue")
            sharedViewModel.addItem(selectedTaxValue)
        }
    }

    private fun observers() {
        sharedViewModel.taxSettings.observe(viewLifecycleOwner) { taxSettings ->
             selectedTaxOption = taxSettings.defaultTaxOption
             selectedTaxValue = taxSettings.selectedTaxPercentage
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey("object") }?.apply {
        }

    }



}