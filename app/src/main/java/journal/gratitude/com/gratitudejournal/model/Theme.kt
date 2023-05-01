package journal.gratitude.com.gratitudejournal.model

data class Theme(
    val name: String,
    val headerColor: Int,
    val backgroundColor: Int,
    val headerItemColor: Int,
    val iconColor: Int,
    val icon: Int,
    val multicolorIcon: Boolean = false,
    val designer: Designer? = null
)

data class Designer(
    val designerName: String,
    val designerWebsite: String
)
