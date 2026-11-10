#!/bin/sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
SRC="$ROOT/app/src/main/java/com/campusshare/app"
KOTLIN_HOME="${KOTLIN_HOME:-$(dirname "$(dirname "$(command -v kotlinc)")")}"
COROUTINES_JAR="$KOTLIN_HOME/lib/kotlinx-coroutines-core-jvm.jar"
if [ ! -f "$COROUTINES_JAR" ]; then
  echo "Kotlin distribution must contain kotlinx-coroutines-core-jvm.jar" >&2
  exit 1
fi
mkdir -p "$ROOT/.local-check"
kotlinc "$SRC/domain/model"/*.kt \
    "$SRC/data/AuthRepository.kt" "$SRC/data/PostRepository.kt" \
    "$SRC/data/CategoryRepository.kt" "$SRC/data/FavoriteRepository.kt" \
    "$SRC/data/RequestRepository.kt" "$SRC/data/UserRepository.kt" \
    "$SRC/data/ContactShareRepository.kt" "$SRC/data/MediaRepository.kt" \
    "$SRC/data/FakeAuthRepository.kt" \
    "$SRC/data/fake"/*.kt "$ROOT/tools/offline_smoke.kt" \
    -cp "$COROUTINES_JAR" -include-runtime -d "$ROOT/.local-check/smoke.jar"
java -cp "$ROOT/.local-check/smoke.jar:$COROUTINES_JAR" Offline_smokeKt
