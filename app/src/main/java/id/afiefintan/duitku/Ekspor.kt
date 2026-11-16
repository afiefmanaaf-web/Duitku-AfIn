package id.afiefintan.duitku

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate

/** Tulis CSV ke cache lalu buka menu bagikan Android (Drive, WhatsApp, Gmail, dsb.). */
fun bagikanCsv(context: Context, isi: String) {
    val dir = File(context.cacheDir, "exports")
    dir.mkdirs()
    val file = File(dir, "Duitku-${LocalDate.now()}.csv")
    file.writeText(isi, Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Ekspor ke CSV"))
}
