package com.kreativesquadz.billkit.adapter
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.bluetooth.BluetoothDevice
import androidx.core.content.ContextCompat

class DeviceAdapter(context: Context, private val devices: MutableList<BluetoothDevice>) :
    ArrayAdapter<BluetoothDevice>(context, 0, devices) {

    private var connectingDevice: BluetoothDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        val nameTextView = view.findViewById<TextView>(android.R.id.text1)
        val statusTextView = view.findViewById<TextView>(android.R.id.text2)

        // Check for BLUETOOTH_CONNECT permission before accessing device name
        val deviceName = if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device?.name ?: "Unknown Device"
        } else {
            "Permission Required"
        }

        nameTextView.text = deviceName
        statusTextView.text = when (device) {
            connectingDevice -> "Connecting..."
            connectedDevice -> "Connected"
            else -> ""
        }

        return view
    }

    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    fun setConnectingDevice(device: BluetoothDevice?) {
        connectingDevice = device
        notifyDataSetChanged()
    }

    fun setConnectedDevice(device: BluetoothDevice?) {
        connectedDevice = device
        notifyDataSetChanged()
    }
}
