#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SECRETS_DIR="${ROOT_DIR}/release-secrets"
RELEASE_PROPS_FILE="${SECRETS_DIR}/release.properties"
GOOGLE_SERVICES_SOURCE="${SECRETS_DIR}/google-services.json"
GCLOUD_SOURCE="${SECRETS_DIR}/gcloud-service-key.json"
FONT_SOURCE_DIR="${SECRETS_DIR}/fonts"
APP_GOOGLE_SERVICES="${ROOT_DIR}/app/google-services.json"
GCLOUD_TARGET="${ROOT_DIR}/gcloud-service-key.json"
RELEASE_FONT_DIR="${ROOT_DIR}/app/src/release/res/font"
VERSIONS_FILE="${ROOT_DIR}/buildSrc/src/main/java/Dependencies.kt"

RUN_UNIT_TESTS=true
INSTRUMENTED_MODE="skip"
BUILD_APK=false
BUMP_LEVEL="none"
TARGET_VERSION_NAME=""
TARGET_VERSION_CODE=""

TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/presently-release.XXXXXX")"
GOOGLE_BACKUP=""
GCLOUD_BACKUP=""
FONT_BACKUP=""

usage() {
  cat <<'EOF'
Usage: ./scripts/release_job.sh [options]

Stages release-only files from ./release-secrets, optionally bumps app version
numbers, runs tests, and builds release artifacts.

Options:
  --bump patch|minor|major    Increment the semantic version and versionCode.
  --version-name X.Y.Z        Set an explicit semantic version.
  --version-code N            Set an explicit versionCode. Defaults to +1 when
                              a version bump/name override is applied.
  --instrumented MODE         MODE is one of: firebase, connected, skip.
  --skip-unit-tests           Skip unit tests.
  --build-apk                 Build assembleRelease in addition to bundleRelease.
  --help                      Show this help text.

Expected local files under release-secrets/:
  google-services.json
  gcloud-service-key.json            (only for --instrumented firebase)
  fonts/larsseit_medium.ttf          (optional)
  fonts/value_serif.ttf              (optional)
  release.properties

release.properties should export:
  DROPBOX_APP_KEY=...
  RELEASE_STORE_FILE=/absolute/path/to/upload-keystore.jks
  RELEASE_STORE_PASSWORD=...
  RELEASE_KEY_ALIAS=...
  RELEASE_KEY_PASSWORD=...
EOF
}

cleanup() {
  if [[ -n "$GOOGLE_BACKUP" && -f "$GOOGLE_BACKUP" ]]; then
    cp "$GOOGLE_BACKUP" "$APP_GOOGLE_SERVICES"
  else
    rm -f "$APP_GOOGLE_SERVICES"
  fi

  if [[ -n "$GCLOUD_BACKUP" && -f "$GCLOUD_BACKUP" ]]; then
    cp "$GCLOUD_BACKUP" "$GCLOUD_TARGET"
  else
    rm -f "$GCLOUD_TARGET"
  fi

  rm -rf "$RELEASE_FONT_DIR"
  if [[ -n "$FONT_BACKUP" && -d "$FONT_BACKUP" ]]; then
    mkdir -p "$(dirname "$RELEASE_FONT_DIR")"
    mv "$FONT_BACKUP" "$RELEASE_FONT_DIR"
  fi

  rm -rf "$TMP_DIR"
}

trap cleanup EXIT

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bump)
      BUMP_LEVEL="${2:-}"
      shift 2
      ;;
    --version-name)
      TARGET_VERSION_NAME="${2:-}"
      shift 2
      ;;
    --version-code)
      TARGET_VERSION_CODE="${2:-}"
      shift 2
      ;;
    --instrumented)
      INSTRUMENTED_MODE="${2:-}"
      shift 2
      ;;
    --skip-unit-tests)
      RUN_UNIT_TESTS=false
      shift
      ;;
    --build-apk)
      BUILD_APK=true
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

case "$BUMP_LEVEL" in
  none|patch|minor|major)
    ;;
  *)
    echo "Invalid --bump value: $BUMP_LEVEL" >&2
    exit 1
    ;;
esac

case "$INSTRUMENTED_MODE" in
  skip|connected|firebase)
    ;;
  *)
    echo "Invalid --instrumented value: $INSTRUMENTED_MODE" >&2
    exit 1
    ;;
esac

