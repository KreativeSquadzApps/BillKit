package com.kreativesquadz.billkit.ui.bills.creditNote.creditNoteDetails.creditNoteReceipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.view.Gravity.CENTER
import android.view.Gravity.RIGHT
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.ColorConstants
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
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.AdapterReceipt
import com.kreativesquadz.billkit.databinding.FragmentCreditNoteReceiptBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.InvoiceTax
import com.kreativesquadz.billkit.model.settings.InvoicePrinterSettings
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import com.kreativesquadz.billkit.ui.bills.ReceiptFragDirections
import com.kreativesquadz.billkit.utils.Glide.GlideHelper
import com.kreativesquadz.billkit.utils.toBoolean
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CreditNoteReceiptFragment : Fragment() {
    private var _binding: FragmentCreditNoteReceiptBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreditNoteReceiptViewModel by viewModels()
    private lateinit var adapter: AdapterReceipt<InvoiceItem>
    private var customerP : Customer? = null
    private var creditNoteP : CreditNote? = null
    var isTaxAvailable = false
    var isMrpAvailable = false
    private var pdfSettings = PdfSettings()
    private var invoicePrinterSettings = InvoicePrinterSettings()
    private var thermalPrinterSetup: ThermalPrinterSetup? = null
    private val PRINTER_WIDTH: Int = 576
    private val INITIAL_MARGIN_LEFT: Int = -5
    private val BIT_WIDTH: Int = 384
    private val WIDTH: Int = 48
    private val HEAD: Int = 8
    val FULL_WIDTH: Int = -1
    private var isLogoPrint = false
    val creditNote by lazy {
        arguments?.getSerializable("creditNote") as CreditNote?
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lllloooo",creditNote.toString())
        creditNote?.let {
            viewModel.fetchAllDetails(it.invoiceId.toString())
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
        _binding = FragmentCreditNoteReceiptBinding.inflate(inflater, container, false)
        creditNoteP = creditNote
        binding.creditNote = creditNoteP
        val totalTax = (creditNoteP?.totalAmount!! - creditNoteP?.amount!!)
        if (totalTax > 0){
            binding.tvTotalTax.text = "Total Tax RS: ${(creditNoteP?.totalAmount!! - creditNoteP?.amount!!)}"
            binding.tvTotalTax.visibility = View.VISIBLE
        }else{
            binding.tvTotalTax.visibility = View.GONE
        }
        observers()
        onClickListeners()
        binding.isPrintLoading  = false

        return binding.root
    }

    private fun observers() {
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
                        this@CreditNoteReceiptFragment.pdfSettings = pdfSettings
                        binding.tvFooter.text = pdfSettings.pdfFooter
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.invoicePrinterSettings.collect { invoicePrinterSettings ->
                        this@CreditNoteReceiptFragment.invoicePrinterSettings = invoicePrinterSettings
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
                        }else{
                            binding.isVisibleGst.visibility = View.GONE
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
                        }else{
                        }

                        if (isPrinterItemTableQty.toBoolean()){
                        }else{
                        }
                    }
                }
            }
        }

        viewModel.companyDetails.observe(viewLifecycleOwner){
            it?.let {
                binding.companyDetails = it
                GlideHelper.loadImage(requireContext(), it.BusinessImage, binding.imageView)
            }
        }
        viewModel.invoiceItems.observe(viewLifecycleOwner){
            it?.forEach {
                if(it.taxRate > 0){
                    isTaxAvailable = true
                    binding.istProductTaxAvalaible = true
                }
                if(it.productMrp != it.unitPrice){
                    isMrpAvailable = true
                }
            }
            binding.istTaxAvalaible = isTaxAvailable
            binding.istMrpAvalaible = isMrpAvailable
            it?.let {
                val list = it.filter { it.returnedQty!! > 0 }
                setupRecyclerView(list)
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
                        printInvoice(customerP,creditNoteP, viewModel.invoiceItems.value!!, viewModel.companyDetails.value!!)
                    }
                }
                // Other operations after all data is ready
            }
        }
    }


    private fun onClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnPrint.setOnClickListener {
            if (thermalPrinterSetup != null && thermalPrinterSetup!!.defaultPrinterAddress.isNotEmpty()){
                printInvoice(customerP,creditNoteP,viewModel.invoiceItems.value!!, viewModel.companyDetails.value!!)
            }else{
                Toast.makeText(requireContext(), "No Default Printer Found", Toast.LENGTH_SHORT).show()
//                val action = ReceiptFragDirections.actionReceiptFragToBluetoothDeviceFragment()
//                findNavController().navigate(action)
            }
        }
        binding.btnShare.setOnClickListener {
            generateReceiptPdf(requireContext(),creditNoteP,pdfSettings, viewModel.invoiceItems.value!!,viewModel.companyDetails.value!!, customerP)
        }
    }

    private fun setupRecyclerView(receiptInvoiceItem: List<InvoiceItem>?) {
        var isTaxAvailable = false
        var isMrpAvailable = false
        receiptInvoiceItem?.forEach {
            if(it.productMrp != it.unitPrice){
                isMrpAvailable = true
            }
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

    private fun generateReceiptPdf(context: Context,creditNote: CreditNote?,pdfSettings: PdfSettings, items: List<InvoiceItem>, companyDetails: CompanyDetails, customer : Customer?) {
        val pdfCompanyInfoValues = pdfSettings.pdfCompanyInfo.split(" ")
        val isCompanyLogo = pdfCompanyInfoValues.getOrNull(0)?.toIntOrNull() ?: 0
        val isCompanyEmail = pdfCompanyInfoValues.getOrNull(1)?.toIntOrNull() ?: 0
        val isCompanyPhone = pdfCompanyInfoValues.getOrNull(2)?.toIntOrNull() ?: 0
        //  isCompanyAddress = pdfCompanyInfoValues.getOrNull(3)?.toIntOrNull() ?: 0
        val  isCompanyGst = pdfCompanyInfoValues.getOrNull(3)?.toIntOrNull() ?: 0

        // Split pdfItemTable and update corresponding variables
        val pdfItemTableValues = pdfSettings.pdfItemTable.split(" ")
        val  isItemTableCustomerDetails = pdfItemTableValues.getOrNull(0)?.toIntOrNull() ?: 0
        val  isItemTableMrp = pdfItemTableValues.getOrNull(1)?.toIntOrNull() ?: 0
        val isItemTablePayment = pdfItemTableValues.getOrNull(2)?.toIntOrNull() ?: 0
        val isItemTableQty = pdfItemTableValues.getOrNull(3)?.toIntOrNull() ?: 0



        val file = File(context.getExternalFilesDir(null), "CreditNote ${creditNote?.invoiceNumber!!}.pdf")
        val writer = PdfWriter(file)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)


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


        val header = Paragraph("CreditNote")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(20f)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
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

        val invoiceId = Paragraph("Invoice No : ${creditNote?.invoiceNumber!!}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(14f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
        document.add(invoiceId)

        val date = Date(creditNote.dateTime.toLong())
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


        table.addCell(
            Cell().add(
                Paragraph("SL No").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderRight(
                Border.NO_BORDER))
        table.addCell(
            Cell().add(
                Paragraph("ITEM").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(
                Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(
            Cell().add(
                Paragraph("QTY").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f)).setBorderLeft(
                Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        if (isItemTableMrp.toBoolean()) {
            table.addCell(
                Cell().add(
                    Paragraph("MRP").setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setFontColor(ColorConstants.WHITE)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setPaddingLeft(5f)
                ).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
            )
        }
        table.addCell(
            Cell().add(
                Paragraph("PRICE").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(
                Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(
            Cell().add(
                Paragraph("TAX[ % ]").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(
                Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(
            Cell().add(
                Paragraph("AMOUNT").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(
                Border.NO_BORDER))



        // Table Rows
        var serialNo = 0
        items.forEach { item ->
            serialNo++
            val  finalRate = item.unitPrice - item.unitPrice * item.taxRate / 100
            val  taxAmount = item.unitPrice - finalRate

            table.addCell(
                Cell().add(Paragraph(serialNo.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderRight(
                    Border.NO_BORDER))
            table.addCell(
                Cell().add(Paragraph(item.itemName.split("(")[0]).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(
                    Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(
                Cell().add(Paragraph(item.quantity.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(
                    Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            if (isItemTableMrp.toBoolean()) {
                table.addCell(
                    Cell().add(
                        Paragraph(item.productMrp.toString()).setPaddingLeft(10f).setFontSize(12f)
                    ).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                )
            }
            table.addCell(
                Cell().add(Paragraph(finalRate.toString()).setPaddingLeft(10f).setFontSize(12f)).setBorderLeft(
                    Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(
                Cell().add(
                    Paragraph((taxAmount * item.quantity).toString() +" ["+ item.taxRate.toString()+"%]").setPaddingLeft(10f).setFontSize(10f).setTextAlignment(
                        TextAlignment.CENTER)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            table.addCell(
                Cell().add(
                    Paragraph(item.totalPrice.toString()).setPaddingRight(10f).setFontSize(12f).setTextAlignment(
                        TextAlignment.RIGHT)).setBorderLeft(Border.NO_BORDER))
        }
        document.add(table)

        val invoiceSubTotal = Paragraph("Sub Total : ${Config.CURRENCY} ${creditNote.amount}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(5f)
        document.add(invoiceSubTotal)

        val invoiceTotalTax = Paragraph((creditNote.totalAmount - creditNote.amount).toString())
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        document.add(invoiceTotalTax)




        val tableTotal = Table(UnitValue.createPercentArray(floatArrayOf(70f, 20f))) // Two columns with equal width
        tableTotal.setWidth(UnitValue.createPercentValue(100f)) // Ensure table width is 100% of page width
        tableTotal.setMarginTop(5f)

// First paragraph
        val amountToWords = Paragraph(amountToWords(creditNote.totalAmount))
            .setTextAlignment(TextAlignment.LEFT)
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setPadding(5f)

// Second paragraph
        val invoiceTotal = Paragraph("Total : ${creditNote.totalAmount}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setPadding(5f)
            .setTextAlignment(TextAlignment.RIGHT)

// Add paragraphs to the table as cells
        tableTotal.addCell(Cell().add(amountToWords).setBorder(Border.NO_BORDER))
        tableTotal.addCell(Cell().add(invoiceTotal).setBorder(Border.NO_BORDER))
        document.add(tableTotal)

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
        val uri: Uri = FileProvider.getUriForFile(context, "com.kreativesquadz.billkit.provider", file)
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

    private fun printInvoice(customer : Customer?,creditNote: CreditNote?, invoiceItems : List<InvoiceItem>, companyDetails : CompanyDetails) {
        val isCustomer = customer != null
        val timestamp = try {
            val date = Date(creditNote?.dateTime!!.toLong())
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
            invoiceId = creditNote?.invoiceNumber!!,
            isCustomerAvailable = isCustomer,
            customerName = customer?.customerName,
            customerNumber = customer?.shopContactNumber,
            customerGst = customer?.gstNo,
            customerAddress = customer?.address,
            items = invoiceItems,
            totalItems = invoiceItems.size,
            subtotal = creditNote.amount,
            totalAmount = creditNote.totalAmount,
            totalTax = (creditNote.totalAmount - creditNote.amount) ,
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
        customerAddress: String?,
        items: List<InvoiceItem>,
        totalItems: Int,
        subtotal: Double,
        totalAmount: Double,
        totalTax: Double?,
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
//        if (isGstAvailable){
//            gst?.let {
//                receipt.write("\n".toByteArray())
//                receipt.write(formatSingleString("GST : $gst", charactersPerLine).toByteArray())
//            }
//        }

        receipt.write("\n\n".toByteArray())

        // Add Invoice Header
        setFontSize(receipt, 2)
        receipt.write(boldOn)
        receipt.write(formatSingleString("CreditNote", charactersPerLine, fontSizeMultiplier = 2).toByteArray())
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

        if (totalTax != null && totalTax > 0) {
            receipt.write(formatSingleStringRight("Total Tax : RS $totalTax", charactersPerLine).toByteArray())
            receipt.write("\n".toByteArray())
        }
        receipt.write(boldOn)
        setFontSize(receipt, 2)
        receipt.write("\n".toByteArray())
        receipt.write(formatSingleString("Total: RS $totalAmount", charactersPerLine,fontSizeMultiplier = 2).toByteArray())
        setFontSize(receipt, 1)
        receipt.write(boldOff)
        receipt.write("\n".toByteArray())

        receipt.write(generateSeparatorLine(charactersPerLine).toByteArray())
        receipt.write(formatSingleString(footer, charactersPerLine).toByteArray())
        receipt.write("\n\n".toByteArray())

        receipt.write(boldOn)
        receipt.write(formatSingleString("Powered by billkit", charactersPerLine).toByteArray())
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
        val print: Boolean = printImage(textData, CENTER,bitmap, 200)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}