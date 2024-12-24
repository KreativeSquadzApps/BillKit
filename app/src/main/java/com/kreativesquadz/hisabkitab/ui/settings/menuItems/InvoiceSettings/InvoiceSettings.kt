package com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericTabAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentInvoiceSettingsBinding
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag.TabInvoiceFragment
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabPdfFrag.TabPdfFrag
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabPrinterFrag.TabPrinterFrag

class InvoiceSettings : Fragment() {
    var _binding: FragmentInvoiceSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InvoiceSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
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
        val fragments = listOf(TabInvoiceFragment(), TabPrinterFrag(), TabPdfFrag())
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            when (position) {
                0 -> tabText.text = "Invoice"
                1 -> tabText.text = "Printer"
                2 -> tabText.text = "PDF"
                // Add more cases for additional tabs if needed
            }
            tabText.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.tab_text_color_selector))
            tab.customView = tabView
        }.attach()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}