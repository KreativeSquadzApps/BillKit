package com.kreativesquadz.billkit.repository

import android.bluetooth.BluetoothDevice
import com.dantsu.escposprinter.EscPosPrinter
import com.kreativesquadz.billkit.bluetooth.BluetoothService
import com.kreativesquadz.billkit.bluetooth.DantSuBluetoothService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService,
                                              private val dantSuBluetoothService: DantSuBluetoothService) {

    // Fetch a list of paired devices
    suspend fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothService.scanDevices()
    }

    // Connect to a specific device
    suspend fun connectToDevice(address : String): Boolean {
        return bluetoothService.connectToDeviceByAddress(address)
    }

    // Print data
    suspend fun printData(data: String) {
        bluetoothService.printData(data)
    }

    // Close the current Bluetooth connection
    fun closeConnection() {
        bluetoothService.closeConnection()
    }

    fun getPrinter() : EscPosPrinter? {
        return dantSuBluetoothService.getPrinter()
    }



}
