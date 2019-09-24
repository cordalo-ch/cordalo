#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. env.sh

cd $BASEDIR
$BASEDIR/stopForceAll.sh
cd $CORDA_HOME
rm -Rf `find . -type d -name build -o -name out -o -name logs`

cd $BASEDIR
$BASEDIR/checkoutCleanAndDeployNode.sh
$BASEDIR/startAll.sh

echo "---------------------------------------"
echo "CORDA and Webservers are UP and running"
echo "---------------------------------------"
$BASEDIR/status.sh

