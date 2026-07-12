---
title: "Grouper container management for Grouper developers"
space: GrIntDev
pageId: 48793038
version: 4
lastUpdated: 2026-07-12T06:46:01.100Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793038/Grouper+container+management+for+Grouper+developers
---

The container config is in git: [https://github.internet2.edu/docker/grouper](https://github.internet2.edu/docker/grouper)

Each commit will kick off a build, so do NOT push to existing branches which are released

Jenkins is here: [https://jenkins.testbed.tier.internet2.edu/](https://jenkins.testbed.tier.internet2.edu/)

Here is an example of a job: [https://jenkins.testbed.tier.internet2.edu/job/docker/job/grouper/job/2.5.25/](https://jenkins.testbed.tier.internet2.edu/job/docker/job/grouper/job/2.5.25/)

After making a Grouper release (tag in git), release in sonatype.

Then make a new branch in docker/grouper (2.5.x). Change the Dockerfile in two places with new version, and commit and push the branch

Grouper packages: [https://hub.docker.com/r/i2incommon/grouper/tags?page=1&ordering=last_updated](https://hub.docker.com/r/i2incommon/grouper/tags?page=1&ordering=last_updated)
