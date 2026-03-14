#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MODE="unit"

usage() {
  cat <<'EOF'
Usage: ./scripts/local_coverage.sh [--connected]

Runs the local coverage flow for this repo.

Options:
  --connected   Include connected Android tests before generating the merged
                JaCoCo report. Requires a running emulator or attached device.
  --help        Show this help text.

Default behavior runs:
  1. assembleDebug assembleDebugAndroidTest
  2. testDebugUnitTest
  3. jacocoFullReport

With --connected it also runs:
  :app:connectedDebugAndroidTest

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

echo "Cleaning previous coverage artifacts..."
find "$ROOT_DIR" -path '*/build/jacoco/*.exec' -delete
find "$ROOT_DIR" -path '*/build/outputs/code-coverage/connected/*coverage.ec' -delete
find "$ROOT_DIR" -path '*/build/outputs/code_coverage/debugAndroidTest/connected/*.ec' -delete
rm -rf "$ROOT_DIR/build/reports/jacoco"

echo "Building debug artifacts..."
"$GRADLEW" "${COVERAGE_ARGS[@]}" assembleDebug assembleDebugAndroidTest

echo "Running unit tests..."
"$GRADLEW" "${COVERAGE_ARGS[@]}" testDebugUnitTest

if [[ "$MODE" == "connected" ]]; then
  echo "Running connected Android tests for the app module..."
  "$GRADLEW" "${COVERAGE_ARGS[@]}" :app:connectedDebugAndroidTest
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
