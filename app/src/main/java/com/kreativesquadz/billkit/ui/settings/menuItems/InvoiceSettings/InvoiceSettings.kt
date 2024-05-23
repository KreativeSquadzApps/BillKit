package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.kreativesquadz.billkit.adapter.GenericTabAdapter
import com.kreativesquadz.billkit.databinding.FragmentInvoiceSettingsBinding
import com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag.TabInvoiceFragment
import com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabPdfFrag.TabPdfFrag
import com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabPrinterFrag.TabPrinterFrag

class InvoiceSettings : Fragment() {
    var _binding: FragmentInvoiceSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InvoiceSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabSetup()
    }

    fun tabSetup(){
        val fragments = listOf(TabInvoiceFragment(), TabPrinterFrag(), TabPdfFrag()) // Replace with your fragments
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Invoice"
                1 -> tab.text = "Thermal Printer"
                2 -> tab.text = "PDF"
                // Add more cases for additional tabs if needed
            }
        }.attach()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}