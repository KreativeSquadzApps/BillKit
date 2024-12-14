package com.kreativesquadz.billkit.ui.settings.menuItems.posSettings

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.databinding.FragmentPosSettingsBinding
import com.kreativesquadz.billkit.model.settings.POSSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PosSettingsFrag : Fragment() {
    private var _binding: FragmentPosSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PosSettingsViewModel by viewModels()
    private var isEnableCashBalance = false
    private var isBlockOutOfStock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        viewModel.getPosSetting()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPosSettingsBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        setupSwitches()
        return binding.root
    }

    private fun observers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                    }
                }
                launch {
                    viewModel.posSettings.collect { printerSettings ->
                        updateUI(printerSettings)
                    }
                }
            }
        }
    }
    private fun onClickListeners() {

    }
    private fun updateUI(settings: POSSettings) {
        updateSwitches(settings)
    }


    private fun updateSwitches(settings: POSSettings) {
        updateSwitch(binding.switchEnableCashBalance, settings.isEnableCashBalance) { isEnableCashBalance = it }
        updateSwitch(binding.switchBlockOutOfStock, settings.isBlockOutOfStock) { isBlockOutOfStock = it }
        isEnableCashBalance = settings.isEnableCashBalance
        isBlockOutOfStock = settings.isBlockOutOfStock

    }

    private fun updateSwitch(switch: SwitchCompat, value: Boolean, onCheckedChange: (Boolean) -> Unit) {
        switch.isChecked = value
        onCheckedChange(value)
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchEnableCashBalance) { isEnableCashBalance = it }
        setupSwitch(binding.switchBlockOutOfStock) { isBlockOutOfStock = it }
    }

    private fun setupSwitch(switch: SwitchCompat, onCheckedChange: (Boolean) -> Unit) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(isChecked)
           viewModel.updatePosSettings(getPosSettingObj())
        }
    }
    private fun getPosSettingObj(): POSSettings {
        return POSSettings(
            id = 0,
            userId = Config.userId,
            isEnableCashBalance = isEnableCashBalance,
            isBlockOutOfStock = isBlockOutOfStock
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}