#!/bin/bash

# Builds docs using maven site plugin, from master branch

# One-time setup:
#   1) Git clone and checkout master
#       - git config --global user.name "FIRST_NAME LAST_NAME"
#       - git config --global user.email "MY_NAME@example.com"
#       - git clone https://github.com/Internet2/grouper.git
#       - cd grouper
#       - git checkout master
#   2) (optional) Git clone and checkout gh-pages
#       - mkdir -p $HOME/Git/gh-pages
#       - cd $HOME/Git/gh-pages
#       - git clone https://github.com/Internet2/grouper.git
#       - cd grouper
#       - git checkout gh-pages
#   3b) (alternate) copy site from webprod3
#       - mkdir -p $HOME/Git/gh-pages/grouper
#       - cd $HOME/Git/gh-pages/grouper
#       - rsync -rtzv webprod3:/home/htdocs/software.internet2.edu/grouper/doc/* $HOME/Git/gh-pages/grouper
#   4) (optional) Maven settings
#       - if Maven fails to download the necessary plugin libraries, try copying it
#         from /opt/maven/conf/settings.xml.grouper to $HOME/.m2/settings.xml

MVN=/opt/maven/bin/mvn

echo $(date) "Starting build"

# Update master branch
cd $HOME/Git/grouper/grouper-parent
git checkout master
git pull
echo $(date) "Checked out master branch (exit $?)"

# Build site in /tmp
rm -r /tmp/groupersite
$MVN clean site site-deploy -Dlicense.skip=true -DskipTests=true
echo $(date) "Built site (exit $?)"

# Update staging site
cd $HOME/Git/gh-pages/grouper
rm -r master
mv /tmp/groupersite master
echo $(date) "Updated master subdirectory or site (exit $?)"

# rsync to webprod3
rsync -rtzv --exclude=.git /home/cer/Git/gh-pages/grouper/* webprod3:/home/htdocs/software.internet2.edu/grouper/doc
echo $(date) " Completed rsync"

exit 0

