---
title: "GrouperShell (gsh) Group finder (GroupFinder)"
space: Grouper
pageId: 28548732
version: 3
lastUpdated: 2026-07-01T05:43:38.402Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548732/GrouperShell+gsh+Group+finder+GroupFinder
---

Use this class to find groups within the Groups registry

Sample call

> Group group = GroupFinder.findByName(grouperSession, "test", true);

Sample call to find groups a subject has specific privileges on

> Set groups = new GroupFinder().assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES) .assignField(Group.getDefaultList()).assignSubject(subject) .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupFinder.html)
