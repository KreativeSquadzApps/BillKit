package com.kreativesquadz.hisabkitab.ui.settings.menuItems.printerSettings

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericSpinnerAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentPrinterSettingsBinding
import com.kreativesquadz.hisabkitab.interfaces.FragmentBaseFunctions
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PrinterSettingsFragment : Fragment(), FragmentBaseFunctions {

    private var _binding: FragmentPrinterSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrinterSettingsViewModel by viewModels()
    private var isUpdateEnabled = false

    // Printer settings variables
    private var isEnableAutoPrint = false
    private var isOpenCash = false
    private var isDisconnectAfterPrint = false
    private var isAutoCut = false
    private var printerAddress = ""
    private var printerName = ""
    private var printerSize : String ?= null

    val printerSizeList = listOf("58MM","80MM")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPrinterSetting()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrinterSettingsBinding.inflate(inflater, container, false)
        binding.isDefaultPrinterAdded = false
        observers()
        onClickListener()
        setupSwitches()
        return binding.root
    }

    override fun observers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        handleLoadingState(isLoading)
                    }
                }
                launch {
                    viewModel.printerSettings.collect { printerSettings ->
                        updateUI(printerSettings)
                    }
                }
            }
        }


    }

    override fun onClickListener() {
        binding.btnChangePrinter.setOnClickListener {
            findNavController().navigate(R.id.action_printerSettingsFragment_to_bluetoothDeviceFragment)
        }

        binding.btnupdate.setOnClickListener {
            if (isUpdateEnabled) {
                val currentSettings = getPrinterSettingObj()
                viewModel.updateSettings(currentSettings)
                Toast.makeText(requireContext(), "Settings Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(settings: ThermalPrinterSetup) {
        updateIsUpdateEnabled()
        updateSwitches(settings)
        binding.thermalPrinter = settings
        printerSize = settings.printerSize
        if (settings.defaultPrinterAddress.isNotEmpty()) {
            printerAddress = settings.defaultPrinterAddress
            printerName = settings.defaultPrinterName
            binding.isDefaultPrinterAdded = true
        }else{
            binding.isDefaultPrinterAdded = false
        }
        setupSpinnerPrinterSize(printerSizeList)
    }

    private fun updateIsUpdateEnabled() {
        val currentSettings = getPrinterSettingObj()
        isUpdateEnabled = viewModel.isSettingsUpdated(viewModel.printerSettings.value, currentSettings)
        binding.isUpdateEnable = isUpdateEnabled
    }

    private fun updateSwitches(settings: ThermalPrinterSetup) {
        updateSwitch(binding.switchEnableAutoPrint, settings.enableAutoPrint) { isEnableAutoPrint = it }
        updateSwitch(binding.switchOpenCashDrawer, settings.openCashDrawer) { isOpenCash = it }
        updateSwitch(binding.switchDisconnectAfterPrint, settings.disconnectAfterPrint) { isDisconnectAfterPrint = it }
        updateSwitch(binding.switchAutoCutAfterPrint, settings.autoCutAfterPrint) { isAutoCut = it }
        isEnableAutoPrint = settings.enableAutoPrint
        isOpenCash = settings.openCashDrawer
        isDisconnectAfterPrint = settings.disconnectAfterPrint
        isAutoCut = settings.autoCutAfterPrint
    }

    private fun updateSwitch(switch: SwitchCompat, value: Boolean, onCheckedChange: (Boolean) -> Unit) {
        switch.isChecked = value
        onCheckedChange(value)
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchEnableAutoPrint) { isEnableAutoPrint = it }
        setupSwitch(binding.switchOpenCashDrawer) { isOpenCash = it }
        setupSwitch(binding.switchDisconnectAfterPrint) { isDisconnectAfterPrint = it }
        setupSwitch(binding.switchAutoCutAfterPrint) { isAutoCut = it }
    }

    private fun setupSwitch(switch: SwitchCompat, onCheckedChange: (Boolean) -> Unit) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(isChecked)
            val currentSettings = getPrinterSettingObj()
            isUpdateEnabled = viewModel.isSettingsUpdated(viewModel.printerSettings.value, currentSettings)
            binding.isUpdateEnable = isUpdateEnabled
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvUpdate.visibility = if (isLoading) View.GONE else View.VISIBLE
        if (isLoading) binding.progressBar.bringToFront()
    }

    private fun getPrinterSettingObj(): ThermalPrinterSetup {
        return ThermalPrinterSetup(
            id = 0,
            userId = Config.userId,
            printerSize = printerSize ?: "80MM",
            printerMode = "CONFIG-1",
            fontSize = "Medium",
            enableAutoPrint = isEnableAutoPrint,
            openCashDrawer = isOpenCash,
            disconnectAfterPrint = isDisconnectAfterPrint,
            autoCutAfterPrint = isAutoCut,
            defaultPrinterAddress = printerAddress,
            defaultPrinterName = printerName,
        )
    }

    private fun setupSpinnerPrinterSize(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownPrinterSize.setText(printerSize, false)

        binding.dropdownPrinterSize.setAdapter(adapterStockUnit)
        binding.dropdownPrinterSize.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            binding.dropdownPrinterSize.setText(selectedItem, false)
            printerSize = selectedItem
            updateIsUpdateEnabled()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
