package id.afiefintan.duitku

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity

// FragmentActivity (turunan ComponentActivity) dibutuhkan oleh BiometricPrompt.
class MainActivity : FragmentActivity() {

    private val vm: DuitkuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val gelap = when (vm.appearance) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(gelap) {
                val gaya = if (gelap) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = gaya, navigationBarStyle = gaya)
            }

            // Sembunyikan isi di daftar aplikasi terbaru (dan blokir tangkapan layar) saat kunci aktif.
            LaunchedEffect(vm.lockEnabled) {
                if (vm.lockEnabled) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            DuitkuTheme(gelap = gelap) {
                DuitkuRoot(vm = vm, onBuka = { mintaBuka() })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Dikunci saat aplikasi ke latar. Abaikan jika layar kunci sistem sedang tampil.
        if (vm.lockEnabled && !vm.sedangMemeriksa) vm.isLocked = true
    }

    override fun onResume() {
        super.onResume()
        mintaBuka()
    }

    private fun mintaBuka() {
        if (!vm.lockEnabled || !vm.isLocked || vm.sedangMemeriksa) return

        // Perangkat tanpa kunci layar sama sekali: jangan kunci pengguna di luar.
        if (!Kunci.bisa(this)) {
            vm.isLocked = false
            return
        }

        vm.sedangMemeriksa = true
        Kunci.minta(this) { berhasil ->
            vm.sedangMemeriksa = false
            if (berhasil) vm.isLocked = false
        }
    }
}
