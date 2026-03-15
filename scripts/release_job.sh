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
PLAY_TRACK=""
PLAY_RELEASE_NAME=""
GIT_COMMIT_VERSION=false
GIT_BRANCH=""
GIT_REMOTE="origin"

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
  --play-track TRACK          Upload the built AAB to a Play track like internal.
  --play-release-name NAME    Override the Play release name for this upload.
  --git-commit-version        Commit the bumped version and push it back to Git.
  --git-branch BRANCH         Branch to push when using --git-commit-version.
  --git-remote REMOTE         Git remote to push when using --git-commit-version. Default: origin
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
  PLAY_SERVICE_ACCOUNT_FILE=/absolute/path/to/play-service-account.json
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
    --play-track)
      PLAY_TRACK="${2:-}"
      shift 2
      ;;
    --play-release-name)
      PLAY_RELEASE_NAME="${2:-}"
      shift 2
      ;;
    --git-commit-version)
      GIT_COMMIT_VERSION=true
      shift
      ;;
    --git-branch)
      GIT_BRANCH="${2:-}"
      shift 2
      ;;
    --git-remote)
      GIT_REMOTE="${2:-}"
      shift 2
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

if [[ -n "$PLAY_TRACK" && ! "$PLAY_TRACK" =~ ^[A-Za-z0-9_-]+$ ]]; then
  echo "Play track must contain only letters, numbers, underscores, or hyphens: $PLAY_TRACK" >&2
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

current_version_name() {
  local major
  local minor
  local patch

  major="$(read_current_version_component "MAJOR")"
  minor="$(read_current_version_component "MINOR")"
  patch="$(read_current_version_component "PATCH")"

  echo "${major}.${minor}.${patch}"
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

authenticated_git_url() {
  local remote_url="$1"

  if [[ -z "${GITHUB_BOT_TOKEN:-}" ]]; then
    echo "GITHUB_BOT_TOKEN is required when using --git-commit-version" >&2
    exit 1
  fi

  case "$remote_url" in
    https://github.com/*)
      echo "${remote_url/https:\/\//https:\/\/x-access-token:${GITHUB_BOT_TOKEN}@}"
      ;;
    git@github.com:*)
      echo "https://x-access-token:${GITHUB_BOT_TOKEN}@github.com/${remote_url#git@github.com:}"
      ;;
    *)
      echo "Unsupported git remote URL for authenticated push: $remote_url" >&2
      exit 1
      ;;
  esac
}

push_version_commit() {
  if [[ "$GIT_COMMIT_VERSION" != true ]]; then
    return
  fi

  if git diff --quiet -- "$VERSIONS_FILE"; then
    echo "Version file was not changed; skipping git commit step."
    return
  fi

  local branch
  local version_name
  local original_remote_url
  local push_remote_url
  local git_user_name
  local git_user_email

  branch="$GIT_BRANCH"
  if [[ -z "$branch" ]]; then
    branch="${CIRCLE_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
  fi

  if [[ "$branch" == "HEAD" || -z "$branch" ]]; then
    echo "Unable to determine git branch for push. Pass --git-branch explicitly." >&2
    exit 1
  fi

  version_name="$(current_version_name)"
  original_remote_url="$(git remote get-url "$GIT_REMOTE")"
  push_remote_url="$(authenticated_git_url "$original_remote_url")"
  git_user_name="${GIT_USER_NAME:-Presently Release Bot}"
  git_user_email="${GIT_USER_EMAIL:-presently-release-bot@users.noreply.github.com}"

  git config user.name "$git_user_name"
  git config user.email "$git_user_email"
  git add "$VERSIONS_FILE"
  git commit -m "Release ${version_name}"
  git remote set-url "$GIT_REMOTE" "$push_remote_url"
  git push "$GIT_REMOTE" "HEAD:${branch}"
  git remote set-url "$GIT_REMOTE" "$original_remote_url"

  echo "Pushed release version commit: ${version_name}"
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

if [[ -n "$PLAY_TRACK" ]]; then
  PLAY_TASKS=(publishReleaseBundle --artifact-dir "${ROOT_DIR}/app/build/outputs/bundle/release" --track "$PLAY_TRACK")
  if [[ -n "$PLAY_RELEASE_NAME" ]]; then
    PLAY_TASKS+=(--release-name "$PLAY_RELEASE_NAME")
  fi

  echo "Uploading release bundle to Google Play track: $PLAY_TRACK"
  run_gradle "${PLAY_TASKS[@]}"
fi

push_version_commit

echo
echo "Release outputs:"
echo "  AAB: ${ROOT_DIR}/app/build/outputs/bundle/release/app-release.aab"
if [[ "$BUILD_APK" == true ]]; then
  echo "  APK: ${ROOT_DIR}/app/build/outputs/apk/release/app-release.apk"
fi
if [[ -n "$PLAY_TRACK" ]]; then
  echo "  Play track upload: ${PLAY_TRACK}"
fi
