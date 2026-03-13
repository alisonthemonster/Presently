#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MODE="unit"
INCLUDE_SHARING_CONNECTED="false"

usage() {
  cat <<'EOF'
Usage: ./scripts/local_coverage.sh [--connected] [--connected-sharing]

Runs the local coverage flow for this repo.

Options:
  --connected   Include connected Android tests before generating the merged
                JaCoCo report. Requires a running emulator or attached device.
  --connected-sharing
                When used with --connected, also run sharing module connected
                Android tests.
  --help        Show this help text.

Default behavior runs:
  1. assembleDebug assembleDebugAndroidTest
  2. testDebugUnitTest
  3. jacocoFullReport

With --connected it also runs:
  :app:connectedDebugAndroidTest

With --connected --connected-sharing it also runs:
  :sharing:connectedDebugAndroidTest

Reports:
  HTML: build/reports/jacoco/html/index.html
  XML:  build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --connected)
      MODE="connected"
      shift
      ;;
    --connected-sharing)
      INCLUDE_SHARING_CONNECTED="true"
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

GRADLEW="./gradlew"
COVERAGE_ARGS=(-Pcoverage -PskipCoverageVerification --stacktrace)
HTML_REPORT="$ROOT_DIR/build/reports/jacoco/html/index.html"
XML_REPORT="$ROOT_DIR/build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml"

echo "Building debug artifacts..."
"$GRADLEW" "${COVERAGE_ARGS[@]}" assembleDebug assembleDebugAndroidTest

echo "Running unit tests..."
"$GRADLEW" "${COVERAGE_ARGS[@]}" testDebugUnitTest

if [[ "$MODE" == "connected" ]]; then
  echo "Running connected Android tests for the app module..."
  "$GRADLEW" "${COVERAGE_ARGS[@]}" :app:connectedDebugAndroidTest

  if [[ "$INCLUDE_SHARING_CONNECTED" == "true" ]]; then
    echo "Running connected Android tests for the sharing module..."
    "$GRADLEW" "${COVERAGE_ARGS[@]}" :sharing:connectedDebugAndroidTest
  fi
elif [[ "$INCLUDE_SHARING_CONNECTED" == "true" ]]; then
  echo "--connected-sharing requires --connected" >&2
  exit 1
fi

echo "Generating JaCoCo report..."
"$GRADLEW" "${COVERAGE_ARGS[@]}" jacocoFullReport --rerun-tasks

if [[ ! -f "$HTML_REPORT" ]]; then
  echo "Expected HTML report was not generated: $HTML_REPORT" >&2
  exit 1
fi

if [[ ! -f "$XML_REPORT" ]]; then
  echo "Expected XML report was not generated: $XML_REPORT" >&2
  exit 1
fi

echo
echo "Coverage reports generated:"
echo "  HTML: $HTML_REPORT"
echo "  XML:  $XML_REPORT"
