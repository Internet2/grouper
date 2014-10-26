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

SOURCE_DIR=$CWD/..

OBJ=morphString

cd /tmp
if [ ! -d /home/mchyzer/tmp/$OBJ ]; then
  /bin/mkdir /home/mchyzer/tmp/$OBJ
  /bin/chmod g+w /home/mchyzer/tmp/$OBJ
fi

cd /home/mchyzer/tmp/$OBJ

export buildDir=/home/mchyzer/tmp/$OBJ/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/morphString/

cd $buildDir/$OBJ

$M2_HOME/bin/mvn install -DskipTests

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
