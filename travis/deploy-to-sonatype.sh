#!/bin/bash

# Only invoke the deployment to Sonatype when it's not a PR and only for master
if [ "$TRAVIS_PULL_REQUEST" == "true" ]; then
  echo -e "Skipping Sonatype deployment because this is a pull request"
  exit 0
fi

# TODO temp allow travis branch to deploy
#[ "$TRAVIS_BRANCH" != "master" ]
#  echo -e "Skipping Sonatype deployment because this is not the master branch"
#  exit 0
#fi

GROUPER_VERSION=$(perl -e '$_ = $ENV{q~TRAVIS_TAG~}; print $1 if /^GROUPER_RELEASE_((\d+\.)+\d+(rc\d+)?)$/')
if [ $? -ne 0 ]; then
  echo -e "Failed to parse travis tag '$TRAVIS_TAG' against /^GROUPER_RELEASE_((\d+\.)+\d+(rc\d+)?)$/"
  exit 129
fi
if [ -z "$GROUPER_VERSION" ]; then
  echo -e "Travis tag '$TRAVIS_TAG' did not match pattern /^GROUPER_RELEASE_((\d+\.)+\d+(rc\d+)?)$/"
  exit 130
fi

echo -e "Extracted grouper version '$GROUPER_VERSION' from tag '$TRAVIS_TAG'"

case "${TRAVIS_JOB_NUMBER}" in
  *\.1) 
    echo -e "Setting Grouper version to $GROUPER_VERSION in Maven pom files"
    cp travis/mvn.settings.xml $HOME/.m2/settings.xml
    mvn -f ./grouper-parent versions:set -DnewVersion=$GROUPER_VERSION
    if [ $? -ne 0 ]; then
      echo -e "Failed to set pom versions to $GROUPER_VERSION"
      exit 131
    fi

    echo -e "building and deploying release artifacts to Sonatype for Travis job ${TRAVIS_JOB_NUMBER}"
    mvn -f ./grouper-parent clean compile package deploy -Prelease
    if [ $? -ne 0 ]; then
      echo -e "Failed to build or deploy the release version"
      exit 132
    fi
    echo -e "Successfully deployed release artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}";;
  *)
    echo -e "Skipping Sonatype deployment because job ${TRAVIS_JOB_NUMBER} does not match *.1"
esac
