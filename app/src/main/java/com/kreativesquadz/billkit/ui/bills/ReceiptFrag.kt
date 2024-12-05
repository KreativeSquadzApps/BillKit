package com.kreativesquadz.billkit.ui.bills

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dantsu.escposprinter.EscPosPrinter
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
import com.kreativesquadz.billkit.model.settings.InvoicePrinterSettings
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchAllDetails(invoiceId!!)
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
        binding.istPackagingAvalaible = false
        return binding.root
    }

    fun observers(){
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
            }
            if (it.packageAmount == null || it.packageAmount <= 0){
                binding.istPackagingAvalaible = false
            }else{
                binding.istPackagingAvalaible = true
                binding.tvPackaging.text = "Packaging Rs "+ it.packageAmount
            }
            binding.tvTotalTax.text =  "Total Tax Rs " + it.totalAmount.toInt().minus(it.subtotal.toInt()).toDouble()
                //   binding.tvtotals.text = it.totalAmount.toInt().minus(it.subtotal.toInt()).toString()
            val discountApplied = it.discount?.toInt() ?: 0
            it.discount?.apply{
                binding.tvTotalTax.text =  "Total Tax Rs " + it.totalAmount.toInt().plus(it.discount.toInt()).minus(it.subtotal.toInt()).toDouble()
               // binding.tvtotals.text = it.totalAmount.toInt().plus(it.discount.toInt()).minus(it.subtotal.toInt()).toString()
            }
            it.packageAmount?.apply {
                binding.tvTotalTax.text =  "Total Tax Rs " + it.totalAmount.toInt().plus(discountApplied).minus(it.packageAmount.toInt()).minus(it.subtotal.toInt()).toDouble()
            }

            invoiceP = it
            binding.isCustomerAvailable = it?.customerId != null
            binding.customer = viewModel.getCustomerById(it?.customerId.toString())
            customerP = viewModel.getCustomerById(it?.customerId.toString())

        }

        viewModel.companyDetails.observe(viewLifecycleOwner){
            it?.let {
                binding.companyDetails = it
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
            setupRecyclerView(it)
            Log.e("TAG", "observersRecipt: $it")
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
        invoiceTax.clear()
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
                    taxAmount = (it.unitPrice * it.quantity) * it.taxRate / 100
                    invoiceTax.add(InvoiceTax("", it.unitPrice * it.quantity, it.taxRate, taxAmount))
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
                        if (invoiceItem.rate == gst.taxAmount) {
                            invoiceItem.taxType = gst.taxType
                        }
                    }

                }

            }
        }
        setupRecyclerViewGst(invoiceTax)


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
        val itemHeight = resources.getDimensionPixelSize(R.dimen._70dp) // set this to your item height
        val params = binding.itemListRecyclerview.layoutParams
        params.height = itemHeight * adapter.itemCount
        binding.itemListRecyclerview.layoutParams = params

    }


    private fun setupRecyclerViewGst(gst: List<InvoiceTax>?) {
        invoiceTax = gst?.toMutableList() ?: mutableListOf()
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


    private fun generateReceiptPdf(context: Context, pdfSettings: PdfSettings, items: List<InvoiceItem>, companyDetails: CompanyDetails, invoice: Invoice, customer : Customer?) {
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



        val file = File(context.getExternalFilesDir(null), "Invoice ${invoice.invoiceNumber}.pdf")
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
            invoiceDate = timestamp,
            invoiceId = invoice!!.invoiceNumber,
            isCustomerAvailable = isCustomer,
            customerName = customer?.customerName,
            customerNumber = customer?.shopContactNumber,
            customerGst = customer?.gstNo,
            packageAmount = invoice.packageAmount,
            customGstAmount = invoice.customGstAmount,
            customerAddress = customer?.address,
            items = invoiceItems,
            totalItems = invoiceItems.size,
            subtotal = invoice.subtotal,
            discount = discount.toDouble(),
            totalAmount = invoice.totalAmount,
            totalTax = invoice.totalAmount - invoice.subtotal - discount,
            cashAmount = invoice.cashAmount,
            onlineAmount = invoice.onlineAmount,
            creditAmount = invoice.creditAmount,
            footer = pdfSettings.pdfFooter
        )
        viewModel.printUsingDefaultPrinter(receipt)

    }


    private fun formatItem(
        slNo: String,
        name: String,
        quantity: String,
        rate: String,
        tax: String,
        total: String,
        paperWidth: Int
    ): String {
        val isTaxAvailable = true // Example toggle for tax availability
        val weights = if (isTaxAvailable) {
            listOf(0.4f, 1.0f, 0.3f, 0.5f, 0.3f, 0.5f)
        } else {
            listOf(0.4f, 2.0f, 0.5f, 0.5f, 1.5f)
        }
        val totalWeight = weights.sum()

        // Calculate column widths based on paperWidth
        val columnWidths = weights.map { weight -> ((weight / totalWeight) * paperWidth).toInt() }

        // Helper function to split a value into lines if it exceeds its column width
        fun splitToLines(value: String, columnWidth: Int): List<String> {
            return value.chunked(columnWidth)
        }

        // Generate rows for each line of content
        val rows = mutableListOf<List<String>>()
        val content = listOf(slNo, name, quantity, rate, tax, total)
        val splitContent = content.mapIndexed { index, value ->
            splitToLines(value, columnWidths[index])
        }

        // Calculate the maximum number of lines needed for this row
        val maxLines = splitContent.maxOf { it.size }

        // Generate the rows by combining lines from each column
        for (i in 0 until maxLines) {
            val row = splitContent.map { it.getOrElse(i) { "" } } // Safe access to get lines or an empty string if missing
            rows.add(row)
        }

        // Format rows into a string
        return buildString {
            rows.forEach { row ->
                row.forEachIndexed { index, value ->
                    val padding = if (index == row.size - 1) "" else " "
                    append(value.padEnd(columnWidths[index])).append(padding)
                }
                //append("\n")
            }
        }
    }

    private fun formatSingleString(
        value: String,
        paperWidth: Int
    ): String {
        // Calculate the space on the left and right to center-align the string
        val padding = (paperWidth - value.length) / 2

        // Return the value with the calculated padding for center alignment
        return buildString {
            append(" ".repeat(padding))      // Add leading spaces for centering
            append(value)                    // Append the value
            append(" ".repeat(padding)) // Add trailing spaces for centering
            append("\n")

        }.take(paperWidth) // Ensure the final string length is exactly `paperWidth`
    }

    private fun formatSingleStringRight(
        value: String,
        paperWidth: Int
    ): String {
        // Calculate the space to the left to right-align the string
        val padding = paperWidth - value.length

        // Return the value with the calculated padding for right alignment
        return buildString {
            append(" ".repeat(padding))  // Add spaces on the left to right-align the value
            append(value)
            append("\n")
        // Append the value
        }
    }

    private fun formatTwoStrings(
        firstValue: String,
        secondValue: String,
        paperWidth: Int
    ): String {
        // Calculate the available width between the two strings
        val totalWidth = paperWidth
        val spacing = totalWidth - (firstValue.length + secondValue.length)

        // Ensure that spacing is non-negative, handle overflow if needed
        return if (spacing >= 0) {
            "$firstValue${" ".repeat(spacing)}$secondValue"  // Align with proper spacing
        } else {
            // Truncate values to fit within the available width if too long
            val maxFirstLength = firstValue.length + spacing / 2
            val maxSecondLength = secondValue.length + spacing / 2

            val truncatedFirst = if (maxFirstLength > 0) firstValue.take(maxFirstLength) else ""
            val truncatedSecond = if (maxSecondLength > 0) secondValue.take(maxSecondLength) else ""

            "$truncatedFirst${" ".repeat(totalWidth - (truncatedFirst.length + truncatedSecond.length))}$truncatedSecond"
        }
    }



    private fun formatTax(paperWidth: Int, taxType: String, taxableAmount: String, rate: String, taxAmount: String): String {
        val weights = listOf(0.25f, 0.25f, 0.25f, 0.25f)  // Equal weights for the four items
        val totalWeight = weights.sum()

        // Calculate column widths based on paperWidth
        val columnWidths = weights.map { weight -> ((weight / totalWeight) * paperWidth).toInt() }

        // Format each column with its calculated width
        return buildString {
            append(taxType.padEnd(columnWidths[0]))     // Pad tax type
            append(taxableAmount.padEnd(columnWidths[1]))  // Pad taxable amount
            append(rate.padStart(columnWidths[2]))       // Pad rate
            append(taxAmount.padStart(columnWidths[3]))   // Pad tax amount
        }
    }


    private fun createReceiptString(
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
        packageAmount: Double?,
        customGstAmount: String?,
        customerAddress: String?,
        items: List<InvoiceItem>,
        totalItems: Int,
        subtotal: Double,
        discount: Double?,
        totalAmount: Double,
        totalTax: Double,
        cashAmount: Double?,
        onlineAmount: Double?,
        creditAmount: Double?,
        footer: String,
    ): String {
        val receipt = StringBuilder()
        var separatorLine = ""
        var paperWidth = "45" // Default to 58MM, adjust as needed

        thermalPrinterSetup?.let {
            //paperWidth = it.printerSize.replace("MM", "")
            Log.e("Paper Width", paperWidth)
            separatorLine = generateSeparatorLine(paperWidth)
        }

        // Company Details Header

        receipt.append(formatSingleString(businessName, paperWidth.toInt()))
        receipt.append(formatSingleString("  "+place, paperWidth.toInt())).append("\n")
        receipt.append(formatSingleString(contactNumber, paperWidth.toInt())).append("\n")
        receipt.append(formatSingleString("Email: $email", paperWidth.toInt()))
        receipt.append(formatSingleString("INVOICE", paperWidth.toInt())).append("\n")

        // Invoice Header
        receipt.append(formatTwoStrings(invoiceDate, "Invoice: $invoiceId", paperWidth.toInt()))
        receipt.append(generateSeparatorLine(paperWidth))


        // Customer Details
        if (isCustomerAvailable) {
            customerName?.let {
                if (customerName != ""){
                    receipt.append(formatSingleString(customerName ?: "", paperWidth.toInt())).append("\n")
                }
            }
            customerNumber?.let {
                if (customerNumber != ""){
                    receipt.append(formatSingleString("Contact: ${customerNumber ?: ""}", paperWidth.toInt())).append("\n")
                }
            }
            customerGst?.let {
                if (customerGst != ""){
                    receipt.append(formatSingleString("GST No: ${customerGst ?: ""}", paperWidth.toInt())).append("\n")
                }
            }
            customerAddress?.let {
                if (customerAddress != ""){
                    receipt.append(formatSingleString(customerAddress ?: "", paperWidth.toInt())).append("\n")
                }
            }

            receipt.append( separatorLine)
        }

            receipt.append(formatItem("SL No.", "Item", "Qty", "Rate","Tax", "Total",paperWidth.replace("MM","").toInt()))
        // Items Header

       receipt.append(generateSeparatorLine(paperWidth))

        // Items
        var slNo = 0
        for (item in items) {
            slNo++
            receipt.append(
                formatItem(
                    slNo.toString(),
                    item.itemName.split("(")[0], // Extract name before parentheses
                    item.quantity.toString(),
                    item.unitPrice.toString(),
                    item.taxRate.toString(),
                    item.totalPrice.toString(),
                    paperWidth.replace("MM","").toInt()
                )
            ).append("\n")
        }
        receipt.append(generateSeparatorLine(paperWidth))

        // Totals
        receipt.append(formatTwoStrings("Items: "+totalItems, "Sub Total: "+ subtotal, paperWidth.toInt()))

        if (discount != null && discount > 0) {
            receipt.append(formatSingleStringRight("Discount: "+ discount, paperWidth.toInt()))
            receipt.append(formatSingleString("You saved Rs "+ discount, paperWidth.toInt()))
        }
        if (isTaxAvailable){
            receipt.append(formatSingleStringRight("Total Tax:"+ totalTax, paperWidth.toInt()))
        }
        receipt.append(generateSeparatorLine(paperWidth))
//
        packageAmount?.let {
            receipt.append(formatSingleStringRight("Package Amount: "+ packageAmount, paperWidth.toInt()))
            receipt.append(generateSeparatorLine(paperWidth))
        }
///
        receipt.append(formatSingleString("Total: $totalAmount", paperWidth.toInt()))

//
        if (isTaxAvailable){
            receipt.append(generateSeparatorLine(paperWidth))
            receipt.append(formatTax(paperWidth.toInt(),"TaxType","Taxable Amount","Rate","Tax Amount"))
            receipt.append(generateSeparatorLine(paperWidth))
            invoiceTax.forEach {
                receipt.append(formatTax(paperWidth.toInt(),it.taxType,it.taxableAmount.toString(),it.rate.toString(),it.taxAmount.toString())).append("\n")
            }
        }
        receipt.append(generateSeparatorLine(paperWidth))
        receipt.append(formatSingleString("Payment Mode", paperWidth.toInt())).append("\n")

        if (cashAmount != null && cashAmount > 0) {
            receipt.append(formatSingleString("Cash: "+ cashAmount, paperWidth.toInt())).append("\n")

        }
        if (onlineAmount != null && onlineAmount > 0) {
            receipt.append(formatSingleString("Online: "+ onlineAmount, paperWidth.toInt())).append("\n")
        }
        if (creditAmount != null && creditAmount > 0) {
            receipt.append(formatSingleString("Credit: "+ creditAmount, paperWidth.toInt())).append("\n")
        }
        receipt.append(generateSeparatorLine(paperWidth))
        receipt.append(formatSingleString(footer, paperWidth.toInt())).append("\n")
        receipt.append(formatSingleString("Powered by billkit", paperWidth.toInt())).append("\n")
        return receipt.toString()
    }

    fun createReceipt(businessName: String,
                      place: String,
                      contactNumber: String,
                      email: String,
                      invoiceDate: String,
                      invoiceId: String,
                      isCustomerAvailable: Boolean,
                      customerName: String?,
                      customerNumber: String?,
                      customerGst: String?,
                      packageAmount: Double?,
                      customGstAmount: String?,
                      customerAddress: String?,
                      items: List<InvoiceItem>,
                      totalItems: Int,
                      subtotal: Double,
                      discount: Double?,
                      totalAmount: Double,
                      totalTax: Double,
                      cashAmount: Double?,
                      onlineAmount: Double?,
                      creditAmount: Double?,
                      footer: String) : String  {
        val slWidth = 4
        val receiptText = """
    [C]<b>$businessName</b>
    [C]$place
    [C]Contact: $contactNumber
    [C]Email: $email

    []Date: $invoiceDate[R]Invoice: $invoiceId
    ----------------------------------------------

    ${if (isCustomerAvailable) """
        [L]Customer: $customerName
        [L]Contact: $customerNumber
        [L]GST: $customerGst
        ----------------------------------------------
    """ else ""}
    
    [L]SL  Item       Qty  Rate  Tax   Total
    ----------------------------------------------
    ${
        items.mapIndexed { index, item ->
                val slNumber = "${index + 1}".padStart((slWidth + 1) / 2).padEnd(slWidth)
                "[C]${slNumber} ${item.itemName.take(8)} [R] ${item.quantity}  ${item.unitPrice} ${item.taxRate} ${item.totalPrice}"
            }.joinToString("\n")
        }
    ----------------------------------------------
    [L]Total Items: $totalItems [R] Subtotal: $subtotal

    ${
            if (isTaxAvailable) """
            [R]Total Tax: $totalTax
            ----------------------------------------------
            [C]<b>Tax Summary</b>
            [L]Tax Type[R]Taxable Amount[R]Rate[R]Tax Amount
            ${
                invoiceTax.joinToString("\n") {
                    "[L]${it.taxType}[R]${it.taxableAmount}[R]${it.rate}%[R]${it.taxAmount}"
                }
            }
            ----------------------------------------------
        """ else ""
        }
    
    [C]Payment Mode
    ${if (cashAmount != null && cashAmount > 0) "[L]Cash: $cashAmount" else ""}
    ${if (onlineAmount != null && onlineAmount > 0) "[L]Online: $onlineAmount" else ""}
    ${if (creditAmount != null && creditAmount > 0) "[L]Credit: $creditAmount" else ""}

    ----------------------------------------------
    [C]$footer
    [C]Powered by BillKit
""".trimIndent()

        return receiptText
    }


    private fun generateSeparatorLine(paperWidth: String): String {
        return "-".repeat(paperWidth.toInt()) + "\n"
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}