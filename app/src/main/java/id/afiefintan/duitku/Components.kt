@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

// ------------------------------------------------------------ Kerangka halaman

@Composable
fun Halaman(
    judul: String,
    modifier: Modifier = Modifier,
    subjudul: String? = null,
    onKembali: (() -> Unit)? = null,
    aksi: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val latar = MaterialTheme.colorScheme.background
    Scaffold(
        modifier = modifier,
        containerColor = latar,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(judul, fontWeight = FontWeight.Bold)
                        if (subjudul != null) {
                            Text(
                                subjudul,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onKembali != null) {
                        IconButton(onClick = onKembali) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                },
                actions = aksi,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = latar,
                    scrolledContainerColor = latar
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = fab,
        content = content
    )
}

// ------------------------------------------------------------ Kartu & lencana

@Composable
fun Kartu(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun IconBadge(ikon: String, warnaHex: String, ukuran: Dp = 40.dp) {
    val w = warnaDari(warnaHex)
    Box(
        modifier = Modifier
            .size(ukuran)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(w.copy(alpha = 0.82f), w))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Palette.ikonDari(ikon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(ukuran * 0.5f)
        )
    }
}

@Composable
fun Label(teks: String) {
    Text(
        teks,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ------------------------------------------------------------ Kontrol sederhana

@Composable
fun ChipPilih(teks: String, aktif: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (aktif) Brand else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            teks,
            style = MaterialTheme.typography.labelLarge,
            color = if (aktif) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Segmen(pilihan: List<String>, terpilih: Int, onPilih: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        pilihan.forEachIndexed { i, teks ->
            val aktif = i == terpilih
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (aktif) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onPilih(i) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    teks,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun NavigatorBulan(bulan: YearMonth, onUbah: (YearMonth) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onUbah(bulan.minusMonths(1)) }) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Bulan sebelumnya")
        }
        Text(
            Waktu.judulBulan(bulan),
            modifier = Modifier
                .weight(1f)
                .clickable { onUbah(YearMonth.now()) },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { onUbah(bulan.plusMonths(1)) }) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Bulan berikutnya")
        }
    }
}

// ------------------------------------------------------------ Input Rupiah

/** Menampilkan "1250000" sebagai "1.250.000" tanpa mengubah teks aslinya. */
class RibuanTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val asli = text.text
        val n = asli.length
        val keluar = StringBuilder()
        val peta = IntArray(n + 1)
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) keluar.append('.')
            peta[i] = keluar.length
            keluar.append(asli[i])
        }
        peta[n] = keluar.length
        val hasil = keluar.toString()

        val pemetaan = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = peta[offset.coerceIn(0, n)]
            override fun transformedToOriginal(offset: Int): Int =
                hasil.take(offset).count { it != '.' }
        }
        return TransformedText(AnnotatedString(hasil), pemetaan)
    }
}

@Composable
fun RupiahField(
    nilai: Long,
    onNilai: (Long) -> Unit,
    modifier: Modifier = Modifier,
    ukuran: TextUnit = 40.sp,
    warna: Color = MaterialTheme.colorScheme.onSurface,
    fokus: FocusRequester? = null
) {
    val gaya = TextStyle(fontSize = ukuran, fontWeight = FontWeight.Bold, color = warna)
    val modField = if (fokus != null) modifier.focusRequester(fokus) else modifier
    BasicTextField(
        value = if (nilai == 0L) "" else nilai.toString(),
        onValueChange = { s ->
            onNilai(s.filter { it in '0'..'9' }.take(12).toLongOrNull() ?: 0L)
        },
        modifier = modField,
        textStyle = gaya,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = RibuanTransformation(),
        cursorBrush = SolidColor(warna),
        decorationBox = { isi ->
            Box {
                if (nilai == 0L) {
                    Text("0", style = gaya.copy(color = warna.copy(alpha = 0.3f)))
                }
                isi()
            }
        }
    )
}

// ------------------------------------------------------------ Pemilih ikon & warna

