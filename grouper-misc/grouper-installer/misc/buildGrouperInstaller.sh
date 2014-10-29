#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the tag to build as the command line argument!"
  echo "e.g. GROUPER_2_0_3, etc"
  echo "e.g. buildGrouperInstaller.sh GROUPER_2_0_3"
  echo
  exit 1
fi  
SOURCEDIR=$PWD/../../../

if [ ! -d $HOME/tmp/grouper-installer ]; then
  /bin/mkdir -p $HOME/tmp/grouper-installer
  /bin/chmod g+w $HOME/tmp/grouper-installer
fi

cd $HOME/tmp/grouper-installer

export buildDir=$HOME/tmp/grouper-installer/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-misc/grouper-installer
git clone -l $SOURCEDIR .
git checkout $1

cd $buildDir/grouper-misc/grouper-installer

ant distPackage

ant distBinary

#mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
