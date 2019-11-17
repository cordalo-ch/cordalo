#!/usr/bin/env bash
# deploy to OSS nexus
# configure your global radle.properties
# read https://github.com/cedricwalter/cicd-gradle-oss-nexus

./gradlew clean
./gradlew jar -x signArchives
#./gradlew --info check test
./gradlew check test
./gradlew assemble -x signArchives
./gradlew uploadArchives