if [[ -n "$TARGET_VERSION_NAME" && ! "$TARGET_VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Version name must use X.Y.Z format: $TARGET_VERSION_NAME" >&2
  exit 1
fi

if [[ -n "$TARGET_VERSION_CODE" && ! "$TARGET_VERSION_CODE" =~ ^[0-9]+$ ]]; then
  echo "Version code must be numeric: $TARGET_VERSION_CODE" >&2
  exit 1
fi

if [[ -f "$RELEASE_PROPS_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$RELEASE_PROPS_FILE"
  set +a
fi

backup_file_if_present() {
  local source_file="$1"
  local backup_file="$2"

  if [[ -f "$source_file" ]]; then
    cp "$source_file" "$backup_file"
  fi
}

stage_google_services() {
  if [[ ! -f "$GOOGLE_SERVICES_SOURCE" ]]; then
    echo "Missing release-secrets/google-services.json" >&2
    exit 1
  fi

  GOOGLE_BACKUP="${TMP_DIR}/google-services.json"
  backup_file_if_present "$APP_GOOGLE_SERVICES" "$GOOGLE_BACKUP"
  cp "$GOOGLE_SERVICES_SOURCE" "$APP_GOOGLE_SERVICES"
}

stage_release_fonts() {
  local larsseit_source="${FONT_SOURCE_DIR}/larsseit_medium.ttf"
  local value_source="${FONT_SOURCE_DIR}/value_serif.ttf"
  local has_larsseit=false
  local has_value=false

  if [[ -f "$larsseit_source" ]]; then
    has_larsseit=true
  fi

  if [[ -f "$value_source" ]]; then
    has_value=true
  fi

  if [[ "$has_larsseit" == false && "$has_value" == false ]]; then
    return
  fi

  if [[ "$has_larsseit" == false || "$has_value" == false ]]; then
    echo "If you override fonts, both release font files must exist under release-secrets/fonts" >&2
    exit 1
  fi

  if [[ -d "$RELEASE_FONT_DIR" ]]; then
    FONT_BACKUP="${TMP_DIR}/release-font-backup"
    mv "$RELEASE_FONT_DIR" "$FONT_BACKUP"
  fi

  mkdir -p "$RELEASE_FONT_DIR"
  cp "$larsseit_source" "${RELEASE_FONT_DIR}/larsseit_medium.ttf"
  cp "$value_source" "${RELEASE_FONT_DIR}/value_serif.ttf"
}

stage_gcloud_key() {
  if [[ "$INSTRUMENTED_MODE" != "firebase" ]]; then
    return
  fi

  if [[ ! -f "$GCLOUD_SOURCE" ]]; then
    echo "Missing release-secrets/gcloud-service-key.json for Firebase Test Lab run" >&2
    exit 1
  fi

  GCLOUD_BACKUP="${TMP_DIR}/gcloud-service-key.json"
  backup_file_if_present "$GCLOUD_TARGET" "$GCLOUD_BACKUP"
  cp "$GCLOUD_SOURCE" "$GCLOUD_TARGET"
}

read_current_version_component() {
  local name="$1"
  awk -v key="$name" '$0 ~ "const val " key " =" { print $5 }' "$VERSIONS_FILE"
}

write_version_numbers() {
  local major="$1"
  local minor="$2"
  local patch="$3"
  local code="$4"

  perl -0pi -e "s/const val APP_VERSION_CODE = \\d+/const val APP_VERSION_CODE = ${code}/" "$VERSIONS_FILE"
  perl -0pi -e "s/const val MAJOR = \\d+/const val MAJOR = ${major}/" "$VERSIONS_FILE"
  perl -0pi -e "s/const val MINOR = \\d+/const val MINOR = ${minor}/" "$VERSIONS_FILE"
  perl -0pi -e "s/const val PATCH = \\d+/const val PATCH = ${patch}/" "$VERSIONS_FILE"
}

apply_version_update() {
  local current_major
  local current_minor
  local current_patch
  local current_code
  local next_major
  local next_minor
  local next_patch
  local next_code

  current_major="$(read_current_version_component "MAJOR")"
  current_minor="$(read_current_version_component "MINOR")"
  current_patch="$(read_current_version_component "PATCH")"
  current_code="$(read_current_version_component "APP_VERSION_CODE")"

  next_major="$current_major"
  next_minor="$current_minor"
  next_patch="$current_patch"

  if [[ -n "$TARGET_VERSION_NAME" ]]; then
    IFS='.' read -r next_major next_minor next_patch <<<"$TARGET_VERSION_NAME"
  else
    case "$BUMP_LEVEL" in
      patch)
        next_patch=$((current_patch + 1))
        ;;
      minor)
        next_minor=$((current_minor + 1))
        next_patch=0
        ;;
      major)
        next_major=$((current_major + 1))
        next_minor=0
        next_patch=0
        ;;
      none)
        ;;
    esac
  fi

  if [[ -n "$TARGET_VERSION_CODE" ]]; then
    next_code="$TARGET_VERSION_CODE"
  elif [[ -n "$TARGET_VERSION_NAME" || "$BUMP_LEVEL" != "none" ]]; then
    next_code=$((current_code + 1))
  else
    next_code="$current_code"
  fi

  if [[ "$next_major" == "$current_major" &&
        "$next_minor" == "$current_minor" &&
        "$next_patch" == "$current_patch" &&
        "$next_code" == "$current_code" ]]; then
    return
  fi

  write_version_numbers "$next_major" "$next_minor" "$next_patch" "$next_code"
  echo "Updated version to ${next_major}.${next_minor}.${next_patch} (${next_code})"
}

run_gradle() {
  ./gradlew --stacktrace "$@"
}

stage_google_services
stage_release_fonts
stage_gcloud_key
apply_version_update

if [[ "$RUN_UNIT_TESTS" == true || "$INSTRUMENTED_MODE" != "skip" ]]; then
  echo "Building debug test artifacts..."
  run_gradle assembleDebug assembleDebugAndroidTest
fi

if [[ "$RUN_UNIT_TESTS" == true ]]; then
  echo "Running unit tests..."
  run_gradle testDebugUnitTest
fi

if [[ "$INSTRUMENTED_MODE" == "connected" ]]; then
  echo "Running connected Android tests..."
  run_gradle :app:connectedDebugAndroidTest
fi

if [[ "$INSTRUMENTED_MODE" == "firebase" ]]; then
  echo "Running Firebase Test Lab instrumentation..."
  run_gradle -PenableFladle runFlank
fi

RELEASE_TASKS=(verifyReleaseConfig bundleRelease)
if [[ "$BUILD_APK" == true ]]; then
  RELEASE_TASKS+=(assembleRelease)
fi

echo "Building release artifacts..."
run_gradle "${RELEASE_TASKS[@]}"

echo
echo "Release outputs:"
echo "  AAB: ${ROOT_DIR}/app/build/outputs/bundle/release/app-release.aab"
if [[ "$BUILD_APK" == true ]]; then
  echo "  APK: ${ROOT_DIR}/app/build/outputs/apk/release/app-release.apk"
fi
