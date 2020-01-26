#!/bin/bash

if [ -z "$TRAVIS_COMMIT_RANGE" ]; then
  echo "No commit range for this build -- skipping tests" >&2
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

# Set up Maven environment. Install artifacts locally so further usage can be built per package
#if [ ! -f "" $BASEDIR/travis/mvn.settings.xml]; then
#    cp $BASEDIR/travis/mvn.settings.xml $HOME/.m2/settings.xml
#fi
#mvn -f $BASEDIR/grouper-parent clean package install
#mvn -f $BASEDIR/grouper dependency:copy-dependencies

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

for var in PWD TRAVIS_COMMIT_RANGE COMMITTER_EMAILS BASEDIR LOGFILE CP; do
  echo "$var = ${!var}"
done

echo $(date) " | START" > $LOGFILE

java -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -XX:MaxPermSize=300m -Xms80m -Xmx640m \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt \
  >> $LOGFILE 2>&1

echo $(date) " | END" >> $LOGFILE

GROUPER_ATTACH=
if [ -n $BASEDIR/grouper/logs/grouper_error.log]; then
  GROUPER_ATTACH="-a $BASEDIR/grouper/logs/grouper_error.log"
fi

echo "Travis completed tests" | echo mailx -s "Travis test results" -a $LOGFILE -a $GROUPER_ATTACH $COMMITTER_EMAILS

cat $LOGFILE
exit 0
