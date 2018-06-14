#!/bin/bash                                                                                                                         

if [ $# -ne "1" ]
then
  echo
  echo "Give the tag to build as the command line argument or master!"
  echo "e.g. GROUPER_1_5_1, etc"
  echo "e.g. buildGrouperClient.sh GROUPER_1_5_1"
  echo
  exit 1
fi

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouperDownload ]; then
  /bin/mkdir /home/mchyzer/tmp/grouperDownload
  /bin/chmod g+w /home/mchyzer/tmp/grouperDownload
fi

cd /home/mchyzer/tmp/grouperDownload

export buildDir=/home/mchyzer/tmp/grouperDownload/build_$USER

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

#https://github.com/Internet2/grouper/archive/master.zip

#https://github.com/Internet2/grouper/archive/GROUPER_2_2_1.zip

wget https://github.com/Internet2/grouper/archive/$1.zip 

# this comes down without a .zip on the end????

mv $1 $1.zip

echo "unzipping $1.zip..."

unzip -q $1.zip

# save space
rm $1.zip

echo "Grouper unzipped to $buildDir/grouper-$1" 