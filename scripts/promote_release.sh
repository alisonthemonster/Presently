#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SECRETS_DIR="${ROOT_DIR}/release-secrets"
RELEASE_PROPS_FILE="${SECRETS_DIR}/release.properties"

FROM_TRACK="internal"
TO_TRACK="production"
RELEASE_STATUS="completed"
USER_FRACTION=""
UPDATE_TRACK=""

usage() {
  cat <<'EOF'
Usage: ./scripts/promote_release.sh [options]

Promotes an existing Play release from one track to another.

Options:
  --from-track TRACK          Source track. Default: internal
  --to-track TRACK            Target track. Default: production
  --release-status STATUS     completed|draft|halted|inProgress. Default: completed
  --user-fraction VALUE       Required only for inProgress or halted promotions.
  --update TRACK              Update an in-progress release on the given track instead of promoting.
  --help                      Show this help text.

Expected release-secrets/release.properties:
  PLAY_SERVICE_ACCOUNT_FILE=/absolute/path/to/play-service-account.json
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --from-track)
      FROM_TRACK="${2:-}"
      shift 2
      ;;
    --to-track)
      TO_TRACK="${2:-}"
      shift 2
      ;;
    --release-status)
      RELEASE_STATUS="${2:-}"
      shift 2
      ;;
    --user-fraction)
      USER_FRACTION="${2:-}"
      shift 2
      ;;
    --update)
      UPDATE_TRACK="${2:-}"
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

if [[ -f "$RELEASE_PROPS_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$RELEASE_PROPS_FILE"
  set +a
fi

if [[ -z "${PLAY_SERVICE_ACCOUNT_FILE:-}" ]]; then
  echo "Missing PLAY_SERVICE_ACCOUNT_FILE in release-secrets/release.properties" >&2
  exit 1
fi

GRADLE_ARGS=(--stacktrace promoteReleaseArtifact --release-status "$RELEASE_STATUS")

if [[ -n "$UPDATE_TRACK" ]]; then
  GRADLE_ARGS+=(--update "$UPDATE_TRACK")
else
  GRADLE_ARGS+=(--from-track "$FROM_TRACK" --promote-track "$TO_TRACK")
fi

if [[ -n "$USER_FRACTION" ]]; then
  GRADLE_ARGS+=(--user-fraction "$USER_FRACTION")
fi

./gradlew "${GRADLE_ARGS[@]}"