@Composable
fun PemilihIkon(terpilih: String, warnaHex: String, onPilih: (String) -> Unit) {
    val w = warnaDari(warnaHex)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Palette.daftarIkon.chunked(6).forEach { baris ->
            Row(Modifier.fillMaxWidth()) {
                baris.forEach { kunci ->
                    val dipilih = kunci == terpilih
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dipilih) w.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(2.dp, if (dipilih) w else Color.Transparent, CircleShape)
                                .clickable { onPilih(kunci) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Palette.ikonDari(kunci),
                                contentDescription = null,
                                tint = if (dipilih) w else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                repeat(6 - baris.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun PemilihWarna(terpilih: String, onPilih: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Palette.warna.chunked(6).forEach { baris ->
            Row(Modifier.fillMaxWidth()) {
                baris.forEach { hex ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(warnaDari(hex))
                                .clickable { onPilih(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hex == terpilih) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                repeat(6 - baris.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun ChipDompet(
    dompets: List<Dompet>,
    terpilihId: Long?,
    onPilih: (Dompet) -> Unit,
    kecualiId: Long? = null
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dompets.filter { it.id != kecualiId }.forEach { d ->
            val dipilih = d.id == terpilihId
            val w = warnaDari(d.warnaHex)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (dipilih) w else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPilih(d) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Palette.ikonDari(d.ikon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (dipilih) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    d.nama,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (dipilih) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ------------------------------------------------------------ Baris daftar

@Composable
fun BarisTransaksi(
    t: Transaksi,
    kategori: Kategori?,
    dompet: Dompet?,
    tujuan: Dompet?,
    onClick: () -> Unit
) {
    val jenis = t.jenis
    val adalahTransfer = jenis == JenisTransaksi.TRANSFER
    val judul = if (adalahTransfer) "Transfer" else (kategori?.nama ?: "Tanpa Kategori")
    val subjudul = if (adalahTransfer) {
        "${dompet?.nama ?: "?"} → ${tujuan?.nama ?: "?"}"
    } else {
        val d = dompet?.nama ?: "-"
        if (t.catatan.isEmpty()) d else "$d · ${t.catatan}"
    }
    val jumlahTeks = when (jenis) {
        JenisTransaksi.PEMASUKAN -> "+" + Money.format(t.jumlah)
        JenisTransaksi.PENGELUARAN -> "-" + Money.format(t.jumlah)
        JenisTransaksi.TRANSFER -> Money.format(t.jumlah)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (adalahTransfer) {
            IconBadge("transfer", "#54A0FF")
        } else {
            IconBadge(kategori?.ikon ?: "tanya", kategori?.warnaHex ?: "#8395A7")
        }
        Column(Modifier.weight(1f)) {
            Text(judul, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subjudul,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(jumlahTeks, fontWeight = FontWeight.SemiBold, color = warnaJenis(jenis), maxLines = 1)
    }
}

@Composable
fun BarisAnggaran(kategori: Kategori?, batas: Long, terpakai: Long, onClick: (() -> Unit)? = null) {
    val rasio = if (batas > 0) terpakai.toFloat() / batas.toFloat() else 0f
    val warna = if (rasio >= 1f) Merah else if (rasio >= 0.8f) Oranye else Hijau
    val dasar = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(
        modifier = dasar.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconBadge(kategori?.ikon ?: "label", kategori?.warnaHex ?: "#8395A7", 34.dp)
            Text(
                kategori?.nama ?: "Kategori dihapus",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "${Money.compact(terpakai)} / ${Money.compact(batas)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { rasio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = warna,
            trackColor = warna.copy(alpha = 0.18f)
        )
        if (terpakai <= batas) {
            Text(
                "Sisa ${Money.format(batas - terpakai)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Melebihi ${Money.format(terpakai - batas)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Merah
            )
        }
    }
}

// ------------------------------------------------------------ Layar kunci

@Composable
fun LayarKunci(onBuka: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = Brand, modifier = Modifier.size(48.dp))
            Text("Duitku Terkunci", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                Identitas.PEMBUAT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBuka, colors = ButtonDefaults.buttonColors(containerColor = Brand)) {
                Text("Buka Kunci")
            }
        }
    }
}
