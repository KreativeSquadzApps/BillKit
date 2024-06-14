package com.kreativesquadz.billkit.ui.inventory.tab.product.add

import android.Manifest
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
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.FragmentAddProductBinding
import com.kreativesquadz.billkit.model.Product
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors


@AndroidEntryPoint
class AddProductFrag : Fragment() {
    private val viewModel: AddProductViewModel by viewModels()
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    val stockUnitList = listOf("Numbers (Nos)", "Kilogram (Kg)",
                               "Liter (L)", "Milliliter (ml)", "Bag (Bag)",
                                "Bundle (Bdl)", "Cans (Can)", "Case (Case)",
                                "Cartons (ctn)", "Dozen (Dzn)", "Meter (Mtr)",
                                "Packs (Pac)", "Piece (Pcs)", "Pair (Prs)",
                                "Quintal (Qtl)", "Roll (Rol)", "Square Feet (Sqf)",
                                "Square Meter (Sqm)", "Tablets (Tbs)","Jar (Jar)")

    val taxTypeList = listOf("Price includes Tax",
        "Price is without Tax",
        "Zero Rated Tax",
        "Exempt Tax")
    private val requestCodeCameraPermission = 200
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isCameraClicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCategories()
        viewModel.getProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        onClickListeners()
        setupSpinner()
        setupSpinnerStockUnit(stockUnitList)
        setupSpinnerTaxType(taxTypeList)
        observers()
        binding.isCameraOpen = isCameraClicked

        return binding.root
    }

    private fun observers(){
        viewModel.products.observe(viewLifecycleOwner){
            Log.e("observe",it.data.toString())
        }

        viewModel.productsStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }

        viewModel.barcodeText.observe(viewLifecycleOwner){
            binding.etBarcode.setText(it)
        }

    }

    private fun onClickListeners(){
        binding.btnAdd.setOnClickListener{
            viewModel.addproductObj(requireContext(),binding.etProductName.text.toString(),getProduct())
        }
        binding.btnBarcode.setOnClickListener{
            if (!isCameraClicked) {
                isCameraClicked = true
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        requestCodeCameraPermission
                    )
                }
            }else{
                isCameraClicked = false
            }
            binding.isCameraOpen = isCameraClicked
        }
    }

    private fun getProduct():Product{
        return Product(userId = Config.userId,
            productName = binding.etProductName.text.toString(),
            category = binding.dropdown.text.toString(),
            productPrice = binding.etProductPrice.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productCost = binding.etProductCost.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productMrp = binding.etProductMrp.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productBarcode = binding.etBarcode.text.toString(),
            productStockUnit = binding.dropdownStockUnit.text.toString(),
            productTax = 0.0,
            productStock = binding.etCurrentStock.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            productDefaultQty = binding.etDefaultQty.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
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
    private fun setupSpinnerTaxType(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownTaxType.setAdapter(adapterStockUnit)
        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
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

            val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.TYPE_ISBN
                )
                .build()

            val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            processImage(image, barcodeScanner, imageProxy)
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                // Handle exceptions here
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImage(image: InputImage, barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    Log.e("barcode",barcode.rawValue.toString())
                    when (barcode.format) {
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.TYPE_ISBN -> {
                            viewModel.setBarcodeText(barcode.rawValue.toString() ?: "No Value")
                        }
                        else -> { /* Handle other barcode formats if needed */ }
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                // Handle the error
                imageProxy.close()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}