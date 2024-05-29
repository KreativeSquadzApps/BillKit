package com.kreativesquadz.billkit.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothHelperImpl : BluetoothHelper {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun enableBluetooth(): Boolean {
        return bluetoothAdapter?.enable() ?: false
    }

    override fun disableBluetooth(): Boolean {
        return bluetoothAdapter?.disable() ?: false
    }

    override fun getPairedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }

    override fun connectToDevice(device: BluetoothDevice): Boolean {
        try {
            val uuid = device.uuids[0].uuid
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            return true
        } catch (e: IOException) {
            Log.e("BluetoothHelper", "Error connecting to device", e)
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Log.e("BluetoothHelper", "Error closing socket", closeException)
            }
            return false
        }
    }

    override fun printData(data: String): Boolean {
        return try {
            outputStream?.write(data.toByteArray())
            outputStream?.flush()
            true
        } catch (e: IOException) {
            Log.e("BluetoothHelper", "Error printing data", e)
            false
        }
    }

    override fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("BluetoothHelper", "Error disconnecting", e)
        }
    }
}
