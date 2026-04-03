# Presently: A Gratitude Journal

[![CircleCI](https://circleci.com/gh/alisonthemonster/Presently/tree/develop.svg?style=svg)](https://circleci.com/gh/alisonthemonster/Presently/tree/develop)

A journal to write what you're thankful for. Contributions welcome!

[Google Play Posting](https://play.google.com/store/apps/details?id=journal.gratitude.com.gratitudejournal&hl=en)

### Philosophy

Presently is built on the idea that gratitude journaling should be free, private, and available to
all. It will never have ads, never have a premium version, and will always be open source. Entries
are kept on device and can be exported to CSV.

Presently is also built with the best Android practices in mind. It follows MVVM to separate
concerns and ensure we write testable code. We require 80%+ code coverage and rely on both unit
tests and instrumented tests (with a heavier weight towards unit tests). Instrumented tests are run
on Firebase Test Lab, crashes are reported through Crashlytics, and analytics are reported with
Firebase Analytics.

We support several languages currently and are always looking for translators to help us! We support
French, Spanish, Italian, German, Finnish, Arabic, Dutch, Polish, Portuguese, Slovakian, Croatian,
Romanian, Turkish, Russian, Afrikaans.

### Tech Stack [So far...]

- MVVM with architecture components
    - ViewModel
    - Room (with FTS and Paging v3)
- Mavericks
- CircleCI
- Jacoco coverage reports (integrated with CI)
- Firebase Crashlytics Crash Reporting
- Dagger + Hilt
- Espresso
- Firebase Test Lab
- WorkManager
- Dropbox Java SDK

## Design

<img src="https://i.imgur.com/EN4oOF4.png" width="200"> <img src="https://i.imgur.com/8RJvH6E.png" width="200"> <img src="https://i.imgur.com/UQQ7wbq.png" width="200"> <img src="https://i.imgur.com/JtYuyq2.png" width="200">


The app currently has over 40 themes to choose from! Here's a few:

<img src="https://user-images.githubusercontent.com/10744793/189284037-5d19021b-17c4-4229-a769-bb8ee74fd8d6.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284043-4ddf2749-e0b6-456a-b6fa-35f49157c3f5.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284046-bf74da64-02e7-45a9-a77d-b357ca300bf8.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284047-d70bde92-8c66-4dea-85ef-bcfd5165dd66.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284049-30e28cf9-5751-4de8-bf29-96ff4c274ef6.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/189284050-082145de-e35e-4d80-a2bf-ed27d80d1b13.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650501-69ed8e1b-0589-4ae8-81e0-cd8bbb9a779c.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122717-de9dc39e-7a85-4b6a-87f7-33d13b72340d.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122723-94abd81a-223d-40d3-8f17-d60b8ac936a3.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/136122868-fc770228-5185-4aef-b8d6-c7f02fce822f.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650505-d26bad6e-02aa-4929-9672-06451e38322d.png" width="200"/> <img src="https://user-images.githubusercontent.com/10744793/137650495-fb0c5fdf-d38e-48c5-bd8c-2c660e6196e3.png" width="200"/>

### Contributions

Contributions are welcome! Please fork the repo and make a PR. Forked PRs will build the app and run
unit tests but will not run instrumented tests. A repo owner will run those tests as they require
secret keys. You can set DROPBOX_KEY in local.properties if you want to test Dropbox features
locally.

### Translation Guide

Presently is translated by volunteers. All translatable app copy now lives under
`app/src/main/res`, so translators only need to work in that tree.

#### Translate via GitHub

1. Fork this repository.
2. In your fork, create a new directory in `app/src/main/res/` named `values-XX`, where `XX` is
   your language locale code. For locale codes and special variants such as Brazilian Portuguese,
   refer to the [IANA language subtag registry](http://www.iana.org/assignments/language-subtag-registry/language-subtag-registry).
3. Create `app/src/main/res/values-XX/presently_strings.xml` and copy the contents of
   `app/src/main/res/values/presently_strings.xml` into it.
4. Translate every string except the ones marked `translatable="false"`.
5. Create `app/src/main/res/values-XX/presently_prompts.xml` and copy the contents of
   `app/src/main/res/values/presently_prompts.xml` into it.
6. Translate every item in that file.
7. Add your Play Store listing translation to the end of
   `StoreListing.md`. Please keep the first line under 50 characters and the second under 80:

```text
[NOTE TO TRANSLATORS: please keep the following line under 50 characters long]

Presently: A gratitude journal

[NOTE TO TRANSLATORS: please keep the following line under 80 characters long]

Celebrate your daily life with Presently, a free & private gratitude journal

[NOTE TO TRANSLATORS: no length limits below this point, translate however you like!]

Practice gratitude with this simple, always free, and private gratitude journaling app.

Presently lets you: ⁕ Record daily entries of gratitude ⁕ Reflect back on your past moments of gratitude ⁕ Find motivation through quotes and prompts ⁕ Set daily reminders to continue your gratitude practice ⁕ Lock your entries with fingerprint or face id ⁕ Automatically back up your entries to Dropbox ⁕ Search your old entries ⁕ Share your entries with family and friends ⁕ Export/import your entries ⁕ Switch to your favorite color scheme

Presently is 100% free and ad free. All of your entries stay on your device and in your control.
```

8. Optional: create `app/src/main/res/values-XX/presently_inspirations.xml` from
   `app/src/main/res/values/presently_inspirations.xml` and translate it. This file contains
   gratitude quotes, so it is appreciated but not required.
9. Add your language label to `app/src/main/res/values/strings.xml`:

```xml
<string name="language_XX" translatable="false">LANGUAGE</string>
```

Replace `XX` with your locale code and `LANGUAGE` with the language name written in that language
such as `Français`, `English`, or `русский`.

10. Add your language to the `AppLanguage` and `AppLanguageValues` arrays in
    `app/src/main/res/values/array.xml`.
11. Add your language to `app/src/main/res/xml/locales_config.xml` so Android's system
    "App language" settings page knows the app supports it.
12. Add your locale qualifier to `supportedAppLanguages` in `app/build.gradle.kts`. This keeps the
    packaged language list aligned with the app picker and system locale config. Use Android
    resource qualifiers here, for example `fr`, `pt-rBR`, or `zh-rHK`.
13. Open a pull request with a short description of the translation.
14. Respond to review comments if needed. Once approved, the translation will ship in a future release.

#### Translate Without GitHub

If you do not want to use GitHub, email your translated files to
`gratitude.journal.app@gmail.com`.

Send:
- `presently_strings.xml`
- `presently_prompts.xml`
- your translated Play Store listing text
- optional `presently_inspirations.xml`

Use these source files as the starting point:
- `app/src/main/res/values/presently_strings.xml`
- `app/src/main/res/values/presently_prompts.xml`
- `app/src/main/res/values/presently_inspirations.xml`

#### Translating XML

String resources look like this:

```xml
<string name="key_name">Translate me!</string>
```

Only translate the text between the tags. Some characters such as `'` must be escaped in Android
XML. See the Android docs on [string resource escaping](https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes).

String arrays look like this:

```xml
<resources>
    <string-array name="prompts">
        <item>What is something that made you smile?</item>
    </string-array>
</resources>
```

Only translate the text inside each `<item>...</item>`.

### Local coverage

Use the local coverage helper script:
- Unit tests only: `./scripts/local_coverage.sh`
- Unit tests + connected Android tests for `app`: `./scripts/local_coverage.sh --connected`

Reports are written to:

- HTML: `build/reports/jacoco/html/index.html`
- XML: `build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml`
