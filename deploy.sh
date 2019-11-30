 #!/usr/bin/env bash

 ## only upload archives for "master" so far - must be adapted later if possible for all branches
 ## you have to export the key again
 if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  openssl aes-256-cbc -K $encrypted_fca62744a4cd_key -iv $encrypted_fca62744a4cd_iv -in secret-ring.gpg.enc -out secret-ring.gpg -d
  ./gradlew uploadArchives -PossrhUsername=${SONATYPE_USERNAME} -PossrhPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=`pwd`/secret-ring.gpg
  rm `pwd`/secret-ring.gpg
 fi
