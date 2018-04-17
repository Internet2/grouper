#!/bin/bash

# Only invoke the deployment to Sonatype when it's not a PR and only for master
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
    *\.1) 
      echo -e "deploying SNAPSHOT artifacts to Sonatype for Travis job ${TRAVIS_JOB_NUMBER}"
      cp travis/mvn.settings.xml $HOME/.m2/settings.xml
      mvn -f ./grouper-parent -DskipTests=true -Dlicense.skip=true deploy
      echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}";;
    *)
      echo -e "Skipping Sonatype deployment because job ${TRAVIS_JOB_NUMBER} does not match *.1"
  esac
else
  echo -e "Skipping Sonatype deployment because this is either a pull request or not the master branch"
fi


