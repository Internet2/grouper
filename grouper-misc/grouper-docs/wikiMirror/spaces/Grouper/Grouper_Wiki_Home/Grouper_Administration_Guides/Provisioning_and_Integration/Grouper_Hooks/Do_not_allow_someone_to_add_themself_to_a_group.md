---
title: "Do not allow someone to add themself to a group"
space: Grouper
pageId: 28547715
version: 5
lastUpdated: 2019-03-17T08:03:20.616Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547715/Do+not+allow+someone+to+add+themself+to+a+group
---

This is included in grouper 2.4 api patch 37, and 2.4 ui patch 18.

You can enforce separation of duties by not allowing a (updater/admin) user to add themself to a group. Someone else has to.

By default a group admin can assign this to a group. But by default only wheel group members can unassign. This is configurable below. Group viewers can see if it is assigned (if this feature is enabled).

This is implemented with an implicit hook (you don't have to configure it)

## Veto screen

## View group screen

View if this feature is assigned to a group (viewers of group can see this in "more info")

## Edit if allowed to edit

## Edit screen if not allowed to edit

## Configure

To enable this feature, in grouper.properties set this to true

```
# if you want a checkbox to not let users add themself to a group
# {valueType: "boolean"}
grouper.enable.rule.cannotAddSelfToGroup = true

```

Here are all the config options:

```
# if you want a checkbox to not let users add themself to a group
# {valueType: "boolean", requiresRestart="true"}
grouper.enable.rule.cannotAddSelfToGroup = false

# if you want group admins to be able to assign cannotAddSelf
# {valueType: "boolean"}
grouper.cannotAddSelfToGroup.allowAssignByGroupAdmins = true

# if group admins are not allowed to assign cannotAddSelf, then this group can, if blank then only Grouper admins can assign
# {valueType: "string"}
grouper.cannotAddSelfToGroup.groupCanAssignByGroupAdmins = $$grouper.rootStemForBuiltinObjects$$:cannotAddSelfToGroup:canAssignCannotAddSelf

# if you want group admins to be able to revoke cannotAddSelf
# {valueType: "boolean"}
grouper.cannotAddSelfToGroup.allowRevokeByGroupAdmins = false

# if group admins are not allowed to revoke cannotAddSelf, then this group can, if blank then only Grouper admins can revoke
# {valueType: "string"}
grouper.cannotAddSelfToGroup.groupCanRevokeByGroupAdmins = $$grouper.rootStemForBuiltinObjects$$:cannotAddSelfToGroup:canRevokeCannotAddSelf
```

## Implementation

This is implemented as a single attribute assigned to a group
