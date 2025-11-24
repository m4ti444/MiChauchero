package com.example.michauchero.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.michauchero.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.FileProvider
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import java.text.NumberFormat
import java.util.Locale
import java.io.File

class ReportRepository {
    suspend fun generateAndSave(context: Context, year: Int, month: Int, income: Double, expenses: Double, budgetAmount: Double?): Uri =
        withContext(Dispatchers.IO) {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val name = "reporte-${year}-${String.format("%02d", month)}.pdf"
            val file = File(dir, name)

            val ok = runCatching {
                val body = ApiClient.service.generarReportePdf(year, month, income, expenses, budgetAmount)
                val bytes = body.bytes()
                file.writeBytes(bytes)
            }.isSuccess

            if (!ok) {
                val doc = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.textSize = 16f
                val nf = NumberFormat.getCurrencyInstance(Locale.getDefault())
                canvas.drawText("Reporte mensual $year-${String.format("%02d", month)}", 72f, 72f, paint)
                canvas.drawText("Ingresos: ${nf.format(income)}", 72f, 100f, paint)
                canvas.drawText("Gastos: ${nf.format(expenses)}", 72f, 124f, paint)
                val budgetText = budgetAmount?.let { nf.format(it) } ?: "No establecido"
                canvas.drawText("Presupuesto: $budgetText", 72f, 148f, paint)
                canvas.drawText("Balance: ${nf.format(income - expenses)}", 72f, 172f, paint)
                doc.finishPage(page)
                file.outputStream().use { doc.writeTo(it) }
                doc.close()
            }

            FileProvider.getUriForFile(context, "${com.example.michauchero.BuildConfig.APPLICATION_ID}.fileprovider", file)
        }
}
