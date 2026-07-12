---
title: "Do not allow non sysadmins to add EveryEntity to groups or privileges"
space: Grouper
pageId: 28549299
version: 1
lastUpdated: 2023-07-03T21:25:17.853Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549299/Do+not+allow+non+sysadmins+to+add+EveryEntity+to+groups+or+privileges
---

This is included in grouper v4.4.0+ and v5.1.1+.

You can veto operations of non system admins from adding EveryEntity to group memberships or privileges.

grouper.properties

```
# if you do not want non sysadmins to be able to add EveryEntity to a group or privilege
# {valueType: "boolean", requiresRestart: "true"}
grouper.enable.rule.cannotAddEveryEntity = false
```

Error message:

```
# veto message when cannot add every entity
hook.veto.cannotAddEveryEntity = Error: you cannot add EveryEntity to a group or privilege.  Only a system administrator can do this.
```
