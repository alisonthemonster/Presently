## TODO
Make reloadable, make sure widget passes specifications below.

Thank you for this, got a minute to look it over and also passed it along to Claude who wrote this review below! Essentially if you follow the approach I used with `Glance` in #321 you'll be a lot closer!




## PR #323 Review — Random Entry Widget

**TL;DR:** The feature itself is functional and well-thought-out (biometric locking in the widget is actually a nice touch), but the implementation clashes significantly with the approach in the recently merged Glance widget (#321) and has several issues worth addressing before merging.

---

### The Big One: Wrong Widget Tech

The merged Glance widget uses **Jetpack Glance** (Compose-based, modern, clean). This PR uses **legacy `RemoteViews` + `RemoteViewsService`/`ListView`** — a pattern that's been superseded by Glance. The biggest tell: `WidgetScrollService` creates a `ListView` adapter with `getCount() = 1`. It's the full RemoteViewsFactory machinery just to display a single item. Glance would handle this with a `Column` and ~20 lines. If both widgets exist in the app, they should use the same technology. Rewriting this on top of Glance would significantly simplify the code and avoid a double maintenance burden.

---

### ContainerActivity Changes

The `ContainerActivity.kt` changes are the most concerning part. The activity is getting loaded with widget-specific responsibilities:

- `handleWidgetIntent()` called from both `onCreate` and `onNewIntent` — fine pattern, but it embeds deep widget navigation logic (date parsing, fragment transactions) directly in the Activity
- `navigateToEntry()` is `public` and does manual `FragmentManager` transactions — inconsistent with how the rest of navigation works in this codebase (using the Navigation component). The existing widget just opens the timeline and relies on standard navigation, which is much simpler.
- `window.decorView.post { navigateToEntry(selectedDate) }` — posting to the view hierarchy to delay fragment transactions is a workaround for a lifecycle timing issue; better to do this in `onStart` after the back stack is settled
- Starting `LockingService` from both `onCreate` and `onNewIntent` could start the service twice
- `e.printStackTrace()` in the catch block of `navigateToEntry` should go through the crash reporter

---

### Date String Fragility

The widget passes the entry date as a locale-formatted string (`"March 28, 2026"`) through the `Intent` extra, then re-parses it with `DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())`. This will silently break for users on non-English locales. The entry date should be passed as an ISO-8601 string (e.g., `LocalDate.toString()` → `"2026-03-28"`) and parsed with `LocalDate.parse()`.

---

### Debug Logs in Production

Multiple `Log.d` calls were left in across `ContainerActivity.kt` and `AppLockFragment.kt` — at least 10. These log out lock state and biometric status, which is sensitive information. They need to come out before merge.

---

### String Resource Issues

In `presently_strings.xml`:
```xml
<string name="widget_no_entries" translatable="false">Write your first...</string>
<string name="widget_refresh" translatable="false">Refresh</string>
```
Both should be translatable. "Refresh" especially is a common word that the translation teams would want to localize. (Contrast with the existing widget strings in `values-af`, `values-ar`, etc. from #321.)

Also in `WidgetScrollFactory.getViewAt()`:
```kotlin
views.setTextViewText(R.id.widget_content, context.getString(R.string.unlock_to_view_entries, "Unlock the app to view your journal entries."))
```
The `unlock_to_view_entries` string resource doesn't contain a `%1$s` format argument — the hardcoded string passed as an argument is just dropped. The `getString()` call should be `getString(R.string.unlock_to_view_entries)` with no extra arg.

---

### `forceLock()` Sentinel Value

Using `0L` as a magic "force lock immediately" value in SharedPreferences is non-obvious. It's fine that the PR added the `-1L` → "never been paused, don't lock" guard (that's actually a good bug fix for the cold-start case), but the `0L` sentinel is easily confused with a real epoch timestamp. A named constant at minimum, or a separate boolean preference, would be clearer.

---

### LockingService

The `LockingService` is a `START_STICKY` service whose only job is to call `forceLock()` in `onTaskRemoved`. That's clever, but sticky services are kept alive by the system and can appear as battery drain to users in battery stats. Worth considering whether a simpler approach (e.g., checking process death time in `shouldLockApp`) would work without a persistent service.

---

### Minor

- `android:src="@android:drawable/ic_popup_sync"` — system drawables can change between API levels and OEM skins; better to use a bundled drawable like the existing widget does
- The `WidgetScrollService.onCreate()` and `onDataSetChanged()` both call `extractIntentData()` and re-resolve the Hilt entry point — `onDataSetChanged()` re-resolves the entry point but the intent data is stale (the service instance persists, the intent only arrives at construction). The refresh button triggers a full `updateAppWidget` which creates a new service intent with the unique URI, so this works correctly, but it's easy to misread.

---

### What's Good

- DAO query `SELECT * FROM entries ORDER BY RANDOM() LIMIT 1` is correct and the tests for it are solid
- The biometric-aware locking in `WidgetScrollFactory.getViewAt()` (hiding entry text when locked) is a thoughtful feature the Glance widget doesn't have
- `getRandomEntry(): Entry?` being nullable and handled properly throughout the stack
- Test coverage for the new settings behavior (`forceLock`, `-1L` → no lock) is appreciated

---

**Recommendation:** Port the implementation to Glance (the refactor would actually make most of the other issues disappear — no more RemoteViewsService, no more date string tricks for entry navigation, cleaner theme application), fix the date serialization and debug logs, then re-review.