@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TransaksiListScreen(vm: DuitkuViewModel, padding: PaddingValues) {
    val semua by vm.transaksis.collectAsState()
    val dompets by vm.dompets.collectAsState()
    val kategoris by vm.kategoris.collectAsState()

    var cari by rememberSaveable { mutableStateOf("") }
    var filterIdx by rememberSaveable { mutableIntStateOf(0) }
    val bulan = vm.bulanTransaksi

    val filter: JenisTransaksi? = when (filterIdx) {
        1 -> JenisTransaksi.PENGELUARAN
        2 -> JenisTransaksi.PEMASUKAN
        3 -> JenisTransaksi.TRANSFER
        else -> null
    }
    val q = cari.trim().lowercase()
    val katMap = remember(kategoris) { kategoris.associateBy { it.id } }
    val dompetMap = remember(dompets) { dompets.associateBy { it.id } }

    val bulanItems = remember(semua, bulan) { semua.filter { Waktu.bulanDari(it.tanggal) == bulan } }

    // Saat mencari, pencarian berlaku untuk semua bulan.
    val tersaring = remember(semua, bulan, q, filterIdx, katMap, dompetMap) {
        semua.filter { t ->
            if (filter != null && t.jenis != filter) {
                false
            } else if (q.isEmpty()) {
                Waktu.bulanDari(t.tanggal) == bulan
            } else {
                t.catatan.lowercase().contains(q) ||
                    (katMap.ambil(t.kategoriId)?.nama?.lowercase()?.contains(q) ?: false) ||
                    (dompetMap.ambil(t.dompetId)?.nama?.lowercase()?.contains(q) ?: false)
            }
        }
    }

    val grup = remember(tersaring) {
        tersaring
            .groupBy { Waktu.tanggalDari(it.tanggal) }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cari,
                    onValueChange = { cari = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari catatan, kategori, atau dompet") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (cari.isNotEmpty()) {
                            IconButton(onClick = { cari = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Pengeluaran", "Pemasukan", "Transfer").forEachIndexed { i, teks ->
                        ChipPilih(teks, filterIdx == i) { filterIdx = i }
                    }
                }
            }
        }

        if (cari.isEmpty()) {
            item {
                Kartu {
                    NavigatorBulan(bulan) { vm.bulanTransaksi = it }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Pemasukan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Money.format(bulanItems.totalPemasukan()), fontWeight = FontWeight.SemiBold, color = Hijau)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pengeluaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Money.format(bulanItems.totalPengeluaran()), fontWeight = FontWeight.SemiBold, color = Merah)
                        }
                    }
                }
            }
        }

        if (tersaring.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (cari.isEmpty()) {
                        Text("Belum ada transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tidak ada transaksi di ${Waktu.judulBulan(bulan)}.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("Tidak ada hasil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tidak ada transaksi yang cocok dengan \"$cari\".",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        grup.forEach { pasangan ->
            val hari = pasangan.first
            val daftar = pasangan.second.sortedByDescending { it.tanggal }
            item(key = hari.toString()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Text(
                            Waktu.judulHari(hari),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            Money.format(daftar.totalPemasukan() - daftar.totalPengeluaran()),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Kartu(padding = 12.dp) {
                        daftar.forEachIndexed { i, t ->
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
        }
    }
}
