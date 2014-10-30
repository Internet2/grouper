#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the tag to build as the command line argument!"
  echo "e.g. GROUPER_1_5_1, etc"
  echo "e.g. buildGrouperClient.sh GROUPER_1_5_1"
  echo
  exit 1
fi  

SOURCEDIR=$PWD/../../../

if [ ! -d $HOME/tmp/grouperClient ]; then
  /bin/mkdir -p $HOME/tmp/grouperClient
  /bin/chmod g+w $HOME/tmp/grouperClient
fi

cd $HOME/tmp/grouperClient

export buildDir=$HOME/tmp/grouperClient/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-misc/grouperClient
git clone -l $SOURCEDIR .
git checkout $1


cd $buildDir/grouper-misc/grouperClient

ant distPackage

ant distBinary

#mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
