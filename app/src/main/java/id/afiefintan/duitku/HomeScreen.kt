@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

@Composable
fun HomeScreen(vm: DuitkuViewModel, padding: PaddingValues) {
    val semua by vm.transaksis.collectAsState()
    val dompets by vm.dompets.collectAsState()
    val kategoris by vm.kategoris.collectAsState()
    val anggaran by vm.anggarans.collectAsState()
    val sembunyi = vm.hideBalance

    val bulanIni = remember(semua) {
        val ym = YearMonth.now()
        semua.filter { Waktu.bulanDari(it.tanggal) == ym }
    }
    val totalSaldo = dompets.sumOf { Hitung.saldo(it, semua) }
    val katMap = remember(kategoris) { kategoris.associateBy { it.id } }
    val dompetMap = remember(dompets) { dompets.associateBy { it.id } }
    val terbaru = semua.take(5)
    val anggaranTeratas = remember(anggaran, semua) {
        val ym = YearMonth.now()
        anggaran
            .map { a -> Pair(a, Hitung.terpakai(a.kategoriId, semua, ym)) }
            .sortedByDescending { p -> if (p.first.batas > 0) p.second.toDouble() / p.first.batas else 0.0 }
            .take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ---------------------------------------------------------- Kartu saldo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Brand, BrandGelap)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Total Saldo",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                IconButton(onClick = { vm.aturSembunyi(!sembunyi) }) {
                    Icon(
                        imageVector = if (sembunyi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (sembunyi) "Tampilkan saldo" else "Sembunyikan saldo",
                        tint = Color.White
                    )
                }
            }
            Text(
                if (sembunyi) "Rp ••••••" else Money.format(totalSaldo),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RingkasanBulan("Pemasukan", bulanIni.totalPemasukan(), Icons.Filled.ArrowDownward, sembunyi)
                RingkasanBulan("Pengeluaran", bulanIni.totalPengeluaran(), Icons.Filled.ArrowUpward, sembunyi)
            }
        }

        // ---------------------------------------------------------- Dompet
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dompet", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { vm.buka(Layar.DaftarDompet) }) { Text("Kelola") }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dompets.forEach { d ->
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconBadge(d.ikon, d.warnaHex, 36.dp)
                            Text(
                                d.nama,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (sembunyi) "••••" else Money.format(Hitung.saldo(d, semua)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------- Anggaran
        if (anggaran.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Anggaran Bulan Ini", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { vm.tab = 2 }) { Text("Lihat") }
                }
                Kartu {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        anggaranTeratas.forEach { p ->
                            BarisAnggaran(katMap.ambil(p.first.kategoriId), p.first.batas, p.second)
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------- Transaksi terbaru
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Transaksi Terbaru", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (semua.isNotEmpty()) {
                    TextButton(onClick = { vm.tab = 1 }) { Text("Semua") }
                }
            }
            if (semua.isEmpty()) {
                Kartu {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Belum ada transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Ketuk tombol + untuk mencatat pemasukan atau pengeluaran pertamamu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Kartu {
                    terbaru.forEachIndexed { i, t ->
                        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        BarisTransaksi(
                            t = t,
                            kategori = katMap.ambil(t.kategoriId),
                            dompet = dompetMap.ambil(t.dompetId),
                            tujuan = dompetMap.ambil(t.dompetTujuanId),
                            onClick = { vm.buka(Layar.FormTransaksi(t.id)) }
                        )
                    }
                }
            }
        }

        Text(
            "${Identitas.NAMA_APLIKASI} · dibuat oleh ${Identitas.PEMBUAT}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun RingkasanBulan(judul: String, nilai: Long, ikon: ImageVector, sembunyi: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(ikon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Column {
            Text(
                "$judul bulan ini",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                if (sembunyi) "••••" else Money.compact(nilai),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
