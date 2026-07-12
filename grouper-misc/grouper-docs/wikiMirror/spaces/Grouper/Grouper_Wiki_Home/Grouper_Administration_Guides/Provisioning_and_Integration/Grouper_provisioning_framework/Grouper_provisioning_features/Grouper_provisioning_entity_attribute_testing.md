---
title: "Grouper provisioning entity attribute testing"
space: Grouper
pageId: 28554183
version: 25
lastUpdated: 2026-07-01T05:40:58.369Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554183/Grouper+provisioning+entity+attribute+testing
---

## Settings

| Test Letter | Test setting | Description |
| --- | --- | --- |
| A | Delete memberships | if false do not delete any memberships (unless deleting user) |
| B | Delete if not exist in grouper | delete everything in target not in grouper (though might have some caveats below) |
| C/D | Delete value only if managed by grouper  Delete memberships only in tracked groups | - do not remove attributes from users who are not provisionable in grouper (either with attributes or recently with attributes) - These two options used to be separable, but we've found them to best to always have the same value. - (BBL): Obviously, I'm suggesting combining C/D into a single option within this document. And listing the values as TT or FF to help people understand how to configure this. |
| E | Delete memberships if group unmarked provisionable | - do not remove attributes if the group which represented that attribute exists and was recently unmarked provisionable - im not sure how that works in a full sync once the sync data falls out of the sync table - i think this only makes sense if you have "Delete if not exist in grouper" set to false |
| F | Delete memberships if deleted by Grouper | - If the attribute was represented by a grouper group, and the user was removed, then remove the attribute - i dont know how "Delete value only if managed by Grouper" relates to this, since i would think all things deleted by grouper are managed by grouper |
| G | Delete memberships if created by Grouper | if grouper tracks that it put that attribute in the target, and it is removed by grouper, then it will be removed |

## Attribute values in test

| Value number | Value | LDAP value | Provisionable group | Description |
| --- | --- | --- | --- | --- |
| 1 | valueNotInGrouper | Preexisting | None | some value not tied to a grouper group |
| 2 | unprovisionableGroup | Preexisting | None | attribute exists with the name as a grouper group, but that group is not and never was provisionable |
| 3 | provisionableGroupCreatedByGrouper | CreatedByGrouper | Marked | attribute created by grouper, the membership is not deleted, and group remains provisionable |
| 4 | provisionableGroupExisting | Preexisting | Marked | attribute existed in target, the membership is not deleted, and group remains provisionable |
| 5 | provisionableGroupCreatedByGrouperThenDeleted | CreatedByGrouper | Marked | provisionable group, the provisioner added this attribute, then removed membership |
| 6 | provisionableGroupMembershipExistedDeletedByGrouper | Preexisting | Marked | provisionable group, attribute already existed, membership is in grouper, membership deleted from grouper |
| 7 | provisionableGroupMembershipNotInGrouper | Preexisting | Marked | provisionable group, attribute already existed, user is not in the grouper group (never was) |
| 8 | provisionableGroupCreatedByGrouperUnmarkedProvisionable | CreatedByGrouper | Unmarked | grouper created attribute, group is unmarked provisionable. user is still in group in grouper |
| 9 | provisionableGroupExistedUnmarkedProvisionable | Preexisting | Unmarked | attribute existed in target, group is unmarked provisionable. user is still in group in grouper |
| 10 | provisionableGroupCreatedByGrouperGroupDeletedInGrouper | CreatedByGrouper | MarkedDeleted | grouper created the attribute, group is deleted from grouper |
| 11 | provisionableGroupExistedGroupDeletedInGrouper | Preexisting | MarkedDeleted | attribute existed in target, group is deleted from grouper |
| 12 | provisionableGroupRenamedOldNameCreatedByGrouper | CreatedByGrouper | Marked | group, provisionable, inserted into target, then renamed |
| 13 | provisionableGroupRenamedNewNameCreatedByGrouper | CreatedByGrouper | Marked | after the rename, the new name will be provisioned |
| 14 | provisionableGroupRenamedOldNameExisted | Preexisting | Marked | this value value existed in the target, then renamed |
| 15 | provisionableGroupRenamedNewNameExisted | CreatedByGrouper | Marked | this is the new value after the group is renamed |

## Users in test

| User | Description |
| --- | --- |
| aclark | Provisionable |
| adoe | Not provisionable, has the values that already exist in target (1, 2, 4, 6, 7, 9, 11, 12, 14), does not have memberships in provisionable groups. |

## Tests

Results

| Test | A (delete) | B (if not exist) | C/D (if managed only)(tracked groups only) | E (unmarked provisionable) | F (if grouper deleted) | G (if grouper created) | Daemon | User | 1 (not in grouper, group doesnt exist) | 2 (unprovisionable and never was) | 3 (created) | 4 (existing) | 5 (created deleted) | 6 (existed deleted) | 7 (not in grouper, group exists) | 8 (unmarked provisionable) | 9 (existed unmarked provisionable) | 10 (grouper created group deleted) | 11 (existed group deleted) | 12 (provisionable renamed old name created by grouper) | 13 (provisionable renamed new name created by grouper) | 14 (provisionable renamed old name existed) | 15 (provisionable renamed new name existed) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Do not delete | F | NA | NA | NA | NA | NA | full | aclark | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | add | remain | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| incremental | aclark | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | remain | add | remain | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| Delete if not exists | T | T | FF   (2 config settings) | T | NA | NA | full | aclark | delete | delete | remain | remain | delete | delete | delete | delete | delete | delete | delete | delete | add | delete | add |
| adoe | delete | delete | na | delete | na | delete | delete | na | delete | na | delete | delete | na | delete | na |
| incremental | aclark | remain | remain | remain | remain | delete | delete | remain | delete | delete | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| Delete value only if managed by grouper | T | T | TT | T | NA | NA | full | aclark | remain | remain | remain | remain | delete | delete | delete | delete | delete | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | delete | na | delete | delete | na | delete | na | delete | delete | na | delete | na |
| incremental | aclark | remain | remain | remain | remain | delete | delete | remain | delete | delete | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| Delete memberships if deleted in grouper | T | F | FF | T | T | NA | full | aclark | remain | remain | remain | remain | delete | delete | remain | delete | delete | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | delete | na | delete | remain | na | remain | na | remain | remain | na | remain | na |
| incremental | aclark | remain | remain | remain | remain | delete | delete | remain | delete | delete | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | renamin | na |
| Delete memberships if created in grouper | T | F | FF | T | F | T | full | aclark | remain | remain | remain | remain | delete | remain | remain | delete | remain | delete | remain | delete | add | remain | add |
| adoe | remain | remain | na | delete | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| incremental | aclark | remain | remain | remain | remain | delete | remain | remain | delete | remain | delete | remain | delete | add | remain | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
| Delete memberships if deleted in grouper, do not delete if unmarked provisionable | T | F | FF | F | T | NA | full | aclark | remain | remain | remain | remain | delete | delete | remain | remain | remain | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | delete | na | delete | remain | na | remain | na | remain | remain | na | remain | na |
| incremental | aclark | remain | remain | remain | remain | delete | delete | remain | remain | remain | delete | delete | delete | add | delete | add |
| adoe | remain | remain | na | remain | na | remain | remain | na | remain | na | remain | remain | na | remain | na |
