#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. env.sh

get_nodes(){
	retval=$(netstat -an | egrep "10103|10106|10109|10112|10115" | grep LISTEN | wc -l)
}
get_webservers(){
        retval=$(netstat -an | egrep "10801|10802|10803|10804|10805" | grep LISTEN | wc -l)
}

get_nodes
nof=$retval

get_webservers
nof2=$retval
echo "Currently $nof CORDA nodes running"
echo "Currently $nof2 Webservers  running"
