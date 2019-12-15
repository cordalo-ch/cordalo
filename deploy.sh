 #!/usr/bin/env bash

 ## only upload archives for "master" so far - must be adapted later if possible for all branches
 ## you have to export the key again
 ## additionally for the sign of archives the gradle.properties entry 'signingKey' is used because the content of secret cannot be passed via command line
 ## remove check for master to allow branches to build snapshots
 ##if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
 if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  export cordalo_new_snapshot=`curl -s https://oss.sonatype.org/service/local/repositories/snapshots/content/ch/cordalo/cordalo/maven-metadata.xml | grep "version\>" | tail -n 1 | awk -F ">" '{print $2}'| awk -F "<" '{print $1}' | awk -F "-" '{print $1}' | awk -F "." '{print $1"."(($2)+1)"-SNAPSHOT"}'`
  openssl aes-256-cbc -K $encrypted_fca62744a4cd_key -iv $encrypted_fca62744a4cd_iv -in secret-ring.gpg.enc -out secret-ring.gpg -d
  DIR=`pwd`
  cat ${DIR}/secret-ring.gpg | awk 'NR == 1 { print "signingKey=" } 1' ORS='\\n' > gradle.properties
  ./gradlew uploadArchives -PossrhUsername=${SONATYPE_USERNAME} -PossrhPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=${DIR}/secret-ring.gpg
  rm ${DIR}/secret-ring.gpg
  rm ${DIR}/gradle.properties
  SIGN_KEY=none
 fi
