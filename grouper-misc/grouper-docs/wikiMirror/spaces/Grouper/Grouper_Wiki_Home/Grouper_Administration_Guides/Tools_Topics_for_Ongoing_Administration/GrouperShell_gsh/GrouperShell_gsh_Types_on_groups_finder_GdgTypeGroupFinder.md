---
title: "GrouperShell (gsh) Types on groups finder (GdgTypeGroupFinder)"
space: Grouper
pageId: 28548985
version: 6
lastUpdated: 2026-07-01T05:43:02.211Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548985/GrouperShell+gsh+Types+on+groups+finder+GdgTypeGroupFinder
---

Use this class to find objects type attributes on groups

Sample call

> GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroup(group).assignType("ref").findGdgTypeGroupAssignment();

Sample call to find multiple object types attributes on a group

> Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeGroupFinder().assignGroup(group).findGdgTypeGroupAssignments();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeGroupFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeGroupFinder.html)
