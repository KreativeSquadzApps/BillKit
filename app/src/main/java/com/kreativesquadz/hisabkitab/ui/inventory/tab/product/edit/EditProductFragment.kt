package com.kreativesquadz.hisabkitab.ui.inventory.tab.product.edit

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import androidx.core.view.MenuProvider
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mlkit.vision.common.InputImage
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.adapter.GenericSpinnerAdapter
import com.kreativesquadz.hisabkitab.adapter.showCustomAlertDialog
import com.kreativesquadz.hisabkitab.databinding.FragmentEditProductBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.DialogData
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.utils.BarcodeScannerUtil
import com.kreativesquadz.hisabkitab.utils.TaxType
import com.kreativesquadz.hisabkitab.utils.collapse
import com.kreativesquadz.hisabkitab.utils.expand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@AndroidEntryPoint
class EditProductFragment : Fragment() {
    private val viewModel: EditProductViewModel by viewModels()
    private var _binding: FragmentEditProductBinding? = null
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

    val productEdit by lazy {
        arguments?.getSerializable("product") as? Product
    }
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
        viewModel.getGstTax()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.product = productEdit
        productEdit?.productTaxType?.let{
            selectedTaxType = it
        }
        setupRecyclerView()
        onClickListeners()
        setupSpinner()
        setupSpinnerStockUnit(stockUnitList)
        observers()
        setupSpinnerTaxType()
        binding.isCameraOpen = isCameraClicked
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onOptionMenu()
    }


    private fun observers(){
        viewModel.gstTax.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
                productEdit?.productTax?.let { savedTaxValue ->
                    // Find the position of the saved GST item
                    selectedPosition = it.indexOfFirst { it.taxAmount == savedTaxValue }

                    if (selectedPosition != -1) {
                        // Ensure that the correct switch is checked if a GST is pre-selected
                        binding.recyclerView.post {
                            val preselectedHolder = binding.recyclerView.findViewHolderForAdapterPosition(selectedPosition)
                            if (preselectedHolder is GenericAdapter.ViewHolder<*>) {
                                val preselectedBinding = preselectedHolder.binding as ViewDataBinding
                                val preselectedSwitch = preselectedBinding.root.findViewById<SwitchCompat>(R.id.switchTax)
                                preselectedSwitch.isChecked = true
                                selectedTaxValue = it[selectedPosition].taxAmount

                            }
                        }
                    }
                }
            }
        }

        viewModel.productsStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            if (it.invoiceId == 200){
                findNavController().popBackStack()
            }
        }

        viewModel.barcodeText.observe(viewLifecycleOwner){
            binding.etBarcode.setText(it)
            binding.barcodeContainer.visibility = View.GONE

        }

    }

    private fun onClickListeners(){
        binding.btnUpdateStock.setOnClickListener{
            productEdit?.productId?.let {
                if (binding.etProductStock.text.toString().isNotEmpty()){
                    viewModel.updateProductStock(requireContext(),it,binding.etProductStock.text.toString().toInt())
                    productEdit?.productStock?.let { stock->
                       binding.etCurrentStock.setText((stock + binding.etProductStock.text.toString().toInt()).toString())
                        binding.tvCurrentStock.text = (stock + binding.etProductStock.text.toString().toInt()).toString()
                        productEdit?.productStock = (stock + binding.etProductStock.text.toString().toInt())
                        binding.etProductStock.setText("")
                    }
                }else{
                    Toast.makeText(requireContext(), "Please enter current stock", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnUpdate.setOnClickListener{
            viewModel.updateproductObj(requireContext(),getProduct())
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
                binding.header.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_n_dark))

            } else {
                binding.dropdownContent.collapse()
                binding.header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                binding.header.background = ContextCompat.getDrawable(requireContext(), R.color.lite_grey_200)
                binding.header.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_main))
            }

        }
    }

    private fun getProduct(): Product {
        return Product(userId = Config.userId,
            productId = productEdit?.productId!!,
            productName = binding.etProductName.text.toString(),
            category = binding.dropdown.text.toString(),
            productPrice = binding.etProductPrice.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productCost = binding.etProductCost.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productMrp = binding.etProductMrp.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productBarcode = binding.etBarcode.text.toString(),
            productStockUnit = binding.dropdownStockUnit.text.toString(),
            productTax = selectedTaxValue,
            productTaxType = selectedTaxType,
            productStock = binding.etCurrentStock.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            productDefaultQty = binding.etDefaultQty.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            isSynced = 1)
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
        productEdit?.category?.let { category ->
                binding.dropdown.setText(category, false) // Set the text and avoid triggering the dropdown

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
        productEdit?.productStockUnit?.let { stockUnit ->
            binding.dropdownStockUnit.setText(stockUnit, false) // Set the text and avoid triggering the dropdown

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
        when(productEdit?.productTaxType){
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
        binding.dropdownTaxType.setText(selectedTaxType,false)


        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            when(selectedItem){
                "Price includes Tax" -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    selectedTaxType = selectedItem

                }
                "Price is without Tax" -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    selectedTaxType = selectedItem
                }
                "Zero Rated Tax" -> {
                    binding.recyclerView.visibility = View.GONE
                    selectedTaxValue = null
                    selectedTaxType = ""

                }
                "Exempt Tax" -> {
                    binding.recyclerView.visibility = View.GONE
                    selectedTaxValue = null
                    selectedTaxType = ""
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
                                    val previousSwitch = previousBinding.root.findViewById<SwitchCompat>(
                                        R.id.switchTax)
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
    private fun onOptionMenu(){
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.edit_product_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu item selected
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        setupPopup(productEdit?.productName.toString()){
                            deleteProduct()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    fun deleteProduct(){
        productEdit?.productId?.let { viewModel.deleteProduct(requireContext(),it)}
        findNavController().popBackStack()
    }

    private fun setupPopup(name : String ,action: () -> Unit){
        val dialogData = DialogData(
            title = "Delete Product",
            info = "Are you sure you want to Delete ${name} Product ?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel"
        )

        showCustomAlertDialog(
            context = requireActivity(),
            dialogData = dialogData,
            positiveAction = {
                action()
            },
            negativeAction = {
                // Handle negative button action
                // E.g., dismiss the dialog
            }
        )
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