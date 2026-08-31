---
title: "Grouper provisioning roles"
space: Grouper
pageId: 28555286
version: 8
lastUpdated: 2026-07-12T05:03:59.748Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555286/Grouper+provisioning+roles
---

Sometimes a provisioner will need to provision roles for the target.

An example is Azure, the group could have owners.

The provisioner could have metadata for the group of owners. This would be a group name or a drop down of available group names. Another metadata for the list (members (default), admins, updaters, readers, etc). If the group is blank, then the list refers to the group that is provisionable. If the list is blank, then its the members list. The provisioned roles are the members of the group, not the group itself.

The provisioning framework has the following database changes:

- The group in the grouper_sync_groups table as 'T' for role_group, or 'F' for role_group. Note: provisionable_start / end is only updated for normal provisionable groups
- The entities in the grouper_sync_members table as 'T' for role_user, or 'F' for role_user.
- A new table calls grouper_sync_role, holds the roles: member is a normal member, admins for Azure members, or other arbitrary roles for a provisioner based on configuration or built in to the provisioner
- The memberships in the grouper_sync_memberships table will have a column: provisioning_role_internal_id which is a foreign key to the grouper_sync_role table. The
- A new table grouper_sync_group_role will link each provisionable group to roles and lists (based on metadata). i.e. the a:b:c group has x:y:z as admins. Note, we do not need entries for normal provisionable member role.

The change log consumer will see changes in metadata, or changes in group membership for roles and process those changes.

Role memberships can be represented as attributes of a group or a user. The DAO will handle inserts/deletes of that translated attribute.

## Examples

Set a boolean based on a group membership

```
${provisioningEntityWrapper.isInGroup('test2:testGroup2')}

${provisioningEntityWrapper.isHasPrivilege('test2:testGroup2', 'readers')}
```

Have a list of subjectIds or identifiers

- the list is either: members, readers, etc
- the identifier: subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email

```
${provisioningGroupWrapper.thisGroupMembers('subjectId')}

${provisioningGroupWrapper.thisGroupPrivilegeHolders('read', 'subjectIdentifier0')}

${provisioningGroupWrapper.groupMembers('test:testGroup', 'subjectId')}

${provisioningGroupWrapper.groupPrivilegeHolders('test2:testGroup2', 'read', 'email')}
```
