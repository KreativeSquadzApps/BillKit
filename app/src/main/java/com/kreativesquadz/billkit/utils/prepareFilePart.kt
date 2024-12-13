package com.kreativesquadz.billkit.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun prepareFilePart(fieldName: String, fileUri: Uri, context: Context): MultipartBody.Part {
    // Create a File from the Uri
    val file = File(context.cacheDir, getFileName(context, fileUri))
    
    // Copy the file contents from the URI to the File
    val inputStream = context.contentResolver.openInputStream(fileUri)
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    // Create a RequestBody instance from the file
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

    // Return a MultipartBody.Part instance
    return MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
}
@SuppressLint("Range")
fun getFileName(context: Context, uri: Uri): String {
    var name = ""
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return name
}
