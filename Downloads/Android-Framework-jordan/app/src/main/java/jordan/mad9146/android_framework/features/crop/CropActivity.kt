package jordan.mad9146.android_framework.features.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jordan.mad9146.android_framework.databinding.ActivityCropBinding
import java.io.File
import java.io.FileOutputStream
import android.app.AlertDialog

class CropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.aspectRatioButton.setOnClickListener {
            showAspectRatioDialog()
        }

        val imageUri: Uri? = intent.getParcelableExtra("imageUri")
        val originalFilePath: String? = intent.getStringExtra("originalFilePath")

        // Load the image into the CropImageView
        binding.cropImageView.setImageUriAsync(imageUri)



        // Handle crop button
        binding.cropButton.setOnClickListener {
            val cropped: Bitmap? = binding.cropImageView.getCroppedImage()
            cropped?.let {
                val file = originalFilePath?.let { path -> File(path) }
                if (file == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return@setOnClickListener
                }

                val outputStream = FileOutputStream(file)
                it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                val resultIntent = Intent().apply {
                    putExtra("croppedImageUri", Uri.fromFile(file))
                }
                setResult(Activity.RESULT_OK, resultIntent)
                Thread.sleep(100)
                finish()
            }
        }
    }

    private fun showAspectRatioDialog() {
        val options = arrayOf("16:9", "4:3", "1:1", "Freeform")

        AlertDialog.Builder(this)
            .setTitle("Choose Aspect Ratio")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // 16:9
                        binding.cropImageView.setFixedAspectRatio(true)
                        binding.cropImageView.setAspectRatio(16, 9)
                    }
                    1 -> { // 4:3
                        binding.cropImageView.setFixedAspectRatio(true)
                        binding.cropImageView.setAspectRatio(4, 3)
                    }
                    2 -> { // 1:1
                        binding.cropImageView.setFixedAspectRatio(true)
                        binding.cropImageView.setAspectRatio(1, 1)
                    }
                    3 -> { // Freeform
                        binding.cropImageView.setFixedAspectRatio(false)
                    }
                }
            }
            .show()
    }
}