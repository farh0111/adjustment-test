package jordan.mad9146.android_framework

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(contentResolver: ContentResolver, uri: Uri, directory: File) {
    val fileName = getFileName(contentResolver, uri) ?: "image_${System.currentTimeMillis()}.jpg"
    val file = File(directory, fileName)
    contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val returnCursor = contentResolver.query(uri, null, null, null, null)
    returnCursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return null
}

fun listSavedImages(directory: File): List<File> {
    return directory.listFiles { file ->
        file.extension.equals("jpg", ignoreCase = true)
    }?.toList() ?: emptyList()
}

fun ComponentActivity.copyAssetToInternalStorage(fileName: String) {
    val file = File(filesDir, fileName)
    if (!file.exists()) {
        assets.open(fileName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}