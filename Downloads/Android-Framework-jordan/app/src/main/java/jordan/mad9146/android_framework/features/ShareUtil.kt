package jordan.mad9146.android_framework.features

import android.content.Context
import android.widget.Toast
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun shareFeature(context: Context, imageFile: File) {
    if (!imageFile.exists()) {
        Toast.makeText(context, "Image File Not Found", Toast.LENGTH_SHORT).show()
        return
    }

    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpg"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Share Image Via")
    context.startActivity(chooser)
}