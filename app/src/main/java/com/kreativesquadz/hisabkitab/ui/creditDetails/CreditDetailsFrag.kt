package com.kreativesquadz.hisabkitab.ui.creditDetails

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
import com.kreativesquadz.hisabkitab.databinding.FragmentCreditDetailsBinding
import com.kreativesquadz.hisabkitab.ui.creditDetails.sales.SalesFrag
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditDetailsFrag : Fragment() {
    private var _binding: FragmentCreditDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreditDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditDetailsBinding.inflate(inflater, container, false)
        setupTabs()
        observers()
        onClickListener()
        return binding.root
    }

    private fun observers() {


    }

    private fun onClickListener() {


    }

    private fun setupTabs() {
        val fragments = listOf(SalesFrag())
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            tabText.text = when (position) {
                0 -> "Sales"
                else -> ""
            }
            tabText.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.tab_text_color_selector))
            tab.customView = tabView
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}