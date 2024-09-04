package com.kreativesquadz.billkit.ui.daybook

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kreativesquadz.billkit.bluetooth.AndroidBluetoothConnection
import com.kreativesquadz.billkit.bluetooth.ConnectionError
import com.kreativesquadz.billkit.databinding.FragmentDayBookBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DayBookFrag : Fragment() {
    private var _binding: FragmentDayBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DayBookViewModel by viewModels()
    private lateinit var bluetoothConnection: AndroidBluetoothConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothConnection = AndroidBluetoothConnection(requireContext())
        bluetoothConnection.init()

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayBookBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            bluetoothConnection.connectionState.collect { connectionState ->
                updateUI(connectionState)
            }
        }

        // Set up button click listeners
        binding.scanButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                bluetoothConnection.scanForPrinters()
            }
        }

        binding.connectButton.setOnClickListener {
            bluetoothConnection.connect()
        }

        binding.disconnectButton.setOnClickListener {
            bluetoothConnection.disconnect()
        }

        binding.printButton.setOnClickListener {
            val toPrint = buildPrintData()
            bluetoothConnection.print(toPrint)
        }

        return binding.root
    }

    private fun updateUI(connectionState: com.kreativesquadz.billkit.bluetooth.ConnectionState) {
        binding.deviceNameTextView.text = connectionState.deviceName
        binding.progressBar.visibility = if (connectionState.isConnecting || connectionState.isPrinting) View.VISIBLE else View.GONE
        binding.scanButton.isEnabled = connectionState.isBluetoothReady && !connectionState.discoveredPrinter && !connectionState.isScanning
        binding.connectButton.isEnabled = !connectionState.isConnected && connectionState.discoveredPrinter
        binding.disconnectButton.isEnabled = connectionState.isConnected
        binding.printButton.isEnabled = connectionState.isConnected && !connectionState.isPrinting

        binding.errorMessageTextView.text = when (connectionState.bluetoothConnectionError) {
            ConnectionError.BLUETOOTH_DISABLED -> "Enable Bluetooth on device and click retry."
            ConnectionError.BLUETOOTH_PERMISSION -> "Switch on location access and click retry."
            ConnectionError.BLUETOOTH_NOT_SUPPORTED -> "Bluetooth is not supported on this device."
            ConnectionError.BLUETOOTH_PRINT_ERROR -> "Error while printing."
            ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND -> "No printer found."
            null -> ""
        }
    }

    private fun buildPrintData(): ByteArray {
        // Build print data (this is just a placeholder, implement as needed)
        return "Hello, World!".toByteArray()
    }
}