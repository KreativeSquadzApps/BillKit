package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.databinding.FragmentTabInvoiceBinding
import com.kreativesquadz.billkit.interfaces.OnTextChangedCallback
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.InvoicePrefixNumber
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.InvoiceSettingsDirections
import com.kreativesquadz.billkit.utils.setupTextWatcher
import kotlinx.coroutines.launch

class TabInvoiceFragment : Fragment(), OnTextChangedCallback {
    var _binding: FragmentTabInvoiceBinding? = null
    val binding get() = _binding!!
    var isDiscount = 0
    var isReverse = 0

    private val viewModel: TabInvoiceViewModel by activityViewModels()
    lateinit var userSetting : UserSetting
    private var oldCompanyDetails : CompanyDetails ?= null
    private var isUpdateEnable = false
    private var businessImage  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel.getCompanyDetailsSetting()
        //tabInvoiceSettingsViewModel.getInvoicePrefixNumberList()
        viewModel.getCompanyDetailsTab()
        viewModel.getUserSettings()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabInvoiceBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        observers()
        setupPermissionLauncher()
        onClickListeners()
        setupEditTextWatchers()
        return binding.root
    }

    private fun observers(){
        viewModel.selectedImageUri.observe(viewLifecycleOwner){ uri ->
            uri?.let {
                if (it.toString().isNotEmpty()){
                    businessImage = it.toString()
                    binding.imageView.setImageURI(it)
                }
                viewModel.companyDetails.let {
                    it.value?.data?.let {
                        if (viewModel.isCompanyDetailsUpdated(it, getCompanyDetails())){
                            isUpdateEnable = true
                            binding.isUpdateEnable = isUpdateEnable
                        }else{
                            isUpdateEnable = false
                            binding.isUpdateEnable = isUpdateEnable

                        }
                    }
                }


            }
        }

        viewModel.companyDetails.observe(viewLifecycleOwner) {
            it.data?.let {
                oldCompanyDetails = it
                binding.companyDetails = oldCompanyDetails
                binding.tvInvoicePrefix.text = it.InvoicePrefix
                binding.tvInvoiceNumber.text = it.InvoiceNumber.toString()
                businessImage = it.BusinessImage

                //tabInvoiceSettingsViewModel.getInvoicePrefixNumber(it.InvoicePrefix)
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

    private fun setupEditTextWatchers() {
        setupTextWatcher(binding.etBusinessName, this)
        setupTextWatcher(binding.etPlace, this)
        setupTextWatcher(binding.etshopContactNumber, this)
        setupTextWatcher(binding.etshopEmail, this)
        setupTextWatcher(binding.etGSTNo, this)
        setupTextWatcher(binding.etFSSAINo, this)
        setupTextWatcher(binding.etCurrencySymbol, this)
    }
    override fun onTextChanged() {
        viewModel.companyDetails.let {
            it.value?.data?.let {
                if (viewModel.isCompanyDetailsUpdated(it, getCompanyDetails())){
                    isUpdateEnable = true
                    binding.isUpdateEnable = isUpdateEnable
                }else{
                    isUpdateEnable = false
                    binding.isUpdateEnable = isUpdateEnable

                }
            }
        }

    }

    private fun onClickListeners(){
        binding.imageView.setOnClickListener {
            viewModel.selectImage(requireContext())
        }
        binding.btnReset.setOnClickListener{
            val action = InvoiceSettingsDirections.actionInvoiceSettingsToInvoiceResetFragment(oldCompanyDetails?.InvoicePrefix ?: "",oldCompanyDetails?.InvoiceNumber ?: 0)
            findNavController().navigate(action)

        }
        binding.btnupdate.setOnClickListener {
            if (isUpdateEnable) {
                val currentSettings = getCompanyDetails()
                viewModel.companyDetails.value?.data?.let {
                    if (viewModel.isCompanyDetailsUpdated(it, currentSettings)) {
                        viewModel.updateCompanyDetailsSettings(currentSettings)
                        Toast.makeText(requireContext(), "Settings Updated", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            userSetting = UserSetting(Config.userId,isDiscount,isReverse,"", "1")
            viewModel.updateDiscount(requireContext(),userSetting)
        }


        binding.discountSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
           lateinit var userSetting : UserSetting
           if(isChecked) {
               isDiscount = 1
             //  userSetting = UserSetting(Config.userId,1,isReverse, "", "1")
            } else {
                 isDiscount = 0
              //   userSetting = UserSetting(Config.userId,0, isReverse,"", "1")
            }
          //  viewModel.updateDiscount(requireContext(),userSetting,isChecked)
        }

        binding.reverseSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked) {
                 isReverse = 1
               //  userSetting = UserSetting(Config.userId,isDiscount,1, "", "1")
            } else {
                 isReverse = 0
                // userSetting = UserSetting(Config.userId,isDiscount,0, "", "1")
            }
           // viewModel.updateDiscount(requireContext(),userSetting,isChecked)
        }

    }


    private fun getCompanyDetails() : CompanyDetails{
        return CompanyDetails(oldCompanyDetails?.id ?: 0,Config.userId, binding.etBusinessName.text.toString(),businessImage,
            binding.etPlace.text.toString(), binding.etshopContactNumber.text.toString(),
            binding.etshopEmail.text.toString(),binding.etGSTNo.text.toString(),
            binding.etFSSAINo.text.toString(),binding.etCurrencySymbol.text.toString(),
            oldCompanyDetails?.InvoicePrefix ?: "", oldCompanyDetails?.InvoiceNumber ?: 0)
    }
    private fun setupPermissionLauncher() {
        // Handle the permission request
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.selectImage(requireContext())
            }
        }

        // Handle the image selection result
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                viewModel.setImageUri(imageUri)
            }
        }

        viewModel.init(activityResultLauncher, permissionLauncher)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}