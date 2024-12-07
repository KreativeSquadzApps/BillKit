package com.kreativesquadz.billkit.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.GenericTabAdapter
import com.kreativesquadz.billkit.databinding.FragmentHomeBinding
import com.kreativesquadz.billkit.ui.dialogs.AddDiscountDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.savedOrderDialogFrag.SavedOrderDialogFragment
import com.kreativesquadz.billkit.ui.dialogs.savedOrderDialogFrag.SavedOrderDialogViewModel
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet.EditItemBottomSheetFrag
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import com.kreativesquadz.billkit.ui.home.tab.quickSale.QuickSaleFragment
import com.kreativesquadz.billkit.ui.home.tab.sale.SaleFragment
import com.kreativesquadz.billkit.ui.home.tab.savedOrders.SavedOrdersFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var drawerToggleListener: DrawerToggleListener? = null
    interface DrawerToggleListener {
        fun toggleDrawer()
    }
    private val homeViewModel: HomeViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
    val sharedViewModel : SharedViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<InvoiceItem>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val requestCodeCameraPermission = 200
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isCameraClicked = false
    private var camera: Camera? = null
    private  var  cameraProvider: ProcessCameraProvider?=null
    var isScanner = true


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DrawerToggleListener) {
            drawerToggleListener = context
        } else {
            throw RuntimeException("$context must implement DrawerToggleListener")
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.getUserSettings()
        sharedViewModel.loadCompanyDetails()
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.getInvoiceHistory().observe(viewLifecycleOwner){
            it.data?.let {
                //Log.d("invoiceHistory", it.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.sharedViewModels = sharedViewModel
        setupRecyclerView()
        observers()
        onClickListeners()
        binding.isCameraOpen = isCameraClicked
        return root
    }


    private fun onClickListeners(){
        binding.drawerIcon.setOnClickListener {
            drawerToggleListener?.toggleDrawer()
        }
        binding.btnGenerateBill.setOnClickListener {
            if (binding.tvBill.text.isNullOrEmpty() || binding.tvBill.text.toString() == "0.0"){
                Toast.makeText(requireContext(), "Please enter amount", Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigate(R.id.action_nav_home_to_billDetailsFrag)
            }
        }

        binding.btnClearItems.setOnClickListener {
            sharedViewModel.clearOrder()
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
                stopCamera()
            }
            binding.isCameraOpen = isCameraClicked
        }

        binding.btnSavedOrder.setOnClickListener {
            showSavedOrderDialog()
        }
    }


    private fun observers(){
        sharedViewModel.loadCompanyDetailsDb().observe(viewLifecycleOwner){
            it?.let {
                Log.e("KKKK",it.toString())
            }
        }

        sharedViewModel.items.observe(viewLifecycleOwner) { items ->
            Log.d("itemssssssss", items.toString())
            adapter.submitList(items.asReversed())
            val totalSum = items.sumOf { it.totalPrice }
            binding.tvBill.text = totalSum.toString()
            binding.tvItemsCount.text = "Items : ${items.size}"
            if(items.isEmpty()){
                binding.isItemAvailable = false
            }else{
                binding.isItemAvailable = true
            }

        }

        sharedViewModel.amount.observe(viewLifecycleOwner){
            binding.tvDisplay.text = it.toString()
        }

        homeViewModel.userSetting.observe(viewLifecycleOwner){
            it.let {
                if (it?.isQtyReverse==0){
                    sharedViewModel.isReversedAmountNQty = false
                }else{
                    sharedViewModel.isReversedAmountNQty = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabSetup()
    }

    private  fun tabSetup(){
        val fragments = listOf(QuickSaleFragment(), SaleFragment(),SavedOrdersFragment()) // Replace with your fragments
        val adapter = GenericTabAdapter(requireActivity(), fragments)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
            val tabText = tabView.findViewById<TextView>(R.id.tab_texts)
            when (position) {
                0 -> tabText.text = "Quick Sale"
                1 -> tabText.text = "Sale"
                2 -> tabText.text = "Saved Orders"
                // Add more cases for additional tabs if needed
            }
            tab.customView = tabView
        }.attach()

    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            sharedViewModel.items.value ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    editItem(item)
                }
            },
            R.layout.item_home,
            BR.item // Variable ID generated by data binding
        )
        binding.itemRecyclerView.adapter = adapter
        binding.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        setupSwipeToDelete(binding.itemRecyclerView)

    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                sharedViewModel.removeItemAt(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun editItem(item: InvoiceItem){
        val editItemBottomSheetFrag = EditItemBottomSheetFrag(item)
        editItemBottomSheetFrag.show(parentFragmentManager, "EditItemBottomSheetFrag")
    }
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
             cameraProvider = cameraProviderFuture.get()
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
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraSetup", "Failed to bind camera use cases", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCamera() {
            cameraProvider?.unbindAll()
    }

    private fun logScannerState(state: Boolean) {
        Log.d("ScannerState", "isScanner is set to: $state")
    }

    private fun delay1sec() {
        isScanner = false
        logScannerState(isScanner) // Log state change
        lifecycleScope.launch {
            delay(1000)
            isScanner = true
            logScannerState(isScanner) // Log state change
        }
    }


    private fun processImage(image: InputImage, barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (!isScanner) {
            Log.d("ScannerState", "Scanner is not ready, skipping image processing.")
            imageProxy.close()
            return
        }

        //isScanner = false // Prevent further scans while processing
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (!sharedViewModel.isProductAdded(homeViewModel.getProductDetailByBarcode(barcode.rawValue.toString()))) {
                        sharedViewModel.addProduct(homeViewModel.getProductDetailByBarcode(barcode.rawValue.toString()))
                    }
                    val beepSoundUri = Uri.parse("android.resource://" + "com.kreativesquadz.billkit" + "/" + R.raw.barcode_beep)
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    val ringtone = RingtoneManager.getRingtone(requireContext(), beepSoundUri)
                    ringtone.audioAttributes = audioAttributes
                    ringtone.play()
                    delay1sec()
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                Log.e("BarcodeScanning", "Failed to process image", it)
                imageProxy.close()
            }
    }

    private fun showSavedOrderDialog() {
        val dialog = SavedOrderDialogFragment()
        dialog.show(childFragmentManager, SavedOrderDialogFragment.TAG)
       // dialogViewModel.setTotalAmount(sharedViewModel.totalLivedata.value.toString())

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isCameraClicked = true
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        drawerToggleListener = null
    }


}
