package jordan.mad9146.android_framework.features.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import jordan.mad9146.android_framework.databinding.ActivityCropBinding
import java.io.File
import java.io.FileOutputStream

class CropActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.aspectRatioButton.setOnClickListener { showAspectRatioDialog() }
        binding.rotateButton.setOnClickListener { binding.cropImageView.rotateImage(90) }
        binding.flipButton.setOnClickListener { binding.cropImageView.flipImageHorizontally() }

        val imageUri: Uri? = intent.getParcelableExtra("imageUri")
        val originalPath: String? = intent.getStringExtra("originalFilePath")
        binding.cropImageView.setImageUriAsync(imageUri)

        binding.cropButton.setOnClickListener {
            val bmp: Bitmap? = binding.cropImageView.getCroppedImage()
            if (bmp == null || originalPath == null) {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return@setOnClickListener
            }
            val file = File(originalPath)
            FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 100, out) }

            // return the same file URI you just overwrote
            Intent().putExtra("croppedImageUri", Uri.fromFile(file)).also {
                setResult(Activity.RESULT_OK, it)
            }
            finish()
        }
    }

    private fun showAspectRatioDialog() {
        val opts = arrayOf("16:9", "4:3", "1:1", "Freeform")
        AlertDialog.Builder(this)
            .setTitle("Choose Aspect Ratio")
            .setItems(opts) { _, i ->
                binding.cropImageView.apply {
                    when (i) {
                        0 -> { setFixedAspectRatio(true); setAspectRatio(16, 9) }
                        1 -> { setFixedAspectRatio(true); setAspectRatio(4, 3) }
                        2 -> { setFixedAspectRatio(true); setAspectRatio(1, 1) }
                        3 -> setFixedAspectRatio(false)
                    }
                }
            }
            .show()
    }
}
