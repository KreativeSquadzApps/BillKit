package com.kreativesquadz.billkit.ui.bills.bluetoothPrinter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kreativesquadz.billkit.adapter.DeviceAdapter
import com.kreativesquadz.billkit.databinding.FragmentBluetoothDeviceBinding
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BluetoothDeviceFragment : Fragment() {
    private var _binding: FragmentBluetoothDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BluetoothDeviceViewModel by viewModels()
    val invoice by lazy {
        arguments?.getSerializable("invoice") as? Invoice
    }
    private var customer: Customer? = null
    lateinit var companyDetails: CompanyDetails
    lateinit var invoiceItems: List<InvoiceItem>


    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.scanDevices()
        } else {
            // Handle permission denial here
            handlePermissionsDenied()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCompanyDetailsRec()
        viewModel.fetchInvoiceItems(invoice?.id!!.toLong())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothDeviceBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        return binding.root
    }

   private fun observers(){

       viewModel.companyDetails.observe(viewLifecycleOwner){
           it.data?.let {
               companyDetails = it
           }
       }
       viewModel.invoiceItems.observe(viewLifecycleOwner){
           invoiceItems = it
           Log.e("invoiceItems",invoiceItems.toString())
       }
       invoice?.let {
         customer =  viewModel.getCustomerById(it.customerId.toString())
       }


       val deviceAdapter = DeviceAdapter(requireContext(), mutableListOf())
       binding.deviceList.adapter = deviceAdapter

       viewModel.pairedDevices.observe(requireActivity(), Observer { devices ->
           deviceAdapter.updateDevices(devices)
       })

       viewModel.connectingDevice.observe(requireActivity(), Observer { device ->
           deviceAdapter.setConnectingDevice(device)
       })

       viewModel.isConnected.observe(requireActivity()) { connected ->
           binding.printButton.isEnabled = connected
           if (connected) {
               deviceAdapter.setConnectedDevice(viewModel.connectingDevice.value)
           } else {
               deviceAdapter.setConnectedDevice(null)
               Toast.makeText(requireContext(), "Connection Error $connected", Toast.LENGTH_SHORT).show()
           }

       }

       viewModel.connectionError.observe(requireActivity()) { error ->
           // Handle connection error, possibly show a message to the user
           Toast.makeText(requireContext(), "Connection Error $error", Toast.LENGTH_SHORT).show()
       }

       viewModel.printStatus.observe(requireActivity()) { status ->
           // Handle print status, possibly show a message to the user
       }

       binding.deviceList.setOnItemClickListener { _, _, position, _ ->
           val selectedDevice = deviceAdapter.getItem(position)
           if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
               viewModel.connectToDevice(selectedDevice!!)
           } else {
               requestPermissions()
           }
       }


       viewModel.isConnected.observe(viewLifecycleOwner) { connected ->
           binding.printButton.isEnabled = connected
       }
   }

    private fun onClickListeners(){
        binding.scanButton.setOnClickListener {
            if (arePermissionsGranted()) {
                viewModel.scanDevices()

            } else {
                requestPermissions()
            }
        }

        binding.printButton.setOnClickListener {
            if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                val iscustomer: Boolean
                var timestamp = ""

                if (customer != null){
                    iscustomer = true
                }else{
                    iscustomer = false
                }

                try {
                    val date = Date(invoice!!.invoiceDate.toLong())
                    val format = SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.getDefault())
                    timestamp = format.format(date)
                } catch (e: Exception) {
                    timestamp = ""
                }
                val receipt = createReceiptString(
                    businessName = companyDetails.BusinessName,
                    place = companyDetails.Place,
                    contactNumber = companyDetails.ShopContactNumber,
                    email = companyDetails.ShopEmail,
                    invoiceDate = timestamp,
                    invoiceId = invoice!!.id.toString(),
                    isCustomerAvailable = iscustomer,
                    customerName = customer?.customerName,
                    customerNumber = customer?.shopContactNumber,
                    customerGst = customer?.gstNo,
                    customerAddress = customer?.address,
                    items = invoiceItems,
                    totalItems = invoiceItems.size,
                    subtotal = invoiceItems.sumOf { it.totalPrice },
                    discount = invoice!!.discount?.toDouble(),
                    totalAmount = invoice!!.totalAmount,
                    cashAmount = invoice!!.cashAmount
                )
                println(receipt)
                viewModel.printData(receipt)
            } else {
                requestPermissions()
            }
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return permissions.all {
            checkPermission(it)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(permissions)
    }

    private fun handlePermissionsDenied() {
        // Notify the user that the required permissions are denied
        // You can show a dialog or a Snackbar here
    }

    fun centerText(text: String, width: Int = 32): String {
        val padding = (width - text.length) / 2
        return if (padding > 0) {
            " ".repeat(padding) + text + " ".repeat(padding)
        } else {
            text
        }
    }

    fun formatItem(name: String, quantity: Int, rate: Double, total: Double): String {
        return String.format("%-10s %5d %7.2f %7.2f", name, quantity, rate, total)
    }

    fun createReceiptString(
        businessName: String,
        place: String,
        contactNumber: String,
        email: String,
        invoiceDate: String,
        invoiceId: String,
        isCustomerAvailable: Boolean,
        customerName: String?,
        customerNumber: String?,
        customerGst: String?,
        customerAddress: String?,
        items: List<InvoiceItem>,
        totalItems: Int,
        subtotal: Double,
        discount: Double?,
        totalAmount: Double,
        cashAmount: Double
    ): String {
        val receipt = StringBuilder()

        // Company Details Header
        receipt.append(centerText(businessName)).append("\n")
        receipt.append(centerText(place)).append("\n")
        receipt.append(centerText(contactNumber)).append("\n")
        receipt.append(centerText("Email: $email")).append("\n")
        receipt.append(centerText("INVOICE")).append("\n\n")

        // Invoice Header
        receipt.append("$invoiceDate    ")
        receipt.append("Invoice: $invoiceId").append("\n")
        receipt.append("--------------------------------\n")

        // Customer Details
        if (isCustomerAvailable) {
            receipt.append(centerText(customerName ?: "")).append("\n")
            receipt.append(centerText("Contact: ${customerNumber ?: ""}")).append("\n")
            receipt.append(centerText("GST No: ${customerGst ?: ""}")).append("\n")
            receipt.append(centerText(customerAddress ?: "")).append("\n")
            receipt.append("--------------------------------\n")
        }

        // Items Header
        receipt.append("Item     Qty    Rate    Total\n")
        receipt.append("--------------------------------\n")

        // Items
        for (item in items) {
            receipt.append(formatItem(item.itemName.split(" ")[0], item.quantity, item.unitPrice, item.totalPrice)).append("\n")
        }
        receipt.append("--------------------------------\n")

        // Totals
        receipt.append("Items: $totalItems\n")
        receipt.append("Sub Total: $subtotal\n")
        if (discount != null && discount > 0) {
            receipt.append("Discount: $discount\n")
            receipt.append("You saved Rs $discount on this purchase\n")
        }
        receipt.append("--------------------------------\n")
        receipt.append(centerText("Total: $totalAmount")).append("\n\n")
        receipt.append(centerText("Payment Mode")).append("\n")
        receipt.append(centerText("Cash: $cashAmount")).append("\n")
        receipt.append("--------------------------------\n")
        receipt.append(centerText("Thank You")).append("\n")

        return receipt.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}