package jordan.mad9146.android_framework.features.metadata

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.android.readMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MetadataEditScreen(
    imageFile: File,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // EXIF state holders
    var caption by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var orientation by remember { mutableStateOf("") }
    var userComment by remember { mutableStateOf("") }
    var focalLength by remember { mutableStateOf("") }
    var exposureTime by remember { mutableStateOf("") }
    var aperture by remember { mutableStateOf("") }
    var iso by remember { mutableStateOf("") }
    var flash by remember { mutableStateOf("") }
    var altitude by remember { mutableStateOf("") }
    var gpsProcessing by remember { mutableStateOf("") }
    var gpsDate by remember { mutableStateOf("") }

    // IPTC state holders
    var iptcTitle by remember { mutableStateOf("") }
    var iptcCaption by remember { mutableStateOf("") }
    var iptcCity by remember { mutableStateOf("") }
    var iptcProvince by remember { mutableStateOf("") }
    var iptcCountry by remember { mutableStateOf("") }
    var iptcKeywords by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()) }

    fun showDateTimePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            TimePickerDialog(context, { _, hour, minute ->
                calendar.set(year, month, dayOfMonth, hour, minute, 0)
                date = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, currentYear, currentMonth, currentDay).show()
    }

    // Pre-fill all of the existing metadata tags (similar to useEffect in RN)
    LaunchedEffect(imageFile) {
        try {
            // EXIF pre-fill
            val exif = ExifInterface(imageFile)

            caption = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION) ?: ""
            date = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: ""
            val latLong = exif.latLong
            latitude = latLong?.getOrNull(0)?.toString() ?: ""
            longitude = latLong?.getOrNull(1)?.toString() ?: ""
            make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
            model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
            orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION) ?: ""
            userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""
            focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: ""
            exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) ?: ""
            aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER) ?: ""
            iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED) ?: ""
            flash = exif.getAttribute(ExifInterface.TAG_FLASH) ?: ""
            altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) ?: ""
            gpsProcessing = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) ?: ""
            gpsDate = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) ?: ""

            // IPTC pre-fill
            if (imageFile.extension.lowercase() in listOf("jpg", "jpeg")) {
                try {
                    val metadata = Kim.readMetadata(imageFile)
                    val iptc = metadata?.iptc
                    val records = iptc?.records ?: emptyList()

                    iptcTitle = records.find { it.iptcType.fieldName == "Object Name" }?.value ?: ""
                    iptcCaption = records.find { it.iptcType.fieldName == "Caption/Abstract" }?.value ?: ""
                    iptcCity = records.find { it.iptcType.fieldName == "City" }?.value ?: ""
                    iptcProvince = records.find { it.iptcType.fieldName == "Province/State" }?.value ?: ""
                    iptcCountry = records.find { it.iptcType.fieldName == "Country/Primary Location Name" }?.value ?: ""
                    iptcKeywords = records.filter { it.iptcType.fieldName == "Keywords" }
                        .joinToString(", ") { it.value }

                } catch (e: Exception) {
                    println("Error reading IPTC: ${e.localizedMessage}")
                }
            }
        } catch (_: Exception) {}
    }

    // TabRow related
    val tabs = listOf("EXIF", "IPTC")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Scroll
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Edit Metadata", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // EXIF Tab
        if (selectedTabIndex == 0) {
            OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Caption") })
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date (yyyy:MM:dd HH:mm:ss)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { showDateTimePicker() }
            )
            OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") })
            OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") })
            OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Camera Make") })
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Camera Model") })
            OutlinedTextField(value = orientation, onValueChange = { orientation = it }, label = { Text("Orientation") })
            OutlinedTextField(value = userComment, onValueChange = { userComment = it }, label = { Text("User Comment") })
            OutlinedTextField(value = focalLength, onValueChange = { focalLength = it }, label = { Text("Focal Length") })
            OutlinedTextField(value = exposureTime, onValueChange = { exposureTime = it }, label = { Text("Exposure Time") })
            OutlinedTextField(value = aperture, onValueChange = { aperture = it }, label = { Text("Aperture") })
            OutlinedTextField(value = iso, onValueChange = { iso = it }, label = { Text("ISO") })
            OutlinedTextField(value = flash, onValueChange = { flash = it }, label = { Text("Flash") })
            OutlinedTextField(value = altitude, onValueChange = { altitude = it }, label = { Text("GPS Altitude") })
            OutlinedTextField(value = gpsProcessing, onValueChange = { gpsProcessing = it }, label = { Text("GPS Processing Method") })
            OutlinedTextField(value = gpsDate, onValueChange = { gpsDate = it }, label = { Text("GPS Date") })
        }

        // IPTC Tab
        if (selectedTabIndex == 1) {
            OutlinedTextField(value = iptcTitle, onValueChange = { iptcTitle = it }, label = { Text("IPTC Title") })
            OutlinedTextField(value = iptcCaption, onValueChange = { iptcCaption = it }, label = { Text("Caption/Abstract") })
            OutlinedTextField(value = iptcCity, onValueChange = { iptcCity = it }, label = { Text("City") })
            OutlinedTextField(value = iptcProvince, onValueChange = { iptcProvince = it }, label = { Text("Province/State") })
            OutlinedTextField(value = iptcCountry, onValueChange = { iptcCountry = it }, label = { Text("Country") })
            OutlinedTextField(value = iptcKeywords, onValueChange = { iptcKeywords = it }, label = { Text("Keywords (comma-separated)") })
        }

        Spacer(Modifier.height(24.dp))

        // Submit Button
        Button(onClick = {
            keyboardController?.hide()

            try {
                val exif = ExifInterface(imageFile)
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, caption)
                exif.setAttribute(ExifInterface.TAG_DATETIME, date)
                if (latitude.isNotBlank() && longitude.isNotBlank()) {
                    exif.setLatLong(latitude.toDouble(), longitude.toDouble())
                }
                exif.setAttribute(ExifInterface.TAG_MAKE, make)
                exif.setAttribute(ExifInterface.TAG_MODEL, model)
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, userComment)
                exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength)
                exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime)
                exif.setAttribute(ExifInterface.TAG_F_NUMBER, aperture)
                exif.setAttribute(ExifInterface.TAG_ISO_SPEED, iso)
                exif.setAttribute(ExifInterface.TAG_FLASH, flash)
                exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, altitude)
                exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gpsProcessing)
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDate)
                exif.saveAttributes()
            } catch (_: Exception) {}


            try {
                val originalBytes = imageFile.readBytes()
                val keywordsList = iptcKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val records = buildList {
                    if (iptcTitle.isNotBlank()) add(IptcRecord(IptcTypes.OBJECT_NAME, iptcTitle))
                    if (iptcCaption.isNotBlank()) add(IptcRecord(IptcTypes.CAPTION_ABSTRACT, iptcCaption))
                    if (iptcCity.isNotBlank()) add(IptcRecord(IptcTypes.CITY, iptcCity))
                    if (iptcProvince.isNotBlank()) add(IptcRecord(IptcTypes.PROVINCE_STATE, iptcProvince))
                    if (iptcCountry.isNotBlank()) add(IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME, iptcCountry))
                    for (keyword in keywordsList) {
                        add(IptcRecord(IptcTypes.KEYWORDS, keyword))
                    }
                }
                val iptc = IptcMetadata(records, emptyList())
                val reader = ByteArrayByteReader(originalBytes)
                val writer = ByteArrayByteWriter()
                JpegRewriter.writeIPTC(reader, writer, iptc)
                imageFile.writeBytes(writer.toByteArray())
            } catch (_: Exception) {}

            onBack()
        }) {
            Text("Submit")
        }

        Spacer(Modifier.height(8.dp))

        // Cancel Button
        Button(onClick = onBack) {
            Text("Cancel")
        }
    }
}