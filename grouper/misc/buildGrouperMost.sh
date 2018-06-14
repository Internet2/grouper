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

/home/mchyzer/bin/buildGrouper.sh $1
/home/mchyzer/bin/buildGrouperWs.sh $1

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

mv -v /home/mchyzer/tmp/grouper/build_$USER/grouper*.tar.gz $buildDir
mv -v /home/mchyzer/tmp/grouperWs/build_$USER/grouper*.tar.gz $buildDir

#rename
cd $buildDir
 
cd /home/mchyzer/tmp/grouperAll

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir

echo
echo "Overall done! All packages have been moved to: $buildDir"
echo