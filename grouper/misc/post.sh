#!/bin/bash

cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-master/grouper-misc/grouper-pspng/


pspTag=2.3.0
GROUPER_D="../../grouper"

TARGET="target/grouper-pspng-$pspTag"
rm -rf "$TARGET"

mkdir -p "$TARGET/dist"
mkdir -p "$TARGET/lib/custom"

cp -p target/grouper-pspng-"$pspTag".jar "$TARGET/dist"
cp -rp src "$TARGET/"
cp -p pom.xml "$TARGET/"
cp -p README.txt "$TARGET/"


# We want to only tar up jars that are not included in the grouper project itself

# Run copy-dependency in pspng
mvn dependency:copy-dependencies

#Run copy-dependencies in grouper
(cd "$GROUPER_D"; mvn dependency:copy-dependencies)


for f in target/dependency/*.jar; do
  f_base=$(basename "$f")
  if [ -e "$GROUPER_D/target/dependency/$f_base" -o -e "$GROUPER_D/target/$f_base" ]; then
    echo "File did exist in grouper project: $f"
  else
    echo "File did not exist in grouper project: $f"
    cp -p $f "$TARGET/lib/custom"
  fi
done

tar -czvf "target/grouper-pspng-$pspTag.tar.gz" -C target "grouper-pspng-$pspTag"
