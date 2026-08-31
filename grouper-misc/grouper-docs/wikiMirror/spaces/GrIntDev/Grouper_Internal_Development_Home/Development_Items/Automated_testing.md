---
title: "Automated testing"
space: GrIntDev
pageId: 48792757
version: 3
lastUpdated: 2026-07-12T06:45:40.951Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792757/Automated+testing
---

A periodic scheduled process on i2midev6 will test the Maven build and then run all the unit tests for the grouper and pspng projects. The process runs as user grprdist on i2midev6, and is scheduled via cron to run on Tuseday, Thursday, Saturday, and Sunday.

Directory structure:

i2midev6:

- /var/grouper-ci/
  
  - bin/
    
    - ci-test-hsql.sh (the script to run tests; is similar to checked in file grouper/misc/ci-test/ci-test-hsql.sh)
    - MAILTO.dat (a list of addresses to mail results to)
  - git/grouper/ (the repository to pull the development branch from, then build and test)
  - log/ (stores logs for the maven build, tests, and a test summary that gets mailed, allremoved after 30 days)

Steps in the test process:

1. Pull from the current git develop branch (i.e. GROUPER_2_*_BRANCH) into the git/grouper directory
2. If there are no new commits since the last run, skip the remaining steps
3. if there is a commit since the last run with a comment "[skip tests]", skip the remaining steps
4. Execute a Maven package build for all projects, abort if the build fails at any point
5. Execute a Maven dependency pull for the grouper subproject (the default scope is test which is what we want)
6. Run a HSQL server on port 9101 in the background
7. Run `grouper/bin/gsh.sh -registry -runscript -noprompt` to init the database
8. Set up Java classpath, including grouper jars, dependencies, grouper/conf directory, and grouper/misc/ci-test/confForTestHSQL directory
9. Run java with arguments `-Dgrouper.allow.db.changes=true -Dgrouper.home=./ -XX:MaxPermSize=300m -Xms80m -Xmx640m edu.internet2.middleware.grouper.AllTests -all -noprompt`
10. Execute a Maven dependency pull for the grouper-pspng subproject
11. Add pspng jars and dependencies to classpath
12. Run java again, with arguments `... edu.internet2.middleware.grouper.AllTests pspng.AllPspngTests -noprompt`
13. Look in the grouper and pspng logs for `^Time:`, and add trailing lines to a summary file
14. Attach the summary file to an email, and mail to the addresses listed in bin/MAILTO.dat
15. Kill the background hsql server
