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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================ Kelola dompet

@Composable
fun DompetScreen(vm: DuitkuViewModel) {
    val dompets by vm.dompets.collectAsState()
    val semua by vm.transaksis.collectAsState()

    Halaman(
        judul = "Dompet",
        onKembali = { vm.kembali() },
        aksi = {
            IconButton(onClick = { vm.buka(Layar.FormDompet()) }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah dompet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Kartu {
                dompets.forEachIndexed { i, d ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    val saldo = Hitung.saldo(d, semua)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.buka(Layar.FormDompet(d.id)) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBadge(d.ikon, d.warnaHex)
                        Column(Modifier.weight(1f)) {
                            Text(d.nama, fontWeight = FontWeight.Medium)
                            Text(
                                "${Hitung.jumlahTransaksi(d, semua)} transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            Money.format(saldo),
                            fontWeight = FontWeight.SemiBold,
                            color = if (saldo < 0) Merah else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Text(
                "Saldo dihitung otomatis dari saldo awal dan semua transaksi. " +
                    "Dompet yang masih punya transaksi tidak bisa dihapus agar data tetap akurat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DompetFormScreen(vm: DuitkuViewModel, id: Long?) {
    val dompets by vm.dompets.collectAsState()
    val semua by vm.transaksis.collectAsState()
    val lama = remember { id?.let { i -> vm.dompets.value.firstOrNull { it.id == i } } }

    var nama by remember { mutableStateOf(lama?.nama ?: "") }
    var ikon by remember { mutableStateOf(lama?.ikon ?: "dompet") }
    var warnaHex by remember { mutableStateOf(lama?.warnaHex ?: Palette.warna[6]) }
    var saldoAwal by remember { mutableLongStateOf(lama?.saldoAwal ?: 0L) }
    var konfirmasiHapus by remember { mutableStateOf(false) }

    val bisaSimpan = nama.trim().isNotEmpty()

    // Aturan hapus sama seperti versi iOS.
    val alasanTidakBisa: String? = when {
        lama == null -> null
        dompets.size <= 1 -> "Minimal harus ada satu dompet."
        Hitung.jumlahTransaksi(lama, semua) > 0 ->
            "Dompet ini masih punya ${Hitung.jumlahTransaksi(lama, semua)} transaksi. Hapus atau ubah transaksinya terlebih dahulu."
        else -> null
    }

    fun simpan() {
        val n = nama.trim()
        if (n.isEmpty()) return
        vm.simpanDompet(lama, n, ikon, warnaHex, saldoAwal)
        vm.kembali()
    }

    Halaman(
        judul = if (lama == null) "Dompet Baru" else "Ubah Dompet",
        modifier = Modifier.imePadding(),
        onKembali = { vm.kembali() },
        aksi = { TextButton(onClick = { simpan() }, enabled = bisaSimpan) { Text("Simpan") } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconBadge(ikon, warnaHex, 64.dp)
            }
            Kartu {
                Label("Nama")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("mis. Tunai, BCA, GoPay") },
                    singleLine = true
                )
            }
            Kartu {
                Label("Saldo awal")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rp", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    RupiahField(nilai = saldoAwal, onNilai = { saldoAwal = it }, modifier = Modifier.weight(1f), ukuran = 28.sp)
                }
            }
            Kartu {
                Label("Ikon")
                Spacer(Modifier.height(10.dp))
                PemilihIkon(ikon, warnaHex) { ikon = it }
            }
            Kartu {
                Label("Warna")
                Spacer(Modifier.height(10.dp))
                PemilihWarna(warnaHex) { warnaHex = it }
            }
            if (lama != null) {
                OutlinedButton(
                    onClick = { konfirmasiHapus = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Merah)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus Dompet")
                }
            }
        }
    }

    if (konfirmasiHapus) {
        if (alasanTidakBisa != null) {
            AlertDialog(
                onDismissRequest = { konfirmasiHapus = false },
                title = { Text("Tidak bisa dihapus") },
                text = { Text(alasanTidakBisa) },
                confirmButton = { TextButton(onClick = { konfirmasiHapus = false }) { Text("OK") } }
            )
        } else {
            AlertDialog(
                onDismissRequest = { konfirmasiHapus = false },
                title = { Text("Hapus dompet ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        lama?.let { vm.hapusDompet(it) }
                        konfirmasiHapus = false
                        vm.kembali()
                    }) { Text("Hapus", color = Merah) }
                },
                dismissButton = { TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") } }
            )
        }
    }
}

// ============================================================ Kelola kategori

@Composable
fun KategoriScreen(vm: DuitkuViewModel) {
    val kategoris by vm.kategoris.collectAsState()
    val semua by vm.transaksis.collectAsState()
    var jenisIdx by remember { mutableIntStateOf(0) }
    val jenis = if (jenisIdx == 0) JenisTransaksi.PENGELUARAN else JenisTransaksi.PEMASUKAN
    val items = kategoris.filter { it.jenis == jenis }

    Halaman(
        judul = "Kategori",
        onKembali = { vm.kembali() },
        aksi = {
            IconButton(onClick = { vm.buka(Layar.FormKategori(null, jenis)) }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah kategori")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Segmen(listOf("Pengeluaran", "Pemasukan"), jenisIdx, { jenisIdx = it })
            Kartu {
                items.forEachIndexed { i, k ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.buka(Layar.FormKategori(k.id, k.jenis)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBadge(k.ikon, k.warnaHex, 36.dp)
                        Text(k.nama, modifier = Modifier.weight(1f))
                        Text(
                            "${semua.count { it.kategoriId == k.id }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun KategoriFormScreen(vm: DuitkuViewModel, id: Long?, jenisAwal: JenisTransaksi) {
    val semua by vm.transaksis.collectAsState()
    val lama = remember { id?.let { i -> vm.kategoris.value.firstOrNull { it.id == i } } }

    var nama by remember { mutableStateOf(lama?.nama ?: "") }
    var ikon by remember { mutableStateOf(lama?.ikon ?: "label") }
    var warnaHex by remember { mutableStateOf(lama?.warnaHex ?: Palette.warna[3]) }
    var jenis by remember { mutableStateOf(lama?.jenis ?: jenisAwal) }
    var konfirmasiHapus by remember { mutableStateOf(false) }

    val bisaSimpan = nama.trim().isNotEmpty()

    fun simpan() {
        val n = nama.trim()
        if (n.isEmpty()) return
        vm.simpanKategori(lama, n, ikon, warnaHex, jenis)
        vm.kembali()
    }

    Halaman(
        judul = if (lama == null) "Kategori Baru" else "Ubah Kategori",
        modifier = Modifier.imePadding(),
        onKembali = { vm.kembali() },
        aksi = { TextButton(onClick = { simpan() }, enabled = bisaSimpan) { Text("Simpan") } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconBadge(ikon, warnaHex, 64.dp)
            }
            if (lama == null) {
                Segmen(
                    listOf("Pengeluaran", "Pemasukan"),
                    if (jenis == JenisTransaksi.PEMASUKAN) 1 else 0,
                    { jenis = if (it == 1) JenisTransaksi.PEMASUKAN else JenisTransaksi.PENGELUARAN }
                )
            }
            Kartu {
                Label("Nama")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("mis. Makan Siang") },
                    singleLine = true
                )
            }
            Kartu {
                Label("Ikon")
                Spacer(Modifier.height(10.dp))
                PemilihIkon(ikon, warnaHex) { ikon = it }
            }
            Kartu {
                Label("Warna")
                Spacer(Modifier.height(10.dp))
                PemilihWarna(warnaHex) { warnaHex = it }
            }
            if (lama != null) {
                OutlinedButton(
                    onClick = { konfirmasiHapus = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Merah)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus Kategori")
                }
            }
        }
    }

    if (konfirmasiHapus && lama != null) {
        val n = semua.count { it.kategoriId == lama.id }
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus kategori?") },
            text = {
                Text("Kategori \"${lama.nama}\" akan dihapus bersama anggarannya. $n transaksi akan menjadi \"Tanpa Kategori\".")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.hapusKategori(lama)
                    konfirmasiHapus = false
                    vm.kembali()
                }) { Text("Hapus", color = Merah) }
            },
            dismissButton = { TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") } }
        )
    }
}
