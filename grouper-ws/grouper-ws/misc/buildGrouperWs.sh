#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. trunk, GROUPER_WS_1_4_0, etc"
  echo "e.g. buildGrouperWs.sh master"
  echo
  exit 1
fi  

SOURCE_DIR=$PWD/../../../

if [ ! -d $HOME/tmp/grouperWs ]; then
  /bin/mkdir -p $HOME/tmp/grouperWs
  /bin/chmod g+w $HOME/tmp/grouperWs
fi

cd $HOME/tmp/grouperWs

export buildDir=$HOME/tmp/grouperWs/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi


cd $buildDir
git clone -l $SOURCE_DIR .

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-ws

if [ $1 == 'master' ]; then
  git checkout master
else
  git checkout $1
fi

cd $buildDir/grouper-ws/grouper-ws

ant distPackage

echo
echo "result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
