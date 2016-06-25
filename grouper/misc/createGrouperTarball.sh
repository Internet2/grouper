#!/bin/bash

if [[ $# -ne "1" ]]; then
  echo
  echo "ERROR: Give the version e.g. 2.3.0"
  echo
  exit 1
fi

grouperBase=/home/htdocs/software.internet2.edu/grouper

version=$1

releaseDir="$grouperBase"/release/"$version"

if [ ! -d "$releaseDir" ]; then

  echo
  echo "ERROR: release dir does not exist: $releaseDir"
  echo
  exit 1

fi

tarballName=grouperAll_"$version"
tmpDir=/tmp/"$tarballName"
tarball="$tmpDir".tgz

rm -rf "$tmpDir"
mkdir "$tmpDir"
rm -f $tarball
rm "$releaseDir"/"$tarballName".tgz

mkdir "$tmpDir"/downloads
cp -R "$grouperBase"/downloads/tools "$tmpDir"/downloads
mkdir "$tmpDir"/release
cp -R "$releaseDir" "$tmpDir"/release

cd /tmp

tar czf "$tarball" "$tarballName"

mv "$tarball" "$releaseDir"

rm -rf "$tmpDir"

echo "tarball is $releaseDir/$tarballName".tgz