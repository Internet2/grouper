#!/bin/bash

if [ $# -ne "3" ]
then
  echo
  echo "Give the dir to remove from, and the dir which has common dependencies and the dir to copy to"
  echo "e.g. buildRemoveCommonDependencies.sh /tmp/module /tmp/grouper /tmp/copyTo"
  echo
  exit 1
fi

dirToLookIn=$1
dirWithCommonDependencies=$2
dirToCopyTo=$3

for f in $dirToLookIn/*.jar; do
  f_base=$(basename "$f")
  if [ -e "$dirWithCommonDependencies/$f_base" -o -e "$dirWithCommonDependencies/$f_base" ]; then
    echo "File did exist in common dependency dir: $f"
  else
    echo "File did not exist in common dependency dir: $f"
    cp -p $f "$dirToCopyTo"
  fi
done