package id.afiefintan.duitku

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DuitkuDao {
    // ---- Baca ----
    @Query("SELECT * FROM dompet ORDER BY urutan, id")
    fun semuaDompet(): Flow<List<Dompet>>

    @Query("SELECT * FROM kategori ORDER BY urutan, id")
    fun semuaKategori(): Flow<List<Kategori>>

    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC, id DESC")
    fun semuaTransaksi(): Flow<List<Transaksi>>

    @Query("SELECT * FROM anggaran ORDER BY dibuat, id")
    fun semuaAnggaran(): Flow<List<Anggaran>>

    @Query("SELECT COUNT(*) FROM dompet")
    suspend fun jumlahDompet(): Int

    @Query("SELECT COUNT(*) FROM kategori")
    suspend fun jumlahKategori(): Int

    // ---- Dompet ----
    @Insert suspend fun tambahDompet(d: Dompet): Long
    @Insert suspend fun tambahDompetBanyak(d: List<Dompet>)
    @Update suspend fun ubahDompet(d: Dompet)
    @Delete suspend fun hapusDompet(d: Dompet)

    // ---- Kategori ----
    @Insert suspend fun tambahKategori(k: Kategori): Long
    @Insert suspend fun tambahKategoriBanyak(k: List<Kategori>)
    @Update suspend fun ubahKategori(k: Kategori)
    @Delete suspend fun hapusKategori(k: Kategori)

    @Query("UPDATE transaksi SET kategoriId = NULL WHERE kategoriId = :id")
    suspend fun lepasKategoriDariTransaksi(id: Long)

    @Query("DELETE FROM anggaran WHERE kategoriId = :id")
    suspend fun hapusAnggaranKategori(id: Long)

    // ---- Transaksi ----
    @Insert suspend fun tambahTransaksi(t: Transaksi): Long
    @Update suspend fun ubahTransaksi(t: Transaksi)
    @Delete suspend fun hapusTransaksi(t: Transaksi)

    // ---- Anggaran ----
    @Insert suspend fun tambahAnggaran(a: Anggaran): Long
    @Update suspend fun ubahAnggaran(a: Anggaran)
    @Delete suspend fun hapusAnggaran(a: Anggaran)

    // ---- Reset ----
    @Query("DELETE FROM transaksi") suspend fun kosongkanTransaksi()
    @Query("DELETE FROM anggaran") suspend fun kosongkanAnggaran()
    @Query("DELETE FROM kategori") suspend fun kosongkanKategori()
    @Query("DELETE FROM dompet") suspend fun kosongkanDompet()
}

@Database(
    entities = [Dompet::class, Kategori::class, Transaksi::class, Anggaran::class],
    version = 1,
    exportSchema = false
)
abstract class DuitkuDatabase : RoomDatabase() {
    abstract fun dao(): DuitkuDao

    companion object {
        @Volatile
        private var instance: DuitkuDatabase? = null

        fun get(context: Context): DuitkuDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DuitkuDatabase::class.java,
                    "duitku.db"
                ).build().also { instance = it }
            }
    }
}
