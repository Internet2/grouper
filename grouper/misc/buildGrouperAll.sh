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

export oldJavaHome=$JAVA_HOME
export oldPath=$PATH
export JAVA_HOME=/opt/java6
export PATH=$JAVA_HOME/bin:$PATH

# don't build with mvn here anymore
# $HOME/bin/buildMorphStringMvn.sh $1
# $HOME/bin/buildSubjectMvn.sh $1
../../grouper-misc/grouperClient/misc/buildGrouperClient.sh $1
./buildGrouper.sh $1
<<<<<<< GROUPER_2_3_BRANCH
../../grouper-ws/misc/buildGrouperWs.sh $1
=======
./buildGrouperWs.sh $1
>>>>>>> 218ea01 fix path in build script
../../grouper-ui/misc/buildGrouperUi.sh $1
#$HOME/bin/buildGrouperQs.sh $1
../../grouper-misc/grouper-installer/misc/buildGrouperInstaller.sh $1
# $HOME/bin/buildGrouperShibMvn.sh $1
# $HOME/bin/buildLdappcng.sh $1

if [ ! -d $HOME/tmp/grouperAll ]; then
  /bin/mkdir $HOME/tmp/grouperAll
  /bin/chmod g+w $HOME/tmp/grouperAll
fi

cd $HOME/tmp/grouperAll

export buildDir=$HOME/tmp/grouperAll/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir -p $buildDir
fi

# no more maven building as of 2.1.0
# build ldappcng with maven
# $HOME/bin/buildMaven.sh $1
# cd $HOME/tmp/grouper/build_$USER/grouper
# ln -s $HOME/tmp/maven/build_$USER/ldappcng ../ldappcng_trunk
# $ANT_HOME/bin/ant ldappcng
# mv $HOME/tmp/grouper/build_$USER/grouper/dist/ldappcng/grouper.ldappcng-*.tar.gz $HOME/tmp/grouper/build_$USER

#lets move instead of copy so we dont waste space

mv -v $HOME/tmp/grouper/build_$USER/grouper*.tar.gz $buildDir
mv -v $HOME/tmp/grouperClient/build_$USER/grouper*.tar.gz $buildDir
#mv -v $HOME/tmp/grouper-qs/build_$USER/grouper*.tar.gz $buildDir
mv -v $HOME/tmp/grouperUi/build_$USER/grouper*.tar.gz $buildDir
mv -v $HOME/tmp/grouperWs/build_$USER/grouper*.tar.gz $buildDir
mv -v $HOME/tmp/grouper-installer/build_$USER/grouper*.tar.gz $buildDir
mv -v $HOME/tmp/grouper-installer/build_$USER/grouper-installer/dist/grouperInstaller.jar $buildDir
# mv -v $HOME/tmp/maven/build_$USER/ldappcng/target/ldappc*.tar.gz $buildDir

#rename
cd $buildDir
 
# no more renaming ldappcng as of 2.1.0
# BINOLD=`ls ldappcng-*-bin.tar.gz`
# BINNEW=`echo $BINOLD | perl -ne 'chomp; s/-([\d.]+)-bin/\.binary-$1/; print $_;'`
# mv $BINOLD $BINNEW
 
# SRCOLD=`ls ldappcng-*-src.tar.gz`
# SRCNEW=`echo $SRCOLD | perl -ne 'chomp; s/-([\d.]+)-src/\.source-$1/; print $_;'`
# mv $SRCOLD $SRCNEW

cd $HOME/tmp/grouperAll

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir

echo
echo "Overall done! All packages have been moved to: $buildDir"
echo

export JAVA_HOME=$oldJavaHome
export PATH=$oldPath
export oldJavaHome=
export oldPath=
