#!/bin/bash 


if [ $# -ne "3" ]
then
  echo
  echo "Give the PSP tag (e.g. 2.3.0), the grouper tag (e.g. GROUPER_2_3_0), and releaseToSonatype or noRelease!"
  echo "e.g. buildGrouperPsp.sh 2.3.0 GROUPER_2_3_0 noRelease"
  echo
  exit 1
fi

gitPspTag=$1
gitGrouperTag=$2
if [ $3 == 'releaseToSonatype' ]; then
  release=true
elif [ $3 == 'noRelease' ]; then
  release=false
else
  echo "third argument must be releaseToSonatype or noRelease!"
  exit 1
fi

export oldJavaHome=$JAVA_HOME
export oldPath=$PATH
export JAVA_HOME=/home/mchyzer/software/java
export PATH=$JAVA_HOME/bin:$PATH
export MAVEN_OPTS=-Xmx1024m

cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$gitPspTag"/grouper
#ARTIFACT=$(xpath "$FULLPOM" 'project/artifactId/text()')
ARTIFACT=$(xpath effective-pom.xml "/*[name()='project']/*[name()='artifactId']/text()")
#VERSION=$(xpath "$FULLPOM" 'project/version/text()')
VERSION=$(xpath effective-pom.xml "/*[name()='project']/*[name()='version']/text()")
#TYPE=$(xpath "$FULLPOM" 'project/packaging/text()')
TYPE=$(xpath effective-pom.xml "/*[name()='project']/*[name()='packaging']/text()")
#NAME=$(xpath "$FULLPOM" 'project/name/text()')
NAME=$(xpath effective-pom.xml "/*[name()='project']/*[name()='name']/text()")
[ -z "$TYPE" ] && TYPE=jar



cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-$VERSION/grouper-misc/grouper-pspng/


GROUPER_D="../../grouper"

PSPNG_STAGING="target/grouper.pspng-$VERSION"
rm -rf "$PSPNG_STAGING"

mkdir -p "$PSPNG_STAGING/dist"
mkdir -p "$PSPNG_STAGING/lib/custom"

cp -p target/grouper.pspng-"$VERSION".jar "$PSPNG_STAGING/dist"
cp -rp src "$PSPNG_STAGING/"
cp -p pom.xml "$PSPNG_STAGING/"
cp -p README.txt "$PSPNG_STAGING/"


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
    cp -p $f "$PSPNG_STAGING/lib/custom"
  fi
done

tar -czvf "target/grouper.pspng-$VERSION.tar.gz" -C target "grouper.pspng-$VERSION"


cp target/grouper.pspng-$VERSION.tar.gz $pspOutput
echo "PSPNG build to $pspOutput/grouper.pspng-$VERSION.tar.gz"