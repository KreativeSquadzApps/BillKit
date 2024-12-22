package com.kreativesquadz.billkit.ui.inventory.tab.product.add

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import com.kreativesquadz.billkit.BR
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.widget.SwitchCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mlkit.vision.common.InputImage
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.FragmentAddProductBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.utils.BarcodeScannerUtil
import com.kreativesquadz.billkit.utils.TaxType
import com.kreativesquadz.billkit.utils.collapse
import com.kreativesquadz.billkit.utils.expand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@AndroidEntryPoint
class AddProductFrag : Fragment() {
    private val viewModel: AddProductViewModel by viewModels()
    private var _binding: FragmentAddProductBinding? = null
    private lateinit var adapter: GenericAdapter<GST>
    private val binding get() = _binding!!
    val stockUnitList = listOf("Numbers (Nos)", "Kilogram (Kg)",
                               "Liter (L)", "Milliliter (ml)", "Bag (Bag)",
                                "Bundle (Bdl)", "Cans (Can)", "Case (Case)",
                                "Cartons (ctn)", "Dozen (Dzn)", "Meter (Mtr)",
                                "Packs (Pac)", "Piece (Pcs)", "Pair (Prs)",
                                "Quintal (Qtl)", "Roll (Rol)", "Square Feet (Sqf)",
                                "Square Meter (Sqm)", "Tablets (Tbs)","Jar (Jar)")


    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isCameraClicked = false
    private  var  cameraProvider: ProcessCameraProvider?=null
    private var isScanner = true
    private var isCameraStopped = false
    private var imageAnalysis: ImageAnalysis? = null
    private var selectedPosition: Int = -1 // Keeps track of selected switch position
    private var selectedTaxValue: Double? = null
    private var selectedTaxType: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Initialize the ActivityResultLauncher in onAttach or onCreate
        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, call the start() function
                startCamera()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCategories()
        viewModel.getProducts()
        viewModel.getGstTax()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        setupRecyclerView()
        onClickListeners()
        setupSpinner()
        setupSpinnerStockUnit(stockUnitList)
        setupSpinnerTaxType()
        observers()
        binding.isCameraOpen = isCameraClicked

