#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. env.sh

cd $BASEDIR
$BASEDIR/startNodes.sh
$BASEDIR/startServers.sh

echo "---------------------------------------"
echo "CORDA and Webservers are UP and running"
echo "---------------------------------------"
$BASEDIR/status.sh

