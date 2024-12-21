package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabPdfFrag

import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.GenericDropDownToggle
import com.kreativesquadz.billkit.databinding.FragmentTabPdfBinding
import com.kreativesquadz.billkit.interfaces.FragmentBaseFunctions
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.ColorItem
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.utils.PdfColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabPdfFrag : Fragment(),FragmentBaseFunctions {
    private var _binding: FragmentTabPdfBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TabPdfViewModel by viewModels()

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
    private var selectedPdfColor : String ?= null
    private lateinit var adapter: GenericAdapter<ColorItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getPdfSetting()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabPdfBinding.inflate(inflater, container, false)
        observers()
        onClickListener()
        setupRecyclerView()


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
                    viewModel.pdfSettings.collect { pdfSettings ->
                        updateUI(pdfSettings)
                    }
                }
            }
        }

        val currentSettings = getPdfSettingObj()

        binding.etFooterText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (viewModel.isSettingsUpdated(viewModel.pdfSettings.value, currentSettings)){
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

    private fun handleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvUpdate.visibility = if (isLoading) View.GONE else View.VISIBLE
        if (isLoading) binding.progressBar.bringToFront()
    }

    private fun updateUI(settings: PdfSettings) {
        updateSwitches(settings)
        updateFooterText(settings)
        updatePdfColor(settings)
        updateIsUpdateEnabled(settings)
    }

    private fun updatePdfColor(settings: PdfSettings){
        selectedPdfColor = settings.pdfColor
        when(settings.pdfColor){
            PdfColor.RED.toString() -> binding.selectedColor = Color.RED
            PdfColor.GREEN.toString() -> binding.selectedColor = Color.GREEN
            PdfColor.BLUE.toString() -> binding.selectedColor = Color.BLUE
            PdfColor.YELLOW.toString()-> binding.selectedColor = Color.YELLOW
            PdfColor.GRAY.toString() -> binding.selectedColor = Color.GRAY
            else -> binding.selectedColor = Color.GRAY
        }
    }

    private fun updateSwitches(settings: PdfSettings) {
        updateSwitchGroup(
            settings.pdfCompanyInfo,
            listOf(
                binding.switchCompanyLogo to { isCompanyLogo = it },
                binding.switchCompanyEmail to { isCompanyEmail = it },
                binding.switchCompanyPhone to { isCompanyPhone = it },
                binding.switchCompanyGst to { isCompanyGst = it }
            )
        )

        updateSwitchGroup(
            settings.pdfItemTable,
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

    private fun updateFooterText(settings: PdfSettings) {
        binding.etFooterText.setText(settings.pdfFooter.takeIf { it.isNotBlank() } ?: "")
    }

    private fun updateIsUpdateEnabled(settings: PdfSettings) {
        isUpdateEnable = !getPdfSettingObj().isContentEqual(settings)
        binding.isUpdateEnable = isUpdateEnable
    }

    override fun onClickListener() {
        binding.btnupdate.setOnClickListener {
            if (isUpdateEnable) {
                val currentSettings = getPdfSettingObj()
                if (viewModel.isSettingsUpdated(viewModel.pdfSettings.value, currentSettings)) {
                    viewModel.updateSettings(currentSettings)
                    Toast.makeText(requireContext(), "Settings Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setupDropdownToggles()
        setupSwitches()
    }

    private fun setupDropdownToggles() {
        GenericDropDownToggle(binding.header, binding.dropdownContent, binding.layout, R.drawable.ic_arrow_up, R.drawable.ic_arrow_down,
            onStateChange = { isExpanded ->
            viewModel.setExpandedState(isExpanded)
        }).initialize()
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
            val currentSettings = getPdfSettingObj()
            if (viewModel.isSettingsUpdated(viewModel.pdfSettings.value, currentSettings)){
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

    private fun getPdfSettingObj(): PdfSettings {
        return PdfSettings(
            id = 0,
            userId = Config.userId,
            pdfCompanyInfo = "$isCompanyLogo $isCompanyEmail $isCompanyPhone $isCompanyGst",
            pdfItemTable = "$isItemTableCustomerDetails $isItemTableMrp $isItemTablePayment $isItemTableQty",
            pdfFooter = binding.etFooterText.text.toString(),
            pdfColor = selectedPdfColor
        )
    }

    private fun setupRecyclerView() {
        val colors = listOf(
            ColorItem(Color.RED,"RED"),
            ColorItem(Color.GREEN,"GREEN"),
            ColorItem(Color.BLUE,"BLUE"),
            ColorItem(Color.YELLOW,"YELLOW"),
            ColorItem(Color.GRAY,"GRAY")
        )
        adapter = GenericAdapter(
            colors,

            object : OnItemClickListener<ColorItem> {
                override fun onItemClick(item: ColorItem) {
                        binding.selectedColor = item.color
                        selectedPdfColor = item.colorName
                    if (viewModel.isSettingsUpdated(viewModel.pdfSettings.value, getPdfSettingObj())){
                        isUpdateEnable = true
                        binding.isUpdateEnable = isUpdateEnable
                    }else{
                        isUpdateEnable = false
                        binding.isUpdateEnable = isUpdateEnable
                    }
                }
            },
            R.layout.item_colors,
            BR.colorItem // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}