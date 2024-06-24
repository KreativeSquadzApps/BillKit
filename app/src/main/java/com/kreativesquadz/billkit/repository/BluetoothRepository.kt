package com.kreativesquadz.billkit.repository

import android.bluetooth.BluetoothDevice
import com.kreativesquadz.billkit.bluetooth.BluetoothService
import javax.inject.Inject


class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService){

    suspend fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothService.scanDevices()
    }

    suspend fun connectToDevice(device: BluetoothDevice): Boolean {
        return bluetoothService.connectToDevice(device)
    }

    suspend fun printData(data: String) {
        bluetoothService.printData(data)
    }

    fun closeConnection() {
        bluetoothService.closeConnection()
    }
}
