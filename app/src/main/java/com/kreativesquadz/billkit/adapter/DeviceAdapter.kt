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
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.R

class DeviceAdapter(
    private val context: Context,
    private var devices: MutableList<BluetoothDevice>
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var connectingDevice: BluetoothDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        // Check for BLUETOOTH_CONNECT permission before accessing device name
        val deviceName = if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device.name ?: "Unknown Device"
        } else {
            "Permission Required"
        }

        holder.nameTextView.text = deviceName
        holder.statusTextView.text = when (device) {
            connectingDevice -> "Connecting..."
            connectedDevice -> "Connected"
            else -> ""
        }
    }

    override fun getItemCount(): Int = devices.size

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

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(android.R.id.text1)
        val statusTextView: TextView = view.findViewById(android.R.id.text2)
    }
}

