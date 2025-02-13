#!/bin/sh

PROG=$(basename "$0")

die() {
    echo "$PROG: $1" >&2
    exit 1
}

info() {
    echo "$PROG: $1" >&2
}

set -e

BUILD=$(cd "$(dirname "$0")" && pwd)
SOURCE=$(cd "$BUILD"/../source && pwd)
STAGING="$BUILD"/challenge_program

info "SOURCE=$SOURCE"
info "STAGING=$STAGING"

info "creating/cleaning staging directories"

rm -rf "$STAGING"
mkdir "$STAGING"

info "building source"
(
    cd "$SOURCE" && mvn clean package dependency:copy-dependencies 
) || die "build failed"

info "copying files"

CHALLENGE_JAR="$SOURCE"/target/matrixmultiply-0.0.1-SNAPSHOT.jar
GSON_JAR="$SOURCE"/target/dependency/gson-2.4.jar
NNHTTP_JAR="$SOURCE"/target/dependency/nanohttpd-2.2.0.jar

cp "$CHALLENGE_JAR" "$STAGING/linalgservice.jar"
cp "$GSON_JAR" "$STAGING"
cp "$NNHTTP_JAR" "$STAGING"


