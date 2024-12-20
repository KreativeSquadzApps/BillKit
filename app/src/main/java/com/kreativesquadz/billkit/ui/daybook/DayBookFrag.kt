package com.kreativesquadz.billkit.ui.daybook

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.databinding.FragmentDayBookBinding
import com.kreativesquadz.billkit.model.DayBook
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.utils.RoundedCellRenderer
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DayBookFrag : Fragment() {
    private var _binding: FragmentDayBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DayBookViewModel by viewModels()
    private var selectedDate: Long = 0L
    private val dayBook = mutableListOf<DayBook>()
    private var invoiceList: List<Invoice>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = viewModel.getSelectedDate()
        val selectedCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate // Use selectedDate here
            set(Calendar.HOUR_OF_DAY, 0) // Set time to midnight (start of the day)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = selectedCalendar.timeInMillis // Start of the selected day
        selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = selectedCalendar.timeInMillis - 1
        viewModel.getInvoices(startOfDay, endOfDay)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayBookBinding.inflate(inflater, container, false)
        binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)
        binding.isLoading = false
        observers()
        onClickListener()
        return binding.root
    }
    private fun observers(){
        viewModel.invoices.observe(viewLifecycleOwner) {
            invoiceList = it
            var totalSales = 0.0
            var totalSalesCredit = 0.0
            var totalSalesCreditCount = 0
            var totalSalesCash = 0.0
            var totalSalesOnline = 0.0
            it.forEach { invoice ->
                totalSales += invoice.totalAmount
                totalSalesCredit += invoice.creditAmount ?: 0.0
                if (invoice.creditAmount != null && invoice.creditAmount != 0.0) {
                    totalSalesCreditCount++
                }
                totalSalesCash += invoice.cashAmount ?: 0.0
                totalSalesOnline += invoice.onlineAmount ?: 0.0
                setInvoiceTable(invoice)
            }
            binding.tvSalesCredit.text = Config.CURRENCY +" "+totalSalesCredit
            binding.tvSalesCreditCount.text = totalSalesCreditCount.toString()

            binding.tvTotalSales.text = Config.CURRENCY +" "+totalSales
            binding.tvTotalSalesCount.text = it.size.toString()

            binding.tvTotalPaymentIn.text = Config.CURRENCY +" "+ totalSalesCash.plus(totalSalesOnline)
            binding.tvTotalCashIn.text = Config.CURRENCY +" "+ totalSalesCash.toString()
            binding.tvTotalOnlineIn.text = Config.CURRENCY +" "+ totalSalesOnline.toString()


        }


    }

    private fun setInvoiceTable(invoice :Invoice){
        val tableRow = TableRow(requireContext())
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        val textViewParams = TableRow.LayoutParams(
            0, // width will be set by weight
            TableRow.LayoutParams.MATCH_PARENT
        ).apply {
            weight = 1f // Each TextView takes equal space
        }
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color_main)
        val textSize = resources.getDimension(R.dimen.txt_14sp)
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.table_border)
        val backgroundLast = ContextCompat.getDrawable(requireContext(), R.drawable.table_border_green)
        fun createTextView(text: String): TextView {
            return TextView(requireContext()).apply {
                layoutParams = textViewParams
                this.text = text
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setBackgroundDrawable(background)
                gravity = Gravity.CENTER
            }
        }
        fun createTextViewLast(text: String): TextView {
            return TextView(requireContext()).apply {
                layoutParams = textViewParams
                this.text = text
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setBackgroundDrawable(backgroundLast)
                gravity = Gravity.CENTER
            }
        }
        var customerName = "--"
        if (invoice.customerId != null) {
            customerName = viewModel.getCustomerById(invoice.customerId.toString()).customerName

        }
        tableRow.addView(createTextView("Sale"))
        tableRow.addView(createTextView(invoice.invoiceNumber.toString()))
        tableRow.addView(createTextView(invoice.createdBy.replace("Created By","")))
        tableRow.addView(createTextView(customerName))
        tableRow.addView(createTextView(invoice.totalAmount.toString()))
        tableRow.addView(createTextViewLast(invoice.cashAmount.toString()))
        binding.invoiceTableLayout.addView(tableRow)
        val daybook = DayBook(category = "Sale", invoiceNumber = invoice.invoiceNumber.toString(),
            createdBy = invoice.createdBy.replace("Created By",""),
            customerName = customerName, totalAmount = invoice.totalAmount, cashAmount = invoice.cashAmount)
        dayBook.add(daybook)
    }

    private fun onClickListener(){
        binding.calenderView.setOnClickListener {
            setCurrentDateOnCalendar(binding.tvDate)
        }
        binding.saveData.setOnClickListener {
//            val dayBookExporter = ExcelExporter<DayBook>()
//            val dayBookSuccess = dayBookExporter.saveDataToExcel(requireContext(),dayBook,"dayBook.xlsx")
//            if (dayBookSuccess) println("dayBook saved successfully!") else println("Failed to save dayBook data.")
            binding.tvDataSaved.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            generateReceiptPdf(invoiceList)
        }
    }

    private fun setCurrentDateOnCalendar(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog and set initial date to current date
        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date
            val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
            textView.text = selectedDate
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedTimestamp = selectedCalendar.timeInMillis
            viewModel.saveSelectedDate(selectedTimestamp)
            for (i in binding.invoiceTableLayout.childCount - 1 downTo 1) {
                binding.invoiceTableLayout.removeViewAt(i)
            }
            val startOfDay = selectedCalendar.timeInMillis
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = selectedCalendar.timeInMillis - 1
            viewModel.getInvoices(startOfDay, endOfDay)
            binding.tvDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedTimestamp)

        }, year, month, day)
        datePickerDialog.show()
    }

    private fun generateReceiptPdf(invoiceList : List<Invoice>?) {
        binding.isLoading = true
        var totalSales = 0.0
        val totalSalesCount = invoiceList?.size.toString()
        var totalSalesCredit = 0.0
        var totalSalesCreditCount = 0
        var totalSalesCash = 0.0
        var totalSalesCashCount = 0
        var totalSalesOnline = 0.0
        val totalSalesOnlineCount = 0
        invoiceList?.forEach { invoice ->
            totalSales += invoice.totalAmount
            totalSalesCredit += invoice.creditAmount ?: 0.0
            if (invoice.creditAmount != null && invoice.creditAmount != 0.0) {
                totalSalesCreditCount++
            }
            totalSalesCash += invoice.cashAmount ?: 0.0
            invoice.cashAmount?.let {
                if (it > 0){
                    totalSalesCashCount++
                }
            }
            totalSalesOnline += invoice.onlineAmount ?: 0.0
        }
        val daybookDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)
        val file = File(requireContext().getExternalFilesDir(null), "DAYBOOK-${daybookDate}.pdf")
        val writer = PdfWriter(file)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val header = Paragraph("ckf")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(20f)
            .setFontColor(ColorConstants.WHITE)
            .setBackgroundColor(ColorConstants.BLUE)
            .setTextAlignment(TextAlignment.CENTER)
        header.setPaddingRight(40f)
        document.add(header)

        val invoiceDate = Paragraph("DAYBOOK - ${daybookDate}")
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(15f)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(-5f)
            .setMarginBottom(10f)
            .setMarginTop(10f)
        document.add(invoiceDate)

