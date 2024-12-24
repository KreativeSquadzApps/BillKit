package com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings

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
import com.kreativesquadz.hisabkitab.databinding.FragmentTaxSettingsBinding
import com.kreativesquadz.hisabkitab.interfaces.FragmentBaseFunctions
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings.tab.tabTaxes.TaxesFragment
import com.kreativesquadz.hisabkitab.ui.settings.menuItems.taxSettings.tab.taxQuickSell.TaxForQuickSellFragment

class TaxSettingsFragment : Fragment(), FragmentBaseFunctions {
    private val viewModel: TaxSettingsViewModel by viewModels()
    private var _binding: FragmentTaxSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxSettingsBinding.inflate(inflater, container, false)
        tabSetup()
        observers()
        onClickListener()


        return binding.root
    }

   override fun observers() {

    }

    override fun onClickListener() {

    }

    fun tabSetup(){
        val fragments = listOf(TaxesFragment(), TaxForQuickSellFragment()) // Replace with your fragments
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            when (position) {
                0 -> tabText.text = "Taxes"
                1 -> tabText.text = "Tax For Quick Sale"
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