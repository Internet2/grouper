#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the tag to build as the command line argument!"
  echo "e.g. GROUPER_2_0_3, etc"
  echo "e.g. buildGrouperVoot.sh GROUPER_2_0_3"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouper-voot ]; then
  /bin/mkdir /home/mchyzer/tmp/grouper-voot
  /bin/chmod g+w /home/mchyzer/tmp/grouper-voot
fi

cd /home/mchyzer/tmp/grouper-voot

export buildDir=/home/mchyzer/tmp/grouper-voot/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper-misc/grouper-voot

#/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/grouper-voot/

cp -R /home/mchyzer/tmp/grouperDownload/build_$USER/grouper-$1/grouper-misc/grouper-voot $buildDir


cd $buildDir/grouper-voot

cp -v build.i2mi.properties build.properties

$ANT_HOME/bin/ant build

#mv $buildDir/grouper-misc/*.tar.gz $buildDir/

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir