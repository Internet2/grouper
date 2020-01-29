#!/usr/bin/env bash

if [ -z "$TRAVIS_COMMIT_RANGE" ]; then
  echo "No commit range for this build (missing TRAVIS_COMMIT_RANGE) -- skipping tests" >&2
  exit 1
fi

# Note, prettier version of emails is --format='%an <%ae>'
# apt install mailx

COMMITTER_EMAILS=$(git --no-pager show -s --format=%ae $TRAVIS_COMMIT_RANGE | sort -u)

if [ -z "$COMMITTER_EMAILS" ]; then
  echo "No committer emails found for commit range $TRAVIS_COMMIT_RANGE -- skipping tests" >&2
  exit 1
fi


# grep -q returns 0 if found and 1 if not, so flag is the opposite of expected
echo "Checking for [skip tests]"
git --no-pager show -s --format="%s %b" $TRAVIS_COMMIT_RANGE | grep -q '\[skip tests\]'
exit_code=$?
if [ $exit_code -eq 0 ]; then
  echo "Found '[skip tests]' within $TRAVIS_COMMIT_RANGE -- skipping tests" >&2
  exit 1
fi

#BASEDIR=$TRAVIS_BUILD_DIR
BASEDIR=.
LOGFILE=$(mktemp)

# Start up postgres container compatible with the travis confForTest hibernate login
echo "Starting Docker database container"
docker run -d -e "POSTGRES_USER=grouper" -e "POSTGRES_PASSWORD=password" -e "POSTGRES_DB=grouper" -p 5432:5432  postgres
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "docker database container startup failed (exit $exit_code)" >&2
  exit 1
fi
echo "Sleeping for 10 seconds to let db start"
sleep 10


# Set up Maven environment. Install artifacts locally so further usage can be built per package
echo "Installing Maven settings"
if [ ! -f $BASEDIR/travis/mvn.settings.xml ]; then
    cp -p $BASEDIR/travis/mvn.settings.xml $HOME/.m2/settings.xml
fi


# Build (all projects) and copy dependencies for grouper subproject, so all can be run from the target/ directory
echo "Building Maven projects"
mvn -f $BASEDIR/grouper-parent clean package install
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven build failed (exit $exit_code)" >&2
  exit 1
fi

echo "Downloading Grouper api dependencies"
mvn -f $BASEDIR/grouper dependency:copy-dependencies
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven dependency:copy-dependencies failed (exit $exit_code)" >&2
  exit 1
fi


# Set up the classpath to use the newly created artifacts

CP="$BASEDIR/travis/confForTest"
CP="$CP":$(compgen -G "$BASEDIR/grouper/target/grouper-[0-9].[0-9].[0-9]*.jar" | grep -v -- '-sources.jar' | tr '\n' ':' | sed -e 's/::/:/')
CP="$CP":"$BASEDIR/grouper/target/dependency/*"
CP="$CP":"$BASEDIR/grouper/conf"
#  RESULT:
#./travis/confForTest
#./grouper/target/grouper-2.5.0-SNAPSHOT-tests.jar
#./grouper/target/grouper-2.5.0-SNAPSHOT.jar
#./grouper/target/dependency/*
#./grouper/conf

# Init the grouper database
echo "Initializing the Grouper database"
chmod u+x $BASEDIR/grouper/bin/gsh.sh && CLASSPATH="$CP" GROUPER_CONF="$BASEDIR/travis/confForTest" $BASEDIR/grouper/bin/gsh.sh -registry -runscript -noprompt

exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Failed to init the database (exit $exit_code)" >&2
  exit 1
fi


# Run the tests

for var in PWD TRAVIS_COMMIT_RANGE COMMITTER_EMAILS BASEDIR LOGFILE CP; do
  echo "$var = ${!var}"
done

echo $(date) " | START"
#echo $(date) " | START" > $LOGFILE

# make sure this runs with Java 8
which java #TODO debugging
JAVA=java

echo "Executing edu.internet2.middleware.grouper.AllTests"
$JAVA -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -XX:MaxPermSize=300m -Xms80m -Xmx640m \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt
#  -all -noprompt \
#  >> $LOGFILE 2>&1

exit_code=$?

echo $(date) " | END (exit code $exit_code)"
#echo $(date) " | END (exit code $exit_code)" >> $LOGFILE


# Travis probably isn't allowing mail. Just output to stdout instead
#GROUPER_ATTACH=
#if [ -n $BASEDIR/grouper/logs/grouper_error.log]; then
#  GROUPER_ATTACH="-a $BASEDIR/grouper/logs/grouper_error.log"
#fi
#
#echo "Travis completed tests" | echo mailx -s "Travis test results" -a $LOGFILE -a $GROUPER_ATTACH $COMMITTER_EMAILS

#cat $LOGFILE

# Exit with the result from the test run
exit $exit_code
