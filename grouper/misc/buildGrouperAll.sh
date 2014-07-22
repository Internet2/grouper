#!/bin/bash                                                                                                          
#
# Copyright 2012 Internet2
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

export oldJavaHome=$JAVA_HOME
export oldPath=$PATH
export JAVA_HOME=/opt/java6
export PATH=$JAVA_HOME/bin:$PATH

# don't build with mvn here anymore
# /home/mchyzer/bin/buildMorphStringMvn.sh $1
# /home/mchyzer/bin/buildSubjectMvn.sh $1
/home/mchyzer/bin/buildGrouperClient.sh $1
/home/mchyzer/bin/buildGrouper.sh $1
/home/mchyzer/bin/buildGrouperWs.sh $1
/home/mchyzer/bin/buildGrouperUi.sh $1
#/home/mchyzer/bin/buildGrouperQs.sh $1
/home/mchyzer/bin/buildGrouperInstaller.sh $1
# /home/mchyzer/bin/buildGrouperShibMvn.sh $1
# /home/mchyzer/bin/buildLdappcng.sh $1

if [ ! -d /home/mchyzer/tmp/grouperAll ]; then
  /bin/mkdir /home/mchyzer/tmp/grouperAll
  /bin/chmod g+w /home/mchyzer/tmp/grouperAll
fi

cd /home/mchyzer/tmp/grouperAll

export buildDir=/home/mchyzer/tmp/grouperAll/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

# no more maven building as of 2.1.0
# build ldappcng with maven
# /home/mchyzer/bin/buildMaven.sh $1
# cd /home/mchyzer/tmp/grouper/build_$USER/grouper
# ln -s /home/mchyzer/tmp/maven/build_$USER/ldappcng ../ldappcng_trunk
# $ANT_HOME/bin/ant ldappcng
# mv /home/mchyzer/tmp/grouper/build_$USER/grouper/dist/ldappcng/grouper.ldappcng-*.tar.gz /home/mchyzer/tmp/grouper/build_$USER

#lets move instead of copy so we dont waste space

mv -v /home/mchyzer/tmp/grouper/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouperClient/build_$USER/grouper*.tar.gz $buildDir
#mv -v /home/mchyzer/tmp/grouper-qs/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouperUi/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouperWs/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouper-installer/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouper-installer/build_$USER/grouper-installer/dist/grouperInstaller.jar $buildDir
# mv -v /home/mchyzer/tmp/maven/build_$USER/ldappcng/target/ldappc*.tar.gz $buildDir

#rename
cd $buildDir
 
# no more renaming ldappcng as of 2.1.0
# BINOLD=`ls ldappcng-*-bin.tar.gz`
# BINNEW=`echo $BINOLD | perl -ne 'chomp; s/-([\d.]+)-bin/\.binary-$1/; print $_;'`
# mv $BINOLD $BINNEW
 
# SRCOLD=`ls ldappcng-*-src.tar.gz`
# SRCNEW=`echo $SRCOLD | perl -ne 'chomp; s/-([\d.]+)-src/\.source-$1/; print $_;'`
# mv $SRCOLD $SRCNEW

cd /home/mchyzer/tmp/grouperAll

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir

echo
echo "Overall done! All packages have been moved to: $buildDir"
echo

export JAVA_HOME=$oldJavaHome
export PATH=$oldPath
export oldJavaHome=
export oldPath=
