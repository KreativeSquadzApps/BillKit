package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.databinding.FragmentTabInvoiceBinding
import com.kreativesquadz.billkit.interfaces.OnTextChangedCallback
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.settings.UserSetting
import com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.InvoiceSettingsDirections
import com.kreativesquadz.billkit.utils.Glide.GlideHelper
import com.kreativesquadz.billkit.utils.setupTextWatcher

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
    private var businessImageName  : String ?=null
    private var isImageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel.getCompanyDetailsSetting()
        //tabInvoiceSettingsViewModel.getInvoicePrefixNumberList()
        GlideHelper.initializeGlideWithOkHttp(requireContext())
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
                GlideHelper.loadImageWithLoader(requireContext(), it.BusinessImage, binding.imageView,binding.progressLoader)
                binding.companyDetails = oldCompanyDetails
                binding.tvInvoicePrefix.text = it.InvoicePrefix
                binding.tvInvoiceNumber.text = it.InvoiceNumber.toString()
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

        viewModel.isUploading.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                showLoader() // Show the loader when uploading
            } else {
                hideLoader() // Hide the loader when the upload finishes
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
            viewModel.selectImage()
        }
        binding.btnReset.setOnClickListener{
            val action = InvoiceSettingsDirections.actionInvoiceSettingsToInvoiceResetFragment(oldCompanyDetails?.InvoicePrefix ?: "",oldCompanyDetails?.InvoiceNumber ?: 0)
            findNavController().navigate(action)

        }
        binding.btnupdate.setOnClickListener {
            if (!isUpdateEnable) return@setOnClickListener

            val currentSettings = getCompanyDetails()
            val companyDetails = viewModel.companyDetails.value?.data ?: return@setOnClickListener

            if (!viewModel.isCompanyDetailsUpdated(companyDetails, currentSettings)) return@setOnClickListener

            viewModel.selectedImageUri.value?.let {
                handleImageUpload(currentSettings)
            } ?: updateCompanyDetails(currentSettings)
            updateUserSettings()
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
            viewModel.updateDiscount(requireContext(),userSetting)
        }

        binding.reverseSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked) {
                 isReverse = 1
                 userSetting = UserSetting(Config.userId,isDiscount,1, "", "1")
            } else {
                 isReverse = 0
                 userSetting = UserSetting(Config.userId,isDiscount,0, "", "1")
            }
            viewModel.updateDiscount(requireContext(),userSetting)
        }

    }

    private fun handleImageUpload(currentSettings: CompanyDetails) {
        viewModel.uploadImage(
            Config.userId.toString(),
            viewModel.selectedImageUri.value,
            requireContext(),
            currentSettings
        )
        viewModel.uploadStatus.observe(viewLifecycleOwner) { success ->
            if (success){
                showToast("Image Updated successfully")
                viewModel.removeImageUri()
            }else{
                showToast("Upload failed: Check Internet Connection")
            }

        }
    }

    private fun updateCompanyDetails(currentSettings: CompanyDetails) {
        viewModel.updateCompanyDetailsSettings(currentSettings)
        showToast("Settings Updated")
        binding.isUpdateEnable = false
    }
    private fun updateUserSettings() {
        val userSetting = UserSetting(Config.userId, isDiscount, isReverse, "", "1")
        viewModel.updateDiscount(requireContext(), userSetting)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getCompanyDetails() : CompanyDetails{
        businessImageName = businessImage.ifEmpty { oldCompanyDetails?.BusinessImage }
        return CompanyDetails(oldCompanyDetails?.id ?: 0,Config.userId, binding.etBusinessName.text.toString(),businessImageName ?:"",
            binding.etPlace.text.toString(), binding.etshopContactNumber.text.toString(),
            binding.etshopEmail.text.toString(),binding.etGSTNo.text.toString(),
            binding.etFSSAINo.text.toString(),binding.etCurrencySymbol.text.toString(),
            oldCompanyDetails?.InvoicePrefix ?: "", oldCompanyDetails?.InvoiceNumber ?: 0)
    }
    private fun setupPermissionLauncher() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.selectImage()
            }
        }
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                viewModel.setImageUri(imageUri)
            }
        }

        viewModel.init(activityResultLauncher, permissionLauncher)
    }
    fun showLoader() {
        isUpdateEnable = false
        binding.isUpdateEnable = isUpdateEnable
        binding.tvUpdate.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.tvUpdate.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }



}