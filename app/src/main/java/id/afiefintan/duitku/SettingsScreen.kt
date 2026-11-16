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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(vm: DuitkuViewModel) {
    val context = LocalContext.current
    val semua by vm.transaksis.collectAsState()
    var konfirmasiReset by remember { mutableStateOf(false) }
    var galat by remember { mutableStateOf<String?>(null) }

    fun ekspor() {
        try {
            bagikanCsv(context, CsvExporter.bangun(semua, vm.dompets.value, vm.kategoris.value))
        } catch (e: Exception) {
            galat = "Gagal membuat file CSV: ${e.localizedMessage}"
        }
    }

    Halaman(judul = "Pengaturan", onKembali = { vm.kembali() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Seksi("Kelola") {
                BarisPengaturan(Icons.Filled.AccountBalanceWallet, "Dompet") { vm.buka(Layar.DaftarDompet) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BarisPengaturan(Icons.Filled.Category, "Kategori") { vm.buka(Layar.DaftarKategori) }
            }

            Seksi("Tampilan") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Brightness6, contentDescription = null, tint = Brand)
                    Text("Tema", fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
                Segmen(listOf("Ikuti Sistem", "Terang", "Gelap"), vm.appearance, { vm.aturTema(it) })
            }

            Seksi(
                "Keamanan",
                footer = "Duitku meminta sidik jari, wajah, atau PIN / pola / kata sandi perangkat setiap kali dibuka."
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = Brand)
                    Text("Kunci Aplikasi", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(
                        checked = vm.lockEnabled,
                        onCheckedChange = { aktif ->
                            if (aktif && !Kunci.bisa(context)) {
                                galat = "Atur dulu kunci layar (PIN, pola, atau sidik jari) di pengaturan perangkat."
                            } else {
                                vm.aturKunci(aktif)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Brand)
                    )
                }
            }

            Seksi(
                "Data",
                footer = "Semua data tersimpan di perangkat ini. Ekspor CSV secara berkala sebagai cadangan."
            ) {
                BarisPengaturan(Icons.Filled.Share, "Ekspor ke CSV", aktif = semua.isNotEmpty()) { ekspor() }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BarisPengaturan(Icons.Filled.Delete, "Hapus Semua Data", warna = Merah) { konfirmasiReset = true }
            }

            Seksi("Tentang") {
                BarisInfo("Aplikasi", Identitas.NAMA_APLIKASI)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BarisInfo("Versi", Identitas.VERSI)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BarisInfo("Jumlah transaksi", "${semua.size}")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BarisInfo("Dibuat oleh", Identitas.PEMBUAT)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (konfirmasiReset) {
        AlertDialog(
            onDismissRequest = { konfirmasiReset = false },
            title = { Text("Hapus semua data?") },
            text = {
                Text("Semua transaksi, dompet, kategori, dan anggaran akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetData()
                    konfirmasiReset = false
                }) { Text("Hapus Semua", color = Merah) }
            },
            dismissButton = { TextButton(onClick = { konfirmasiReset = false }) { Text("Batal") } }
        )
    }

    if (galat != null) {
        AlertDialog(
            onDismissRequest = { galat = null },
            title = { Text("Terjadi kesalahan") },
            text = { Text(galat ?: "") },
            confirmButton = { TextButton(onClick = { galat = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun Seksi(judul: String, footer: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            judul.uppercase(),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Kartu(content = content)
        if (footer != null) {
            Text(
                footer,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarisPengaturan(
    ikon: ImageVector,
    teks: String,
    aktif: Boolean = true,
    warna: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val warnaTeks = if (warna == Color.Unspecified) MaterialTheme.colorScheme.onSurface else warna
    val warnaIkon = if (warna == Color.Unspecified) Brand else warna
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = aktif, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(ikon, contentDescription = null, tint = if (aktif) warnaIkon else warnaIkon.copy(alpha = 0.4f))
        Text(
            teks,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            color = if (aktif) warnaTeks else warnaTeks.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun BarisInfo(label: String, nilai: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(nilai, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}
