#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh
cd $CORDA_HOME
max_nof=4
echo "Starting max $max_nof Webserver to CORDA"
echo "----------------------------------------"

get_sshd(){
	retval=$(netstat -an | grep 1080 | grep LISTEN | wc -l)
}

wait_until_first_started(){
        get_sshd
        nof=$retval
	x=0
        echo "searching for 1st webserver to be started ... stop ctrl-c any time - $nof"
        while [ "$nof" -eq 0 ]
        do
                get_sshd
                nof=$retval
		x=$(( $x + 10 ))
                echo "slept $x s until first started...."
                sleep 10s
        done
        echo "$nof started"
        retval=$nof
}
wait_until_all_started() {
        wait_until_first_started
        nof=$retval
        x=0
        while [ "$nof" -lt "$max_nof" ] && [ "$x" -lt 80 ]
        do
                get_sshd
                nof=$retval
                echo "slept $x s to see if all started... $nof"
                sleep 10s
                x=$(( $x + 10 ))
        done
        echo "$nof started"
        retval=$nof
}

get_sshd
if [ "$retval" -eq $max_nof ]; then
	echo "all started"
	exit 0
fi

nohup $CORDA_HOME/gradlew runWebserver1 >> nohup-runWebserver1.out 2>/dev/null &
nohup $CORDA_HOME/gradlew runWebserver2 >> nohup-runWebserver2.out 2>/dev/null &
nohup $CORDA_HOME/gradlew runWebserver3 >> nohup-runWebserver3.out 2>/dev/null &
nohup $CORDA_HOME/gradlew runWebserver4 >> nohup-runWebserver4.out 2>/dev/null &

echo "Wait 20s to spin up first log files"
sleep 20s
wait_until_all_started
if [ "$retval" -lt "$max_nof" ]; then
	echo "kill all servers again"
	cd ..
	$BASEDIR/stopServers.sh
  echo "start servers again"
  $BASEDIR/startServers.sh
fi
