package com.kreativesquadz.billkit.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

class BluetoothService @Inject constructor(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null

    suspend fun scanDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("Bluetooth connect permission not granted")
        }

        val devices = mutableListOf<BluetoothDevice>()
        try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device -> devices.add(device) }
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw e
        }
        devices
    }

    suspend fun connectToDevice(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("Bluetooth connect permission not granted")
        }

        return@withContext try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connectedDevice = device
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun printData(data: String) = withContext(Dispatchers.IO) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("Bluetooth connect permission not granted")
        }

        try {
            outputStream?.write(data.toByteArray())
            outputStream?.write("\n\n\n".toByteArray())

            // ESC/POS command to cut the paper
            val cutCommand = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
            outputStream?.write(cutCommand)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
