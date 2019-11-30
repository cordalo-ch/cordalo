#!/usr/bin/env bash
# deploy to OSS nexus
# configure your global radle.properties
# read https://github.com/cedricwalter/cicd-gradle-oss-nexus


./gradlew clean
rm -R `find . -name out`
rm -R `find . -name bin`
rm -R `find . -name logs | grep -v git`
./gradlew jar -x signArchives
#./gradlew --info check test
./gradlew check test
./gradlew assemble -x signArchives
./gradlew ploadArchives

#make a 3x sound
echo -en "\007"
echo -en "\007"
echo -en "\007"

