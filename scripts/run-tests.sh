#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
./gradlew --no-daemon :app:testDebugUnitTest --stacktrace
