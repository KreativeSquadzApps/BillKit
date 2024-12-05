package com.kreativesquadz.billkit.ui.customerManag.createCustomer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.billkit.databinding.FragmentCreateCustomerBinding
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel

class CreateCustomerFrag : Fragment() {
    var _binding: FragmentCreateCustomerBinding? = null
    val binding get() = _binding!!
    private val viewModel: CreateCustomerViewModel by activityViewModels()
    val sharedViewModel: SharedViewModel by activityViewModels()

    private val target by lazy{
        arguments?.getString("target")
    }

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                getContactDetails(contactUri)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCustomerBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.resetValue = ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observers()
        onClickListeners()
    }

    private fun onClickListeners(){
        binding.loadContact.setOnClickListener {
            openContactPicker()
        }
        binding.btnSubmit.setOnClickListener {
            val contactNumber = binding.etShopContactNumber.text.toString().trim()
            val gstNumber = binding.etGSTNo.text.toString().trim().uppercase()

            if (contactNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter contact number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (gstNumber.isNotEmpty() && !isValidGstNumber(gstNumber)) {
                Toast.makeText(requireContext(), "Please enter a valid GST number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addCustomerObj(getCustomerData(), requireContext()){ customer ->
                if (customer == null) {
                    Toast.makeText(context, "Customer with this name or number already exists", Toast.LENGTH_SHORT).show()
                } else {
                    if (!target.isNullOrEmpty()){

                        sharedViewModel.updateSelectedCustomer(customer)
                    }
                    binding.resetValue = ""
                    Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }


        }
    }

    private fun getCustomerData(): Customer {
        return Customer(0,binding.etCustomerName.text.toString(),
                        binding.etShopContactNumber.text.toString(),
                        binding.etGSTNo.text.toString().uppercase(),
                        binding.etTotalSales.text.toString(),
                        binding.etAddress.text.toString(),
                        binding.etCreditAmount.text.toString(),0)
    }
    private fun observers(){
        viewModel.customer.observe(viewLifecycleOwner) {
            Log.e("Customerllll",it.data.toString())

        }
        viewModel.customerStatus.observe(viewLifecycleOwner) { result ->

        }
    }
    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }

    @SuppressLint("Range")
    private fun getContactDetails(contactUri: Uri) {
        val cursor = requireActivity().contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("Contact", "Name: $name, Phone: $phoneNumber")
                binding.etShopContactNumber.setText(phoneNumber.replace(" ","").replace("-",""))
                binding.etCustomerName.setText(name)
                // Display the contact details in a TextView, Toast, etc.
            }
        }
    }

    fun isValidGstNumber(gstNumber: String): Boolean {
        val gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$".toRegex()
        return gstNumber.matches(gstRegex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}