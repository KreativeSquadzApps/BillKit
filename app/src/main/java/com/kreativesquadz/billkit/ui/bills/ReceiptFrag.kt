package com.kreativesquadz.billkit.ui.bills

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
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
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.AdapterReceipt
import com.kreativesquadz.billkit.databinding.FragmentReceiptBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.InvoiceTax
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.utils.addBackPressHandler
import com.kreativesquadz.billkit.utils.toBoolean
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
    val invoiceTax = mutableListOf<InvoiceTax>()
    var isTaxAvailable = false
    var isMrpAvailable = false
    val invoiceId by lazy {
        arguments?.getString("invoiceId")
    }
    val target by lazy {
        arguments?.getString("target")
    }

    private var pdfSettings = PdfSettings()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCompanyDetailsRec()
        viewModel.fetchInvoiceItems(invoiceId!!.toLong())
        viewModel.getPdfSetting()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        addBackPressHandler(viewLifecycleOwner, ::shouldAllowBack)
        return binding.root
    }

    fun observers(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pdfSettings.collect { pdfSettings ->
                        this@ReceiptFrag.pdfSettings = pdfSettings
                    }
                }
            }
        }


        val invoice= viewModel.getInvoiceDetails(invoiceId!!)
        invoice.observe(viewLifecycleOwner) {
            binding.invoice = it
            binding.tvTotalTax.text =  "Total Tax : " + it.totalAmount.toInt().minus(it.subtotal.toInt())
                //   binding.tvtotals.text = it.totalAmount.toInt().minus(it.subtotal.toInt()).toString()
            it.discount?.apply{
                binding.tvTotalTax.text =  "Total Tax : " + it.totalAmount.toInt().plus(it.discount.toInt()).minus(it.subtotal.toInt())
               // binding.tvtotals.text = it.totalAmount.toInt().plus(it.discount.toInt()).minus(it.subtotal.toInt()).toString()

            }
            invoiceP = it
            binding.isCustomerAvailable = it?.customerId != null
            binding.customer = viewModel.getCustomerById(it?.customerId.toString())
            customerP = viewModel.getCustomerById(it?.customerId.toString())

        }
        viewModel.companyDetails.observe(viewLifecycleOwner){
            it.data?.let {
                binding.companyDetails = it
            }
        }
        viewModel.invoiceItems.observe(viewLifecycleOwner){
            setupRecyclerView(it)


            it?.forEach {
                if(it.taxRate > 0){
                    isTaxAvailable = true
                }
                if(it.productMrp != it.unitPrice){
                    isMrpAvailable = true
                }

            }
            binding.istTaxAvalaible = isTaxAvailable
            binding.istMrpAvalaible = isMrpAvailable

        }

        target?.let {
            if (it.equals(Config.BillDetailsFragmentToReceiptFragment)){
                binding.backImage.setBackgroundResource(R.drawable.home_light)
                binding.backText.text = "New Sale"
            }else{
                binding.backImage.setBackgroundResource(R.drawable.back_light)
                binding.backText.text = "Back"
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
            val action = ReceiptFragDirections.actionReceiptFragToBluetoothDeviceFragment(invoiceP)
            findNavController().navigate(action)
        }

        binding.btnShare.setOnClickListener {
            generateReceiptPdf(requireContext(),pdfSettings, viewModel.invoiceItems.value!!,viewModel.companyDetails.value?.data!!, invoiceP, customerP)
        }

    }


    private fun setupRecyclerView(receiptInvoiceItem: List<InvoiceItem>?) {
        var isTaxAvailable = false
        var isMrpAvailable = false
        val taxList = mutableListOf<Double>()
        var taxAmount: Double
        var taxableAmount : Double = 0.0

        receiptInvoiceItem?.forEach {
            if (it.taxRate > 0) {
                isTaxAvailable = true
                val index = taxList.indexOf(it.taxRate)
                if (index >= 0) {
                    // Tax rate exists, update the taxable amount and tax amount
                    invoiceTax[index].taxableAmount = invoiceTax[index].taxableAmount.plus(it.totalPrice)
                    invoiceTax[index].taxAmount = invoiceTax[index].taxableAmount * it.taxRate / 100
                } else {
                    // Tax rate doesn't exist, add a new entry
                    taxList.add(it.taxRate)
                    val taxAmount = it.totalPrice * it.taxRate / 100
                    invoiceTax.add(InvoiceTax("", it.totalPrice, it.taxRate, taxAmount))
                }
            }
            if(it.productMrp != it.unitPrice){
                isMrpAvailable = true
            }

        }

        viewModel.getGstListByValue(taxList).observe(viewLifecycleOwner) { gstList ->
            gstList?.let { gstItems ->
                gstItems.forEach { gst ->
                    invoiceTax.forEach { invoiceItem ->
                        if (invoiceItem.rate == gst.taxAmount) {
                            invoiceItem.taxType = gst.taxType

                        }
                    }
                }
            }
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
            isMrpAvailable
        )
        binding.itemListRecyclerview.adapter = adapter
        binding.itemListRecyclerview.layoutManager = LinearLayoutManager(context)


    }


    private fun setupRecyclerViewGst(gst: List<InvoiceTax>?) {
        adapterGst = GenericAdapter(
            gst ?: emptyList(),
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


    fun generateReceiptPdf(context: Context,pdfSettings: PdfSettings, items: List<InvoiceItem>,companyDetails: CompanyDetails,invoice: Invoice,customer : Customer?) {
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



        val file = File(context.getExternalFilesDir(null), "Invoice ${invoiceId}.pdf")
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


            val header = Paragraph("INVOICE")
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

        val invoiceId = Paragraph("Invoice No : ${invoiceId}")
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


        table.addCell(Cell().add(Paragraph("SL No").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("ITEM").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("QTY").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
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
        table.addCell(Cell().add(Paragraph("PRICE").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("TAX[ % ]").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("AMOUNT").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER))



        // Table Rows
        var serialNo = 0
        items.forEach { item ->
            serialNo++
            val  finalRate = item.unitPrice - item.unitPrice * item.taxRate / 100
            val  taxAmount = item.unitPrice - finalRate

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

        val invoiceTotalTax = Paragraph(binding.tvTotalTax.text.toString())
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(-5f)
        document.add(invoiceTotalTax)

        val totalQty = Paragraph("Total Qty : ${serialNo}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.LEFT)
        if (isItemTableQty.toBoolean()){
            document.add(totalQty)
        }


        invoice.cashAmount?.let {
            if (isItemTablePayment.toBoolean()) {
                val cash = Paragraph("Cash : ${it}")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                    .setFontSize(12f)
                    .setFontColor(ColorConstants.BLACK)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(-5f)
                document.add(cash)

                invoice.onlineAmount?.let {
                    val onlineAmount = Paragraph("Online : ${it}")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(12f)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginTop(-5f)
                    document.add(onlineAmount)
                }
                invoice.creditAmount?.let {
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
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
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

            tableTax.addCell(Cell().add(Paragraph("SL No").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAX TYPE").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAXABLE AMOUNT").setFontSize(10f).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("RATE").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(5f)).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER))
            tableTax.addCell(Cell().add(Paragraph("TAX AMOUNT").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderLeft(Border.NO_BORDER))

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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}