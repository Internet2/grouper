#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_WS_1_4_0_RC2, etc"
  echo "e.g. buildGrouperWs.sh HEAD"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /tmp/grouperWs ]; then
  /bin/mkdir /tmp/grouperWs
  /bin/chmod g+w /tmp/grouperWs
fi

cd /tmp/grouperWs

export buildDir=/tmp/grouperWs/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

export CVSROOT=/home/cvs/i2mi

/usr/bin/cvs export -r $1 grouper-ws

cd $buildDir/grouper-ws/grouper-ws

/home/mchyzer/ant/bin/ant distPackage

echo
echo "result is in $buildDir/" 
echo