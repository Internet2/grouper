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

MVN=/opt/maven/bin/mvn
GP=/var/grouper-docs/git/grouper
SITE=/tmp/groupersite
CURRENT_BRANCH=GROUPER_4_BRANCH

echo $(date) "Starting build"

cd $GP
git status
git checkout $CURRENT_BRANCH
git pull

echo $(date) "Checked out branch '$CURRENT_BRANCH' (exit $?)"

# Build branch site in /tmp

cd $GP/grouper-parent
rm -rf $SITE
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

