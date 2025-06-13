package jordan.mad9146.android_framework.features.adjust

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jordan.mad9146.android_framework.databinding.ActivityImageAdjustBinding
import java.io.File
import java.io.FileOutputStream

class ImageAdjustActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageAdjustBinding
    private lateinit var gpuImage: GPUImage
    private lateinit var brightnessFilter: GPUImageBrightnessFilter
    private lateinit var saturationFilter: GPUImageSaturationFilter
    private lateinit var filterGroup: GPUImageFilterGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageAdjustBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the incoming URI and file
        val croppedUri: Uri? = intent.getParcelableExtra("croppedImageUri")
        if (croppedUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        val imageFile = File(croppedUri.path!!)
        // Decode into a Bitmap synchronously
        val originalBitmap: Bitmap? = BitmapFactory.decodeFile(imageFile.absolutePath)
        if (originalBitmap == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        // Initialize GPUImage with the decoded Bitmap
        gpuImage = GPUImage(this).apply {
            setImage(originalBitmap)
        }

        // Prepare filters
        brightnessFilter = GPUImageBrightnessFilter(0.0f)
        saturationFilter = GPUImageSaturationFilter(1.0f)
        filterGroup = GPUImageFilterGroup(listOf(brightnessFilter, saturationFilter))

        // Display the original unfiltered image
        binding.gpuImageView.setImage(originalBitmap)

        // Brightness SeekBar (0–200 → –1.0…+1.0)
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                brightnessFilter.setBrightness((progress - 100) / 100.0f)
                applyFilters()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Saturation SeekBar (0–200 → 0…2.0)
        binding.saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                saturationFilter.setSaturation(progress / 100.0f)
                applyFilters()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Apply & Save button
        binding.applyButton.setOnClickListener {
            gpuImage.setFilter(filterGroup)
            val outBmp: Bitmap = gpuImage.bitmapWithFilterApplied
            FileOutputStream(imageFile).use { fos ->
                outBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("adjustedImageUri", croppedUri)
            })
            finish()
        }
    }

    // Re-render the view with updated filters
    private fun applyFilters() {
        gpuImage.setFilter(filterGroup)
        val filtered: Bitmap = gpuImage.bitmapWithFilterApplied
        binding.gpuImageView.setImage(filtered)
    }
}
