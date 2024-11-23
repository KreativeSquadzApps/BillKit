package com.kreativesquadz.billkit.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.repository.UserSettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import javax.inject.Inject



class BluetoothService @Inject constructor(
    private val context: Context,
    private val userSettingRepository: UserSettingRepository
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null

    // Scan for paired Bluetooth devices
    suspend fun scanDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) || !hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                throw SecurityException("Bluetooth permissions not granted")
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error scanning devices: ${e.message}")
        }
    }

    // Connect to a Bluetooth device by MAC address directly without pairing
    suspend fun connectToDeviceByAddress(address: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("Bluetooth connect permission not granted")
            }

            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device != null) {
                // Using insecure connection method to avoid pairing prompt
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                connectedDevice = device
                userSettingRepository.getPrinterSetting(Config.userId)?.let {
                    if (it.defaultPrinterAddress != address) {
                        it.defaultPrinterAddress = address
                        userSettingRepository.updatePrinterSetting(Config.userId, it.printerSize, it.printerMode,
                            it.fontSize, it.enableAutoPrint, it.openCashDrawer, it.disconnectAfterPrint,
                            it.autoCutAfterPrint, it.defaultPrinterAddress, it.defaultPrinterName)
                }
                }

                true
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error connecting to device: ${e.message}")
        }
    }

    // Print data using the connected device
    suspend fun printData(data: String) = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("Bluetooth connect permission not granted")
            }

            if (connectedDevice == null) {
                val defaultPrinterAddress = userSettingRepository.getPrinterSetting(Config.userId)?.defaultPrinterAddress
                if (defaultPrinterAddress != null) {
                    connectToDeviceByAddress(defaultPrinterAddress)
                } else {
                    throw IllegalStateException("No default printer found")
                }
            }

            // Print data using classic Bluetooth
            outputStream?.write(data.toByteArray())
            outputStream?.write("\n\n\n".toByteArray())

            // ESC/POS command to cut the paper
            val printerSettings = userSettingRepository.getPrinterSetting(Config.userId)
            if (printerSettings != null && printerSettings.autoCutAfterPrint) {
                val cutCommand = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
                outputStream?.write(cutCommand)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error printing data: ${e.message}")
        } finally {
            closeConnection() // Auto-disconnect after printing
        }
    }

    // Close the Bluetooth connection
    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            connectedDevice = null // Reset the connected device
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Check if the device is connected
    fun getConnectedDevice(): BluetoothDevice? {
        return connectedDevice
    }

    // Check if a specific permission is granted
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

