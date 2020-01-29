#!/bin/bash

if [ -z "$TRAVIS_COMMIT_RANGE" ]; then
  echo "No commit range for this build (missing TRAVIS_COMMIT_RANGE) -- skipping tests" >&2
  exit 1
fi

# Note, prettier version of emails is --format='%an <%ae>'
# apt install mailx
# mailx [-BDdEFintv~] [-s subject] [-a attachment ] [-c cc-addr] [-b bcc-addr] [-r from-addr] [-h hops] [-A account] [-S variable[=value]] to-addr . . .
# mailx [-BDdeEHiInNRv~] [-T name] [-A account] [-S variable[=value]] -f [name]
# mailx [-BDdeEinNRv~] [-A account] [-S variable[=value]] [-u user] 
# What should it be? mailx -s "Travis test results" -a $LOGFILE $COMMITTER_EMAILS

COMMITTER_EMAILS=$(git --no-pager show -s --format=%ae $TRAVIS_COMMIT_RANGE | sort -u)

if [ -z "$COMMITTER_EMAILS" ]; then
  echo "No committer emails found for commit range $TRAVIS_COMMIT_RANGE -- skipping tests" >&2
  exit 1
fi


# grep -q returns 0 if found and 1 if not, so flag is the opposite of expected
git --no-pager show -s --format="%s %b" $TRAVIS_COMMIT_RANGE | grep -q '\[skip tests\]'
if [ $? -eq 0 ]; then
  echo "Found '[skip tests]' within $TRAVIS_COMMIT_RANGE -- skipping tests" >&2
  exit 1
fi

#BASEDIR=$TRAVIS_BUILD_DIR
BASEDIR=.
LOGFILE=$(mktemp)

# Start up postgres container compatible with the travis confForTest hibernate login
docker run -d -e "POSTGRES_USER=grouper" -e "POSTGRES_PASSWORD=password" -e "POSTGRES_DB=grouper" -p 5432:5432  postgres


# Set up Maven environment. Install artifacts locally so further usage can be built per package
if [ ! -f $BASEDIR/travis/mvn.settings.xml ]; then
    cp -p $BASEDIR/travis/mvn.settings.xml $HOME/.m2/settings.xml
fi


# Build (all projects) and copy dependencies for grouper subproject, so all can be run from the target/ directory
mvn -f $BASEDIR/grouper-parent clean package install
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven build failed (exit $exit_code)" >&2
  exit 1
fi

mvn -f $BASEDIR/grouper dependency:copy-dependencies
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven dependency:copy-dependencies failed (exit $exit_code)" >&2
  exit 1
fi


# Set up the classpath to use the newly created artifacts

CP="$BASEDIR/travis/confForTest"
CP="$CP":$(echo $BASEDIR/grouper/target/grouper-*.jar | tr ' ' ':')
CP="$CP":"$BASEDIR/grouper/target/dependency/*"
CP="$CP":"$BASEDIR/grouper/conf"
#  RESULT:
#./travis/confForTest
#./grouper/target/grouper-2.5.0-SNAPSHOT-tests.jar
#./grouper/target/grouper-2.5.0-SNAPSHOT.jar
#./grouper/target/dependency/*
#./grouper/conf

# Init the grouper database
#   - even though everything gsh needs is in the classpath, gsh still complains if GROUPER_HOME/dist/lib/grouper.jar is missing
chmod u+x $BASEDIR/grouper/bin/gsh.sh && CLASSPATH="$CP" GROUPER_HOME=$BASEDIR/grouper $BASEDIR/grouper/bin/gsh.sh -registry -runscript -noprompt

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
which java #TODO debug
JAVA=java

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
