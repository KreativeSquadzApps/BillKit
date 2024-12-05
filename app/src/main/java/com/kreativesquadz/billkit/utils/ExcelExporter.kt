package com.kreativesquadz.billkit.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import android.media.MediaScannerConnection
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.full.memberProperties

class ExcelExporter<T : Any> {

    fun saveDataToExcel(context: Context, dataList: List<T>, fileName: String): Boolean {
        if (dataList.isEmpty()) return false  // Handle empty list early

        return try {
            val workbook: Workbook = XSSFWorkbook()
            val sheet: Sheet = workbook.createSheet("Data Export")

            // Create header row
            val headerRow: Row = sheet.createRow(0)
            val kClass = dataList[0]::class
            val properties = kClass.memberProperties

            // Set headers
            properties.forEachIndexed { index, prop ->
                val cell: Cell = headerRow.createCell(index)
                cell.setCellValue(prop.name)  // Use property names for headers
            }

            // Create data rows
            dataList.forEachIndexed { rowIndex, item ->
                val row: Row = sheet.createRow(rowIndex + 1)
                properties.forEachIndexed { colIndex, prop ->
                    val value = prop.getter.call(item)?.toString() ?: ""
                    row.createCell(colIndex).setCellValue(value)  // Safely access values
                }
            }

            // Save file in the public Documents directory
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )
            FileOutputStream(file).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()

            // Trigger media scan to make the file visible in file managers
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { _, uri ->
                Log.d("ExcelExporter", "File scanned into media store: $uri")
            }

            true  // Successfully saved
        } catch (e: Exception) {
            e.printStackTrace()
            false  // Save failed
        }
    }
}
