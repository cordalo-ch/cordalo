#!/usr/bin/env bash
# deploy to OSS nexus
# configure your global radle.properties
# read https://github.com/cedricwalter/cicd-gradle-oss-nexus

# precondition to run deploy locally is a gradle.properties with the following entries
# place the gradle.properties into $HOME/.gradle/gradle.properties
# and NEVER on a github repo
#   ossrhUsername=cordalo.ch
#   ossrhPassword=...
#   signingKeyId=5FDC...
#   signingPassword=...


# generate the signingKey into the gradle properties while running gpg --export-secret-keys
# \n must be replaced
# gpg --armor --export-secret-keys 5FDC... | awk 'NR == 1 { print "signingKey=" } 1' ORS='\\n' >> gradle.properties

export cordalo_new_snapshot=`curl -s https://oss.sonatype.org/service/local/repositories/snapshots/content/ch/cordalo/cordalo/maven-metadata.xml | grep version\> | tail -n 1 | awk -F ">" '{print $2}'| awk -F "<" '{print $1}' | awk -F "-" '{print $1}' | awk -F "." '{print $1"."(($2)+1)"-SNAPSHOT"}'`
echo "build and upload $cordalo_new_snapshot"
./gradlew clean
rm -R `find . -name out`
rm -R `find . -name bin`
rm -R `find . -name logs | grep -v git`
./gradlew jar -x signArchives
#./gradlew --info check test
./gradlew check test
./gradlew assemble -x signArchives
./gradlew uploadArchives

#make a 3x sound
echo -en "\007"
echo -en "\007"
echo -en "\007"

