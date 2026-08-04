#!/bin/bash

# Builds docs using maven site plugin, from master branch

# One-time setup:
#   1) Git clone and checkout master
#       - git config --global user.name "FIRST_NAME LAST_NAME"
#       - git config --global user.email "MY_NAME@example.com"
#       - git clone https://github.com/Internet2/grouper.git
#       - cd grouper
#       - git checkout master
#   4) (optional) Maven settings
#       - if Maven fails to download the necessary plugin libraries, try copying it
#         from /opt/maven/conf/settings.xml.grouper to $HOME/.m2/settings.xml

#MVN=/opt/maven/bin/mvn
MVN=/opt/apache-maven-3.6.3/bin/mvn
GP=/var/grouper-docs/git/grouper
SITE=/tmp/groupersite
CURRENT_BRANCH=GROUPER_5_BRANCH
#CURRENT_BRANCH=GROUPER_RELEASE_5.4.0
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

# Register a new key for the National Vulnerability Database
# at https://nvd.nist.gov/developers/request-an-api-key .
# Then store the key in /var/grouper-docs/bin/NVD_API_KEY
NVD_API_KEY_FILE=/var/grouper-docs/bin/NVD_API_KEY
if [ -f "$NVD_API_KEY_FILE" ]; then
  export NVD_API_KEY=$(grep -vE '^[[:space:]]*(#|$)' "$NVD_API_KEY_FILE" | head -n1)
fi

# Register an OSS Index account at https://ossindex.sonatype.org/user/register .
# Then store OSS_INDEX_USERNAME=... and OSS_INDEX_PASSWORD=... (one per line)
# in /var/grouper-docs/bin/OSS_API_KEY
OSS_API_KEY_FILE=/var/grouper-docs/bin/OSS_API_KEY
if [ -f "$OSS_API_KEY_FILE" ]; then
  eval $(grep -vE '^[[:space:]]*(#|$)' "$OSS_API_KEY_FILE")
  export OSS_INDEX_USERNAME
  export OSS_INDEX_PASSWORD
fi

echo $(date) "Starting build"

cd $GP
git status
git checkout $CURRENT_BRANCH
git pull

echo $(date) "Checked out branch '$CURRENT_BRANCH' (exit $?)"

# Build branch site in /tmp

cd $GP/grouper-parent
rm -rf $SITE

# Without installing packages, complains "Failure to find edu.internet2.middleware.grouper:grouperClient:jar:tests:6.0.0-SNAPSHOT in https://oss.sonatype.org/content/repositories/releases/"
$MVN install
$MVN clean site site-deploy -Dlicense.skip=true -DskipTests=true

echo $(date) "Built site (exit $?)"

###rsync -rtz --delete --dry-run --exclude=.git /tmp/groupersite/* webprod3:/home/htdocs/software.internet2.edu/grouper/doc/master
##rsync -rtzv --delete --dry-run --exclude=.git /tmp/groupersite/* webprod3:/home/htdocs/software.internet2.edu/grouper/doc/master
#rsync -rtzv --delete  --exclude=.git /tmp/groupersite/* webprod3:/home/htdocs/software.internet2.edu/grouper/doc/master
#echo $(date) " Completed rsync"

# Signal to webprod3 that the process is finished

echo $$ > /var/grouper-docs/var/grouper-docs.WRI
date >> /var/grouper-docs/var/grouper-docs.WRI


# Verify with http://software.internet2.edu/grouper/doc/

exit 0
