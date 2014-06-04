#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. trunk, GROUPER_WS_1_4_0, etc"
  echo "e.g. buildGrouperWs.sh HEAD"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouperWs ]; then
  /bin/mkdir /home/mchyzer/tmp/grouperWs
  /bin/chmod g+w /home/mchyzer/tmp/grouperWs
fi

cd /home/mchyzer/tmp/grouperWs

export buildDir=/home/mchyzer/tmp/grouperWs/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-ws

if [ $1 == 'trunk' ]; then
  /usr/bin/svn export https://svn.internet2.edu/svn/i2mi/trunk/grouper-ws/
else
  /usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-ws/
fi



cd $buildDir/grouper-ws/grouper-ws

$ANT_HOME/bin/ant distPackage

echo
echo "result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir