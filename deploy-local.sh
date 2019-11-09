#!/usr/bin/env bash
# deploy to OSS nexus
# configure your global gradle.properties
# read https://github.com/cedricwalter/cicd-gradle-oss-nexus

./gradlew jar -x signArchives
./gradlew --info check test
./gradlew assemble -x signArchives
./gradlew uploadArchives
