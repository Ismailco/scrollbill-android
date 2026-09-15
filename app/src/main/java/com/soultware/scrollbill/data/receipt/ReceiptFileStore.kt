package com.soultware.scrollbill.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface ReceiptFileStore {
    suspend fun writePng(bitmap: Bitmap): File

    fun contentUri(file: File): Uri
}

class CacheReceiptFileStore(
    context: Context,
) : ReceiptFileStore {
    private val appContext = context.applicationContext
    private val cacheDirectory = File(appContext.cacheDir, RECEIPT_DIRECTORY)
    private val receiptFile = File(cacheDirectory, RECEIPT_FILE_NAME)
    private val providerAuthority = "${appContext.packageName}.fileprovider"

    override suspend fun writePng(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        ensureCacheDirectory()
        try {
            FileOutputStream(receiptFile).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Receipt PNG encoding failed"
                }
            }
        } catch (exception: IOException) {
            throw ReceiptFileException(exception)
        } catch (exception: IllegalStateException) {
            throw ReceiptFileException(exception)
        } catch (exception: SecurityException) {
            throw ReceiptFileException(exception)
        }
        receiptFile
    }

    override fun contentUri(file: File): Uri = try {
        FileProvider.getUriForFile(appContext, providerAuthority, file)
    } catch (exception: IllegalArgumentException) {
        throw ReceiptFileException(exception)
    } catch (exception: SecurityException) {
        throw ReceiptFileException(exception)
    }

    private fun ensureCacheDirectory() {
        try {
            if (!cacheDirectory.isDirectory && !cacheDirectory.mkdirs()) {
                throw ReceiptFileException(IOException("Receipt cache directory unavailable"))
            }
        } catch (exception: SecurityException) {
            throw ReceiptFileException(exception)
        }
    }

    companion object {
        const val RECEIPT_DIRECTORY = "shared_receipts"
        const val RECEIPT_FILE_NAME = "scrollbill-weekly-receipt.png"
    }
}

class ReceiptFileException(cause: Throwable) : Exception(cause)