        return binding.root
    }

    private fun observers(){
        viewModel.gstTax.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
            }
        }
        viewModel.products.observe(viewLifecycleOwner){
            Log.e("observe",it.data.toString())
        }

        viewModel.productsStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            if (it.invoiceId == 200){
                findNavController().popBackStack()
            }
        }

        viewModel.barcodeText.observe(viewLifecycleOwner){
            binding.etBarcode.setText(it)
            if (it.isNotEmpty()){
                binding.barcodeContainer.visibility = View.GONE
                isCameraClicked = false
                stopCamera()
                updateBarcodeIcon(isCameraClicked)
            }else{
                binding.barcodeContainer.visibility = View.VISIBLE
            }

        }

    }

    private fun onClickListeners(){
        binding.btnAdd.setOnClickListener{
            viewModel.addproductObj(requireContext(),binding.etProductName.text.toString(),getProduct())
        }
        binding.btnBarcode.setOnClickListener{
            if (!isCameraClicked) {
                checkCameraPermission()
                // Check and request permission when needed
            }else{
                isCameraClicked = false
                stopCamera()
            }
            binding.isCameraOpen = isCameraClicked
            updateBarcodeIcon(isCameraClicked)
        }
        binding.header.setOnClickListener {
            if (binding.dropdownContent.visibility == View.GONE) {
                binding.dropdownContent.expand()
                binding.header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
                binding.header.background = ContextCompat.getDrawable(requireContext(), R.color.white_n_black)
                binding.header.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

            } else {
                binding.dropdownContent.collapse()
                binding.header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                binding.header.background = ContextCompat.getDrawable(requireContext(), R.color.lite_grey_200)
                binding.header.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

        }
    }

    private fun getProduct():Product{
        return Product(userId = Config.userId,
            productName = binding.etProductName.text.toString().trim(),
            category = binding.dropdown.text.toString(),
            productPrice = binding.etProductPrice.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productCost = binding.etProductCost.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productMrp = binding.etProductMrp.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productBarcode = binding.etBarcode.text.toString().trim(),
            productStockUnit = binding.dropdownStockUnit.text.toString(),
            productTax = selectedTaxValue,
            productTaxType = binding.dropdownTaxType.text.toString(),
            productStock = binding.etCurrentStock.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            productDefaultQty = binding.etDefaultQty.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 1,
            isSynced = 0)
    }


    private fun setupSpinner() {
       val adapter  = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            liveDataItems = viewModel.getCategories()
        )
        binding.dropdown.setAdapter(adapter)
        binding.dropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinnerStockUnit(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownStockUnit.setAdapter(adapterStockUnit)
        binding.dropdownStockUnit.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupSpinnerTaxType() {
      val itemList = TaxType.getList().map{ it.displayName }
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownTaxType.setAdapter(adapterStockUnit)
        binding.dropdownTaxType.setText(itemList[0],false)

        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            when(selectedItem){
                "Price includes Tax" -> {
                    binding.recyclerView.visibility = View.VISIBLE
                }
                "Price is without Tax" -> {
                    binding.recyclerView.visibility = View.VISIBLE

                }
                "Zero Rated Tax" -> {

                    binding.recyclerView.visibility = View.GONE

                }
                "Exempt Tax" -> {
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }
    }


    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                isCameraClicked = true
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show rationale to the user, then request permission
                AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission Needed")
                    .setMessage("This app requires camera access to function properly.")
                    .setPositiveButton("OK") { _, _ ->
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                // Directly request permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (!isScanner || isCameraStopped) {
                            Log.d("ScannerState", "Scanner is not ready or camera stopped. Skipping image processing.")
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            BarcodeScannerUtil.getBarcodeScanner().process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        Log.e("barcode", barcode.rawValue.toString())
                                        viewModel.setBarcodeText(barcode.rawValue.toString() ?: "No Value")
                                        delay1sec()
                                    }
                                    imageProxy.close()
                                }
                                .addOnFailureListener {
                                    Log.e("BarcodeScanner", "Error processing barcode: ${it.message}")
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
                this.cameraProvider = cameraProvider // Save the reference for later use
                this.imageAnalysis = imageAnalysis // Save reference to the analysis
                isCameraStopped = false // Reset the flag when starting the camera
            } catch (exc: Exception) {
                Log.e("CameraSetup", "Exception: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCamera() {
        try {
            isCameraStopped = true // Flag to stop further processing
            imageAnalysis?.clearAnalyzer() // Remove the analyzer to stop processing frames
            cameraProvider?.unbindAll() // Unbind the camera provider
            Log.d("CameraState", "Camera stopped successfully.")
        } catch (exc: Exception) {
            Log.e("CameraState", "Error stopping the camera: ${exc.message}")
        }
    }


    private fun delay1sec() {
        isScanner = false
        lifecycleScope.launch {
            delay(1000) // Delay for 2 seconds
            isScanner = true // Resume scanning
        }
    }


    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.gstTax.value?.data ?: emptyList(),
            object : OnItemClickListener<GST> {
                override fun onItemClick(item: GST) {
                    val gstPosition = viewModel.gstTax.value?.data?.indexOf(item) ?: -1
                    val currentHolder = binding.recyclerView.findViewHolderForAdapterPosition(gstPosition)
                    if (currentHolder is GenericAdapter.ViewHolder<*>) {
                        val currentBinding = currentHolder.binding as ViewDataBinding
                        val currentSwitch = currentBinding.root.findViewById<SwitchCompat>(R.id.switchTax)

                        if (selectedPosition == gstPosition) {
                            // If the current item is already selected, unselect it
                            currentSwitch.isChecked = false
                            selectedPosition = -1 // Reset the selection
                        } else {
                            // Unselect previously selected switch if exists
                            if (selectedPosition != -1) {
                                val previousHolder = binding.recyclerView.findViewHolderForAdapterPosition(selectedPosition)
                                if (previousHolder is GenericAdapter.ViewHolder<*>) {
                                    val previousBinding = previousHolder.binding as ViewDataBinding
                                    val previousSwitch = previousBinding.root.findViewById<SwitchCompat>(R.id.switchTax)
                                    previousSwitch.isChecked = false // Uncheck the previous switch
                                }
                            }

                            // Set the current switch as selected
                            currentSwitch.isChecked = true
                            selectedPosition = gstPosition // Update the selected position
                            selectedTaxValue = item.taxAmount
                        }
                    }
                }

            },
            R.layout.item_tax_add_products,
            BR.gst // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    private fun updateBarcodeIcon(isCameraOpen:Boolean){
        if (isCameraOpen){
            binding.imgBarcode.setImageResource(R.drawable.baseline_close_24)
        }else{
            binding.imgBarcode.setImageResource(R.drawable.barcode)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}