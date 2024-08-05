package com.kreativesquadz.billkit.ui.settings.menuItems.taxSettings.tab.taxQuickSell

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R

class TaxForQuickSellFragment : Fragment() {

    companion object {
        fun newInstance() = TaxForQuickSellFragment()
    }

    private val viewModel: TaxForQuickSellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tax_for_quick_sell, container, false)
    }
}