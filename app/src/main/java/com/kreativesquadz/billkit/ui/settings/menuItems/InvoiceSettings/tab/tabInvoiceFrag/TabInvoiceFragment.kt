package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.databinding.FragmentTabInvoiceBinding
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.UserSetting

class TabInvoiceFragment : Fragment() {
        var _binding: FragmentTabInvoiceBinding? = null
        val binding get() = _binding!!
    var isDiscount = 0
    var isReverse = 0

    private val viewModel: TabInvoiceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCompanyDetailsTab()
        viewModel.getUserSettings()
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
        viewModel.userSetting.observe(viewLifecycleOwner){
            it.let {
                if (it?.isdiscount==0){
                    binding.discountSwitch.isChecked = false
                    isDiscount = 0
                }else{
                    binding.discountSwitch.isChecked = true
                    isDiscount = 1
                }
                if (it?.isQtyReverse==0){
                    binding.reverseSwitch.isChecked = false
                    isReverse = 0
                }else{
                    binding.reverseSwitch.isChecked = true
                    isReverse = 1
                }
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

        binding.discountSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
           lateinit var userSetting : UserSetting
           if(isChecked) {
               isDiscount = 1
               userSetting = UserSetting(Config.userId,1,isReverse, "", "1")
            } else {
                 isDiscount = 0
                 userSetting = UserSetting(Config.userId,0, isReverse,"", "1")
            }
            viewModel.updateDiscount(requireContext(),userSetting,isChecked)
        }

        binding.reverseSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
           lateinit var userSetting : UserSetting
            if(isChecked) {
                 isReverse = 1
                 userSetting = UserSetting(Config.userId,isDiscount,1, "", "1")
            } else {
                 isReverse = 0
                 userSetting = UserSetting(Config.userId,isDiscount,0, "", "1")
            }
            viewModel.updateDiscount(requireContext(),userSetting,isChecked)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}