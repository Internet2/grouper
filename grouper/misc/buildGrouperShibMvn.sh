#!/bin/bash
#
# Copyright 2014 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_1_3_1, etc"
  echo "e.g. buildGrouper.sh HEAD"
  echo
  exit 1
fi  

OBJ=grouper-shib

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

/usr/bin/svn export https://svn.internet2.edu/svn/i2mi/tags/$1/ldappcng/grouper-shib/

cd $buildDir/$OBJ

$M2_HOME/bin/mvn install -DskipTests

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir