package id.afiefintan.duitku

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Brand = Color(0xFF0F9D7A)
val BrandGelap = Color(0xFF0B6E7A)
val Hijau = Color(0xFF2DB55D)
val Merah = Color(0xFFE5484D)
val Oranye = Color(0xFFF59E0B)
val Biru = Color(0xFF3B82F6)

fun warnaJenis(jenis: JenisTransaksi): Color = when (jenis) {
    JenisTransaksi.PEMASUKAN -> Hijau
    JenisTransaksi.PENGELUARAN -> Merah
    JenisTransaksi.TRANSFER -> Biru
}

fun warnaDari(hex: String): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }

@Composable
fun DuitkuTheme(gelap: Boolean, content: @Composable () -> Unit) {
    val skema = if (gelap) {
        darkColorScheme(
            primary = Brand,
            onPrimary = Color.White,
            background = Color(0xFF000000),
            onBackground = Color(0xFFF2F2F7),
            surface = Color(0xFF1C1C1E),
            onSurface = Color(0xFFF2F2F7),
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFF9A9AA2),
            outlineVariant = Color(0xFF3A3A3C),
            error = Merah
        )
    } else {
        lightColorScheme(
            primary = Brand,
            onPrimary = Color.White,
            background = Color(0xFFF2F2F7),
            onBackground = Color(0xFF1C1C1E),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFFE6E6EA),
            onSurfaceVariant = Color(0xFF6B6B72),
            outlineVariant = Color(0xFFDADADF),
            error = Merah
        )
    }
    MaterialTheme(colorScheme = skema, content = content)
}
