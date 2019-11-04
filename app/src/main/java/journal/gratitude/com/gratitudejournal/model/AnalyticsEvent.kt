package journal.gratitude.com.gratitudejournal.model

//TIMELINE EVENTS
const val CAME_FROM_NOTIFICATION = "cameFromNotification"
const val CANCELLED_NOTIFS = "cancelledNotifications"

const val EXPORTED_DATA = "exportedData"
const val LOOKED_FOR_DATA = "lookedForData"
const val IMPORTED_DATA_SUCCESS = "importedData"
const val IMPORTING_BACKUP_ERROR = "importedDataError"

const val LOOKED_AT_SETTINGS = "lookedAtSettings"
const val OPENED_CONTACT_FORM = "openedContactForm"
const val OPENED_PRIVACY_POLICY = "openedPrivacyPolicy"
const val OPENED_TERMS_CONDITIONS = "openedTermsAndConditions"
const val OPENED_THEMES = "openedThemes"

const val CLICKED_SEARCH = "clickedSearch"
const val CLICKED_NEW_ENTRY = "clickedNewEntry"
const val CLICKED_EXISTING_ENTRY = "clickedExistingEntry"

const val OPENED_CALENDAR = "clickedCalendar"
const val CLICKED_NEW_ENTRY_CALENDAR = "clickedNewEntryFromCal"
const val CLICKED_EXISTING_ENTRY_CALENDAR = "clickedExistingEntryFromCal"

//ENTRY EVENTS
const val SHARED_ENTRY = "sharedEntry"
const val CLICKED_PROMPT = "clickedNewPrompt"
const val EDITED_EXISTING_ENTRY = "editedExistingEntry"

//SEARCH EVENTS
const val CLICKED_SEARCH_ITEM = "clickedSearchItem"

//USER PROPERTIES
const val HAS_NOTIFICATIONS_TURNED_ON = "hasNotificationsTurnedOn"
const val NOTIF_TIME = "notifTime"

//DIALOG EVENTS
const val CLICKED_RATE = "clickedRate"
const val CLICKED_SHARE_MILESTONE= "clickedShareMilestone"