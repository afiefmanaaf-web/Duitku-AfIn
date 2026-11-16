@file:OptIn(ExperimentalMaterial3Api::class)

package id.afiefintan.duitku

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput

private class ItemTab(val judul: String, val ikon: ImageVector)

private val daftarTab = listOf(
    ItemTab("Beranda", Icons.Filled.Home),
    ItemTab("Transaksi", Icons.Filled.Receipt),
    ItemTab("Anggaran", Icons.Filled.PieChart),
    ItemTab("Laporan", Icons.Filled.BarChart)
)

@Composable
fun DuitkuRoot(vm: DuitkuViewModel, onBuka: () -> Unit) {
    val layar = vm.tumpuk.lastOrNull()

    BackHandler(enabled = vm.tumpuk.isNotEmpty()) { vm.kembali() }

    Box(Modifier.fillMaxSize()) {
        TabsScreen(vm)

        if (layar != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {}
            ) {
                when (layar) {
                    is Layar.Pengaturan -> SettingsScreen(vm)
                    is Layar.DaftarDompet -> DompetScreen(vm)
                    is Layar.DaftarKategori -> KategoriScreen(vm)
                    is Layar.FormTransaksi -> TransaksiFormScreen(vm, layar.id)
                    is Layar.FormAnggaran -> AnggaranFormScreen(vm, layar.id)
                    is Layar.FormDompet -> DompetFormScreen(vm, layar.id)
                    is Layar.FormKategori -> KategoriFormScreen(vm, layar.id, layar.jenis)
                }
            }
        }

        // Juga menutup isi aplikasi saat kunci aktif.
        if (vm.lockEnabled && vm.isLocked) {
            LayarKunci(onBuka = onBuka)
        }
    }
}

@Composable
private fun TabsScreen(vm: DuitkuViewModel) {
    val tab = vm.tab

    Halaman(
        judul = if (tab == 0) Identitas.NAMA_APLIKASI else daftarTab[tab].judul,
        subjudul = if (tab == 0) Identitas.PEMBUAT else null,
        aksi = {
            if (tab == 0) {
                IconButton(onClick = { vm.buka(Layar.Pengaturan) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Pengaturan")
                }
            }
            if (tab == 2) {
                IconButton(onClick = { vm.buka(Layar.FormAnggaran()) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah anggaran")
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                daftarTab.forEachIndexed { i, item ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { vm.tab = i },
                        icon = { Icon(item.ikon, contentDescription = null) },
                        label = { Text(item.judul) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Brand,
                            selectedTextColor = Brand,
                            indicatorColor = Brand.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        fab = {
            if (tab <= 1) {
                FloatingActionButton(
                    onClick = { vm.buka(Layar.FormTransaksi()) },
                    containerColor = Brand,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah transaksi")
                }
            }
        }
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(vm, padding)
            1 -> TransaksiListScreen(vm, padding)
            2 -> AnggaranScreen(vm, padding)
            else -> LaporanScreen(vm, padding)
        }
    }
}
