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
11. Open a pull request with a short description of the translation.
12. Respond to review comments if needed. Once approved, the translation will ship in a future release.

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

### Release automation

The repo now includes local release helpers:

`./scripts/release_job.sh`
`./scripts/promote_release.sh`

It handles the release-only steps that should not live in git:
- stages `app/google-services.json`
- loads Dropbox + signing secrets from a local `release.properties`
- optionally overlays paid fonts for the `release` source set only
- optionally bumps `versionName` / `versionCode`
- runs unit tests
- runs connected Android tests or Firebase Test Lab tests
- builds a signed release bundle, plus an APK if requested
- optionally uploads the built AAB to a Play track

Create a local `release-secrets/` directory like this:

```text
release-secrets/
  google-services.json
  gcloud-service-key.json        # only if using --instrumented firebase
  release.properties
  fonts/
    larsseit_medium.ttf
    value_serif.ttf
```

Start from `scripts/release.properties.example` and copy it to `release-secrets/release.properties`.

Examples:
- `./scripts/release_job.sh --bump patch`
- `./scripts/release_job.sh --version-name 1.22.3 --version-code 91 --instrumented connected`
- `./scripts/release_job.sh --bump minor --instrumented firebase --build-apk`
- `./scripts/release_job.sh --bump patch --instrumented firebase --play-track internal`

To promote an existing Play release:
- `./scripts/promote_release.sh --from-track internal --to-track production`

The script cleans up staged secret files when it exits, so you do not need to stash or swap tracked files for a release build.

### CircleCI release job

CircleCI is set up so you can trigger a release pipeline manually.

The `release` workflow is intended to run with a restricted CircleCI context named `presently-release`.

In CircleCI, use `Trigger Pipeline` and pass:
- `run_release: true`
- optional `release_bump: patch|minor|major|none`
- optional `release_version_name: X.Y.Z`
- optional `release_version_code: N`
- optional `release_instrumented: firebase|connected|skip`
- optional `release_build_apk: true|false`
- optional `release_play_track: internal|production|...`
- optional `release_play_release_name: your label`
- optional `release_commit_version: true|false`

To promote an existing uploaded release, trigger a pipeline with:
- `run_promote_release: true`
- optional `promote_from_track: internal|beta|...`
- optional `promote_to_track: production|beta|...`
- optional `promote_release_status: completed|draft|halted|inProgress`
- optional `promote_user_fraction: 0.1`

Required CircleCI environment variables:
- `GOOGLE_SERVICES_JSON_B64` or `GOOGLE_SERVICES_JSON`
- `DROPBOX_APP_KEY`
- `ANDROID_KEYSTORE_B64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`
- `GITHUB_BOT_TOKEN` if you use `release_commit_version: true`

Optional CircleCI environment variables:
- `GCLOUD_SERVICE_KEY` for `release_instrumented=firebase` or for GCS-hosted fonts
- `GOOGLE_PLAY_SERVICE_ACCOUNT_B64` for Play upload/promotion. If absent, the workflow falls back to `GCLOUD_SERVICE_KEY`
- `LARSSEIT_MEDIUM_TTF_GCS_URI` and `VALUE_SERIF_TTF_GCS_URI` if you want CI to swap in paid fonts from private Cloud Storage objects

The CircleCI release job will:
- materialize the release-only files into `release-secrets/`
- run `./scripts/release_job.sh`
- store the generated AAB and optional APK as CircleCI artifacts
- optionally upload the AAB to Google Play

Important: version bumping in CI changes the workspace used for that build unless you also enable `release_commit_version`. With `release_commit_version: true`, the job will push the bumped version file back to the triggering branch after the release succeeds.

Recommended production flow:
- `run_release: true` with `release_bump: patch`, `release_play_track: internal`, and `release_commit_version: true`
- verify the internal release in Play
- `run_promote_release: true` to move the existing artifact from `internal` to `production`

When `release_commit_version` is enabled, the release job will:
- commit the bumped version in `buildSrc/src/main/java/Dependencies.kt`
- push that commit back to the triggering branch

This requires a `GITHUB_BOT_TOKEN` with push access to the repository. If the branch is protected against direct pushes from that token, the release job will fail at the final push step.

To host paid fonts privately for CircleCI:
- upload each font to a private GCS bucket
- grant the CircleCI service account `roles/storage.objectViewer` on that bucket
- set `LARSSEIT_MEDIUM_TTF_GCS_URI` and `VALUE_SERIF_TTF_GCS_URI` to the `gs://...` object URIs

To restrict who can run releases:
- create a CircleCI context named `presently-release`
- move the release-only secrets into that context instead of plain project env vars
- restrict that context to the GitHub team or project that should be allowed to run releases

If a user triggers the release pipeline without access to that context, the `release_build` job will fail with `Unauthorized`.
