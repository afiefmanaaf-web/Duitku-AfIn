package id.afiefintan.duitku

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs

// ---------------------------------------------------------------- Rupiah

object Money {
    /** Batas input: 12 digit (di bawah 1 triliun). */
    const val BATAS = 999_999_999_999L

    /** 1250000 -> "1.250.000" */
    fun grouped(value: Long, emptyIfZero: Boolean = false): String {
        if (emptyIfZero && value == 0L) return ""
        val s = abs(value).toString()
        val sb = StringBuilder()
        for (i in s.indices) {
            if (i > 0 && (s.length - i) % 3 == 0) sb.append('.')
            sb.append(s[i])
        }
        return (if (value < 0) "-" else "") + sb.toString()
    }

    /** 1250000 -> "Rp 1.250.000" */
    fun format(value: Long): String =
        (if (value < 0) "-Rp " else "Rp ") + grouped(abs(value))

    /** 1250000 -> "1,25 jt" (untuk ruang sempit dan sumbu grafik) */
    fun compact(value: Long): String {
        val a = abs(value)
        val tanda = if (value < 0) "-" else ""
        if (a >= 1_000_000_000L) return tanda + ringkas(a / 1_000_000_000.0) + " M"
        if (a >= 1_000_000L) return tanda + ringkas(a / 1_000_000.0) + " jt"
        if (a >= 1_000L) return tanda + ringkas(a / 1_000.0) + " rb"
        return value.toString()
    }

    private fun ringkas(d: Double): String {
        val s = String.format(Locale.US, "%.1f", d).replace('.', ',')
        return if (s.endsWith(",0")) s.dropLast(2) else s
    }
}

// ---------------------------------------------------------------- Tanggal

