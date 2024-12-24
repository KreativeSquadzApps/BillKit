package com.kreativesquadz.hisabkitab.ui.bills

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity.CENTER
import android.view.Gravity.RIGHT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.AdapterReceipt
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentReceiptBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.InvoiceTax
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import com.kreativesquadz.hisabkitab.model.settings.PdfSettings
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import com.kreativesquadz.hisabkitab.utils.Glide.GlideHelper
import com.kreativesquadz.hisabkitab.utils.PdfColor
import com.kreativesquadz.hisabkitab.utils.addBackPressHandler
import com.kreativesquadz.hisabkitab.utils.toBoolean
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class ReceiptFrag : Fragment() {
    var _binding: FragmentReceiptBinding? = null
    val binding get() = _binding!!
    private val viewModel: ReceiptViewModel by viewModels()
    private lateinit var adapter: AdapterReceipt<InvoiceItem>
    private lateinit var adapterGst: GenericAdapter<InvoiceTax>
    lateinit var invoiceP: Invoice
    private var customerP : Customer? = null
    var invoiceTax = mutableListOf<InvoiceTax>()
    var isTaxAvailable = false
    var isMrpAvailable = false
    val invoiceId by lazy {
        arguments?.getString("invoiceId")
    }
    val target by lazy {
        arguments?.getString("target")
    }
    private var pdfSettings = PdfSettings()
    private var invoicePrinterSettings = InvoicePrinterSettings()
    private var thermalPrinterSetup: ThermalPrinterSetup? = null

    private var customGstAmount: String? = null
    private var isGstAvailable = false
   // private val PRINTER_WIDTH: Int = 384
    private val PRINTER_WIDTH: Int = 576
    private val INITIAL_MARGIN_LEFT: Int = -5
    private val BIT_WIDTH: Int = 384
    private val WIDTH: Int = 48
    private val HEAD: Int = 8
    val FULL_WIDTH: Int = -1
    private var isLogoPrint = false
    private var CompanyLogoUrl : String ?= null
    private var companyLogoBitmap : Bitmap ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invoiceId?.let {
            viewModel.fetchAllDetails(it)
        }
        GlideHelper.initializeGlideWithOkHttp(requireContext())
    }
    override fun onResume() {
        super.onResume()
        viewModel.getPrinterSetting()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        addBackPressHandler(viewLifecycleOwner, ::shouldAllowBack)
        binding.isPrintLoading  = false
        return binding.root
    }

    fun observers(){
        viewModel.companyDetails.observe(viewLifecycleOwner){
            it?.let {
                binding.companyDetails = it
                CompanyLogoUrl  = it.BusinessImage
                GlideHelper.loadImage(requireContext(), it.BusinessImage, binding.imageView)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.printerSettings.collect { printerSettings ->
                        thermalPrinterSetup = printerSettings
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pdfSettings.collect { pdfSettings ->
                        this@ReceiptFrag.pdfSettings = pdfSettings
                        binding.tvFooter.text = pdfSettings.pdfFooter
                        val pdfCompanyInfoValues = pdfSettings.pdfCompanyInfo.split(" ")
                        val isCompanyLogo = pdfCompanyInfoValues.getOrNull(0)?.toIntOrNull() ?: 0

                        if(isCompanyLogo.toBoolean()){
                            CompanyLogoUrl?.let {
                                GlideHelper.loadBitmap(requireContext(), it) { bitmap ->
                                    companyLogoBitmap = bitmap
                                }
                            }

                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        launch {
                            viewModel.invoicePrinterSettings.collect { invoicePrinterSettings ->
                                this@ReceiptFrag.invoicePrinterSettings = invoicePrinterSettings
                                binding.tvFooter.text = invoicePrinterSettings.printerFooter

                                val printerCompanyInfoValues = invoicePrinterSettings.printerCompanyInfo.split(" ")
                                val isPrinterCompanyLogo = printerCompanyInfoValues.getOrNull(0)?.toIntOrNull() ?: 0
                                val isPrinterCompanyEmail = printerCompanyInfoValues.getOrNull(1)?.toIntOrNull() ?: 0
                                val isPrinterCompanyPhone = printerCompanyInfoValues.getOrNull(2)?.toIntOrNull() ?: 0
                                val isPrinterCompanyGst = printerCompanyInfoValues.getOrNull(3)?.toIntOrNull() ?: 0




                                val printerItemTableValues = invoicePrinterSettings.printerItemTable.split(" ")
                                val  isPrinterItemTableCustomerDetails = printerItemTableValues.getOrNull(0)?.toIntOrNull() ?: 0
                                val  isPrinterItemTableMrp = printerItemTableValues.getOrNull(1)?.toIntOrNull() ?: 0
                                val isPrinterItemTablePayment = printerItemTableValues.getOrNull(2)?.toIntOrNull() ?: 0
                                val isPrinterItemTableQty = printerItemTableValues.getOrNull(3)?.toIntOrNull() ?: 0

                                if (isPrinterCompanyLogo.toBoolean()){
                                    binding.imageView.visibility = View.VISIBLE
                                    isLogoPrint = true
                                } else{
                                    binding.imageView.visibility = View.GONE
                                    isLogoPrint = false
                                }

                                if (isPrinterCompanyEmail.toBoolean()){
                                    binding.isVisibleEmail.visibility = View.VISIBLE
                                }else{
                                    binding.isVisibleEmail.visibility = View.GONE
                                }

                                if (isPrinterCompanyPhone.toBoolean()){
                                    binding.isVisibleContact.visibility = View.VISIBLE
                                }else{
                                    binding.isVisibleContact.visibility = View.GONE
                                }

                                if (isPrinterCompanyGst.toBoolean()){
                                    binding.isVisibleGst.visibility = View.VISIBLE
                                    isGstAvailable = true
                                }else{
                                    binding.isVisibleGst.visibility = View.GONE
                                    isGstAvailable = false
                                }

                                if (!isPrinterItemTableCustomerDetails.toBoolean()){
                                    binding.isCustomerAvailable = false
                                }

                                if (isPrinterItemTableMrp.toBoolean()){
                                    binding.istMrpAvalaible = true
                                }else{
                                    binding.istMrpAvalaible = false
                                }

                                if (isPrinterItemTablePayment.toBoolean()){
                                    binding.paymentViews.visibility = View.VISIBLE
                                }else{
                                    binding.paymentViews.visibility = View.GONE
                                }

                                if (isPrinterItemTableQty.toBoolean()){
                                    binding.tvItems.visibility = View.VISIBLE
                                }else{
                                    binding.tvItems.visibility = View.GONE
                                }



                            }
                        }
                    }
        }

        viewModel.invoiceData.observe(viewLifecycleOwner) {
            binding.invoice = it
            if (it.customGstAmount != null){
                isTaxAvailable = true
                customGstAmount = it.customGstAmount
                val firstValue = it.customGstAmount.split("|").first()
                binding.tvCustomGst.text = "Custom Gst : Rs "+firstValue.toDouble()
            }
            invoiceP = it
            binding.tvDiscount.text = "Discount : Rs "+it.discount?.toDouble()
            binding.tvCreditNote.text = "Credit Note : Rs "+it.creditNoteAmount?.toDouble()
            binding.isCustomerAvailable = it?.customerId != null
            binding.customer = viewModel.getCustomerById(it?.customerId.toString())
            customerP = viewModel.getCustomerById(it?.customerId.toString())
        }



        viewModel.invoiceItems.observe(viewLifecycleOwner){
            var totalQty = 0.0
            it?.forEach {
                if(it.taxRate > 0){
                    isTaxAvailable = true
                    binding.istProductTaxAvalaible = true
                }
                if(it.productMrp != it.unitPrice){
                    isMrpAvailable = true
                }
                totalQty += it.quantity
            }
            binding.tvTotalQty.setText("Total Qty : "+totalQty.toInt().toString())
            binding.istTaxAvalaible = isTaxAvailable
            binding.istMrpAvalaible = isMrpAvailable
            setupRecyclerView(it)
        }



        target?.let {
            if (it == Config.BillDetailsFragmentToReceiptFragment) {
                // Set the image resource for the ImageView
                binding.backImage.setImageResource(R.drawable.home_light)

                // Apply the tint to the image
                binding.backImage.post {
                    val drawable = binding.backImage.drawable
                    drawable?.let { d ->
                        val wrappedDrawable = DrawableCompat.wrap(d)
                        DrawableCompat.setTint(
                            wrappedDrawable,
                            ContextCompat.getColor(requireContext(), R.color.image_primary_n_dark)
                        )
                        binding.backImage.setImageDrawable(wrappedDrawable)
                    }
                }

                binding.backText.text = "New Sale"
            } else {
                // Set the image resource for the ImageView for the "Back" case
                binding.backImage.setImageResource(R.drawable.back_light)

                // Apply the tint to the image
                binding.backImage.post {
                    val drawable = binding.backImage.drawable
                    drawable?.let { d ->
                        val wrappedDrawable = DrawableCompat.wrap(d)
                        DrawableCompat.setTint(
                            wrappedDrawable,
                            ContextCompat.getColor(requireContext(), R.color.image_primary_n_dark)
                        )
                        binding.backImage.setImageDrawable(wrappedDrawable)
                    }
                }

                binding.backText.text = "Back"
            }
        }
        viewModel.printStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            if (it == "printingStart"){
                binding.isPrintLoading = true
            }else{
                binding.isPrintLoading = false
            }
        }

        viewModel.allDataReady.observe(viewLifecycleOwner) { ready ->
            if (ready) {
                thermalPrinterSetup?.let {
                    if (it.defaultPrinterAddress.isNotEmpty() && it.enableAutoPrint){
                        printInvoice(customerP, invoiceP, viewModel.invoiceItems.value!!, viewModel.companyDetails.value!!)
                }
           }
                // Other operations after all data is ready
            }
        }
    }

    fun onClickListeners(){
        binding.btnBack.setOnClickListener {
            if(target == Config.BillDetailsFragmentToReceiptFragment)
                findNavController().navigate(R.id.action_receiptFrag_to_nav_home)
            else
                findNavController().popBackStack()
            }



        binding.btnPrint.setOnClickListener {
            if (thermalPrinterSetup != null && thermalPrinterSetup!!.defaultPrinterAddress.isNotEmpty()){
                printInvoice(customerP, invoiceP, viewModel.invoiceItems.value!!, viewModel.companyDetails.value!!)
            }else{
                Toast.makeText(requireContext(), "No Default Printer Found", Toast.LENGTH_SHORT).show()
                val action = ReceiptFragDirections.actionReceiptFragToBluetoothDeviceFragment(invoiceP)
                findNavController().navigate(action)
            }
        }

        binding.btnShare.setOnClickListener {
            generateReceiptPdf(requireContext(),pdfSettings, viewModel.invoiceItems.value!!,viewModel.companyDetails.value!!, invoiceP, customerP)
        }

    }

    private fun setupRecyclerView(receiptInvoiceItem: List<InvoiceItem>?) {
        var isTaxAvailable = false
        var isMrpAvailable = false
        val taxList = mutableListOf<Double>()
        var taxAmount: Double
        var taxableAmount: Double
        invoiceTax.clear()

        receiptInvoiceItem?.forEach {
            if (it.taxRate > 0) {
                isTaxAvailable = true
                val index = taxList.indexOf(it.taxRate)
                taxAmount = (it.unitPrice * it.quantity) * it.taxRate / 100
                taxableAmount = it.unitPrice * it.quantity
                if (index >= 0) {
                    invoiceTax[index].taxableAmount += taxableAmount
                    invoiceTax[index].taxAmount += taxAmount
                } else {
                    taxList.add(it.taxRate)
                    invoiceTax.add(InvoiceTax("", taxableAmount, it.taxRate, taxAmount))
                }
            }
            if(it.productMrp != it.unitPrice){
                isMrpAvailable = true
            }

        }
        customGstAmount?.let {
            val customGstAmount = it.split("|")[0].toDouble()
            val customGstRateApplied = it.split("|")[1].toDouble()
            val taxableAmount = it.split("|")[2].toDouble()
                invoiceTax.add(InvoiceTax("Custom GST",taxableAmount, customGstRateApplied, customGstAmount))
             }

        viewModel.getGstListByValue(taxList).observe(viewLifecycleOwner) { gstList ->
            gstList?.let { gstItems ->
                gstItems.forEach { gst ->
                    invoiceTax.forEach { invoiceItem ->
                        if (invoiceItem.rate == gst.taxAmount && invoiceItem.taxType != "Custom GST") {
                            invoiceItem.taxType = gst.taxType
                        }
                    }

                }

            }
            Log.e("llllll","$invoiceTax")
            setupRecyclerViewGst(invoiceTax)
        }
        adapter = AdapterReceipt(
            receiptInvoiceItem ?: emptyList(),
            object : OnItemClickListener<InvoiceItem> {
                override fun onItemClick(item: InvoiceItem) {
                    // Handle item click
                }
            },
            R.layout.item_invoice_item_receipt,
            BR.item, // Variable ID generated by data binding
            isTaxAvailable,
            isMrpAvailable,
            viewModel = viewModel
        )
        binding.itemListRecyclerview.adapter = adapter
        binding.itemListRecyclerview.layoutManager = LinearLayoutManager(context)
        val itemHeight = resources.getDimensionPixelSize(R.dimen._70dp) // set this to your item height
        val params = binding.itemListRecyclerview.layoutParams
        params.height = itemHeight * adapter.itemCount
        binding.itemListRecyclerview.layoutParams = params

    }

    private fun setupRecyclerViewGst(gst: List<InvoiceTax>?) {
        Log.e("kkkk","$invoiceTax")
        adapterGst = GenericAdapter(
            invoiceTax,
            object : OnItemClickListener<InvoiceTax> {
                override fun onItemClick(item: InvoiceTax) {
                    // Handle item click
                }
            },
            R.layout.item_gst,
            BR.taxGst, // Variable ID generated by data binding
        )
        binding.gstRecyclerview.adapter = adapterGst
        binding.gstRecyclerview.layoutManager = LinearLayoutManager(context)
    }

    private fun shouldAllowBack(): Boolean {
        // Your logic to allow or restrict back action
        return false // Change this according to your logic
    }

    @SuppressLint("SuspiciousIndentation")
    private fun generateReceiptPdf(context: Context, pdfSettings: PdfSettings, items: List<InvoiceItem>, companyDetails: CompanyDetails, invoice: Invoice, customer : Customer?) {
        val pdfCompanyInfoValues = pdfSettings.pdfCompanyInfo.split(" ")
      val isCompanyLogo = pdfCompanyInfoValues.getOrNull(0)?.toIntOrNull() ?: 0
       val isCompanyEmail = pdfCompanyInfoValues.getOrNull(1)?.toIntOrNull() ?: 0
       val isCompanyPhone = pdfCompanyInfoValues.getOrNull(2)?.toIntOrNull() ?: 0
        //  isCompanyAddress = pdfCompanyInfoValues.getOrNull(3)?.toIntOrNull() ?: 0
        val  isCompanyGst = pdfCompanyInfoValues.getOrNull(3)?.toIntOrNull() ?: 0
        val pdfColor : DeviceRgb
        var titleColor : DeviceRgb
            titleColor = getDeviceRgbFromColorResource(Color.WHITE)
                when(pdfSettings.pdfColor){
                PdfColor.RED.toString() -> pdfColor = getDeviceRgbFromColorResource(Color.RED)
                PdfColor.GREEN.toString() -> pdfColor = getDeviceRgbFromColorResource(Color.GREEN)
                PdfColor.BLUE.toString() -> pdfColor = getDeviceRgbFromColorResource(Color.BLUE)
                PdfColor.YELLOW.toString()-> pdfColor = getDeviceRgbFromColorResource(Color.YELLOW)
                PdfColor.GRAY.toString() -> {
                    pdfColor = getDeviceRgbFromColorResource(Color.GRAY)
                    titleColor = getDeviceRgbFromColorResource(Color.BLACK)
                }
                else -> {
                    pdfColor = getDeviceRgbFromColorResource(Color.GRAY)
                    titleColor = getDeviceRgbFromColorResource(Color.BLACK)
                }
            }



        // Split pdfItemTable and update corresponding variables
        val pdfItemTableValues = pdfSettings.pdfItemTable.split(" ")
        val  isItemTableCustomerDetails = pdfItemTableValues.getOrNull(0)?.toIntOrNull() ?: 0
        val  isItemTableMrp = pdfItemTableValues.getOrNull(1)?.toIntOrNull() ?: 0
        val isItemTablePayment = pdfItemTableValues.getOrNull(2)?.toIntOrNull() ?: 0
        val isItemTableQty = pdfItemTableValues.getOrNull(3)?.toIntOrNull() ?: 0



        val file = File(context.getExternalFilesDir(null), "Invoice ${invoice.invoiceNumber}.pdf")
        val writer = PdfWriter(file)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        if (isCompanyLogo.toBoolean()){
            val CompanyLogo = companyLogoBitmap
            val streamCompany = ByteArrayOutputStream()
            CompanyLogo?.compress(Bitmap.CompressFormat.PNG, 100, streamCompany)
            val logoImageDataCompany = streamCompany.toByteArray()
            val imageCompanyLogo = Image(ImageDataFactory.create(logoImageDataCompany))
            imageCompanyLogo.setHeight(100f)
            imageCompanyLogo.setWidth(100f)
            imageCompanyLogo.setMarginTop(10f)
            imageCompanyLogo.setMarginBottom(10f)
            imageCompanyLogo.setHorizontalAlignment(HorizontalAlignment.CENTER)
            document.add(imageCompanyLogo)
        }
        val businessName = Paragraph(companyDetails.BusinessName)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(16f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
        document.add(businessName)

        val businessPlace = Paragraph(companyDetails.Place)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(14f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        document.add(businessPlace)

        val businessNumber = Paragraph("Tel : "+companyDetails.ShopContactNumber)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(16f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        if (isCompanyPhone.toBoolean()){
            document.add(businessNumber)
        }


        val businessEmail = Paragraph("Email: ${companyDetails.ShopEmail}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(14f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        if (isCompanyEmail.toBoolean()){
            document.add(businessEmail)
        }


        val businessGstNo = Paragraph("GST No: ${companyDetails.GSTNo}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(14f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
        if (isCompanyGst.toBoolean()){
            document.add(businessGstNo)
        }

        companyDetails.FSSAINo?.let {
            val businessFssaiNo = Paragraph("FSSAI No: $it")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)

            document.add(businessFssaiNo)
        }


            val header = Paragraph("INVOICE")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(20f)
            .setFontColor(titleColor)
            .setBackgroundColor(pdfColor)
            .setTextAlignment(TextAlignment.RIGHT)
        header.setPaddingRight(40f)
        document.add(header)


        customer?.let {
            if (isItemTableCustomerDetails.toBoolean()){
            val billTo = Paragraph("Bill To,")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(12f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
            document.add(billTo)

            val customerName = Paragraph(customer.customerName)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(-5f)
            document.add(customerName)

            val customerMobile = Paragraph(customer.shopContactNumber)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(12f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(-5f)
            document.add(customerMobile)

            val customerGst = Paragraph(customer.gstNo)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(12f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(-5f)
            document.add(customerGst)

            val customerAddress = Paragraph(customer.address)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(12f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(-5f)
            document.add(customerAddress)
        }
    }

        val invoiceId = Paragraph("Invoice No : ${invoice.invoiceNumber}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(14f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
        document.add(invoiceId)

        val date = Date(invoice.invoiceDate.toLong())
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.getDefault())

        val invoiceDate = Paragraph(format.format(date))
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
            .setMarginBottom(10f)

        document.add(invoiceDate)
        // Add Item Table
        val table: Table
        if (isItemTableMrp.toBoolean()){
             table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 1f, 2f, 2f, 2f,2f)))
        }else{
             table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 1f, 2f, 2f, 2f)))
        }
        table.setWidth(UnitValue.createPercentValue(100f))

        // Table Header


        table.addCell(Cell().add(Paragraph("SL No").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("ITEM").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("QTY").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        if (isItemTableMrp.toBoolean()) {
            table.addCell(
                Cell().add(
                    Paragraph("MRP")
                        .setFontColor(ColorConstants.WHITE)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setPaddingLeft(5f).setBackgroundColor(pdfColor)
                ).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
            )
        }
        table.addCell(Cell().add(Paragraph("PRICE").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("TAX[ % ]").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("AMOUNT").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER))



        // Table Rows
        var serialNo = 0
        var totalItemQty = 0
        items.forEach { item ->
            serialNo++
            val  finalRate = item.unitPrice - item.unitPrice * item.taxRate / 100
            val  taxAmount = item.unitPrice - finalRate
            totalItemQty += item.quantity

            table.addCell(Cell().add(Paragraph(serialNo.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderRight(Border.NO_BORDER))
            table.addCell(Cell().add(Paragraph(item.itemName.split("(")[0]).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(Cell().add(Paragraph(item.quantity.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            if (isItemTableMrp.toBoolean()) {
                table.addCell(
                    Cell().add(
                        Paragraph(item.productMrp.toString()).setPaddingLeft(10f).setFontSize(12f)
                    ).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                )
            }
            table.addCell(Cell().add(Paragraph(finalRate.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(Cell().add(Paragraph((taxAmount * item.quantity).toString() +" ["+ item.taxRate.toString()+"%]").setPaddingLeft(10f).setFontSize(10f).setTextAlignment(TextAlignment.CENTER)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(Cell().add(Paragraph(item.totalPrice.toString()).setPaddingRight(10f).setFontSize(12f).setTextAlignment(TextAlignment.RIGHT)).setBorderLeft(Border.NO_BORDER))
        }
        document.add(table)

        val invoiceSubTotal = Paragraph("Sub Total : ${Config.CURRENCY} ${invoice.subtotal}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(5f)
        document.add(invoiceSubTotal)

        val invoiceTotalTax = Paragraph("Total Tax : RS ${invoice.totalTax }")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        document.add(invoiceTotalTax)

        invoice.packageAmount?.let {
            val invoicePackageAmount = Paragraph("Package Amount : RS ${it.toDouble()}")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(15f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)
            document.add(invoicePackageAmount)
        }

        invoice.otherChargesAmount?.let {
            val invoiceOtherChargesAmount = Paragraph("Other Charges : RS $it")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(15f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)
            document.add(invoiceOtherChargesAmount)
        }

        customGstAmount?.let {
            val firstValue = it.split("|").first()
            val invoiceCustomGstAmount = Paragraph("Custom GST: RS ${firstValue.toDouble()}")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(15f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)
            document.add(invoiceCustomGstAmount)
        }

        if (invoice.discount != null && invoice.discount > 0) {
            val invoiceDiscount = Paragraph("Discount : RS ${invoice.discount}")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(15f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)
            document.add(invoiceDiscount)
        }

        if (invoice.creditNoteAmount != null && invoice.creditNoteAmount > 0) {
            val invoiceCreditNoteAmount = Paragraph("Credit Note : RS ${invoice.creditNoteAmount}")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(15f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(-5f)
            document.add(invoiceCreditNoteAmount)
        }


        val totalItem = Paragraph("Total Item : ${serialNo}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.LEFT)
             document.add(totalItem)

        if (isItemTableQty.toBoolean()){
            val totalQty = Paragraph("Total Qty : ${totalItemQty}")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(12f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
            document.add(totalQty)
        }
        if (isItemTablePayment.toBoolean()) {
                invoice.cashAmount?.let {
                    if (it > 0){
                    val cash = Paragraph("Cash : ${it}")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(12f)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginTop(-5f)
                    document.add(cash)
                }
                }


                invoice.onlineAmount?.let {
                    if (it > 0){
                        val onlineAmount = Paragraph("Online : ${it}")
                            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                            .setFontSize(12f)
                            .setFontColor(ColorConstants.BLACK)
                            .setTextAlignment(TextAlignment.LEFT)
                            .setMarginTop(-5f)
                        document.add(onlineAmount)
                    }
                }
                invoice.creditAmount?.let {
                    if (it > 0){
                    val creditAmount = Paragraph("Credit : ${it}")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(12f)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginTop(-5f)
                    document.add(creditAmount)
                  }
                }

        }

        val tableTotal = Table(UnitValue.createPercentArray(floatArrayOf(70f, 20f))) // Two columns with equal width
        tableTotal.setWidth(UnitValue.createPercentValue(100f)) // Ensure table width is 100% of page width
        tableTotal.setMarginTop(5f)

// First paragraph
        val amountToWords = Paragraph(amountToWords(invoice.totalAmount))
            .setTextAlignment(TextAlignment.LEFT)
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setPadding(5f)

// Second paragraph
        val invoiceTotal = Paragraph("Total : ${invoice.totalAmount}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(titleColor)
            .setBackgroundColor(pdfColor)
            .setPadding(5f)
            .setTextAlignment(TextAlignment.RIGHT)

// Add paragraphs to the table as cells
        tableTotal.addCell(Cell().add(amountToWords).setBorder(Border.NO_BORDER))
        tableTotal.addCell(Cell().add(invoiceTotal).setBorder(Border.NO_BORDER))
        document.add(tableTotal)





        if (isTaxAvailable){
            val taxDetails = Paragraph("TAX DETAILS")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(14f)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
            document.add(taxDetails)

            val tableTax = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 3f, 2f, 3f)))
            tableTax.setWidth(UnitValue.createPercentValue(100f))

            tableTax.addCell(Cell().add(Paragraph("SL No").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAX TYPE").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAXABLE AMOUNT").setFontSize(10f).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("RATE").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAX AMOUNT").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f).setBackgroundColor(pdfColor)).setBorderLeft(Border.NO_BORDER))

           var serialNoTax = 0
            invoiceTax.forEach{ item ->
                serialNoTax ++
                tableTax.addCell(Cell().add(Paragraph(serialNoTax.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderRight(Border.NO_BORDER))
                tableTax.addCell(Cell().add(Paragraph(item.taxType).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
                tableTax.addCell(Cell().add(Paragraph(item.taxableAmount.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
                tableTax.addCell(Cell().add(Paragraph(item.rate.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
                tableTax.addCell(Cell().add(Paragraph(item.taxAmount.toString()).setPaddingRight(10f).setTextAlignment(TextAlignment.RIGHT).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(Border.NO_BORDER))

            }
            document.add(tableTax)
        }

        val thankYou = Paragraph(pdfSettings.pdfFooter)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginTop(5f)
        document.add(thankYou)



        val separator = LineSeparator(SolidLine(1f))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setMarginTop(50f)
            .setMarginBottom(10f)
        document.add(separator)
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        val stream = ByteArrayOutputStream()
        logo.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val logoImageData = stream.toByteArray()
        val image = Image(ImageDataFactory.create(logoImageData))
        image.setHeight(100f)
        image.setWidth(100f)
        image.setMarginTop(10f)
        image.setMarginLeft(20f)
        image.setHorizontalAlignment(HorizontalAlignment.LEFT)
        document.add(image)

        // Add Footer
        val footer = Paragraph(pdfSettings.pdfFooter)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(12f)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20f)
        document.add(footer)

        document.close()
        sharePdf(context, file)
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "com.kreativesquadz.hisabkitab.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }

    private fun amountToWords(amount: Double): String {
        val units = arrayOf(
            "Zero", "One", "Two", "Three", "Four", "Five",
            "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
            "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )

        val tens = arrayOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty",
            "Sixty", "Seventy", "Eighty", "Ninety"
        )

        fun convertToWords(n: Int): String {
            if (n < 20) return units[n]
            if (n < 100) return tens[n / 10] + if (n % 10 != 0) " " + units[n % 10] else ""
            if (n < 1000) return units[n / 100] + " Hundred" + if (n % 100 != 0) " and " + convertToWords(n % 100) else ""
            if (n < 100000) return convertToWords(n / 1000) + " Thousand" + if (n % 1000 != 0) " " + convertToWords(n % 1000) else ""
            if (n < 10000000) return convertToWords(n / 100000) + " Lakh" + if (n % 100000 != 0) " " + convertToWords(n % 100000) else ""
            return convertToWords(n / 10000000) + " Crore" + if (n % 10000000 != 0) " " + convertToWords(n % 10000000) else ""
        }

        val intPart = amount.toInt()
        val fractionPart = ((amount - intPart) * 100).toInt()

        val intPartWords = convertToWords(intPart)
        val fractionPartWords = if (fractionPart > 0) " and " + convertToWords(fractionPart) + " Paise" else ""

        return "$intPartWords $fractionPartWords"
    }

    private fun printInvoice(customer : Customer?, invoice : Invoice?, invoiceItems : List<InvoiceItem>, companyDetails : CompanyDetails) {
        val isCustomer = customer != null
        val discount = invoice?.discount ?: 0
        val timestamp = try {
            val date = Date(invoice!!.invoiceDate.toLong())
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.getDefault())
            format.format(date)
        } catch (e: Exception) {
            ""
        }

        val receipt = createReceiptString(
            businessName = companyDetails.BusinessName,
            place = companyDetails.Place,
            contactNumber = companyDetails.ShopContactNumber,
            email = companyDetails.ShopEmail,
            gst = companyDetails.GSTNo,
            invoiceDate = timestamp,
            invoiceId = invoice!!.invoiceNumber,
            isCustomerAvailable = isCustomer,
            customerName = customer?.customerName,
            customerNumber = customer?.shopContactNumber,
            customerGst = customer?.gstNo,
            packageAmount = invoice.packageAmount,
            otherChargesAmount = invoice.otherChargesAmount,
            customGstAmount = invoice.customGstAmount,
            creditNoteAmount = invoice.creditNoteAmount.toDouble(),
            customerAddress = customer?.address,
            items = invoiceItems,
            totalItems = invoiceItems.size,
            subtotal = invoice.subtotal,
            discount = discount.toDouble(),
            totalAmount = invoice.totalAmount,
            totalTax = invoice.totalTax,
            cashAmount = invoice.cashAmount,
            onlineAmount = invoice.onlineAmount,
            creditAmount = invoice.creditAmount,
            footer = pdfSettings.pdfFooter
        )
        if (isLogoPrint){
            printByteArray(receipt)
        }else{
            viewModel.printUsingDefaultPrinter(receipt)
        }
    }

    private fun createReceiptString(
        businessName: String,
        place: String,
        contactNumber: String,
        email: String,
        gst: String?,
        invoiceDate: String,
        invoiceId: String,
        isCustomerAvailable: Boolean,
        customerName: String?,
        customerNumber: String?,
        customerGst: String?,
        packageAmount: Double?,
        otherChargesAmount: Double?,
        customGstAmount: String?,
        creditNoteAmount : Double?,
        customerAddress: String?,
        items: List<InvoiceItem>,
        totalItems: Int,
        subtotal: Double,
        discount: Double?,
        totalAmount: Double,
        totalTax: Double?,
        cashAmount: Double?,
        onlineAmount: Double?,
        creditAmount: Double?,
        footer: String,
    ): ByteArray {
        val receipt = ByteArrayOutputStream()

        // ESC/POS commands for bold text
        val boldOn = byteArrayOf(0x1B, 0x45, 0x01) // ESC E 1
        val boldOff = byteArrayOf(0x1B, 0x45, 0x00) // ESC E 0

        // Define paper width for formatting
        var separatorLine = ""
        var paperWidth = "45" // Default to 58MM, adjust as needed

        thermalPrinterSetup?.let {
            paperWidth = it.printerSize.replace("MM", "")
        }
        val charactersPerLine: Int = when (paperWidth.toInt()) {
            58 -> 32 // 58MM typically fits 32 characters
            80 -> 48 // 80MM typically fits 48 characters
            else -> 32 // Default to 32 if unknown
        }
        separatorLine = generateSeparatorLine(charactersPerLine)

        setFontSize(receipt, 2)
        receipt.write(boldOn)
        receipt.write(formatSingleString(businessName, charactersPerLine,fontSizeMultiplier = 2).toByteArray())
        receipt.write("\n\n".toByteArray())
        receipt.write(boldOff)
        setFontSize(receipt, 1)

        receipt.write(formatSingleString(" "+place, charactersPerLine).toByteArray())
        receipt.write("\n".toByteArray())
        receipt.write(formatSingleString(contactNumber, charactersPerLine).toByteArray())
        receipt.write("\n".toByteArray())
        receipt.write(formatSingleString("Email: $email", charactersPerLine).toByteArray())
        if (isGstAvailable){
            gst?.let {
                receipt.write("\n".toByteArray())
                receipt.write(formatSingleString("GST : $gst", charactersPerLine).toByteArray())
            }
        }

        receipt.write("\n\n".toByteArray())

        // Add Invoice Header
        setFontSize(receipt, 2)
        receipt.write(boldOn)
        receipt.write(formatSingleString("INVOICE", charactersPerLine, fontSizeMultiplier = 2).toByteArray())
        receipt.write("\n\n".toByteArray())
        receipt.write(boldOff)
        setFontSize(receipt, 1)

        receipt.write(
            formatLineWithAlignment(invoiceDate, "Invoice: $invoiceId", charactersPerLine).toByteArray()
        )
        receipt.write("\n".toByteArray())
        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())

        // Add Customer Details if available
        if (isCustomerAvailable) {
            customerName?.let {
                if (it.isNotEmpty()) {
                    receipt.write(formatSingleString(it, charactersPerLine).toByteArray())
                    receipt.write("\n".toByteArray())
                }
            }
            customerNumber?.let {
                if (it.isNotEmpty()) {
                    receipt.write(formatSingleString("Contact: $it", charactersPerLine).toByteArray())
                    receipt.write("\n".toByteArray())
                }
            }
            customerGst?.let {
                if (it.isNotEmpty()) {
                    receipt.write(formatSingleString("GST No: $it", charactersPerLine).toByteArray())
                    receipt.write("\n".toByteArray())
                }
            }
            customerAddress?.let {
                if (it.isNotEmpty()) {
                    receipt.write(formatSingleString(it, charactersPerLine).toByteArray())
                    receipt.write("\n".toByteArray())
                }
            }
            receipt.write(separatorLine.toByteArray())
        }
        var isProductTaxAvailable = 0.0
        items.forEach {
            isProductTaxAvailable += it.taxRate
        }
        if (isProductTaxAvailable != 0.0){
            receipt.write(
                formatLineWithMultipleAlignment(
                    listOf(
                        "SL No." to 10,
                        "Item" to 30,
                        "Qty" to 12,
                        "Rate" to 16,
                        "Tax" to 16,
                        "Total" to 16
                    ),
                    charactersPerLine
                ).toByteArray()
            )
        }else{
            receipt.write(
                formatLineWithMultipleAlignment(
                    listOf(
                        "SL No." to 10,
                        "Item" to 30,
                        "Qty" to 18,
                        "Rate" to 21,
                        "Total" to 21
                    ),
                    charactersPerLine
                ).toByteArray()
            )
        }

        receipt.write("\n".toByteArray())
        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())

        // Add Items
        var slNo = 0
        var totalQty = 0

        for (item in items) {
            slNo++
            totalQty += item.quantity
            if (isProductTaxAvailable != 0.0){
                receipt.write(
                    formatLineWithMultipleAlignment(
                        listOf(
                            slNo.toString() to 10,
                            item.itemName.split("(")[0] to 34,
                            item.quantity.toString() to 11,
                            item.unitPrice.toString() to 15,
                            item.taxRate.toString() to 15,
                            item.totalPrice.toString() to 15
                        ),
                        charactersPerLine
                    ).toByteArray()
                )
            }else{
                receipt.write(
                    formatLineWithMultipleAlignment(
                        listOf(
                            slNo.toString() to 10,
                            item.itemName.split("(")[0] to 34,
                            item.quantity.toString() to 18,
                            item.unitPrice.toString() to 21,
                            item.totalPrice.toString() to 20
                        ),
                        charactersPerLine
                    ).toByteArray()
                )
            }


            receipt.write("\n".toByteArray())
        }
        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())

//        // Add Totals
        receipt.write(formatLineWithAlignment("Items: $totalItems", "Sub Total: RS $subtotal", charactersPerLine).toByteArray())
        receipt.write("\n".toByteArray())
//

        val hasOptionalValues = packageAmount.toString().isNotEmpty() ||
                otherChargesAmount.toString().isNotEmpty() ||
                !customGstAmount.isNullOrEmpty() ||
                (discount != null && discount > 0) || (creditNoteAmount != null && creditNoteAmount > 0.0)

// Track which optional value has been displayed
        var displayedOptionalLabel: String? = null

        if (hasOptionalValues) {
            displayedOptionalLabel = when {
                packageAmount != null  -> "Package Amount : RS ${packageAmount.toDouble()}"
                otherChargesAmount != null -> "Other Charges : RS $otherChargesAmount"
                !customGstAmount.isNullOrEmpty() -> "Custom GST : RS ${customGstAmount.split("|").first().toDouble()}"
                discount != null && discount > 0 -> "Discount : RS $discount"
                creditNoteAmount != null && creditNoteAmount > 0.0 -> "Credit Note : RS $creditNoteAmount"
                else -> null
            }
            if(displayedOptionalLabel == null){
                receipt.write(formatSingleStringLeft("Qty : $totalQty", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }else{
                receipt.write(formatLineWithAlignment("Qty : $totalQty", displayedOptionalLabel, charactersPerLine).toByteArray())
            }
        }
        if (totalTax != null && totalTax > 0) {
            receipt.write(formatSingleStringRight("Total Tax : RS $totalTax", charactersPerLine).toByteArray())
            receipt.write("\n".toByteArray())
        }
        packageAmount?.let {
            if (displayedOptionalLabel != "Package Amount : RS ${it.toDouble()}") {
                receipt.write(formatSingleStringRight("Package Amount : RS $it", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }
        }

        otherChargesAmount?.let {
            if (displayedOptionalLabel != "Other Charges : RS $it") {
                receipt.write(formatSingleStringRight("Other Charges : RS $it", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }
        }

        customGstAmount?.let {
            val firstValue = it.split("|").first()
            if (displayedOptionalLabel != "Custom GST: RS ${firstValue.toDouble()}") {
                receipt.write(formatSingleStringRight("Custom GST : RS ${firstValue.toDouble()}", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }
        }

        if (discount != null && discount > 0) {
            if (displayedOptionalLabel != "Discount: RS $discount") {
                receipt.write(formatSingleStringRight("Discount : RS $discount", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }
        }

        if (creditNoteAmount != null && creditNoteAmount > 0) {
            if (displayedOptionalLabel != "Credit Note : RS $creditNoteAmount") {
                receipt.write(formatSingleStringRight("Credit Note : RS $creditNoteAmount", charactersPerLine).toByteArray())
                receipt.write("\n".toByteArray())
            }
        }

        receipt.write(boldOn)
        setFontSize(receipt, 2)
        receipt.write("\n".toByteArray())
        receipt.write(formatSingleString("Total: RS $totalAmount", charactersPerLine,fontSizeMultiplier = 2).toByteArray())
        setFontSize(receipt, 1)
        receipt.write(boldOff)
        receipt.write("\n".toByteArray())
        if (discount != null && discount > 0) {
            receipt.write(formatSingleString("You saved Rs $discount", charactersPerLine).toByteArray())
        }
        receipt.write("\n".toByteArray())

        if (isTaxAvailable){
            receipt.write("\n".toByteArray())
            receipt.write(formatSingleString("Tax Details", charactersPerLine).toByteArray())
            receipt.write("\n\n".toByteArray())
            receipt.write(
                formatLineWithMultipleAlignment(
                    listOf(
                        "TaxType" to 20,
                        "Amount" to 30,
                        "Rate" to 20,
                        "Tax Amount" to 30,
                    ),
                    charactersPerLine
                ).toByteArray()
            )
            receipt.write("\n".toByteArray())
            receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())
            for (invoiceTaxItem in invoiceTax) {
                receipt.write(
                    formatLineWithMultipleAlignment(
                        listOf(
                            invoiceTaxItem.taxType to 20,
                            invoiceTaxItem.taxableAmount.toString() to 30,
                            invoiceTaxItem.rate.toString() to 20,
                            invoiceTaxItem.taxAmount.toString() to 30
                        ),
                        charactersPerLine
                    ).toByteArray()
                )
                receipt.write("\n".toByteArray())
            }
        }

        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())
        receipt.write(formatSingleString("Payment Mode", charactersPerLine).toByteArray())
        receipt.write("\n".toByteArray())

        if (cashAmount != null && cashAmount > 0) {
            receipt.write(formatSingleString("Cash : RS $cashAmount", charactersPerLine).toByteArray())
            receipt.write("\n".toByteArray())
        }
        if (onlineAmount != null && onlineAmount > 0) {
            receipt.write(formatSingleString("Online : RS $onlineAmount", charactersPerLine).toByteArray())
            receipt.write("\n".toByteArray())
        }

        if (creditAmount != null && creditAmount > 0) {
            receipt.write(formatSingleString("Credit : RS $creditAmount", charactersPerLine).toByteArray())
            receipt.write("\n".toByteArray())
        }
        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())
        receipt.write(formatSingleString(footer, charactersPerLine).toByteArray())
        receipt.write("\n\n".toByteArray())

        receipt.write(boldOn)
        receipt.write(formatSingleString("Powered by hisabkitab", charactersPerLine).toByteArray())
        receipt.write(boldOff)
        return receipt.toByteArray()
    }


    fun setFontSize(receipt: ByteArrayOutputStream, size: Int) {
        val command = when (size) {
            1 -> byteArrayOf(0x1D, 0x21, 0x00) // Normal size
            2 -> byteArrayOf(0x1D, 0x21, 0x11) // Double width and height
            3 -> byteArrayOf(0x1D, 0x21, 0x22) // Triple width and height
            else -> byteArrayOf(0x1D, 0x21, 0x00) // Default to normal
        }
        receipt.write(command)
    }

    fun formatLineWithAlignment(left: String, right: String, paperWidth: Int): String {
        val totalColumns = paperWidth
        val leftWeight = (totalColumns * 40) / 100
        val rightWeight = totalColumns - leftWeight

        // Format left side to take its weight
        val leftFormatted = left.padEnd(leftWeight).take(leftWeight)

        // Format right side to take the remaining space, and add 3 spaces to the right
        val rightFormatted = right.padStart(rightWeight - 3).take(rightWeight - 3) + "   "

        // Combine left and right with proper alignment
        return leftFormatted + rightFormatted
    }


    fun formatLineWithMultipleAlignment(values: List<Pair<String, Int>>, paperWidth: Int): String {
        val totalColumns = paperWidth
        val formattedLine = StringBuilder()

        for ((text, weight) in values) {
            val allocatedWidth = (totalColumns * weight) / 100
            formattedLine.append(text.padStart((allocatedWidth + text.length) / 2).padEnd(allocatedWidth))
        }

        return formattedLine.toString()
    }

    fun generateSeparatorLine(paperWidth: Int): String {
        return "-".repeat(paperWidth) + "\n"
    }

    fun formatSingleString(text: String, paperWidth: Int, fontSizeMultiplier: Int = 1): String {
        // Adjust paperWidth based on font size multiplier
        val adjustedWidth = paperWidth / fontSizeMultiplier

        // Center-align the text within the adjusted width
        return text.padStart((adjustedWidth + text.length) / 2).padEnd(adjustedWidth)
    }

    fun formatSingleStringRight(text: String, paperWidth: Int): String {
        val totalWidth = paperWidth - 3
        return text.padStart(totalWidth.coerceAtLeast(0))
    }
    fun formatSingleStringLeft(text: String, paperWidth: Int): String {
        val totalWidth = paperWidth
        return text.padEnd(totalWidth.coerceAtLeast(0))
    }


    fun printImage(textData: ByteArray,alignment: Int, bitmap: Bitmap, width: Int): Boolean {
        val scaledBitmap = scaledBitmap(bitmap, width)
        if (scaledBitmap != null) {
            var marginLeft: Int = INITIAL_MARGIN_LEFT
            if (alignment == CENTER) {
                marginLeft = marginLeft + ((PRINTER_WIDTH - scaledBitmap.width) / 2)
            } else if (alignment == RIGHT) {
                marginLeft = marginLeft + PRINTER_WIDTH - scaledBitmap.width
            }
            val command = autoGrayScale(scaledBitmap, marginLeft, 5)
            val lines: Int = (command.size - HEAD) / WIDTH
            System.arraycopy(
                byteArrayOf(
                    0x1D, 0x76, 0x30, 0x00, 0x30, 0x00, (lines and 0xff).toByte(),
                    ((lines shr 8) and 0xff).toByte()
                ), 0, command, 0, HEAD
            )
            return printUnicode(command,textData)
        } else {
            return false
        }
    }

    private fun printUnicode(imageData: ByteArray, textData: ByteArray): Boolean {
        val combinedData = imageData + "\n".toByteArray()+ textData
        viewModel.printUsingDefaultPrinter(combinedData)
        return true
    }


    private fun printByteArray(textData: ByteArray) {
        val bitmap: Bitmap = imageViewToBitmap(binding.imageView) ?: return
         val print: Boolean = printImage(textData,CENTER,bitmap, 200)
            if (!print) {
                Toast.makeText(requireContext(), "Print image failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun autoGrayScale(bm: Bitmap, bitMarginLeft: Int, bitMarginTop: Int): ByteArray {
        val result: ByteArray
        val n = bm.height + bitMarginTop
        val offset: Int = HEAD
        result = ByteArray(n * WIDTH + offset)
        for (y in 0 until bm.height) {
            for (x in 0 until bm.width) {
                if (x + bitMarginLeft < BIT_WIDTH) {
                    val color = bm.getPixel(x, y)
                    val alpha = Color.alpha(color)
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    if (alpha > 128 && (red < 128 || green < 128 || blue < 128)) {
                        // set the color black
                        val bitX = bitMarginLeft + x
                        val byteX = bitX / 8
                        val byteY = y + bitMarginTop
                        result[offset + byteY * WIDTH + byteX] =
                            (result[offset + byteY * WIDTH + byteX].toInt() or (0x80 shr (bitX - byteX * 8))).toByte()
                    }
                } else {
                    // ignore the rest data of this line
                    break
                }
            }
        }
        return result
    }

    private fun scaledBitmap(bitmap: Bitmap, widths: Int): Bitmap? {
        var width = widths
        if (width == FULL_WIDTH) width = PRINTER_WIDTH
        try {
            var desiredWidth =
                if (width == 0 || bitmap.width <= PRINTER_WIDTH) bitmap.width else PRINTER_WIDTH
            if (width > 0 && width <= PRINTER_WIDTH) {
                desiredWidth = width
            }
            val height: Int
            val scale = desiredWidth.toFloat() / bitmap.width.toFloat()
            height = (bitmap.height * scale).toInt()
            return Bitmap.createScaledBitmap(bitmap, desiredWidth, height, true)
        } catch (e: NullPointerException) {
            Log.e("TAG", "Maybe resource is vector or mipmap?")
            return null
        }
    }

    fun imageViewToBitmap(imageView: ImageView): Bitmap? {
        return try {
            // Measure and layout the ImageView
            imageView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            // Create a Bitmap with the same dimensions as the ImageView
            val bitmap = Bitmap.createBitmap(
                imageView.measuredWidth,
                imageView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )

            // Draw the content of the ImageView onto the Bitmap
            val canvas = Canvas(bitmap)
            imageView.draw(canvas)

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(imageView.context, "Logo is not compatible, try another logo.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun getDeviceRgbFromColorResource(colorInt: Int): DeviceRgb {
        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)

        return DeviceRgb(red, green, blue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}