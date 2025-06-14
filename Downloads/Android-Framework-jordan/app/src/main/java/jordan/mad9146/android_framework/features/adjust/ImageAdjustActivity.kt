package jordan.mad9146.android_framework.features.adjust

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jordan.mad9146.android_framework.databinding.ActivityImageAdjustBinding
import java.io.File
import java.io.FileOutputStream

class ImageAdjustActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageAdjustBinding

    // filters
    private lateinit var brightnessFilter: GPUImageBrightnessFilter
    private lateinit var saturationFilter: GPUImageSaturationFilter
    private lateinit var filterGroup: GPUImageFilterGroup

    // for re-export we still keep the original file URI
    private lateinit var imageFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageAdjustBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) grab the URI
        val uri: Uri? = intent.getParcelableExtra("croppedImageUri")
        if (uri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        imageFile = File(uri.path!!)

        // 2) decode and display in GPUImageView
        val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: run {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }

        // 3) build filters
        brightnessFilter = GPUImageBrightnessFilter(0f)
        saturationFilter = GPUImageSaturationFilter(1f)
        filterGroup = GPUImageFilterGroup(listOf(brightnessFilter, saturationFilter))

        // 4) set on GPUImageView
        binding.gpuImageView.apply {
            setImage(bmp)
            setFilter(filterGroup)
            requestRender()
        }

        // 5) brightness slider
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                brightnessFilter.setBrightness((p - 100) / 100f)
                binding.gpuImageView.apply {
                    setFilter(filterGroup)
                    requestRender()
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar) = Unit
            override fun onStopTrackingTouch(sb: SeekBar) = Unit
        })

        // 6) saturation slider
        binding.saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                saturationFilter.setSaturation(p / 100f)
                binding.gpuImageView.apply {
                    setFilter(filterGroup)
                    requestRender()
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar) = Unit
            override fun onStopTrackingTouch(sb: SeekBar) = Unit
        })

        // 7) apply & save
        binding.applyButton.setOnClickListener {
            // capture from the GL view itself
            val finalBmp = (binding.gpuImageView as GPUImageView).capture()
            FileOutputStream(imageFile).use { fos ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
            setResult(Activity.RESULT_OK, Intent().putExtra("adjustedImageUri", uri))
            finish()
        }
    }
}
