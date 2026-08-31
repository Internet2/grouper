---
title: "Jenkins build for Grouper jars"
space: GrIntDev
pageId: 48792653
version: 3
lastUpdated: 2026-07-12T07:01:10.608Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792653/Jenkins+build+for+Grouper+jars
---

Grouper project jar files are built from the Internet2 Jenkins server. This is not a true CI, since it will only build specific releases, and not every commit. As of 2021-December, the Travis CI that had been building artifacts had changed their policy to limit the number of builds allowed for free software projects. As a result, we were no longer able to use Travis to build and deploy releases to the Sonatype Maven server.

## Building a Grouper Version

The Git repository controlling builds is [https://github.internet2.edu/internet2/grouper](https://github.internet2.edu/internet2/grouper) . To start a new build, create a new branch corresponding to the release number, add a simple commit (edit file temp.txt, or possibly an empty commit will work) and push back to the remote repository. The branch number (a.b.c) must match a corresponding tag in the Grouper source repository (GROUPER_RELEASE_a.b.c). Jenkins will detect the new commit, and start a build. To watch the build, go to [https://jenkins.testbed.tier.internet2.edu/job/internet2/job/grouper/](https://jenkins.testbed.tier.internet2.edu/job/internet2/job/grouper/) and look for the latest build for the target branch. Console output will be updated dynamically, so you can watch as it's building.

Once the build completes successfully, the end result will be PGP-signed jar files that are uploaded to our staging repository on the Sonatype Maven repository. See the [separate page for how to release the jars from the staging folder](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793408/Grouper+Sonatype+repository).

## Details on Jenkins Setup

The Grouper build works differently from the Docker builds that comprise most of the projects on the Jenkins server. The multi-branch pipeline project is tied to a repository on the Internet2 GitHub Enterprise, and commits there will trigger a build. But the Grouper source is hosted on GitHub, and there is not yet an integration hook from there. Instead, the branch number on the Internet2 repository (e.g. "a.b.c") must match a tag in GitHub that starts with "GROUPER_RELEASE_*" (e.g. GROUPER_RELEASE_a.b.c). If the branch in the Jenkins repo doesn't match a dotted version pattern ("/^(\d+\.\d+\.\w+)$/"), the build will be skipped. If a corresponding GROUPER_RELEASE_* tag in the GitHub repository doesn't exist, the build will fail.

As this was the first Maven build on the server, a few different tasks were needed to initialize the project.

1. Corretto JDK 8 was needed for Grouper builds. It was added to Jenkins and can be referenced by ID "Corretto-JDK8" and JAVA_HOME=/home/centos/agent/tools/hudson.model.JDK/Corretto-JDK8/amazon-corretto-8.322.06.2-linux-x64
2. This was the first Maven project, so a Maven installation was added to Jenkins. It can be referenced by a Jenkinsfile withMaven() method with ID "maven"
3. To be able to PGP-sign artifacts, the PGP key for Grouper was set up as a credential with ID "grouper-gpg-key", and an import step in the Jenkinsfile sets up the keyring for later signing by Maven
4. Credentials to upload to Sonatype were set up in credential with ID "grouper-sonatype-login"

The build itself is a normal Jenkins file, using scripted (not declarative) syntax written in Groovy.
