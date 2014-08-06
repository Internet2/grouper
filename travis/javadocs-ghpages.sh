#!/bin/bash

invokeJavadoc=false
grouperDocsDirectory="/tmp/grouper-parent"

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
       *\.1) 
  		echo -e "Invoking Javadoc deployment for Travis job ${TRAVIS_JOB_NUMBER}"
  		invokeJavadoc=true;;
  esac
fi 

if [ "$invokeJavadoc" == true ]; then

  echo -e "Start to publish lastest Javadoc to gh-pages...\n"
  
  echo -e "Invoking Maven to generate the site documentation...\n"
  cd grouper-parent
  mvn site site:deploy -q -ff -B -DskipTests=true
  
  echo -e "Copying the generated docs over from $grouperDocsDirectory...\n"
  cp -R $grouperDocsDirectory $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Jasig/cas gh-pages > /dev/null

  cd gh-pages
  echo -e "Removing javadocs...\n"
  git rm -rf ./master/**

  echo -e "Copying new javadocs...\n"
  cp -Rf $HOME/javadoc-latest ./master
  echo -e "Adding changes to the index...\n"
  git add -f .
  echo -e "Committing changes...\n"
  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages > /dev/null

  echo -e "Done magic with auto publishment to gh-pages.\n"
  
fi
