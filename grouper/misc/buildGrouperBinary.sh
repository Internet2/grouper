#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_1_3_1, etc"
  echo "e.g. buildGrouperBinary.sh HEAD"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /tmp/grouper ]; then
  /bin/mkdir /tmp/grouper
  /bin/chmod g+w /tmp/grouper
fi

cd /tmp/grouper

export buildDir=/tmp/grouper/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

export CVSROOT=/home/cvs/i2mi

/usr/bin/cvs export -r $1 grouper

cd $buildDir/grouper

/home/mchyzer/ant/bin/ant distBinary

echo
echo "result is in $buildDir/grouper/dist/binary/" 
echo