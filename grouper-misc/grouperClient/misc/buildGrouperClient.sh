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
if [ ! -d /home/mchyzer/tmp/grouperClient ]; then
  /bin/mkdir /home/mchyzer/tmp/grouperClient
  /bin/chmod g+w /home/mchyzer/tmp/grouperClient
fi

cd /home/mchyzer/tmp/grouperClient

export buildDir=/home/mchyzer/tmp/grouperClient/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

export CVSROOT=/home/cvs/i2mi

/usr/bin/cvs export -r $1 grouper-misc/grouperClient

cd $buildDir/grouper-misc/grouperClient

/home/mchyzer/ant/bin/ant distPackage

/home/mchyzer/ant/bin/ant distBinary

mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
