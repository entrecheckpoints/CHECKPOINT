#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
./gradlew --no-daemon :app:assembleDebug --stacktrace
printf '\nAPK generado:\n%s\n' "$(pwd)/app/build/outputs/apk/debug/app-debug.apk"
