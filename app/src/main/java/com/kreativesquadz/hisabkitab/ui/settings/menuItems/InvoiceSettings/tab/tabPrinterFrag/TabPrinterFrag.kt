package com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabPrinterFrag

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericDropDownToggle
import com.kreativesquadz.hisabkitab.databinding.FragmentTabPrinterBinding
import com.kreativesquadz.hisabkitab.interfaces.FragmentBaseFunctions
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabPrinterFrag : Fragment(),FragmentBaseFunctions {
    private var _binding: FragmentTabPrinterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TabPrinterViewModel by viewModels()
    private var isCompanyLogo = 0
    private var isCompanyEmail = 0
    private var isCompanyPhone = 0
    private var isCompanyAddress = 0
    private var isCompanyGst = 0
    private var isItemTableCustomerDetails = 0
    private var isItemTableMrp = 0
    private var isItemTablePayment = 0
    private var isItemTableQty = 0
    private var isUpdateEnable = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getInvoicePrinterSetting()
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        handleLoadingState(isLoading)
                    }
                }
                launch {
                    viewModel.invoicePrinterSettings.collect { invoicePrinterSettings ->
                        updateUI(invoicePrinterSettings)
                    }
                }
            }
        }

        val currentSettings = getInvoicePrinterSettingObj()

        binding.etFooterText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (viewModel.isInvoicePrinterSettingsUpdated(viewModel.invoicePrinterSettings.value, currentSettings)){
                    isUpdateEnable = true
                    binding.isUpdateEnable = isUpdateEnable
                }else{
                    isUpdateEnable = false
                    binding.isUpdateEnable = isUpdateEnable
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    override fun onClickListener() {
        binding.btnupdate.setOnClickListener {
            if (isUpdateEnable) {
                val currentSettings = getInvoicePrinterSettingObj()
                if (viewModel.isInvoicePrinterSettingsUpdated(viewModel.invoicePrinterSettings.value, currentSettings)) {
                    viewModel.updateInvoicePrinterSettings(currentSettings)
                    Toast.makeText(requireContext(), "Settings Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setupDropdownToggles()
        setupSwitches()
    }


    private fun handleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvUpdate.visibility = if (isLoading) View.GONE else View.VISIBLE
        if (isLoading) binding.progressBar.bringToFront()
    }

    private fun updateUI(settings: InvoicePrinterSettings) {
        updateSwitches(settings)
        updateFooterText(settings)
        updateIsUpdateEnabled(settings)
    }

    private fun updateSwitches(settings: InvoicePrinterSettings) {
        updateSwitchGroup(
            settings.printerCompanyInfo,
            listOf(
                binding.switchCompanyLogo to { isCompanyLogo = it },
                binding.switchCompanyEmail to { isCompanyEmail = it },
                binding.switchCompanyPhone to { isCompanyPhone = it },
                binding.switchCompanyGst to { isCompanyGst = it }
            )
        )

        updateSwitchGroup(
            settings.printerItemTable,
            listOf(
                binding.switchCustomerDetails to { isItemTableCustomerDetails = it },
                binding.switchMrp to { isItemTableMrp = it },
                binding.switchPaymentMode to { isItemTablePayment = it },
                binding.switchTotalQty to { isItemTableQty = it }
            )
        )
    }

    private fun updateSwitchGroup(values: String, switches: List<Pair<SwitchCompat, (Int) -> Unit>>) {
        if (values.isNotBlank()) {
            val splitValues = values.split(" ")
            switches.forEachIndexed { index, (switch, variable) ->
                updateSwitch(switch, splitValues.getOrNull(index) ?: "0", variable)
            }
        }
    }

    private fun updateSwitch(switch: SwitchCompat, value: String, variable: (Int) -> Unit) {
        val isChecked = value == "1"
        switch.isChecked = isChecked
        variable(if (isChecked) 1 else 0)
    }

    private fun updateFooterText(settings: InvoicePrinterSettings) {
        binding.etFooterText.setText(settings.printerFooter.takeIf { it.isNotBlank() } ?: "")
    }

    private fun updateIsUpdateEnabled(settings: InvoicePrinterSettings) {
        isUpdateEnable = !getInvoicePrinterSettingObj().isContentEqual(settings)
        binding.isUpdateEnable = isUpdateEnable
    }


    private fun setupDropdownToggles() {
        GenericDropDownToggle(binding.headerCompanyInfo, binding.dropdownContentCompanyInfo, binding.layoutCompanyInfo, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down
            ,onStateChange = { isExpanded ->
                viewModel.setExpandedState(isExpanded)
            }).initialize()
        GenericDropDownToggle(binding.headerItemTable, binding.dropdownContentItemTable, binding.layoutItemTable, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down,
            onStateChange = { isExpanded ->
                viewModel.setExpandedState(isExpanded)
            }).initialize()
        GenericDropDownToggle(binding.headerFooter, binding.dropdownContentFooter, binding.layoutFooter, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down, onStateChange = { isExpanded ->
            viewModel.setExpandedState(isExpanded)
        }).initialize()
    }

    private fun setupSwitch(switch: SwitchCompat, onCheckedChange: (Int) -> Unit) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(if (isChecked) 1 else 0)
            val currentSettings = getInvoicePrinterSettingObj()
            if (viewModel.isInvoicePrinterSettingsUpdated(viewModel.invoicePrinterSettings.value, currentSettings)){
                isUpdateEnable = true
                binding.isUpdateEnable = isUpdateEnable
            }else{
                isUpdateEnable = false
                binding.isUpdateEnable = isUpdateEnable
            }
        }
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchCompanyLogo) { isCompanyLogo = it }
        setupSwitch(binding.switchCompanyEmail) { isCompanyEmail = it }
        setupSwitch(binding.switchCompanyPhone) { isCompanyPhone = it }
        setupSwitch(binding.switchCompanyGst) { isCompanyGst = it }
        setupSwitch(binding.switchCustomerDetails) { isItemTableCustomerDetails = it }
        setupSwitch(binding.switchMrp) { isItemTableMrp = it }
        setupSwitch(binding.switchPaymentMode) { isItemTablePayment = it }
        setupSwitch(binding.switchTotalQty) { isItemTableQty = it }
    }

    private fun getInvoicePrinterSettingObj(): InvoicePrinterSettings {
        return InvoicePrinterSettings(
            id = 0,
            userId = Config.userId,
            printerCompanyInfo = "$isCompanyLogo $isCompanyEmail $isCompanyPhone $isCompanyGst",
            printerItemTable = "$isItemTableCustomerDetails $isItemTableMrp $isItemTablePayment $isItemTableQty",
            printerFooter = binding.etFooterText.text.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}