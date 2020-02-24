#!/usr/bin/env bash

# SETUP:
#    sudo mkdir -p /var/grouper-ci
#    sudo chgrp not-staff /var/grouper-ci
#    sudo chmod g+ws /var/grouper-ci
#    
#    GROUPER_HOME=/var/grouper-ci/git/grouper
#    mkdir -p $GROUPER_HOME
#    chmod g+ws $GROUPER_HOME
#    GROUPER_LOGDIR=/var/grouper-ci/log
#    mkdir -p $GROUPER_LOGDIR
#    chmod g+ws $GROUPER_LOGDIR
#
#    cd $GROUPER_HOME/..
#    git clone --depth 30 https://github.com/Internet2/grouper.git

GROUPER_HOME=/var/grouper-ci/git/grouper
GROUPER_LOGDIR=/var/grouper-ci/log
LOGFILE=$GROUPER_LOGDIR/grouper-ci-test-$(date +%Y%m%d_%H%M%S)-$$.log

MVN=/opt/apache-maven-3.6.3/bin/mvn

echo $(date) "CI test started" > $LOGFILE
echo '----------------------' >>$LOGFILE

if [ ! -d "$GROUPER_HOME" ]; then
  echo "Grouper home '$GROUPER_HOME' missing -- skipping tests" >>$LOGFILE
  exit 1
fi

cd $GROUPER_HOME
git checkout master >>$LOGFILE 2>&1

PULL_OUTFILE=$(mktemp --tmpdir=$GROUPER_LOGDIR)
git pull > $PULL_OUTFILE

echo "Git pull output:" >>$LOGFILE
cat $PULL_OUTFILE >>$LOGFILE

if [ -z $PULL_OUTFILE -o "Already up-to-date." = "$(cat $PULL_OUTFILE)" ]; then
  echo "Git already up to date -- exiting" >>$LOGFILE
  rm $PULL_OUTFILE
fi
exit 0

rm $PULL_OUTFILE


# Note, prettier version of emails is --format='%an <%ae>'
# apt install mailx
COMMITTER_EMAILS=$(git --no-pager show -s --format=%ae master@{1}..master | sort -u)

if [ -z "$COMMITTER_EMAILS" ]; then
  echo "No committer emails found for latest pull -- skipping tests" >>$LOGFILE
  exit 1
fi

# Check for '[skip tests]' in the commit text
# Note: grep -q returns 0 if found and 1 if not, so flag is the opposite of expected
echo "Checking for [skip tests]" >>$LOGFILE
git --no-pager show -s --format="%s %b" master@{1}..master 2>>$LOGFILE | grep -q '\[skip tests\]'
exit_code=$?
if [ $exit_code -eq 0 ]; then
  echo "Found '[skip tests]' within latest pull -- skipping tests" >>$LOGFILE
  exit 1
fi

# Build (all projects) and copy dependencies for grouper subproject, so all can be run from the target/ directory
# NOTE: This needs to be java 8, not 11!
echo "Building Maven projects" >>$LOGFILE
$MVN -f grouper-parent clean package install  >>$LOGFILE 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven build failed (exit $exit_code)" >>$LOGFILE
  exit 1
fi

echo "Downloading Grouper api dependencies" >>$LOGFILE
#since we are testing in this script, we don't want to skip the test dependencies
#$MVN -f grouper dependency:copy-dependencies -DincludeScope=runtime
$MVN -f grouper dependency:copy-dependencies >>$LOGFILE 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven dependency:copy-dependencies failed (exit $exit_code)" >>$LOGFILE
  exit 1
fi

## Start up postgres container compatible with the confForTest hibernate login
#echo "Starting Docker database container"
#docker run -d -e "POSTGRES_USER=grouper" -e "POSTGRES_PASSWORD=password" -e "POSTGRES_DB=grouper" -p 5432:5432  postgres
#exit_code=$?
#if [ $exit_code -ne 0 ]; then
#  echo "docker database container startup failed (exit $exit_code)" >&2
#  exit 1
#fi
#echo "Sleeping for 10 seconds to let db start"
#sleep 10

# Start up HSQL container compatible with the confForTestHSQL hibernate login
java -cp grouper/target/dependency/hsqldb-2.3.5.jar org.hsqldb.server.Server \
  --database.0 "mem:grouper;user=grouper;password=test" \
  --dbname.0 grouper \
  --address 127.0.0.1 \
  --port 9101  >>$LOGFILE 2>&1 &


# Set up the classpath to use the newly created artifacts

CP=grouper/misc/ci-test/confForTestHSQL
CP=$CP:$(compgen -G "grouper/target/grouper-[0-9].[0-9].[0-9]*.jar" | grep -v -- '-sources.jar' | tr '\n' ':' | sed -e 's/::/:/;s/:$//')
CP=$CP:"grouper/target/dependency/*"
CP=$CP:grouper/conf
#  RESULT:
#grouper/misc/ci-test/confForTestHSQL
#grouper/target/grouper-2.5.0-SNAPSHOT-tests.jar
#grouper/target/grouper-2.5.0-SNAPSHOT.jar
#grouper/target/dependency/*
#grouper/conf

# Init the grouper database
echo "Initializing the Grouper database" >>$LOGFILE
chmod u+x grouper/bin/gsh.sh
CLASSPATH="$CP" GROUPER_CONF="grouper/misc/ci-test/confForTestHSQL" grouper/bin/gsh.sh -registry -runscript -noprompt >>$LOGFILE 2>&1

exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Failed to init the database (exit $exit_code)" >>$LOGFILE
  exit 1
fi


# Run the tests

# make sure this runs with Java 8
which java >>$LOGFILE
JAVA=java

for var in PWD COMMITTER_EMAILS LOGFILE CP; do
  echo "$var = ${!var}" >>$LOGFILE
done

echo $(date) " | START" >>$LOGFILE

# clean out logs from previous run
rm -f grouper/logs/*.log >>$LOGFILE 2>&1

echo "Executing edu.internet2.middleware.grouper.AllTests"
$JAVA -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -XX:MaxPermSize=300m -Xms80m -Xmx640m \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt \
  >>$LOGFILE 2>&1

exit_code=$?

echo $(date) "CI test started (exit code $exit_code)" >>$LOGFILE


GROUPER_ATTACH=
if [ -n grouper/logs/grouper_error.log]; then
  GROUPER_ATTACH="-a grouper/logs/grouper_error.log"
fi

jobs >>$LOGFILE
kill %1 >>$LOGFILE 2>&1

#DEBUG
COMMITTER_EMAILS=chad_redman@unc.edu

echo "CI completed tests" | mailx -s "CI test results" -a $LOGFILE $GROUPER_ATTACH $COMMITTER_EMAILS 2>&1

# Exit with the result from the test run
exit $exit_code
