export CORDA_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"/..

export NodeName0=Notary
export NodeName1=Node1
export NodeName2=Node2
export NodeName3=Node3
export NodeName4=Node4


if [ "$NodeName1" -eq "Node1" ];
	echo "WARNING WARNING WARNING WARNING WARNING WARNING WARNING "
	echo "environment is set to default .... CHANGE"
	echo "WARNING WARNING WARNING WARNING WARNING WARNING WARNING "
	exit 1
fi