//        val table: Table
//            table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 1f, 2f, 2f, 2f)))
//
//        table.addCell(
//            Cell().add(
//                Paragraph("SL No").setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontColor(
//                    ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setPaddingLeft(10f)).setBorderRight(
//                Border.NO_BORDER))

        val tableTotal = Table(UnitValue.createPercentArray(floatArrayOf(48f,4f, 48f))) // Two columns with equal width
        tableTotal.setWidth(UnitValue.createPercentValue(100f)) // Ensure table width is 100% of page width
        tableTotal.setMarginTop(5f)
        tableTotal.setSpacingRatio(2f)

        val tableinternalLeft = Table(UnitValue.createPercentArray(floatArrayOf(55f,5f,40f)))
        tableinternalLeft.setWidth(UnitValue.createPercentValue(100f))

        createAlignedCell("Total Sales",totalSalesCount,totalSales.toString(),tableinternalLeft)
        createAlignedCell("Total Sales Credit",totalSalesCreditCount.toString(),totalSalesCredit.toString(),tableinternalLeft)
        createAlignedCell("Total Sales Cash",totalSalesCashCount.toString(),totalSales.toString(),tableinternalLeft)


        val tableinternalRight = Table(UnitValue.createPercentArray(floatArrayOf(55f,5f,40f)))
        tableinternalRight.setWidth(UnitValue.createPercentValue(100f))

        createAlignedCell("Purchase Credit Add","0","0",tableinternalRight)
        createAlignedCell("Purchase Credit Paid","0","0",tableinternalRight)
        createAlignedCell("Sales Credit Received","0","0",tableinternalRight)

        val spacerCell = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(ColorConstants.WHITE)

        val cellContent1 = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.lite_grey_200))
            .setPadding(10f)
            .add(tableinternalLeft)

        val cellContent2 = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.lite_grey_200))
            .setPadding(10f)
            .add(tableinternalRight)

        tableTotal.addCell(cellContent1)
        tableTotal.addCell(spacerCell) // Add spacer cell
        tableTotal.addCell(cellContent2)
        cellContent1.setNextRenderer(RoundedCellRenderer(cellContent1))
        cellContent2.setNextRenderer(RoundedCellRenderer(cellContent2))
        document.add(tableTotal)

        val tableInOut = Table(UnitValue.createPercentArray(floatArrayOf(48f,4f, 48f))) // Two columns with equal width
        tableInOut.setWidth(UnitValue.createPercentValue(100f)) // Ensure table width is 100% of page width
        tableInOut.setMarginTop(20f)
        tableInOut.setSpacingRatio(2f)

        val tableIn = Table(UnitValue.createPercentArray(floatArrayOf(55f,5f,40f)))
        tableIn.setWidth(UnitValue.createPercentValue(100f))

        val tableOut = Table(UnitValue.createPercentArray(floatArrayOf(55f,5f,40f)))
        tableOut.setWidth(UnitValue.createPercentValue(100f))

        val cellTableIn = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.darker_green))
            .setPadding(10f)
            .add(tableIn)

        val cellTableOut = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.darker_red))
            .setPadding(10f)
            .add(tableOut)

        val labelParagraph = Paragraph("RS 0.0")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

         val totalAmountOut = Paragraph("Total Payment Out")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f)
                    .setFontColor(ColorConstants.WHITE)
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val whiteSeparator = LineSeparator(SolidLine(1f).apply {
            setColor(ColorConstants.WHITE) // Set the line color to white
        })
            .setMarginTop(5f)
            .setMarginBottom(10f)

        val tableCashOnlineOut = Table(UnitValue.createPercentArray(floatArrayOf(50f,50f)))
        tableCashOnlineOut.setWidth(UnitValue.createPercentValue(100f))

        val cashValueOut = Paragraph("RS 0.0")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val txtCashOut = Paragraph("Cash")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))


        val onlineValueOut = Paragraph("RS 0.0")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val onlinetxtOnlineOut = Paragraph("Online")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))


        val cellCashOut = Cell().setBorder(Border.NO_BORDER)
            .setPadding(10f)
            .add(cashValueOut)
            .add(txtCashOut)


        val cellOnlineOut = Cell().setBorder(Border.NO_BORDER)
            .setPadding(10f)
            .add(onlineValueOut)
            .add(onlinetxtOnlineOut)

        tableCashOnlineOut.addCell(cellCashOut)
        tableCashOnlineOut.addCell(cellOnlineOut)

        cellTableOut.add(labelParagraph)
        cellTableOut.add(totalAmountOut)
        cellTableOut.add(whiteSeparator)
        cellTableOut.add(tableCashOnlineOut)


        val labelTotalSales = Paragraph("RS $totalSales")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val totalAmountIn = Paragraph("Total Payment In")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))


        val tableCashOnlineIn = Table(UnitValue.createPercentArray(floatArrayOf(50f,50f)))
        tableCashOnlineIn.setWidth(UnitValue.createPercentValue(100f))

        val cashValueIn = Paragraph("RS $totalSalesCash")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val txtCashIn = Paragraph("Cash")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))


        val onlineValueIn = Paragraph("RS $totalSalesOnline")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setBold()
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val onlinetxtOnlineIn = Paragraph("Online")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))


        val cellCashIn = Cell().setBorder(Border.NO_BORDER)
            .setPadding(10f)
            .add(cashValueIn)
            .add(txtCashIn)


        val cellOnlineIn = Cell().setBorder(Border.NO_BORDER)
            .setPadding(10f)
            .add(onlineValueIn)
            .add(onlinetxtOnlineIn)

        tableCashOnlineIn.addCell(cellCashIn)
        tableCashOnlineIn.addCell(cellOnlineIn)


        cellTableIn.add(labelTotalSales)
        cellTableIn.add(totalAmountIn)
        cellTableIn.add(whiteSeparator)
        cellTableIn.add(tableCashOnlineIn)



        cellTableIn.setNextRenderer(RoundedCellRenderer(cellTableIn))
        cellTableOut.setNextRenderer(RoundedCellRenderer(cellTableOut))
        tableInOut.addCell(cellTableOut)
        tableInOut.addCell(spacerCell) // Add spacer cell
        tableInOut.addCell(cellTableIn)
        document.add(tableInOut)

        val tableList = Table(UnitValue.createPercentArray(floatArrayOf(8f,15f,15f,12f,15f,15f,15f,5f)))
        tableList.setWidth(UnitValue.createPercentValue(100f))
        tableList.setMarginTop(20f)


        tableList.addCell(Cell().add(Paragraph("SL No").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("CATEGORY").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("PARTICULARS").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("CREATED").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("NAME").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("TOTAL").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(ColorConstants.BLUE).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("CASHIN").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.darker_green)).setPadding(5f))
        tableList.addCell(Cell().add(Paragraph("CASHOUT").setFontColor(ColorConstants.WHITE).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)).setBorderLeft(Border.NO_BORDER).setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.darker_red)).setPadding(5f))


        var slNo = 0
        dayBook.forEach {
            slNo++
            tableList.addCell(Cell().add(Paragraph(slNo.toString()).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.category).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.invoiceNumber).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.createdBy).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.customerName).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.totalAmount.toString()).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph(it.cashAmount.toString()).setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
            tableList.addCell(Cell().add(Paragraph("0").setTextAlignment(TextAlignment.CENTER).setFontSize(8f)))
        }
        document.add(tableList)

        val tableFooter = Table(UnitValue.createPercentArray(floatArrayOf(62f,38f)))
        tableFooter.setWidth(UnitValue.createPercentValue(100f))



        val cashInCashOut = Paragraph("CASHIN - CASHOUT")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val cellFooter1 = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(ColorConstants.BLUE)
            .setPadding(5f)
            .add(cashInCashOut)


        val footerFinalSales = Paragraph("RS $totalSales")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12f)
            .setFontColor(ColorConstants.WHITE)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val cellFooter2 = Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(getDeviceRgbFromColorResource(requireContext(),R.color.darker_green))
            .setPadding(5f)
            .add(footerFinalSales)

        tableFooter.addCell(cellFooter1)
        tableFooter.addCell(cellFooter2)
        document.add(tableFooter)


        val separator = LineSeparator(SolidLine(1f))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setMarginTop(50f)
            .setMarginBottom(10f)
        document.add(separator)

        val logo = BitmapFactory.decodeResource(requireContext().resources, R.drawable.logo)
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

        document.close()
        Toast.makeText(requireContext(), "PDF generated successfully", Toast.LENGTH_SHORT).show()
        binding.isLoading = false
        binding.tvDataSaved.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        sharePdf(requireContext(), file)
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

    fun createAlignedCell(label: String,count : String, value: String,table : Table){
        val labelParagraph = Paragraph(label)
            .setTextAlignment(TextAlignment.LEFT)
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val countParagraph = Paragraph(count)
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val valueParagraph = Paragraph("RS $value")
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(12f)
            .setFontColor(ColorConstants.BLACK)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))

        val cellLabel = Cell().setBorder(Border.NO_BORDER)
            .add(labelParagraph)
        val cellCountParagraph = Cell().setBorder(Border.NO_BORDER)
            .add(countParagraph)
        val cellValueParagraph = Cell().setBorder(Border.NO_BORDER)
            .add(valueParagraph)

        table.addCell(cellLabel)
        table.addCell(cellCountParagraph)
        table.addCell(cellValueParagraph)
    }
    fun getDeviceRgbFromColorResource(context : Context,colorResId: Int): DeviceRgb {
        val colorInt = ContextCompat.getColor(context, colorResId)

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