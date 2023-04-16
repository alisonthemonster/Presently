package journal.gratitude.com.gratitudejournal.model

//TIMELINE EVENTS
const val CAME_FROM_NOTIFICATION = "cameFromNotification" //todo this is missing now
const val CANCELLED_NOTIFS = "cancelledNotifications"

const val EXPORTED_DATA = "exportedData"
const val LOOKED_FOR_DATA = "lookedForData"
const val IMPORTED_DATA_SUCCESS = "importedData"
const val IMPORTING_BACKUP_ERROR = "importedDataError"

//TIMELINE EVENTS
const val CLICKED_SEARCH = "clickedSearch"
const val CLICKED_NEW_ENTRY = "clickedNewEntry"
const val CLICKED_EXISTING_ENTRY = "clickedExistingEntry"
const val OPENED_CALENDAR = "clickedCalendar"
const val CLICKED_NEW_ENTRY_CALENDAR = "clickedNewEntryFromCal"
const val CLICKED_EXISTING_ENTRY_CALENDAR = "clickedExistingEntryFromCal"
const val CLICKED_THEMES = "clickedThemes"
const val CLICKED_SETTINGS = "clickedSettings"
const val OPENED_CONTACT_FORM = "openedContactForm"


//SETTINGS EVENTS
const val OPENED_PRIVACY_POLICY = "openedPrivacyPolicy"
const val OPENED_SHARE_APP = "openedShareApp"
const val OPENED_TERMS_CONDITIONS = "openedTermsAndConditions"
const val OPENED_FAQ = "openedFaq"
const val DROPBOX_DEAUTH = "dropboxDeauthorization"
const val DROPBOX_AUTH_ATTEMPT = "dropboxAuthorizaitonAttempt"
const val LANGUAGE_INSTALLED = "languageInstalled"

//BIOMETRIC EVENTS
const val BIOMETRICS_SELECT = "biometricsSelected"
const val BIOMETRICS_DESELECT = "biometricsDeselected"
const val BIOMETRICS_USER_CANCELLED = "biometricsScanUserCancelled"
const val BIOMETRICS_CANCELLED = "biometricsScanCancelled"
const val BIOMETRICS_LOCKOUT = "biometricsLockout"

//ENTRY EVENTS
const val SHARED_ENTRY = "sharedEntry"
const val CLICKED_PROMPT = "clickedNewPrompt"
const val EDITED_EXISTING_ENTRY = "editedExistingEntry"
const val COPIED_QUOTE = "copiedQuote"

//SEARCH EVENTS
const val CLICKED_SEARCH_ITEM = "clickedSearchItem"

//DIALOG EVENTS
const val CLICKED_RATE = "clickedRate"
const val CLICKED_SHARE_MILESTONE= "clickedShareMilestone"

//USER PROPERTIES
const val HAS_NOTIFICATIONS_TURNED_ON = "hasNotificationsTurnedOn"
const val NOTIF_TIME = "notifTime"
const val BIOMETRICS_ENABLED = "hasBiometricsEnabled"
const val THEME = "theme"