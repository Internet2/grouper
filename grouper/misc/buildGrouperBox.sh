#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the tag to build as the command line argument!"
  echo "e.g. GROUPER_2_0_3, etc"
  echo "e.g. buildGrouperBox.sh GROUPER_2_0_3"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouper-box ]; then
  /bin/mkdir /home/mchyzer/tmp/grouper-box
  /bin/chmod g+w /home/mchyzer/tmp/grouper-box
fi

cd /home/mchyzer/tmp/grouper-box

export buildDir=/home/mchyzer/tmp/grouper-box/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-misc/grouper-box

#/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/grouper-box/

cp -R /home/mchyzer/tmp/grouperDownload/build_$USER/grouper-$1/grouper-misc/grouper-box $buildDir

cd $buildDir/grouper-box

pwd

cp -v build.i2mi.properties build.properties

$ANT_HOME/bin/ant build

#mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir