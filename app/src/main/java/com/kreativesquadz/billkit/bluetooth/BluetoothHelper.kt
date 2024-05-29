package com.kreativesquadz.billkit.bluetooth

import android.bluetooth.BluetoothDevice

interface BluetoothHelper {
    fun enableBluetooth(): Boolean
    fun disableBluetooth(): Boolean
    fun getPairedDevices(): Set<BluetoothDevice>
    fun connectToDevice(device: BluetoothDevice): Boolean
    fun printData(data: String): Boolean
    fun disconnect()
}
