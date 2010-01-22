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
if [ ! -d /home/mchyzer/tmp/grouper-qs ]; then
  /bin/mkdir -p /home/mchyzer/tmp/grouper-qs
  /bin/chmod g+w /home/mchyzer/tmp/grouper-qs
fi

cd /home/mchyzer/tmp/grouper-qs

export buildDir=/home/mchyzer/tmp/grouper-qs/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

#export CVSROOT=/home/cvs/i2mi

#/usr/bin/cvs export -r $1 grouper
#/usr/bin/cvs export -r $1 grouper-ui
#/usr/bin/cvs export -r $1 grouper-qs-builder
#/usr/bin/cvs export -r $1 subject

/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper/
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-ui/
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-qs-builder/
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/subject/

cd $buildDir/grouper-qs-builder
cp build.properties.template build.properties

$ANT_HOME/bin/ant

echo
echo "QuickStart result is in $buildDir/"
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir