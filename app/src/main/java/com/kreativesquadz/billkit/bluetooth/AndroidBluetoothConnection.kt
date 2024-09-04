package com.kreativesquadz.billkit.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.util.*

class AndroidBluetoothConnection(val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> get() = _connectionState

    // UUID for SPP (Serial Port Profile)
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun init() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                _connectionState.value = _connectionState.value.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_DISABLED)
            } else {
                _connectionState.value = _connectionState.value.copy(isBluetoothReady = true)
            }
        } else {
            _connectionState.value = _connectionState.value.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_NOT_SUPPORTED)
        }
    }

    fun scanForPrinters() {
        if (_connectionState.value.isBluetoothReady) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            if (!pairedDevices.isNullOrEmpty()) {
                for (device in pairedDevices) {
                    if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING ||
                        device.name.contains("printer", ignoreCase = true)) {
                        _connectionState.value = _connectionState.value.copy(deviceName = device.name, discoveredPrinter = true)
                        bluetoothDevice = device
                        break
                    }
                }
            }

            if (bluetoothDevice != null) {
                _connectionState.value = _connectionState.value.copy(discoveredPrinter = true)
            } else {
                _connectionState.value = _connectionState.value.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND)
            }
        }
    }

    fun connect(specificPrinterAddress: String? = null) {
        _connectionState.value = _connectionState.value.copy(isConnecting = true, bluetoothConnectionError = null)

        if (!specificPrinterAddress.isNullOrBlank()) {
            val specificPrinter = bluetoothAdapter?.bondedDevices?.find { it.address == specificPrinterAddress }
            if (specificPrinter != null) {
                bluetoothDevice = specificPrinter
            } else {
                _connectionState.value = _connectionState.value.copy(isConnecting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND)
                return
            }
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            _connectionState.value = _connectionState.value.copy(isConnected = true, isConnecting = false)
        } catch (e: IOException) {
            e.printStackTrace()
            _connectionState.value = _connectionState.value.copy(isConnecting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINT_ERROR)
            disconnect()
        }

    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        _connectionState.value = _connectionState.value.copy(isConnected = false)
    }

    fun print(data: ByteArray) {
        if (_connectionState.value.isConnected) {
            try {
                bluetoothSocket?.outputStream?.write(data)
                _connectionState.value = _connectionState.value.copy(isPrinting = true)
            } catch (e: IOException) {
                e.printStackTrace()
                _connectionState.value = _connectionState.value.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINT_ERROR)
            } finally {
                _connectionState.value = _connectionState.value.copy(isPrinting = false)
            }
        }
    }
}

data class ConnectionState(
    val isBluetoothReady: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isPrinting: Boolean = false,
    val bluetoothConnectionError: ConnectionError? = null,
    val deviceName: String = "",
    val discoveredPrinter: Boolean = false,
    val isScanning: Boolean = false
)

enum class ConnectionError {
    BLUETOOTH_DISABLED,
    BLUETOOTH_PERMISSION,
    BLUETOOTH_NOT_SUPPORTED,
    BLUETOOTH_PRINT_ERROR,
    BLUETOOTH_PRINTER_DEVICE_NOT_FOUND
}
