package com.kreativesquadz.billkit.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.dantsu.escposprinter.exceptions.EscPosEncodingException
import com.dantsu.escposprinter.exceptions.EscPosParserException
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.repository.UserSettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class DantSuBluetoothService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userSettingRepository: UserSettingRepository
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothConnection: BluetoothConnection? = null
    private var printer: EscPosPrinter? = null
    private var connectedDevice: BluetoothDevice? = null

    // Scan for paired Bluetooth devices
    suspend fun scanDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT) || !hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            throw SecurityException("Bluetooth permissions not granted")
        }
        bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    // Connect to a Bluetooth device using DantSu's library
    suspend fun connectToDeviceByAddress(address: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("Bluetooth connect permission not granted")
            }
            val device = bluetoothAdapter?.getRemoteDevice(address)
            bluetoothConnection = BluetoothConnection(device)
            bluetoothConnection?.connect()
            
            printer = EscPosPrinter(bluetoothConnection, 203, 48f, 32) // Adjust DPI & size if needed
            connectedDevice = device

            updatePrinterSettings(address)
            true
        } catch (e: EscPosConnectionException) {
            e.printStackTrace()
            false
        }
    }

    fun getPrinter() : EscPosPrinter? {
        return printer
    }

    // Print data using DantSu library
    suspend fun printData(data: String) = withContext(Dispatchers.IO) {
        try {
            if (printer == null) throw IllegalStateException("Printer not connected")

            // Apply settings based on paper size (58mm or 80mm)
            val paperWidth = userSettingRepository.getPrinterSetting(Config.userId)?.printerSize ?: 58
            val maxCharsPerLine = if (paperWidth == 58) 32 else 48

            // Format data dynamically
            val formattedData = formatData(data, maxCharsPerLine)
            printer?.printFormattedText(formattedData)

            // ESC/POS command for auto-cut if enabled
            val settings = userSettingRepository.getPrinterSetting(Config.userId)
            if (settings != null && settings.autoCutAfterPrint) {
                printer?.printFormattedTextAndCut("[L]\n\n\n")  // Auto cut after print
            }

        } catch (e: EscPosParserException) {
            e.printStackTrace()
        } catch (e: EscPosEncodingException) {
            e.printStackTrace()
        } finally {
            closeConnection()
        }
    }

    // Close connection
    fun closeConnection() {
        bluetoothConnection?.disconnect()
        connectedDevice = null
    }


    // Helper: Update printer settings in the repository
    private suspend fun updatePrinterSettings(address: String) {
        val settings = userSettingRepository.getPrinterSetting(Config.userId)
        settings?.let {
            if (it.defaultPrinterAddress != address) {
                it.defaultPrinterAddress = address
                userSettingRepository.updatePrinterSetting(Config.userId, it.printerSize, it.printerMode, it.fontSize,
                    it.enableAutoPrint, it.openCashDrawer, it.disconnectAfterPrint, it.autoCutAfterPrint,
                    it.defaultPrinterAddress, it.defaultPrinterName)
            }
        }
    }

    // Helper: Format text for center alignment
    private fun formatData(data: String, maxChars: Int): String {
        val padding = (maxChars - data.length) / 2
        return " ".repeat(maxOf(padding, 0)) + data
    }

    // Permission check
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
