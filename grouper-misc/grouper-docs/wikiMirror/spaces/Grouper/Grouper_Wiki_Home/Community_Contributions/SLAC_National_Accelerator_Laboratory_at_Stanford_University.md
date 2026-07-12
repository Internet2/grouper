---
title: "SLAC National Accelerator Laboratory at Stanford University"
space: Grouper
pageId: 28543439
version: 14
lastUpdated: 2026-07-01T05:49:47.150Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543439/SLAC+National+Accelerator+Laboratory+at+Stanford+University
---

[See update from 2023 on SLAC Grouper container in Amazon AWS ECS Elastic Container Service](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544144/SLAC+Grouper+container+in+Amazon+AWS+ECS+Elastic+Container+Service)

As of 2020, SLAC National Accelerator Laboratory embarked on a program to replace a number of IAM systems with Internet2 [InCommon Trusted Access Platform components](https://www.incommon.org/software/software-solutions/). The Grouper component is one of the first to be investigated for adoption as it requires the fewest pre-requisite changes before it can provide a visible benefit for the program.

The first phase of deployment is to supplement existing NIS/LDAP groups, which are maintained using manual/scripted process today.

Goals include:

- Provide automation around affiliation status events
- Simplify delegation of group management tasks
- Provide web services interface or loaders for managing experiment groups
- Provision groups to Active Directory
- Eventual retirement of NIS (YP commands)
