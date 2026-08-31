---
title: "GrouperShell (gsh) Member finder (MemberFinder)"
space: Grouper
pageId: 28548476
version: 3
lastUpdated: 2026-07-01T05:44:25.449Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548476/GrouperShell+gsh+Member+finder+MemberFinder
---

Use this class to find members within the Groups registry

Sample call

> Set members = MemberFinder.findAll(grouperSession, gsaSource);

Sample call to find members by attribute def names

> Set members = new MemberFinder().assignNameOfAttributeDefName(attributeDefName.getName()) .assignAttributeCheckReadOnAttributeDef(true).assignQueryOptions(QueryOptions.create("subjectId", true, null, null)).findMembers();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MemberFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MemberFinder.html)
