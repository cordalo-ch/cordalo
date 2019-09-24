#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

cd $BASEDIR
nof=`$BASEDIR/killServers.sh | wc -l`
if [ "$nof" -gt 0 ]; then
	echo "servers killed: $nof (sleep 5s) $(eval `$BASEDIR/killServers.sh`)"
	sleep 5s
fi
cd $BASEDIR
