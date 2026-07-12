---
title: "GrouperShell (gsh) Password for Grouper insert / update / delete (GrouperPasswordSave)"
space: Grouper
pageId: 28548991
version: 8
lastUpdated: 2026-07-01T05:43:01.233Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548991/GrouperShell+gsh+Password+for+Grouper+insert+update+delete+GrouperPasswordSave
---

Use this class to add username and password in grouper registry

Sample call to create a username password for grouper ui

> new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignApplication(GrouperPassword.Application.UI).save();

Sample call to create a username password for grouper webservices

> new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignApplication(GrouperPassword.Application.WS).save();

Sample call to delete a username password for grouper ui

> new GrouperPasswordSave().assignUsername("GrouperSystem").assignApplication(GrouperPassword.Application.UI).assignSaveMode("DELETE").save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/authentication/GrouperPasswordSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/authentication/GrouperPasswordSave.html)
