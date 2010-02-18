#!/bin/bash                                                                                                         \
                                                                                                                     

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
/home/mchyzer/bin/buildGrouperUi.sh $1
/home/mchyzer/bin/buildGrouperQs.sh $1
/home/mchyzer/bin/buildGrouperClient.sh $1

echo
echo "Overall done!"
echo



