package android.marc.com.fastfoodcalorieestimationandroid.activity.error

import android.marc.com.fastfoodcalorieestimationandroid.activity.main.MainActivity
import android.marc.com.fastfoodcalorieestimationandroid.databinding.ActivityErrorBinding
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class ErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorMessage = intent.getStringExtra(MainActivity.ERROR_MESSAGE_KEY) ?: ""
        binding.errorMessageTv.text = errorMessage

        hideActionBar()

        Handler(Looper.myLooper()!!).postDelayed({
            onBackPressedDispatcher.onBackPressed()
        }, 1500)
    }

    private fun hideActionBar() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}