package com.kreativesquadz.billkit.ui.inventory

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericTabAdapter
import com.kreativesquadz.billkit.databinding.FragmentInventoryBinding
import com.kreativesquadz.billkit.ui.home.tab.quickSale.QuickSaleFragment
import com.kreativesquadz.billkit.ui.home.tab.sale.SaleFragment
import com.kreativesquadz.billkit.ui.home.tab.savedOrders.SavedOrdersFragment
import com.kreativesquadz.billkit.ui.inventory.tab.category.CategoryFrag
import com.kreativesquadz.billkit.ui.inventory.tab.product.ProductFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : Fragment() {
    private val viewModel: InventoryViewModel by viewModels()
    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        tabSetup()
        return binding.root
    }
    private  fun tabSetup(){
        val fragments = listOf(CategoryFrag(), ProductFragment()) // Replace with your fragments
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            when (position) {
                0 -> tabText.text = "Category"
                1 -> tabText.text = "Product"
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