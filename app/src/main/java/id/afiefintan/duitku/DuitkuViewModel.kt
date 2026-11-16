package id.afiefintan.duitku

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

/** Layar yang ditumpuk di atas tab utama (pengganti NavigationStack / sheet pada iOS). */
sealed interface Layar {
    data object Pengaturan : Layar
    data object DaftarDompet : Layar
    data object DaftarKategori : Layar
    data class FormTransaksi(val id: Long? = null) : Layar
    data class FormAnggaran(val id: Long? = null) : Layar
    data class FormDompet(val id: Long? = null) : Layar
    data class FormKategori(val id: Long? = null, val jenis: JenisTransaksi = JenisTransaksi.PENGELUARAN) : Layar
}

class DuitkuViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DuitkuDatabase.get(app)
    private val dao = db.dao()
    private val prefs = app.getSharedPreferences("duitku", Context.MODE_PRIVATE)

    // ------------------------------------------------------------ Data

    val dompets: StateFlow<List<Dompet>> =
        dao.semuaDompet().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val kategoris: StateFlow<List<Kategori>> =
        dao.semuaKategori().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val transaksis: StateFlow<List<Transaksi>> =
        dao.semuaTransaksi().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val anggarans: StateFlow<List<Anggaran>> =
        dao.semuaAnggaran().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ------------------------------------------------------------ Pengaturan

    var hideBalance by mutableStateOf(prefs.getBoolean(K_SEMBUNYI, false))
        private set
    var appearance by mutableIntStateOf(prefs.getInt(K_TEMA, 0))
        private set
    var lockEnabled by mutableStateOf(prefs.getBoolean(K_KUNCI, false))
        private set

    var isLocked by mutableStateOf(prefs.getBoolean(K_KUNCI, false))
    var sedangMemeriksa = false

    fun aturSembunyi(nilai: Boolean) {
        hideBalance = nilai
        prefs.edit().putBoolean(K_SEMBUNYI, nilai).apply()
    }

    fun aturTema(nilai: Int) {
        appearance = nilai
        prefs.edit().putInt(K_TEMA, nilai).apply()
    }

    fun aturKunci(aktif: Boolean) {
        lockEnabled = aktif
        if (!aktif) isLocked = false
        prefs.edit().putBoolean(K_KUNCI, aktif).apply()
    }

    // ------------------------------------------------------------ Navigasi

    var tab by mutableIntStateOf(0)
    val tumpuk = mutableStateListOf<Layar>()

    var bulanTransaksi by mutableStateOf(YearMonth.now())
    var bulanAnggaran by mutableStateOf(YearMonth.now())
    var bulanLaporan by mutableStateOf(YearMonth.now())

    fun buka(layar: Layar) {
        tumpuk.add(layar)
    }

    fun kembali() {
        if (tumpuk.isNotEmpty()) tumpuk.removeAt(tumpuk.lastIndex)
    }

    // ------------------------------------------------------------ Aksi data

    init {
        viewModelScope.launch { seedJikaPerlu() }
    }

    private suspend fun seedJikaPerlu() {
        if (prefs.getBoolean(K_SEED, false)) return
        if (dao.jumlahDompet() == 0) dao.tambahDompetBanyak(Seeder.dompet())
        if (dao.jumlahKategori() == 0) dao.tambahKategoriBanyak(Seeder.kategori())
        prefs.edit().putBoolean(K_SEED, true).apply()
    }

    fun simpanTransaksi(
        lama: Transaksi?,
        jumlah: Long,
        jenis: JenisTransaksi,
        tanggal: Long,
        catatan: String,
        kategoriId: Long?,
        dompetId: Long,
        tujuanId: Long?
    ) {
        viewModelScope.launch {
            if (lama == null) {
                dao.tambahTransaksi(
                    Transaksi(
                        jumlah = jumlah, jenisRaw = jenis.name, tanggal = tanggal, catatan = catatan,
                        kategoriId = kategoriId, dompetId = dompetId, dompetTujuanId = tujuanId
                    )
                )
            } else {
                dao.ubahTransaksi(
                    lama.copy(
                        jumlah = jumlah, jenisRaw = jenis.name, tanggal = tanggal, catatan = catatan,
                        kategoriId = kategoriId, dompetId = dompetId, dompetTujuanId = tujuanId
                    )
                )
            }
        }
    }

    fun hapusTransaksi(t: Transaksi) {
        viewModelScope.launch { dao.hapusTransaksi(t) }
    }

    fun simpanAnggaran(lama: Anggaran?, kategoriId: Long, batas: Long) {
        viewModelScope.launch {
            if (lama == null) {
                dao.tambahAnggaran(Anggaran(batas = batas, kategoriId = kategoriId))
            } else {
                dao.ubahAnggaran(lama.copy(batas = batas))
            }
        }
    }

    fun hapusAnggaran(a: Anggaran) {
        viewModelScope.launch { dao.hapusAnggaran(a) }
    }

    fun simpanDompet(lama: Dompet?, nama: String, ikon: String, warnaHex: String, saldoAwal: Long) {
        viewModelScope.launch {
            if (lama == null) {
                dao.tambahDompet(
                    Dompet(nama = nama, ikon = ikon, warnaHex = warnaHex, saldoAwal = saldoAwal, urutan = dompets.value.size)
                )
            } else {
                dao.ubahDompet(lama.copy(nama = nama, ikon = ikon, warnaHex = warnaHex, saldoAwal = saldoAwal))
            }
        }
    }

    fun hapusDompet(d: Dompet) {
        viewModelScope.launch { dao.hapusDompet(d) }
    }

    fun simpanKategori(lama: Kategori?, nama: String, ikon: String, warnaHex: String, jenis: JenisTransaksi) {
        viewModelScope.launch {
            if (lama == null) {
                dao.tambahKategori(
                    Kategori(
                        nama = nama, ikon = ikon, warnaHex = warnaHex,
                        jenisRaw = jenis.name, urutan = kategoris.value.size + 200
                    )
                )
            } else {
                dao.ubahKategori(lama.copy(nama = nama, ikon = ikon, warnaHex = warnaHex))
            }
        }
    }

    /** Transaksi menjadi "Tanpa Kategori" dan anggarannya ikut dihapus. */
    fun hapusKategori(k: Kategori) {
        viewModelScope.launch {
            db.withTransaction {
                dao.lepasKategoriDariTransaksi(k.id)
                dao.hapusAnggaranKategori(k.id)
                dao.hapusKategori(k)
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            db.withTransaction {
                dao.kosongkanTransaksi()
                dao.kosongkanAnggaran()
                dao.kosongkanKategori()
                dao.kosongkanDompet()
            }
            prefs.edit().putBoolean(K_SEED, false).apply()
            seedJikaPerlu()
        }
    }

    private companion object {
        const val K_SEED = "didSeedV1"
        const val K_KUNCI = "appLockEnabled"
        const val K_TEMA = "appearance"
        const val K_SEMBUNYI = "hideBalance"
    }
}
