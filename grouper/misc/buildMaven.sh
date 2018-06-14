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

OBJ=maven

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

/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-parent
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/subject
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/morphString
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper-misc/grouperClient
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/grouper
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/ldappcng/grouper-shib
/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/ldappcng/ldappcng

cd grouper-parent

$M2_HOME/bin/mvn package -DskipTests

echo
echo "done"
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir