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
LOGTAG=$(date +%Y%m%d_%H%M%S)-$$
TESTLOG=$GROUPER_LOGDIR/grouper-ci-test-$LOGTAG.log
SUMMARYLOG=$GROUPER_LOGDIR/grouper-ci-test-$LOGTAG.summary.log
BUILDLOG=$GROUPER_LOGDIR/grouper-ci-mvn-$LOGTAG.log
CURRENT_BRANCH=GROUPER_4_0_BRANCH

# Whether to still run the tests even if there are no git updates
# can enable these during debugging
#FORCE_TESTS=true
#SKIP_PULL=true

MVN=/opt/apache-maven-3.6.3/bin/mvn

echo $(date) "CI test started" > $TESTLOG
echo '----------------------' >>$TESTLOG

if [ ! -d "$GROUPER_HOME" ]; then
  echo "Grouper home '$GROUPER_HOME' missing -- skipping tests" >>$TESTLOG
  exit 1
fi

cd $GROUPER_HOME
if [ "$SKIP_PULL" = "" ]; then
  echo "Pulling from active branch ($CURRENT_BRANCH)" >>$TESTLOG 2>&1

  git checkout $CURRENT_BRANCH  >>$TESTLOG 2>&1

  PULL_OUTFILE=$(mktemp --tmpdir=$GROUPER_LOGDIR)
  git pull > $PULL_OUTFILE

  echo "Git pull output:" >>$TESTLOG
  cat $PULL_OUTFILE >>$TESTLOG

  if [ -z $PULL_OUTFILE -o "Already up-to-date." = "$(cat $PULL_OUTFILE)" ]; then
    if [ "$FORCE_TESTS" = "true" ]; then
      echo "Git already up to date, but continuing since FORCE_TESTS=true" >>$TESTLOG
    else
      echo "Git already up to date -- exiting" >>$TESTLOG
      rm $PULL_OUTFILE
      exit 0
    fi
  fi
  rm $PULL_OUTFILE
else
  echo "Skipping Git pull due to SKIP_PULL=$SKIP_PULL" >>$TESTLOG 2>&1
fi

## Note, prettier version of emails is --format='%an <%ae>'
## apt install mailx
#COMMITTER_EMAILS=$(git --no-pager show -s --format=%ae $CURRENT_BRANCH@{1}..$CURRENT_BRANCH | sort -u)
#
#if [ -z "$COMMITTER_EMAILS" ]; then
#  echo "No committer emails found for latest pull -- skipping tests" >>$TESTLOG
#  exit 1
#fi
COMMITTER_EMAILS=$(cat /var/grouper-ci/bin/MAILTO.dat)

# Check for '[skip tests]' in the commit text
# Note: grep -q returns 0 if found and 1 if not, so flag is the opposite of expected
echo "Checking for [skip tests]" >>$TESTLOG
git --no-pager show -s --format="%s %b" $CURRENT_BRANCH@{1}..$CURRENT_BRANCH 2>>$TESTLOG | grep -q '\[skip tests\]'
exit_code=$?
if [ $exit_code -eq 0 ]; then
  echo "Found '[skip tests]' within latest pull -- skipping tests" >>$TESTLOG
  exit 1
fi

# Build (all projects) and copy dependencies for grouper subproject, so all can be run from the target/ directory
# NOTE: This needs to be java 8, not 11!
echo "Building Maven projects (logged to $BUILDLOG)" >>$TESTLOG
$MVN -f grouper-parent clean package install  >>$BUILDLOG 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven build failed (exit $exit_code)" >>$TESTLOG
  echo "Maven build failed (exit $exit_code)" | mailx -s "CI test results *Maven build failed*" -a $BUILDLOG $COMMITTER_EMAILS 2>&1
  exit 1
fi

echo "Downloading Grouper api dependencies" >>$TESTLOG
#since we are testing in this script, we don't want to skip the test dependencies
#$MVN -f grouper dependency:copy-dependencies -DincludeScope=runtime
$MVN -f grouper dependency:copy-dependencies >>$BUILDLOG 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven dependency:copy-dependencies failed (exit $exit_code)" >>$TESTLOG
  exit 1
fi


## Remove the postgres container if it wasn't deleted properly from the last run
docker rm -f grouper-ci-pgsql

## Start up postgres container compatible with the confForTest hibernate login
echo "Starting Docker database container"
docker run --name grouper-ci-pgsql -d -e "POSTGRES_USER=grouper" -e "POSTGRES_PASSWORD=test" -e "POSTGRES_DB=grouper" -p 15432:5432  postgres >>$TESTLOG 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "docker database container startup failed (exit $exit_code)" >&2
  exit 1
fi
echo "Sleeping for 10 seconds to let db start"
sleep 10

# Start up HSQL container compatible with the confForTestHSQL hibernate login
#java -cp grouper/target/dependency/hsqldb-2.3.5.jar org.hsqldb.server.Server \
#  --database.0 "mem:grouper;user=grouper;password=test" \
#  --dbname.0 grouper \
#  --address 127.0.0.1 \
#  --port 9101  >>$TESTLOG 2>&1 &


# Set up the classpath to use the newly created artifacts

CP=grouper/misc/ci-test/confForTestPGSQL
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
echo "Initializing the Grouper database" >>$TESTLOG
chmod u+x grouper/bin/gsh.sh
CLASSPATH="$CP" GROUPER_CONF="grouper/misc/ci-test/confForTestPGSQL" grouper/bin/gsh.sh -registry -runscript -noprompt >>$TESTLOG 2>&1

exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Failed to init the database (exit $exit_code)" >>$TESTLOG
  exit 1
fi


# Run the tests

# make sure this runs with Java 8
which java >>$TESTLOG
JAVA=java

for var in PWD COMMITTER_EMAILS TESTLOG CP; do
  echo "$var = ${!var}" >>$TESTLOG
done

echo $(date) " | START" >>$TESTLOG

# clean out logs from previous run
rm -f grouper/logs/*.log >>$TESTLOG 2>&1

echo "Executing edu.internet2.middleware.grouper.AllTests" >>$TESTLOG 2>&1
$JAVA -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -Xms80m -Xmx640m \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.sql/java.sql=ALL-UNNAMED \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt \
  >>$TESTLOG 2>&1

exit_code=$?

echo $(date) "CI test finished (exit code $exit_code)" >>$TESTLOG

echo "GROUPER TESTS" > $SUMMARYLOG
echo "=============" >> $SUMMARYLOG

tail -n +$(( $(egrep -n '^Time: ' $TESTLOG | tail -n1 | cut -d: -f1) )) $TESTLOG >>$SUMMARYLOG 2>>$TESTLOG
summary_code=$?


# Run pspng as a separate set of tests
$MVN -f grouper-misc/grouper-pspng dependency:copy-dependencies >>$BUILDLOG 2>&1
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "Maven pspng dependency:copy-dependencies failed (exit $exit_code)" >>$TESTLOG
  exit 1
fi

CP=$(compgen -G "grouper-misc/grouper-pspng/target/grouper-pspng-[0-9].[0-9].[0-9]*.jar" | grep -v -- '-sources.jar' | tr '\n' ':' | sed -e 's/::/:/;s/:$//'):$CP
CP=$CP:"grouper-misc/grouper-pspng/target/dependency/*"
echo $CP

echo "Executing edu.internet2.middleware.grouper.AllTests" >>$TESTLOG 2>&1
echo "CP=$CP" >>$TESTLOG 2>&1
$JAVA -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=grouper-misc/grouper-pspng \
  -Xms80m -Xmx640m \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.sql/java.sql=ALL-UNNAMED \
  edu.internet2.middleware.grouper.AllTests pspng.AllPspngTests \
  -noprompt \
  >>$TESTLOG 2>&1

exit_code=$?

echo $(date) "CI test (PSPNG) finished (exit code $exit_code)" >>$TESTLOG

echo "PSPNG TESTS" >> $SUMMARYLOG
echo "===========" >> $SUMMARYLOG

tail -n +$(( $(egrep -n '^Time: ' $TESTLOG | tail -n1 | cut -d: -f1) )) $TESTLOG >>$SUMMARYLOG 2>>$TESTLOG
pspng_summary_code=$?


GROUPER_ATTACH=
if [ -s grouper/logs/grouper_error.log ]; then
  GROUPER_ATTACH="-a grouper/logs/grouper_error.log"
fi

jobs >>$TESTLOG
kill %1 >>$TESTLOG 2>&1

#DEBUG

if [ $summary_code -ne 0 -o $pspng_summary_code -ne 0 -o -z $SUMMARYLOG ]; then
  echo "DEBUG: summary_code=$summary_code; pspng_summary_code=$pspng_summary_code" >>$TESTLOG 2>&1
  ls -alFd $SUMMARYLOG >>$TESTLOG 2>&1
  echo "CI completed tests (summary failed)" | mailx -s "CI test results" -a $TESTLOG -a $BUILDLOG $GROUPER_ATTACH $COMMITTER_EMAILS 2>&1
  exit $exit_code
fi

echo "CI completed tests. Details can be found on $(hostname) in file $TESTLOG" | mailx -s "CI test results (summary)" -a $SUMMARYLOG $COMMITTER_EMAILS 2>&1

# The unit tests leave extra grouperImportRecordReport* files around that will slowly accumulate
rm -f /var/grouper-ci/git/grouper/grouperImportRecordReport_*.txt

## Remove the postgres container before exiting
docker rm -f grouper-ci-pgsql

# Exit with the result from the test run
exit $exit_code

