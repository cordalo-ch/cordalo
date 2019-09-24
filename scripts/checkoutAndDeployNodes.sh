#!/bin/bash
. env.sh
cd $CORDA_HOME
git pull
./gradlew deployNodes
cd ~
