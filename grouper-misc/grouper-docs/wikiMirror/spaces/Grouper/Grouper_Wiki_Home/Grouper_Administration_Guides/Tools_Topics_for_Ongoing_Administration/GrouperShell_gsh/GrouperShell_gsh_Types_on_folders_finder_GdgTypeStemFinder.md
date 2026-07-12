---
title: "GrouperShell (gsh) Types on folders finder (GdgTypeStemFinder)"
space: Grouper
pageId: 28547518
version: 6
lastUpdated: 2026-07-01T05:46:35.538Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547518/GrouperShell+gsh+Types+on+folders+finder+GdgTypeStemFinder
---

Use this class to find objects type attributes on stems

Sample call

> GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStem(stem).assignType("ref").findGdgTypeStemAssignment();

Sample call to find multiple object types attributes on a stem

> Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeStemFinder().assignStem(stem).findGdgTypeStemAssignments();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeStemFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeStemFinder.html)
