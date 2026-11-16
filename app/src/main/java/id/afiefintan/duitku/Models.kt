package id.afiefintan.duitku

import androidx.room.Entity
import androidx.room.PrimaryKey

// Nilai uang disimpan sebagai Long (Rupiah utuh) agar bebas galat pembulatan
// dan aman untuk angka di atas 2 miliar.

enum class JenisTransaksi(val judul: String) {
    PENGELUARAN("Pengeluaran"),
    PEMASUKAN("Pemasukan"),
    TRANSFER("Transfer");

    companion object {
        fun dari(raw: String): JenisTransaksi =
            entries.firstOrNull { it.name == raw } ?: PENGELUARAN
    }
}

@Entity(tableName = "dompet")
data class Dompet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val ikon: String,
    val warnaHex: String,
    val saldoAwal: Long = 0,
    val urutan: Int = 0,
    val dibuat: Long = System.currentTimeMillis()
)

@Entity(tableName = "kategori")
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val ikon: String,
    val warnaHex: String,
    val jenisRaw: String,
    val urutan: Int = 0
)

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jumlah: Long,
    val jenisRaw: String,
    /** Epoch milidetik. */
    val tanggal: Long,
    val catatan: String = "",
    val dibuat: Long = System.currentTimeMillis(),
    val kategoriId: Long? = null,
    val dompetId: Long? = null,
    /** Hanya dipakai untuk jenis TRANSFER. */
    val dompetTujuanId: Long? = null
)

@Entity(tableName = "anggaran")
data class Anggaran(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batas: Long,
    val dibuat: Long = System.currentTimeMillis(),
    val kategoriId: Long
)

val Kategori.jenis: JenisTransaksi get() = JenisTransaksi.dari(jenisRaw)
val Transaksi.jenis: JenisTransaksi get() = JenisTransaksi.dari(jenisRaw)
