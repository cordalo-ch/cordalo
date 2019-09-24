#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. env.sh

cd $BASEDIR
nof=`$BASEDIR/killNodes.sh | wc -l`
if [ "$nof" -gt 0 ]; then
	echo "Nodes killed: $nof (wait 5s) $(eval `./killNodes.sh`)"
	sleep 5s
fi
cd $BASEDIR
