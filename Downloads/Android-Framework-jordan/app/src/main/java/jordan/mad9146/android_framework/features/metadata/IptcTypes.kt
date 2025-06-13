package jordan.mad9146.android_framework.features.metadata

import com.ashampoo.kim.format.jpeg.iptc.IptcType

@Suppress("MagicNumber")
internal enum class IptcTypes(
    override val type: Int,        // The IPTC tag number (e.g., 5 = Object Name, 120 = Caption)
    override val fieldName: String // A readable label for the tag (e.g., "City", "Caption/Abstract")
) : IptcType {

    // Common IPTC fields used in metadata
    RECORD_VERSION(0, "Record Version"),
    OBJECT_TYPE_REFERENCE(3, "Object Type Reference"),
    OBJECT_ATTRIBUTE_REFERENCE(4, "Object Attribute Reference"),
    OBJECT_NAME(5, "Object Name"),  // Title or name of the image
    EDIT_STATUS(7, "Edit Status"),
    EDITORIAL_UPDATE(8, "Editorial Update"),
    URGENCY(10, "Urgency"),
    SUBJECT_REFERENCE(12, "Subject Reference"),
    CATEGORY(15, "Category"),
    SUPPLEMENTAL_CATEGORY(20, "Supplemental Category"),
    FIXTURE_IDENTIFIER(22, "Fixture Identifier"),
    KEYWORDS(25, "Keywords"),  // Comma-separated tags or terms
    CONTENT_LOCATION_CODE(26, "Content Location Code"),
    CONTENT_LOCATION_NAME(27, "Content Location Name"),
    RELEASE_DATE(30, "Release Date"),
    RELEASE_TIME(35, "Release Time"),
    EXPIRATION_DATE(37, "Expiration Date"),
    EXPIRATION_TIME(38, "Expiration Time"),
    SPECIAL_INSTRUCTIONS(40, "Special Instructions"),
    ACTION_ADVISED(42, "Action Advised"),
    REFERENCE_SERVICE(45, "Reference Service"),
    REFERENCE_DATE(47, "Reference Date"),
    REFERENCE_NUMBER(50, "Reference Number"),
    DATE_CREATED(55, "Date Created"),
    TIME_CREATED(60, "Time Created"),
    DIGITAL_CREATION_DATE(62, "Digital Creation Date"),
    DIGITAL_CREATION_TIME(63, "Digital Creation Time"),
    ORIGINATING_PROGRAM(65, "Originating Program"),
    PROGRAM_VERSION(70, "Program Version"),
    OBJECT_CYCLE(75, "Object Cycle"),
    BYLINE(80, "By-line"),  // Author or creator
    BYLINE_TITLE(85, "By-line Title"),
    CITY(90, "City"),  // Location city
    SUBLOCATION(92, "Sublocation"),
    PROVINCE_STATE(95, "Province/State"),
    COUNTRY_PRIMARY_LOCATION_CODE(100, "Country/Primary Location Code"),
    COUNTRY_PRIMARY_LOCATION_NAME(101, "Country/Primary Location Name"),
    ORIGINAL_TRANSMISSION_REFERENCE(103, "Original Transmission, Reference"),
    HEADLINE(105, "Headline"),
    CREDIT(110, "Credit"),
    SOURCE(115, "Source"),
    COPYRIGHT_NOTICE(116, "Copyright Notice"),
    CONTACT(118, "Contact"),
    CAPTION_ABSTRACT(120, "Caption/Abstract"),  // Short description
    WRITER_EDITOR(122, "Writer/Editor"),
    RASTERIZED_CAPTION(125, "Rasterized Caption"),
    IMAGE_TYPE(130, "ImageType"),
    IMAGE_ORIENTATION(131, "Image Orientation"),
    LANGUAGE_IDENTIFIER(135, "Language Identifier"),
    AUDIO_TYPE(150, "Audio Type"),
    AUDIO_SAMPLING_RATE(151, "Audio Sampling Rate"),
    AUDIO_SAMPLING_RESOLUTION(152, "Audio Sampling Resolution"),
    AUDIO_DURATION(153, "Audio Duration"),
    AUDIO_OUTCUE(154, "Audio Outcue"),
    OBJECT_DATA_PREVIEW_FILE_FORMAT(200, "Object Data Preview, File Format"),
    OBJECT_DATA_PREVIEW_FILE_FORMAT_VERSION(201, "Object Data Preview, File Format Version"),
    OBJECT_DATA_PREVIEW_DATA(202, "Object Data Preview Data");

    // Overrides how the enum will be shown as a string
    override fun toString(): String =
        "$fieldName ($type)"

    companion object {

        private val iptcTypeMap = mutableMapOf<Int, IptcType>()

        init {
            // Fill the map with all known IPTC types
            for (iptcType in IptcTypes.entries)
                iptcTypeMap[iptcType.type] = iptcType
        }

        // Allow retrieving a known or fallback IPTC type by numeric code
        @JvmStatic
        fun getIptcType(type: Int): IptcType =
            iptcTypeMap[type] ?: createUnknownIptcType(type)

        // Generate a fallback type object when the tag isn't in the enum
        @JvmStatic
        private fun createUnknownIptcType(type: Int): IptcType {
            return object : IptcType {
                override val fieldName: String get() = "Unknown"
                override val type: Int get() = type
                override fun toString(): String = "Unknown ($type)"
            }
        }
    }
}