object Waktu {
    private val namaBulan = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    private val singkatBulan = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )
    private val namaHari = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    private val zona: ZoneId get() = ZoneId.systemDefault()

    fun tanggalDari(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()

    fun bulanDari(millis: Long): YearMonth = YearMonth.from(tanggalDari(millis))

    fun judulBulan(ym: YearMonth): String = "${namaBulan[ym.monthValue - 1]} ${ym.year}"

    fun bulanSingkat(ym: YearMonth): String = singkatBulan[ym.monthValue - 1]

    /** "16 Nov 2026" */
    fun judulPendek(millis: Long): String {
        val d = tanggalDari(millis)
        return "${d.dayOfMonth} ${singkatBulan[d.monthValue - 1]} ${d.year}"
    }

    /** "Hari ini", "Kemarin", atau "Senin, 16 Nov 2026". */
    fun judulHari(date: LocalDate): String {
        val hariIni = LocalDate.now(zona)
        if (date == hariIni) return "Hari ini"
        if (date == hariIni.minusDays(1)) return "Kemarin"
        return "${namaHari[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${singkatBulan[date.monthValue - 1]} ${date.year}"
    }

    fun iso(millis: Long): String = tanggalDari(millis).toString()

    /** Gabungkan tanggal terpilih dengan jam dari transaksi lama, agar urutan dalam sehari tetap. */
    fun gabung(tanggal: LocalDate, jamDariMillis: Long): Long {
        val jam = Instant.ofEpochMilli(jamDariMillis).atZone(zona).toLocalTime()
        return LocalDateTime.of(tanggal, jam).atZone(zona).toInstant().toEpochMilli()
    }

    /** DatePicker Material bekerja dalam UTC tengah malam. */
    fun utcTengahMalam(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun dariUtcTengahMalam(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun sisaHariBulanIni(): Int {
        val h = LocalDate.now(zona)
        return h.lengthOfMonth() - h.dayOfMonth + 1
    }
}

// ---------------------------------------------------------------- Palet & ikon

object Palette {
    val warna = listOf(
        "#FF6B6B", "#FF9F43", "#FECA57", "#1DD1A1", "#10AC84", "#48DBFB",
        "#54A0FF", "#5F27CD", "#C56CF0", "#FF6FB5", "#8395A7", "#576574"
    )

    /** Kunci ikon yang bisa dipilih pengguna (disimpan di database). */
    val daftarIkon = listOf(
        "makan", "kopi", "keranjang", "belanja", "pakaian", "mobil", "bensin", "pesawat",
        "rumah", "listrik", "wifi", "dokumen", "game", "film", "musik", "kesehatan",
        "hati", "hewan", "buku", "sekolah", "uang", "kartu", "bank", "dompet",
        "ponsel", "kerja", "hadiah", "investasi", "koin", "bintang", "label", "lainnya"
    )

    private val peta: Map<String, ImageVector> by lazy {
        mapOf(
            "makan" to Icons.Filled.Restaurant,
            "kopi" to Icons.Filled.LocalCafe,
            "keranjang" to Icons.Filled.ShoppingCart,
            "belanja" to Icons.Filled.ShoppingBag,
            "pakaian" to Icons.Filled.Checkroom,
            "mobil" to Icons.Filled.DirectionsCar,
            "bensin" to Icons.Filled.LocalGasStation,
            "pesawat" to Icons.Filled.Flight,
            "rumah" to Icons.Filled.Home,
            "listrik" to Icons.Filled.Bolt,
            "wifi" to Icons.Filled.Wifi,
            "dokumen" to Icons.Filled.Description,
            "game" to Icons.Filled.SportsEsports,
            "film" to Icons.Filled.Movie,
            "musik" to Icons.Filled.MusicNote,
            "kesehatan" to Icons.Filled.MedicalServices,
            "hati" to Icons.Filled.Favorite,
            "hewan" to Icons.Filled.Pets,
            "buku" to Icons.Filled.MenuBook,
            "sekolah" to Icons.Filled.School,
            "uang" to Icons.Filled.Payments,
            "kartu" to Icons.Filled.CreditCard,
            "bank" to Icons.Filled.AccountBalance,
            "dompet" to Icons.Filled.AccountBalanceWallet,
            "ponsel" to Icons.Filled.PhoneAndroid,
            "kerja" to Icons.Filled.Work,
            "hadiah" to Icons.Filled.CardGiftcard,
            "investasi" to Icons.Filled.TrendingUp,
            "koin" to Icons.Filled.MonetizationOn,
            "bintang" to Icons.Filled.Star,
            "label" to Icons.Filled.LocalOffer,
            "lainnya" to Icons.Filled.MoreHoriz,
            "transfer" to Icons.Filled.SwapHoriz,
            "tanya" to Icons.Filled.Info
        )
    }

    fun ikonDari(kunci: String): ImageVector = peta[kunci] ?: Icons.Filled.LocalOffer
}

// ---------------------------------------------------------------- Perhitungan

fun <V> Map<Long, V>.ambil(id: Long?): V? = if (id == null) null else this[id]

fun List<Transaksi>.totalPemasukan(): Long =
    filter { it.jenis == JenisTransaksi.PEMASUKAN }.sumOf { it.jumlah }

fun List<Transaksi>.totalPengeluaran(): Long =
    filter { it.jenis == JenisTransaksi.PENGELUARAN }.sumOf { it.jumlah }

object Hitung {
    /** Saldo selalu dihitung dari transaksi, jadi tidak pernah "melenceng". */
    fun saldo(d: Dompet, transaksi: List<Transaksi>): Long {
        var total = d.saldoAwal
        for (t in transaksi) {
            if (t.dompetId == d.id) {
                when (t.jenis) {
                    JenisTransaksi.PEMASUKAN -> total += t.jumlah
                    JenisTransaksi.PENGELUARAN -> total -= t.jumlah
                    JenisTransaksi.TRANSFER -> total -= t.jumlah
                }
            }
            if (t.dompetTujuanId == d.id) total += t.jumlah
        }
        return total
    }

    fun jumlahTransaksi(d: Dompet, transaksi: List<Transaksi>): Int =
        transaksi.count { it.dompetId == d.id } + transaksi.count { it.dompetTujuanId == d.id }

    /** Total pengeluaran satu kategori pada bulan tertentu. */
    fun terpakai(kategoriId: Long?, transaksi: List<Transaksi>, bulan: YearMonth): Long {
        if (kategoriId == null) return 0L
        return transaksi
            .filter {
                it.jenis == JenisTransaksi.PENGELUARAN &&
                    it.kategoriId == kategoriId &&
                    Waktu.bulanDari(it.tanggal) == bulan
            }
            .sumOf { it.jumlah }
    }
}

// ---------------------------------------------------------------- Data bawaan

object Seeder {
    fun dompet(): List<Dompet> = listOf(
        Dompet(nama = "Tunai", ikon = "uang", warnaHex = "#10AC84", urutan = 0),
        Dompet(nama = "Rekening Bank", ikon = "bank", warnaHex = "#54A0FF", urutan = 1),
        Dompet(nama = "E-Wallet", ikon = "ponsel", warnaHex = "#C56CF0", urutan = 2)
    )

    fun kategori(): List<Kategori> {
        val keluar = listOf(
            Triple("Makan & Minum", "makan", "#FF9F43"),
            Triple("Transportasi", "mobil", "#54A0FF"),
            Triple("Belanja", "belanja", "#FF6FB5"),
            Triple("Tagihan", "dokumen", "#5F27CD"),
            Triple("Hiburan", "game", "#C56CF0"),
            Triple("Kesehatan", "kesehatan", "#FF6B6B"),
            Triple("Pendidikan", "buku", "#48DBFB"),
            Triple("Rumah", "rumah", "#1DD1A1"),
            Triple("Lainnya", "lainnya", "#8395A7")
        )
        val masuk = listOf(
            Triple("Gaji", "uang", "#10AC84"),
            Triple("Bonus", "hadiah", "#FECA57"),
            Triple("Investasi", "investasi", "#1DD1A1"),
            Triple("Lainnya", "lainnya", "#8395A7")
        )
        val hasil = mutableListOf<Kategori>()
        keluar.forEachIndexed { i, k ->
            hasil.add(Kategori(nama = k.first, ikon = k.second, warnaHex = k.third, jenisRaw = JenisTransaksi.PENGELUARAN.name, urutan = i))
        }
        masuk.forEachIndexed { i, k ->
            hasil.add(Kategori(nama = k.first, ikon = k.second, warnaHex = k.third, jenisRaw = JenisTransaksi.PEMASUKAN.name, urutan = 100 + i))
        }
        return hasil
    }
}

// ---------------------------------------------------------------- CSV

object CsvExporter {
    fun bangun(items: List<Transaksi>, dompets: List<Dompet>, kategoris: List<Kategori>): String {
        val namaDompet = dompets.associate { it.id to it.nama }
        val namaKategori = kategoris.associate { it.id to it.nama }
        val baris = mutableListOf("Tanggal,Jenis,Kategori,Dompet,Dompet Tujuan,Jumlah,Catatan")

        for (t in items.sortedBy { it.tanggal }) {
            val kolom = listOf(
                Waktu.iso(t.tanggal),
                t.jenis.judul,
                namaKategori.ambil(t.kategoriId) ?: "",
                namaDompet.ambil(t.dompetId) ?: "",
                namaDompet.ambil(t.dompetTujuanId) ?: "",
                t.jumlah.toString(),
                t.catatan
            )
            baris.add(kolom.joinToString(",") { escape(it) })
        }

        // BOM agar Excel membaca UTF-8 dengan benar.
        return "\uFEFF" + baris.joinToString("\r\n")
    }

    private fun escape(nilai: String): String {
        var s = nilai
        // Cegah sel diperlakukan sebagai rumus di Excel / Sheets.
        if (s.isNotEmpty() && s[0] in "=+-@") s = "'$s"
        val perluKutip = s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!perluKutip) return s
        return "\"" + s.replace("\"", "\"\"") + "\""
    }
}
