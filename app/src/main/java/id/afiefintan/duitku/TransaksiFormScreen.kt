@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Form tambah / ubah transaksi. Dirancang agar bisa dicatat dalam beberapa ketukan:
 * jumlah (keyboard langsung terbuka) -> kategori -> simpan.
 */
@Composable
fun TransaksiFormScreen(vm: DuitkuViewModel, id: Long?) {
    val dompets by vm.dompets.collectAsState()
    val kategoris by vm.kategoris.collectAsState()
    val lama = remember { id?.let { i -> vm.transaksis.value.firstOrNull { it.id == i } } }

    var jenis by remember { mutableStateOf(lama?.jenis ?: JenisTransaksi.PENGELUARAN) }
    var jumlah by remember { mutableLongStateOf(lama?.jumlah ?: 0L) }
    var tanggal by remember { mutableLongStateOf(lama?.tanggal ?: System.currentTimeMillis()) }
    var catatan by remember { mutableStateOf(lama?.catatan ?: "") }
    var kategoriId by remember { mutableStateOf(lama?.kategoriId) }
    var dompetId by remember { mutableStateOf(lama?.dompetId) }
    var tujuanId by remember { mutableStateOf(lama?.dompetTujuanId) }
    var konfirmasiHapus by remember { mutableStateOf(false) }
    var tampilTanggal by remember { mutableStateOf(false) }
    val fokus = remember { FocusRequester() }

    // Nilai awal yang masuk akal (pengganti siapkanDefault pada versi iOS).
    LaunchedEffect(dompets, kategoris) {
        if (dompetId == null) dompetId = dompets.firstOrNull()?.id
        if (jenis == JenisTransaksi.TRANSFER) {
            if (tujuanId == null || tujuanId == dompetId) {
                tujuanId = dompets.firstOrNull { it.id != dompetId }?.id
            }
        } else if (kategoriId == null) {
            kategoriId = kategoris.firstOrNull { it.jenis == jenis }?.id
        }
    }

    // Keyboard angka langsung terbuka untuk transaksi baru.
    LaunchedEffect(Unit) {
        if (lama == null) {
            delay(350)
            try {
                fokus.requestFocus()
            } catch (e: Exception) {
                // Abaikan: fokus hanyalah kenyamanan.
            }
        }
    }

    fun ubahJenis(baru: JenisTransaksi) {
        jenis = baru
        if (baru == JenisTransaksi.TRANSFER) {
            kategoriId = null
            if (tujuanId == null || tujuanId == dompetId) {
                tujuanId = dompets.firstOrNull { it.id != dompetId }?.id
            }
        } else if (kategoris.firstOrNull { it.id == kategoriId }?.jenis != baru) {
            kategoriId = kategoris.firstOrNull { it.jenis == baru }?.id
        }
    }

    val bisaSimpan = jumlah > 0 && dompetId != null &&
        (if (jenis == JenisTransaksi.TRANSFER) {
            tujuanId != null && tujuanId != dompetId
        } else {
            kategoriId != null
        })

    fun simpan() {
        val d = dompetId ?: return
        if (!bisaSimpan) return
        val transfer = jenis == JenisTransaksi.TRANSFER
        vm.simpanTransaksi(
            lama = lama,
            jumlah = jumlah,
            jenis = jenis,
            tanggal = tanggal,
            catatan = catatan.trim(),
            kategoriId = if (transfer) null else kategoriId,
            dompetId = d,
            tujuanId = if (transfer) tujuanId else null
        )
        vm.kembali()
    }

    Halaman(
        judul = if (lama == null) "Transaksi Baru" else "Ubah Transaksi",
        modifier = Modifier.imePadding(),
        onKembali = { vm.kembali() },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = { simpan() },
                        enabled = bisaSimpan,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand)
                    ) {
                        Text(
                            if (lama == null) "Simpan" else "Simpan Perubahan",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
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
            Segmen(
                pilihan = JenisTransaksi.entries.map { it.judul },
                terpilih = JenisTransaksi.entries.indexOf(jenis),
                onPilih = { ubahJenis(JenisTransaksi.entries[it]) }
            )

            // ------------------------------------------------------ Jumlah
            Kartu {
                Label("Jumlah")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Rp",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    RupiahField(
                        nilai = jumlah,
                        onNilai = { jumlah = it },
                        modifier = Modifier.weight(1f),
                        warna = warnaJenis(jenis),
                        fokus = fokus
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10_000L, 50_000L, 100_000L, 500_000L).forEach { n ->
                        ChipPilih("+${Money.compact(n)}", false) {
                            jumlah = minOf(jumlah + n, Money.BATAS)
                        }
                    }
                    if (jumlah > 0) {
                        ChipPilih("Hapus", false) { jumlah = 0L }
                    }
                }
            }

            if (jenis == JenisTransaksi.TRANSFER) {
                // -------------------------------------------------- Transfer
                Kartu {
                    Label("Dari dompet")
                    Spacer(Modifier.height(10.dp))
                    ChipDompet(dompets, dompetId, onPilih = { dompetId = it.id })
                    Spacer(Modifier.height(14.dp))
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Label("Ke dompet")
                    Spacer(Modifier.height(10.dp))
                    ChipDompet(dompets, tujuanId, onPilih = { tujuanId = it.id }, kecualiId = dompetId)
                }
            } else {
                // -------------------------------------------------- Kategori
                val items = kategoris.filter { it.jenis == jenis }
                Kartu {
                    Label("Kategori")
                    Spacer(Modifier.height(12.dp))
                    if (items.isEmpty()) {
                        Text(
                            "Belum ada kategori. Tambahkan di Pengaturan → Kategori.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            items.chunked(4).forEach { baris ->
                                Row(Modifier.fillMaxWidth()) {
                                    baris.forEach { k ->
                                        val dipilih = k.id == kategoriId
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { kategoriId = k.id },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .then(
                                                        if (dipilih) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                        else Modifier
                                                    )
                                                    .padding(3.dp)
                                            ) {
                                                IconBadge(k.ikon, k.warnaHex, 46.dp)
                                            }
                                            Text(
                                                k.nama,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (dipilih) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    repeat(4 - baris.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------- Dompet
                Kartu {
                    Label(if (jenis == JenisTransaksi.PEMASUKAN) "Masuk ke dompet" else "Bayar dari dompet")
                    Spacer(Modifier.height(10.dp))
                    ChipDompet(dompets, dompetId, onPilih = { dompetId = it.id })
                }
            }

            // ------------------------------------------------------ Tanggal & catatan
            Kartu {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tampilTanggal = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Text("Tanggal", modifier = Modifier.weight(1f))
                    Text(Waktu.judulPendek(tanggal), fontWeight = FontWeight.SemiBold, color = Brand)
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Catatan (opsional)") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    maxLines = 3
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
                    Text("Hapus Transaksi")
                }
            }
        }
    }

    if (tampilTanggal) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = Waktu.utcTengahMalam(Waktu.tanggalDari(tanggal))
        )
        DatePickerDialog(
            onDismissRequest = { tampilTanggal = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        tanggal = Waktu.gabung(Waktu.dariUtcTengahMalam(ms), tanggal)
                    }
                    tampilTanggal = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { tampilTanggal = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus transaksi ini?") },
            confirmButton = {
                TextButton(onClick = {
                    lama?.let { vm.hapusTransaksi(it) }
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
