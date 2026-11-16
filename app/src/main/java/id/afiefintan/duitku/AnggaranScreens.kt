@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import java.time.YearMonth

@Composable
fun AnggaranScreen(vm: DuitkuViewModel, padding: PaddingValues) {
    val anggaran by vm.anggarans.collectAsState()
    val semua by vm.transaksis.collectAsState()
    val kategoris by vm.kategoris.collectAsState()
    val bulan = vm.bulanAnggaran
    val katMap = remember(kategoris) { kategoris.associateBy { it.id } }

    if (anggaran.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.PieChart, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text("Belum ada anggaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Tetapkan batas pengeluaran bulanan per kategori agar uangmu lebih terkendali.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.buka(Layar.FormAnggaran()) }, colors = ButtonDefaults.buttonColors(containerColor = Brand)) {
                Text("Buat Anggaran")
            }
        }
        return
    }

    val data = remember(anggaran, semua, bulan) {
        anggaran
            .map { a -> Pair(a, Hitung.terpakai(a.kategoriId, semua, bulan)) }
            .sortedByDescending { p -> if (p.first.batas > 0) p.second.toDouble() / p.first.batas else 0.0 }
    }
    val totalBatas = anggaran.sumOf { it.batas }
    val totalTerpakai = data.sumOf { it.second }
    val sisa = totalBatas - totalTerpakai
    val sisaHari = if (bulan == YearMonth.now()) Waktu.sisaHariBulanIni() else 0
    val rasioTotal = if (totalBatas > 0) (totalTerpakai.toFloat() / totalBatas.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Kartu {
            NavigatorBulan(bulan) { vm.bulanAnggaran = it }
            Spacer(Modifier.height(8.dp))
            Label("Sisa Anggaran")
            Spacer(Modifier.height(6.dp))
            Text(
                Money.format(sisa),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = if (sisa < 0) Merah else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { rasioTotal },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Brand,
                trackColor = Brand.copy(alpha = 0.18f)
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Terpakai ${Money.format(totalTerpakai)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Total ${Money.format(totalBatas)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (sisaHari > 0 && sisa > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Maks. ${Money.format(sisa / sisaHari)} per hari • $sisaHari hari lagi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Per Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Kartu {
            data.forEachIndexed { i, p ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                BarisAnggaran(
                    kategori = katMap.ambil(p.first.kategoriId),
                    batas = p.first.batas,
                    terpakai = p.second,
                    onClick = { vm.buka(Layar.FormAnggaran(p.first.id)) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ------------------------------------------------------------ Form anggaran

@Composable
fun AnggaranFormScreen(vm: DuitkuViewModel, id: Long?) {
    val kategoris by vm.kategoris.collectAsState()
    val semuaAnggaran by vm.anggarans.collectAsState()
    val lama = remember { id?.let { i -> vm.anggarans.value.firstOrNull { it.id == i } } }

    var kategoriId by remember { mutableStateOf<Long?>(lama?.kategoriId) }
    var batas by remember { mutableLongStateOf(lama?.batas ?: 0L) }
    var konfirmasiHapus by remember { mutableStateOf(false) }

    val dipakai = semuaAnggaran.map { it.kategoriId }.toSet()
    val tersedia = kategoris.filter { it.jenis == JenisTransaksi.PENGELUARAN && it.id !in dipakai }
    val kategoriLama = kategoris.firstOrNull { it.id == lama?.kategoriId }

    LaunchedEffect(tersedia) {
        if (lama == null && kategoriId == null) kategoriId = tersedia.firstOrNull()?.id
    }

    val bisaSimpan = batas > 0 && kategoriId != null

    fun simpan() {
        val k = kategoriId ?: return
        if (batas <= 0) return
        vm.simpanAnggaran(lama, k, batas)
        vm.kembali()
    }

    Halaman(
        judul = if (lama == null) "Anggaran Baru" else "Ubah Anggaran",
        modifier = Modifier.imePadding(),
        onKembali = { vm.kembali() },
        aksi = {
            TextButton(onClick = { simpan() }, enabled = bisaSimpan) { Text("Simpan") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Kartu {
                Label("Kategori")
                Spacer(Modifier.height(10.dp))
                if (lama != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconBadge(kategoriLama?.ikon ?: "label", kategoriLama?.warnaHex ?: "#8395A7", 34.dp)
                        Text(kategoriLama?.nama ?: "-", fontWeight = FontWeight.Medium)
                    }
                } else if (tersedia.isEmpty()) {
                    Text(
                        "Semua kategori pengeluaran sudah punya anggaran.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    tersedia.forEach { k ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { kategoriId = k.id }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(selected = kategoriId == k.id, onClick = null)
                            IconBadge(k.ikon, k.warnaHex, 32.dp)
                            Text(k.nama)
                        }
                    }
                }
            }

            Kartu {
                Label("Batas per bulan")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rp", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    RupiahField(nilai = batas, onNilai = { batas = it }, modifier = Modifier.weight(1f), ukuran = 28.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Anggaran ini berlaku setiap bulan dan dihitung dari pengeluaran pada kategori tersebut.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (lama != null) {
                OutlinedButton(
                    onClick = { konfirmasiHapus = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Merah)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus Anggaran")
                }
            }
        }
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus anggaran ini?") },
            confirmButton = {
                TextButton(onClick = {
                    lama?.let { vm.hapusAnggaran(it) }
                    konfirmasiHapus = false
                    vm.kembali()
                }) { Text("Hapus", color = Merah) }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") }
            }
        )
    }
}
