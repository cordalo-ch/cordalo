#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

stopped=
checkPorts(){
	port=$1
	name=$2
	nof=$(netstat -an | grep $port.*LISTEN | wc -l)
	if [ $nof -eq 0 ];  then
		echo "kill Java Nodes for $name"
		cd $BASEDIR
		echo "$(eval `./_killNode.sh $name`)"
		stopped="${stopped}${name}"
	fi
}

checkPorts 10103 $NodeName0
checkPorts 10106 $NodeName1
checkPorts 10109 $NodeName2
checkPorts 10112 $NodeName3
checkPorts 10115 $NodeName4

retval=${stopped}

