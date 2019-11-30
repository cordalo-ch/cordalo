 #!/usr/bin/env bash

 ## only upload archives for "master" so far - must be adapted later if possible for all branches
 if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  ./gradlew uploadArchives -PossrhUsername=${SONATYPE_USERNAME} -PossrhPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=`pwd`/secret-ring.gpg
 fi
