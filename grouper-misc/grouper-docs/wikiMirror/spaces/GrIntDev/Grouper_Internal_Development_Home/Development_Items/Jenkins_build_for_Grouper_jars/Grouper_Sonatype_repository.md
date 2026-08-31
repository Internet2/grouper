---
title: "Grouper Sonatype repository"
space: GrIntDev
pageId: 48793408
version: 2
lastUpdated: 2026-07-12T07:01:41.576Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793408/Grouper+Sonatype+repository
---

# Quick Start

A successful Jenkins build (previously Travis) will upload signed artifacts to our holding repository on Sonatype. From this point:

- Log into Sonatype ([https://oss.sonatype.org/](https://oss.sonatype.org/)) (get the shared username/password from the Grouper project lead), find the current artifact set among the staging repositories
- Promote the directory from closed to release
- Confirm current version of artifacts appear in Sonatype releases ([https://oss.sonatype.org/#nexus-search;quick~grouper-parent](https://oss.sonatype.org/#nexus-search;quick~grouper-parent))
- wait a few hours, then confirm they were propagated to the Maven repositories (e.g. [https://search.maven.org/artifact/edu.internet2.middleware.grouper/grouper-parent](https://search.maven.org/artifact/edu.internet2.middleware.grouper/grouper-parent))

# Detailed Description

After a successful build, If successful, jar files and associate Maven files (pom.xml) and signature files (*.asc) will appear in a new folder in our Sonatype staging repository ([https://oss.sonatype.org/](https://oss.sonatype.org/)). There is a shared account to access this repository. A subfolder will appear with a name such as "eduinternet2middlewaregrouper-####". The status should be "closed", indicating that Travis was able to finalize its upload of the artifacts. In this state, the repository can be tested as a private repository, by adding it as a profile in maven settings.xml (see below), or it can be promoted to "release" which will publish it in the public repository. After being released, the artifacts will eventually be propagated to other Maven repository sites, such as [https://search.maven.org/](https://search.maven.org/) and [https://mvnrepository.com/](https://mvnrepository.com/).

# Testing the staging repository before release

This step will not be needed often, but can be useful when verifying jar files before they are irrevocably published. For this, add to ~/.m2/settings.xml

| `<``settings``>`   `...`   ```<``profiles``>`   ```<``profile``>`   ```<``id``>grouper-stage</``id``>`   ```<``repositories``>`   ```<``repository``>`   ```<``id``>grouper-stage</``id``>`   ```<``url``>[https://oss.sonatype.org/service/local/repositories/eduinternet2middlewaregrouper-1014/content](https://oss.sonatype.org/service/local/repositories/eduinternet2middlewaregrouper-1014/content)</``url``>`   ```<``releases``>`   ```<``enabled``>true</``enabled``>`   ```</``releases``>`   ```</``repository``>`   ```</``profile``>`   ```</``profiles``>`   `...` |
| --- |

Then, when running maven commands, include parameter `-P grouper-stage` to activate the profile. Maven downloads will then use this location as an additional download source.
