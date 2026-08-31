---
title: "Programmatic access to Grouper - find group"
space: Grouper
pageId: 28549598
version: 4
lastUpdated: 2026-07-01T05:41:42.379Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549598/Programmatic+access+to+Grouper+-+find+group
---

# Programmatic access to Grouper - find group

This class is used to find a group programmatically. This is going to search by the name in the search box. It will page through the results until it finds the right group. The group will be clicked on and the main group page will be ready.

### Find a group by name

 GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage)  
.assignGroupToFindName("test:test25").browse();

### Find a group by Uuid

 GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage)  
.assignGroupToFindId("a1b2c3d4").browse();

### Find a group by group object

 GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage)  
.assignGroupToFind(group).browse();
