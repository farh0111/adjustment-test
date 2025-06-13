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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import jordan.mad9146.android_framework.features.adjust.ImageAdjustActivity
import jordan.mad9146.android_framework.features.crop.CropActivity
import jordan.mad9146.android_framework.features.metadata.MetadataEditScreen
import jordan.mad9146.android_framework.features.metadata.MetadataScreen
import jordan.mad9146.android_framework.features.shareFeature
import jordan.mad9146.android_framework.ui.theme.AndroidFrameworkTheme
import java.io.File

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
                NavHost(navController = navController, startDestination = "framework") {
                    composable("framework") { FrameworkConcept(navController) }

                    composable("metadata/{path}") { backStackEntry ->
                        backStackEntry.arguments?.getString("path")?.let { path ->
                            MetadataScreen(
                                imageFile = File(path),
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("metadata_edit/{path}") { backStackEntry ->
                        backStackEntry.arguments?.getString("path")?.let { path ->
                            MetadataEditScreen(File(path)) {
                                navController.popBackStack()
                            }
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

    // --- State ---
    var imageFiles by remember { mutableStateOf(listSavedImages(context.filesDir)) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var forceReload by remember { mutableStateOf(0) }

    // --- Adjust launcher ---
    val adjustLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getParcelableExtra<Uri>("adjustedImageUri")
                ?.let { uri ->
                    Log.d("AdjustResult", "Adjusted URI: $uri")
                    selectedImageUri = uri
                    forceReload++
                }
        }
    }

    // --- Crop launcher chains into Adjust ---
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getParcelableExtra<Uri>("croppedImageUri")
                ?.let { croppedUri ->
                    Log.d("CropResult", "Cropped URI: $croppedUri")
                    Intent(context, ImageAdjustActivity::class.java)
                        .putExtra("croppedImageUri", croppedUri)
                        .also { adjustLauncher.launch(it) }
                }
        }
    }

    // --- Gallery launcher ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data
            ?.getStringExtra("selectedImagePath")
            ?.let { path ->
                val file = File(path)
                selectedImageUri = Uri.fromFile(file)
                imageFiles = listOf(file) + imageFiles.filterNot { it.absolutePath == path }
                forceReload++
            }
    }

    // --- Photo picker launcher ---
    val photoPickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let {
            saveImageToInternalStorage(context.contentResolver, it, context.filesDir)
            imageFiles = listSavedImages(context.filesDir)
            getFileName(context.contentResolver, it)?.let { name ->
                selectedImageUri = Uri.fromFile(File(context.filesDir, name))
                forceReload++
            }
        }
    }

    // --- UI ---
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
                photoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
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

        selectedImageUri?.let { baseUri ->
            val displayedUri = remember(forceReload) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(baseUri.path!!)
                ).buildUpon()
                    .appendQueryParameter("t", System.currentTimeMillis().toString())
                    .build()
            }

            Image(
                painter = rememberAsyncImagePainter(displayedUri),
                contentDescription = "Selected Image",
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
                // Crop
                Button(onClick = {
                    selectedImageUri?.let { uri ->
                        Intent(context, CropActivity::class.java)
                            .putExtra("imageUri", uri)
                            .putExtra("originalFilePath", File(uri.path!!).absolutePath)
                            .also { cropLauncher.launch(it) }
                    }
                }) {
                    Text("Crop")
                }

                // Adjust
                Button(onClick = {
                    selectedImageUri?.let { uri ->
                        Intent(context, ImageAdjustActivity::class.java)
                            .putExtra("croppedImageUri", uri)
                            .also { adjustLauncher.launch(it) }
                    }
                }) {
                    Text("Adjust")
                }

                // Metadata
                Button(onClick = {
                    selectedImageUri?.path
                        ?.let { navController.navigate("metadata/${Uri.encode(it)}") }
                }) {
                    Text("Metadata")
                }

                // Share
                Button(onClick = {
                    selectedImageUri?.let { uri ->
                        shareFeature(context, File(uri.path!!))
                    }
                }) {
                    Text("Share")
                }
            }
        }
    }
}
