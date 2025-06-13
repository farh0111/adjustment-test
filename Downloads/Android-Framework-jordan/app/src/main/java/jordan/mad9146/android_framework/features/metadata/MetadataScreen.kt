package jordan.mad9146.android_framework.features.metadata

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import java.io.File
import androidx.navigation.NavController
import com.ashampoo.kim.Kim
import com.ashampoo.kim.android.readMetadata
import com.ashampoo.kim.format.tiff.constant.ExifTag

@Composable
fun MetadataScreen(
    imageFile: File,
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val bitmap = remember(imageFile) {
        if (imageFile.exists()) {
            BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
        } else null
    }

    val scrollState = rememberScrollState()

    // For TabRow
    val tabs = listOf("EXIF", "IPTC")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // EXIF State
    var exifCaption by remember { mutableStateOf("") }
    var exifDate by remember { mutableStateOf("") }
    var exifMake by remember { mutableStateOf("") }
    var exifModel by remember { mutableStateOf("") }
    var exifOrientation by remember { mutableStateOf("") }
    var exifUserComment by remember { mutableStateOf("") }
    var exifFocalLength by remember { mutableStateOf("") }
    var exifExposureTime by remember { mutableStateOf("") }
    var exifAperture by remember { mutableStateOf("") }
    var exifIso by remember { mutableStateOf("") }
    var exifFlash by remember { mutableStateOf("") }
    var exifLatitude by remember { mutableStateOf("") }
    var exifLongitude by remember { mutableStateOf("") }
    var exifAltitude by remember { mutableStateOf("") }
    var exifGpsProcessing by remember { mutableStateOf("") }
    var exifGpsDate by remember { mutableStateOf("") }

    // IPTC State
    var iptcTitle by remember { mutableStateOf("") }
    var iptcCaption by remember { mutableStateOf("") }
    var iptcCity by remember { mutableStateOf("") }
    var iptcProvince by remember { mutableStateOf("") }
    var iptcCountry by remember { mutableStateOf("") }
    var iptcKeywords by remember { mutableStateOf("") }

    // Set the metadata fields - similar to useEffect
    LaunchedEffect(imageFile) {
        try {
            val exif = ExifInterface(imageFile)
            exifCaption = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION) ?: ""
            exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: ""
            exifMake = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
            exifModel = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
            exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION) ?: ""
            exifUserComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""
            exifFocalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: ""
            exifExposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) ?: ""
            exifAperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER) ?: ""
            exifIso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED) ?: ""
            exifFlash = exif.getAttribute(ExifInterface.TAG_FLASH) ?: ""
            val latLong = exif.latLong
            exifLatitude = latLong?.getOrNull(0)?.toString() ?: ""
            exifLongitude = latLong?.getOrNull(1)?.toString() ?: ""
            exifAltitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) ?: ""
            exifGpsProcessing = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) ?: ""
            exifGpsDate = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) ?: ""

            if (imageFile.extension.lowercase() in listOf("jpg", "jpeg")) {
                val metadata = Kim.readMetadata(imageFile)
                val iptc = metadata?.iptc
                val records = iptc?.records ?: emptyList()

                // for testing purposes
//                records.forEach {
//                    println(it.iptcType)
//                    println(it.value)
//                }

                iptcTitle = records.find { it.iptcType.fieldName == "Object Name" }?.value ?: ""
                iptcCaption = records.find { it.iptcType.fieldName == "Caption/Abstract" }?.value ?: ""
                iptcCity = records.find { it.iptcType.fieldName == "City" }?.value ?: ""
                iptcProvince = records.find { it.iptcType.fieldName == "Province/State" }?.value ?: ""
                iptcCountry = records.find { it.iptcType.fieldName == "Country/Primary Location Name" }?.value ?: ""
                iptcKeywords = records.filter { it.iptcType.fieldName == "Keywords" }
                    .joinToString(", ") { it.value }
            } else {
                iptcCaption = "Not a JPEG — no IPTC"
            }
        } catch (e: Exception) {
            exifCaption = "Error reading EXIF"
            iptcCaption = "Error reading IPTC"
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Metadata Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp)
            )
        } ?: Text("Failed to load image.")

        Text("Image Path: ${imageFile.path}")
        Spacer(modifier = Modifier.height(16.dp))

        // TabRow works similarly to SegmentedControl in SwiftUI
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show EXIF Tab
        if (selectedTabIndex == 0) {
            Text("EXIF Metadata", style = MaterialTheme.typography.titleMedium)

            Text("Caption: $exifCaption")
            Text("Date: $exifDate")

            Spacer(Modifier.height(16.dp))

            Text("Camera Make: $exifMake")
            Text("Camera Model: $exifModel")
            Text("Orientation: $exifOrientation")
            Text("User Comment: $exifUserComment")

            Spacer(Modifier.height(8.dp))

            Text("Focal Length: $exifFocalLength")
            Text("Exposure Time: $exifExposureTime")
            Text("Aperture: $exifAperture")
            Text("ISO: $exifIso")
            Text("Flash: $exifFlash")

            Spacer(Modifier.height(8.dp))

            Text("Latitude: $exifLatitude")
            Text("Longitude: $exifLongitude")
            Text("GPS Altitude: $exifAltitude")
            Text("GPS Processing Method: $exifGpsProcessing")
            Text("GPS Date: $exifGpsDate")
        }

        // Show IPTC Tab
        if (selectedTabIndex == 1) {
            Text("IPTC Metadata", style = MaterialTheme.typography.titleMedium)

            Text("Object Name / Title: $iptcTitle")
            Text("Caption: $iptcCaption")
            Text("City: $iptcCity")
            Text("Province: $iptcProvince")
            Text("Country: $iptcCountry")
            Text("Keywords: $iptcKeywords")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Row of buttons (Back & Edit)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }

            Button(onClick = {
                navController.navigate("metadata_edit/${Uri.encode(imageFile.absolutePath)}")
            }) {
                Text("Edit")
            }
        }
    }
}