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

tarballName=grouper.all-"$version"
tmpDir=/tmp/"$tarballName"
tarball="$tmpDir".tar.gz

rm -rf "$tmpDir"
mkdir "$tmpDir"
rm -f $tarball
rm "$releaseDir"/"$tarballName".tar.gz

mkdir "$tmpDir"/downloads
cp -R "$grouperBase"/downloads/tools "$tmpDir"/downloads
mkdir "$tmpDir"/release
mkdir "$tmpDir"/release/"$version"
cp "$releaseDir"/*.tar.gz "$tmpDir"/release/"$version"
cp "$releaseDir"/*.jar "$tmpDir"/release/"$version"
cp "$releaseDir"/quickstart.xml "$tmpDir"/release/"$version"
cp "$releaseDir"/subjects.sql "$tmpDir"/release/"$version"

mkdir "$tmpDir"/release/"$version"/patches
cp -R "$releaseDir"/patches/*.tar.gz "$tmpDir"/release/"$version"/patches

cd /tmp

tar czf "$tarball" "$tarballName"

mv "$tarball" "$releaseDir"

rm -rf "$tmpDir"

echo "tarball is $releaseDir/$tarballName".tar.gz