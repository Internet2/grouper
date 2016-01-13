#!/bin/bash

invokeJavadoc=false

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "gradle" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
       *\.1) 
  		echo -e "Invoking Javadoc deployment for Travis job ${TRAVIS_JOB_NUMBER}"
  		invokeJavadoc=true;;
  esac
fi 

if [ "$invokeJavadoc" == true ]; then
  echo -e "Start to publish lastest Javadoc to gh-pages...\n"
  
  echo -e "Invoking Gradle to generate the site documentation...\n"
  echo "Current working directory is $PWD"
  ./gradlew alljavadoc -q
  
  echo -e "Copying the generated docs over...\n"
  cp -R build/javadoc $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Internet2/grouper gh-pages > /dev/null

  cd gh-pages
  echo "Current working directory is $PWD"

  echo -e "Removing javadocs...\n"
  git rm -rf ./master/**

  echo -e "Creating directory...\n"
  mkdir -p ./master

  echo -e "Listing generated directory...\n"
  ls $HOME/javadoc-latest

  echo -e "Copying new javadocs...\n"
  cp -Rf $HOME/javadoc-latest ./master

  echo -e "Listing new javadocs...\n"
  ls ./master

  echo -e "Adding changes to the index...\n"
  git add -f --all . 

  echo -e "Committing changes...\n"
  git commit -m "Javadoc on build $TRAVIS_BUILD_NUMBER pushed to gh-pages"

  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages

  echo -e "Done magic with auto publishment to gh-pages.\n"
  
fi
