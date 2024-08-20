package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabPrinterFrag

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.databinding.FragmentTabPrinterBinding
import com.kreativesquadz.billkit.interfaces.FragmentBaseFunctions
import com.kreativesquadz.billkit.utils.collapse
import com.kreativesquadz.billkit.utils.expand

class TabPrinterFrag : Fragment(),FragmentBaseFunctions {
    private var _binding: FragmentTabPrinterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TabPrinterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabPrinterBinding.inflate(inflater, container, false)

        observers()
        onClickListener()

        return binding.root
    }

    override fun observers() {
    }

    override fun onClickListener() {
        binding.headerCompanyInfo.setOnClickListener {
            if (binding.dropdownContentCompanyInfo.visibility == View.GONE) {
                binding.dropdownContentCompanyInfo.expand()
                binding.headerCompanyInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)

                binding.layoutCompanyInfo.setBackgroundColor(requireContext().getColor(R.color.lite_grey_200))
            } else {
                binding.dropdownContentCompanyInfo.collapse()
                binding.headerCompanyInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                binding.layoutCompanyInfo.setBackgroundColor(requireContext().getColor(R.color.white))

                // binding.headerText.text = "Click to Expand"
            }
        }
        binding.headerItemTable.setOnClickListener {
            if (binding.dropdownContentItemTable.visibility == View.GONE) {
                binding.dropdownContentItemTable.expand()
                binding.headerItemTable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
                binding.layoutItemTable.setBackgroundColor(requireContext().getColor(R.color.lite_grey_200))
            } else {
                binding.dropdownContentItemTable.collapse()
                binding.headerItemTable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                binding.layoutItemTable.setBackgroundColor(requireContext().getColor(R.color.white))

                // binding.headerText.text = "Click to Expand"
            }
        }
        binding.headerFooter.setOnClickListener {
            if (binding.dropdownContentFooter.visibility == View.GONE) {
                binding.dropdownContentFooter.expand()
                binding.headerFooter.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
                binding.layoutFooter.setBackgroundColor(requireContext().getColor(R.color.lite_grey_200))
                binding.etFooterText.isFocusable = true
            } else {
                binding.dropdownContentFooter.collapse()
                binding.headerFooter.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                binding.layoutFooter.setBackgroundColor(requireContext().getColor(R.color.white))

                // binding.headerText.text = "Click to Expand"
            }
        }
    }

}