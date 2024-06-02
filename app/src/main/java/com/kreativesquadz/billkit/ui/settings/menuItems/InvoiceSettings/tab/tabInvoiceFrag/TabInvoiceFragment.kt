package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.databinding.FragmentTabInvoiceBinding
import com.kreativesquadz.billkit.model.CompanyDetails

class TabInvoiceFragment : Fragment() {
        var _binding: FragmentTabInvoiceBinding? = null
        val binding get() = _binding!!

    private val viewModel: TabInvoiceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCompanyDetailsTab()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabInvoiceBinding.inflate(inflater, container, false)
        binding.companyDetails = viewModel.companyDetails.value?.data
        observers()
        onClickListeners()
        return binding.root
    }

    private fun observers(){
        viewModel.companyDetails.observe(viewLifecycleOwner) {
            it.data?.let {
                binding.companyDetails = it
            }

        }
    }

    private fun onClickListeners(){
        binding.btnupdate.setOnClickListener {
                val companyDetails = CompanyDetails(0,Config.userId, binding.etBusinessName.text.toString(),
                binding.etPlace.text.toString(), binding.etshopContactNumber.text.toString(),
                binding.etshopEmail.text.toString(),binding.etGSTNo.text.toString(),
                binding.etFSSAINo.text.toString(),binding.etCurrencySymbol.text.toString(),
                binding.tvInvoicePrefix.text.toString(), 2)
                viewModel.putCompanyObjDetails(companyDetails)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}