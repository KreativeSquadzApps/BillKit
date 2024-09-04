package com.kreativesquadz.billkit.ui.bluetoothPrinter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentBluetoothDeviceBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BluetoothDeviceFragment : Fragment() {
    private var _binding: FragmentBluetoothDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BluetoothDeviceViewModel by viewModels()
    private lateinit var adapter: GenericAdapter<BluetoothDevice>
    private var thermalPrinterSetup : ThermalPrinterSetup? = null
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.scanDevices()
        } else {
            handlePermissionsDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getPrinterSetting()
        if (arePermissionsGranted()) {
            viewModel.scanDevices()
        } else {
            requestPermissions()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothDeviceBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun observers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        //handleLoadingState(isLoading)
                    }
                }
                launch {
                    viewModel.printerSettings.collect { printerSettings ->
                        thermalPrinterSetup = printerSettings
                    }
                }
            }
        }
        viewModel.pairedDevices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
        }

        viewModel.connectingDevice.observe(viewLifecycleOwner) { device ->
            if (device != null) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            if (connected) {
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Connection Error", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.connectionError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), "Connection Error: $error", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
        viewModel.defaultPrinter.observe(viewLifecycleOwner) { printer ->
            if (printer != null) {
                if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    viewModel.updateSettings(thermalPrinterSetup?.copy(defaultPrinterName = printer,
                        defaultPrinterAddress = printer)!!)
                } else {
                    requestPermissions()
                }
            }

        }

    }

    private fun onClickListeners() {
        binding.tvRefresh.setOnClickListener {
            if (arePermissionsGranted()) {
                viewModel.scanDevices()
            } else {
                requestPermissions()
            }
        }

    }


    private fun arePermissionsGranted(): Boolean {
        return permissions.all { checkPermission(it) }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(permissions)
    }

    private fun handlePermissionsDenied() {
        // Notify the user that the required permissions are denied
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.pairedDevices.value ?: emptyList(),
            object : OnItemClickListener<BluetoothDevice> {
                override fun onItemClick(item: BluetoothDevice) {
                    if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        viewModel.connectToDevice(item)
                    } else {
                        requestPermissions()
                    }
                }
            },
            R.layout.item_bluetooth_device,
            BR.device // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


