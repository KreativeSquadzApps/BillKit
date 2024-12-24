package com.kreativesquadz.hisabkitab.utils

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeScannerUtil {

    companion object {
        private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()

        fun getBarcodeScanner(): BarcodeScanner {
            return BarcodeScanning.getClient(barcodeScannerOptions)
        }
    }
}
