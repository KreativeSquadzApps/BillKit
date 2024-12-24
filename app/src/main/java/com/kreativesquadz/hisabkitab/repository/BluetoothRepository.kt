package com.kreativesquadz.hisabkitab.repository

import android.bluetooth.BluetoothDevice
import com.kreativesquadz.hisabkitab.bluetooth.BluetoothService
import javax.inject.Inject


class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService) {

    // Fetch a list of paired devices
    suspend fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothService.scanDevices()
    }

    // Connect to a specific device
    suspend fun connectToDevice(address : String): Boolean {
        return bluetoothService.connectToDeviceByAddress(address)
    }

    // Print data
    suspend fun printData(data: ByteArray) {
        bluetoothService.printData(data)
    }

    // Close the current Bluetooth connection
    fun closeConnection() {
        bluetoothService.closeConnection()
    }


}
