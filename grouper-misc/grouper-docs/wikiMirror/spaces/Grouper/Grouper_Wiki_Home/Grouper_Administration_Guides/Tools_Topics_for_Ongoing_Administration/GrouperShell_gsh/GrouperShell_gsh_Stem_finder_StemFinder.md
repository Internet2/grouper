---
title: "GrouperShell (gsh) Stem finder (StemFinder)"
space: Grouper
pageId: 28548738
version: 3
lastUpdated: 2026-07-01T05:43:37.405Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548738/GrouperShell+gsh+Stem+finder+StemFinder
---

Use this class to find stems within the registry

Sample call

> Stem stem = StemFinder.findByName(grouperSession, "test", true);

Sample call to find stems where an attribute def name and a value is assigned

> Set stems = new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName()) .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemFinder.html)
