package jordan.mad9146.android_framework

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.io.File
import jordan.mad9146.android_framework.features.crop.CropActivity
import jordan.mad9146.android_framework.features.adjust.ImageAdjustActivity
import jordan.mad9146.android_framework.features.metadata.MetadataScreen
import jordan.mad9146.android_framework.features.metadata.MetadataEditScreen
import jordan.mad9146.android_framework.features.shareFeature
import jordan.mad9146.android_framework.ui.theme.AndroidFrameworkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Copy bundled assets on first run
        val prefs = getSharedPreferences("initPrefs", MODE_PRIVATE)
        if (prefs.getBoolean("isFirstRun", true)) {
            copyAssetToInternalStorage("pinky1.JPG")
            copyAssetToInternalStorage("pinky4.JPG")
            prefs.edit().putBoolean("isFirstRun", false).apply()
        }

        setContent {
            AndroidFrameworkTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "framework") {
                    composable("framework") { FrameworkConcept(navController) }
                    composable("metadata/{path}") { back ->
                        back.arguments?.getString("path")?.let { path ->
                            MetadataScreen(File(path), navController) { navController.popBackStack() }
                        }
                    }
                    composable("metadata_edit/{path}") { back ->
                        back.arguments?.getString("path")?.let { path ->
                            MetadataEditScreen(File(path)) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FrameworkConcept(navController: androidx.navigation.NavController) {
    val context = LocalContext.current

    var imageFiles by remember { mutableStateOf(listSavedImages(context.filesDir)) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var forceReload by remember { mutableStateOf(0) }

    // 1) Use OpenDocument to pick images from any provider → no overload issues
    val photoPicker = rememberLauncherForActivityResult(OpenDocument()) { uri: Uri? ->
        uri?.let {
            // Persist permission
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            saveImageToInternalStorage(context.contentResolver, it, context.filesDir)
            imageFiles = listSavedImages(context.filesDir)
            getFileName(context.contentResolver, it)?.let { name ->
                selectedImageUri = Uri.fromFile(File(context.filesDir, name))
                forceReload++
            }
        }
    }

    // 2) Custom gallery Activity
    val galleryLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        result.data
            ?.getStringExtra("selectedImagePath")
            ?.let { path ->
                val f = File(path)
                selectedImageUri = Uri.fromFile(f)
                imageFiles = listOf(f) + imageFiles.filterNot { it.absolutePath == path }
                forceReload++
            }
    }

    // 3) Crop only returns croppedImageUri
    val cropLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getParcelableExtra<Uri>("croppedImageUri")
                ?.let { croppedUri ->
                    selectedImageUri = croppedUri
                    forceReload++
                }
        }
    }

    // 4) Adjust only returns adjustedImageUri
    val adjustLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getParcelableExtra<Uri>("adjustedImageUri")
                ?.let { adjustedUri ->
                    selectedImageUri = adjustedUri
                    forceReload++
                }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text(
            text = "MetaPi Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                // Limit to images
                photoPicker.launch(arrayOf("image/*"))
            }) {
                Text("Add Image")
            }

            Spacer(Modifier.width(12.dp))

            Button(onClick = {
                galleryLauncher.launch(Intent(context, SavedImagesActivity::class.java))
            }) {
                Text("View Gallery")
            }
        }

        Spacer(Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            val displayUri = remember(forceReload) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(uri.path!!)
                ).buildUpon()
                    .appendQueryParameter("t", System.currentTimeMillis().toString())
                    .build()
            }

            Image(
                painter = rememberAsyncImagePainter(displayUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    Intent(context, CropActivity::class.java).also {
                        it.putExtra("imageUri", uri)
                        it.putExtra("originalFilePath", File(uri.path!!).absolutePath)
                        cropLauncher.launch(it)
                    }
                }) {
                    Text("Crop")
                }

                Button(onClick = {
                    Intent(context, ImageAdjustActivity::class.java).also {
                        it.putExtra("croppedImageUri", uri)
                        adjustLauncher.launch(it)
                    }
                }) {
                    Text("Adjust")
                }

                Button(onClick = {
                    navController.navigate("metadata/${Uri.encode(uri.path!!)}")
                }) {
                    Text("Metadata")
                }

                Button(onClick = {
                    shareFeature(context, File(uri.path!!))
                }) {
                    Text("Share")
                }
            }
        }
    }
}
