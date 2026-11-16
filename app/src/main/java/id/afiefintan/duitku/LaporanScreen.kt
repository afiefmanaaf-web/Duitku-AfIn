@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.max

private class TotalKategori(
    val nama: String,
    val ikon: String,
    val warnaHex: String,
    val total: Long
)

private class TrenBulan(
    val label: String,
    val pemasukan: Long,
    val pengeluaran: Long
)

@Composable
fun LaporanScreen(vm: DuitkuViewModel, padding: PaddingValues) {
    val semua by vm.transaksis.collectAsState()
    val kategoris by vm.kategoris.collectAsState()
    val bulan = vm.bulanLaporan
    var jenisIdx by rememberSaveable { mutableIntStateOf(0) }
    val jenis = if (jenisIdx == 0) JenisTransaksi.PENGELUARAN else JenisTransaksi.PEMASUKAN

    val totals = remember(semua, kategoris, bulan, jenisIdx) {
        semua
            .filter { it.jenis == jenis && Waktu.bulanDari(it.tanggal) == bulan }
            .groupBy { it.kategoriId }
            .map { entri ->
                val k = kategoris.firstOrNull { it.id == entri.key }
                TotalKategori(
                    nama = k?.nama ?: "Tanpa Kategori",
                    ikon = k?.ikon ?: "tanya",
                    warnaHex = k?.warnaHex ?: "#8395A7",
                    total = entri.value.sumOf { it.jumlah }
                )
            }
            .sortedByDescending { it.total }
    }
    val total = totals.sumOf { it.total }

    val tren = remember(semua, bulan) {
        (5 downTo 0).map { offset ->
            val m = bulan.minusMonths(offset.toLong())
            val items = semua.filter { Waktu.bulanDari(it.tanggal) == m }
            TrenBulan(Waktu.bulanSingkat(m), items.totalPemasukan(), items.totalPengeluaran())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Kartu { NavigatorBulan(bulan) { vm.bulanLaporan = it } }

        Segmen(listOf("Pengeluaran", "Pemasukan"), jenisIdx, { jenisIdx = it })

        if (totals.isEmpty()) {
            Kartu {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.PieChart, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Belum ada data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Belum ada ${jenis.judul.lowercase()} di ${Waktu.judulBulan(bulan)}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ------------------------------------------------ Donat
            Kartu {
                Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(220.dp)) {
                        val tebal = size.minDimension * 0.18f
                        val diameter = size.minDimension - tebal
                        val kiriAtas = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val celah = if (totals.size > 1) 2f else 0f
                        var mulai = -90f
                        totals.forEach { item ->
                            val sapuan = 360f * item.total.toFloat() / total.toFloat()
                            drawArc(
                                color = warnaDari(item.warnaHex),
                                startAngle = mulai + celah / 2f,
                                sweepAngle = (sapuan - celah).coerceAtLeast(0.5f),
                                useCenter = false,
                                topLeft = kiriAtas,
                                size = Size(diameter, diameter),
                                style = Stroke(width = tebal)
                            )
                            mulai += sapuan
                        }
                    }
                    Column(
                        modifier = Modifier.widthIn(max = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (jenis == JenisTransaksi.PENGELUARAN) "Total Pengeluaran" else "Total Pemasukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            Money.format(total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ------------------------------------------------ Rincian
            Kartu {
                Text("Rincian per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    totals.forEach { item ->
                        val persen = if (total > 0) item.total.toFloat() / total.toFloat() else 0f
                        val w = warnaDari(item.warnaHex)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                IconBadge(item.ikon, item.warnaHex, 32.dp)
                                Text(item.nama, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(Money.format(item.total), fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${Math.round(persen * 100)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { persen.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = w,
                                trackColor = w.copy(alpha = 0.18f)
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------- Tren 6 bulan
        Kartu {
            Text("Tren 6 Bulan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            GrafikTren(tren)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Legenda("Pemasukan", Hijau)
                Legenda("Pengeluaran", Merah)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Legenda(teks: String, warna: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(warna))
        Text(teks, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GrafikTren(tren: List<TrenBulan>) {
    val maks = max(1L, tren.maxOf { max(it.pemasukan, it.pengeluaran) })
    val garis = MaterialTheme.colorScheme.outlineVariant
    val lebarSumbu = 48.dp

    Column {
        Row(Modifier.fillMaxWidth().height(170.dp)) {
            Column(
                modifier = Modifier.width(lebarSumbu).fillMaxHeight().padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf(3, 2, 1, 0).forEach { k ->
                    Text(
                        Money.compact(maks * k / 3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val h = size.height
                val w = size.width
                for (k in 0..3) {
                    val y = h - h * k / 3f
                    drawLine(garis, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                val slot = w / tren.size
                val lebar = slot * 0.28f
                tren.forEachIndexed { i, d ->
                    val tengah = slot * i + slot / 2f
                    val tinggiMasuk = h * d.pemasukan.toFloat() / maks.toFloat()
                    val tinggiKeluar = h * d.pengeluaran.toFloat() / maks.toFloat()
                    drawRect(Hijau, Offset(tengah - lebar - 1f, h - tinggiMasuk), Size(lebar, tinggiMasuk))
                    drawRect(Merah, Offset(tengah + 1f, h - tinggiKeluar), Size(lebar, tinggiKeluar))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(lebarSumbu))
            tren.forEach { d ->
                Text(
                    d.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
