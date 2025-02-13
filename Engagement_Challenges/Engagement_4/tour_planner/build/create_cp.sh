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
JAR_STAGING="$BUILD"/jar-staging

info "SOURCE=$SOURCE"
info "STAGING=$STAGING"

info "creating/cleaning staging directories"

rm -rf "$STAGING"
mkdir "$STAGING"

rm -rf "$JAR_STAGING"
mkdir "$JAR_STAGING"

info "building source"
(
    cd "$SOURCE" &&
        mvn install assembly:single -DskipTests=true --projects core,tour,web
) || die "build failed"

info "creating jars"

info "  extracting original jar"
ORIG_JAR="$SOURCE"/web/target/graphhopper-web-0.5.0-with-dep.jar
(cd "$JAR_STAGING" && jar xf "$ORIG_JAR") || die "jar extraction failed"

info "  creating challenge jar"
cat >"$JAR_STAGING"/META-INF/MANIFEST.MF <<EOF
Main-Class: com.graphhopper.http.GHServer
Class-Path: deps.jar
EOF
CHALLENGE_JAR="$STAGING/challenge.jar"
(
    cd "$JAR_STAGING" &&
        jar cmf META-INF/MANIFEST.MF "$CHALLENGE_JAR" com/graphhopper log4j.xml
) || die "jar creation failed"

info "  creating deps jar"
DEPS_JAR="$STAGING/deps.jar"
(
    cd "$JAR_STAGING" &&
        jar cf "$DEPS_JAR" com/google gnu javax org
) || die "jar creation failed"

rm -rf "$JAR_STAGING"
