#!/bin/bash

# Make sure problems cause this script to report failure
set -o errexit
set -o pipefail
set -o nounset

fail() {
  echo "$*" 1>&2
  exit 1
}

# Where this script physically lives (resolving symlinks, etc)
D=$(dirname $(readlink -f "$0"))
cd "$D"

T=$(mktemp -d)
trap '[ -n "$T" ] && rm -rf "${T}"' exit

TAR_OUTPUT_DIR=${TAR_OUTPUT_DIR:-/tmp}

# Get the full (aka effective) pom so we can search through it for 
# build properties (version, etc)
FULLPOM=$T/pspng.pom
echo "Getting our effective pom"
mvn -q help:effective-pom "-Doutput=$FULLPOM"

ARTIFACT=$(xpath -q -e 'project/artifactId/text()' $FULLPOM)
VERSION=$(xpath -q -e 'project/version/text()' $FULLPOM)
TYPE=$(xpath -q -e 'project/packaging/text()' $FULLPOM)
NAME=$(xpath -q -e 'project/name/text()' $FULLPOM)
[ -z "$TYPE" ] && TYPE=jar


echo "Ensuring that the right grouper.jar is installed in our local maven repository"
# Following recommendations to install local jars into maven repository
# http://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-in-maven-project
GROUPER_D=$D/../../grouper
GROUPER_JAR="$GROUPER_D/dist/binary/grouper.apiBinary-${VERSION}/dist/lib/grouper.jar"
[ ! -e "$GROUPER_JAR" ] && fail "grouper.jar not found: $GROUPER_JAR"
mvn -q install:install-file -Dfile=$GROUPER_JAR -DpomFile="$GROUPER_D/pom.xml"


# Time to build project
mvn -Dlicense.skip=true clean install

echo "Packaging ${ARTIFACT}.${TYPE} and its dependencies"
mkdir -p target/dist/lib
cp target/${ARTIFACT}-${VERSION}.${TYPE} target/dist/lib/${ARTIFACT}.${TYPE}

# Create a README file
cat <<EOF > target/dist/README.txt
$(date)
This is the binary distribution of version $VERSION of $NAME.

lib: contains $ARTIFACT.$TYPE and its dependencies that are not included with grouper itself

-- Project README --
$(cat README.txt)
EOF


mvn -q dependency:copy-dependencies
for f in target/dependency/*.jar; do
 j=$(basename "$f")
 if [ -r $GROUPER_D/dist/binary/grouper.apiBinary-${VERSION}/lib/grouper/$j ]; then 
   echo dependency packaged with grouper: $j 
 else 
   echo packaging dependency with $ARTIFACT: $j
   cp $f target/dist/lib
 fi
done

TAR_FILE="${TAR_OUTPUT_DIR}/$ARTIFACT-$VERSION.tar.gz"
echo "Creating $TAR_FILE"
tar -czf "$TAR_FILE" -C target/dist .
