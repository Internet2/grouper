BRANCH=2.3.0
BRANCH_DIR=2_3

cd $(dirname "${BASH_SOURCE[0]}")
D=$(pwd -P)

[ -d m2 ] && rm -rf m2/repository/edu/internet2/middleware/
[ ! -d m2 ] && mkdir m2
export MAVEN_OPTS=-Dmaven.repo.local=$D/m2

echo "PSPNG Patches take two runs of this script to create."
sleep 2
echo ""

if [ ! -d work ]; then
  mkdir work
else
  echo "Work directory exists"
  echo "You should keep the work directory only when you're in the second pass or otherwise iterating on the same patch"
  read -p "Do you want to resuse the existing temporary work/ directory [y/N]" ans
  if [[ "$ans" =~ [yY].* ]]; then
    # just remove some basics
    echo "Removing selective files"
    rm -rf work/GROUPER_2_3_BRANCH*
    rm -rf work/grouper_v2_3_0_pspng_patch_*
  else
    echo "Deleting and recreating temporary work directory"
    rm -rf work
    mkdir work
  fi
fi

export M2_HOME=$(pwd -P)/work/apache-maven-3.2.5/
export JAVA_HOME=/home/mchyzer/software/java
PATH=$M3_HOME/bin:$JAVA_HOME/bin:$PATH

# Install grouper.jar if it exists
# work/GROUPER_2_3_BRANCH/grouper-GROUPER_2_3_BRANCH/grouper/dist/lib/grouper.jar
ARTIFACT=grouper
FILE=work/GROUPER_${BRANCH_DIR}_BRANCH/grouper-GROUPER_${BRANCH_DIR}_BRANCH/grouper/dist/lib/$ARTIFACT.jar
if [ -r "$FILE" ]; then
  echo Installing $ARTIFACT.jar to HOME maven repository
  mvn -q install:install-file \
        "-Dfile=$FILE" \
        -DgroupId=edu.internet2.middleware.grouper \
        -Dversion=${BRANCH}-SNAPSHOT \
        -DartifactId=$ARTIFACT \
        -Dpackaging=jar
fi

# Install grouperClient.jar if it exists
# work/GROUPER_2_3_BRANCH/grouper-GROUPER_2_3_BRANCH/grouper/lib/grouper/grouperClient.jar
ARTIFACT=grouperClient
FILE=work/GROUPER_${BRANCH_DIR}_BRANCH/grouper-GROUPER_${BRANCH_DIR}_BRANCH/grouper/lib/grouper/$ARTIFACT.jar
if [ -r "$FILE" ]; then
  echo Installing $ARTIFACT.jar to HOME maven repository
  mvn -q install:install-file \
        "-Dfile=$FILE" \
        -DgroupId=edu.internet2.middleware.grouper \
        -Dversion=${BRANCH}-SNAPSHOT \
        -DartifactId=$ARTIFACT \
        -Dpackaging=jar
fi

wget http://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar

echo "Running grouper installer. Use 'work' as the temporary directory...."
java -jar grouperInstaller.jar
