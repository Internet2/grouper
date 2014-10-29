#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_1_3_1, etc"
  echo "e.g. buildGrouper.sh HEAD"
  echo
  exit 1
fi  

if [ ! -d ../../.git ]; then
  echo "You must invoke this command from the grouper/misc directory"
  echo 
  exit 1
fi

if [ ! -d $HOME/tmp/grouper ]; then
  /bin/mkdir -p $HOME/tmp/grouper
  /bin/chmod g+w $HOME/tmp/grouper
fi

SOURCE_DIR=$PWD/../../

cd $HOME/tmp/grouper

export buildDir=$HOME/tmp/grouper/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir
git clone -l $SOURCE_DIR .

if [ $1 == 'master' ]; then
 git checkout master 
else
 git checkout $1 
fi

cd $buildDir/grouper

ant distPackage

ant distBinary

mv $buildDir/grouper/dist/binary/*.tar.gz $buildDir/

# $M2_HOME/bin/mvn install -DskipTests

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
