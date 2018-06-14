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

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-misc/grouperClient

#if [ $1 == 'trunk' ]; then
#  /usr/bin/svn export https://svn.internet2.edu/svn/i2mi/trunk/grouper-misc/grouperClient/
#else
#  /usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/grouperClient/
#fi

cp -R /home/mchyzer/tmp/grouperDownload/build_$USER/grouper-$1/grouper-misc/grouperClient $buildDir


cd $buildDir/grouperClient

$ANT_HOME/bin/ant distPackage

$ANT_HOME/bin/ant distBinary

# $M2_HOME/bin/mvn install -DskipTests

#mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
