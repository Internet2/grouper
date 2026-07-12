---
title: "Grouper rules pattern - Add member to group if added to another group"
space: Grouper
pageId: 28555018
version: 11
lastUpdated: 2026-07-01T05:39:08.049Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555018/Grouper+rules+pattern+-+Add+member+to+group+if+added+to+another+group
---

For various reasons, you may want subjects to be added directly (instead of indirectly) to a group (A) if they have been added to another group (B). The main reason for this, is that you don't want them automatically removed from the group (A) when removed from the other group (B).  
You can also do the inverse of this with 'flattenedMembershipRemove'.

Assign this rule to the group where the member should be added.

# Configure rule for v5+

# Configure rule for v4 and previous

TODO: remove the EL.

| Attribute | Attribute metadata | Value |
| --- | --- | --- |
| rule |  |  |
|  | ruleActAsSubjectId | GrouperSystem |
|  | ruleActAsSubjectSourceId | g:isa |
|  | ruleCheckType | flattenedMembershipAdd |
|  | ruleThenEl | ``` ${ruleElUtils.addMemberToGroupId('<target_group_uuid>', memberId)} ``` |
|  | ruleValid | T |
