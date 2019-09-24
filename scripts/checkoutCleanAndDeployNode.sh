#!/bin/bash
. env.sh
cd $CORDA_HOME
rm -rf ./build ./out
git pull
./gradlew clean deployNodes
cd ~
