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

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouper ]; then
  /bin/mkdir /home/mchyzer/tmp/grouper
  /bin/chmod g+w /home/mchyzer/tmp/grouper
fi

cd /home/mchyzer/tmp/grouper

export buildDir=/home/mchyzer/tmp/grouper/build_$USER

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

/home/mchyzer/ant/bin/ant distPackage

/home/mchyzer/ant/bin/ant distBinary

mv $buildDir/grouper/dist/binary/*.tar.gz $buildDir/
mv $buildDir/grouper/dist/binary/*.zip $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
