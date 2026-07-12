---
title: "Generic provisioning configuration"
space: Grouper
pageId: 28555478
version: 45
lastUpdated: 2026-07-12T15:27:14.093Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555478/Generic+provisioning+configuration
---

> The info on this page applies to Grouper v2.6 and above.

This page presents the configuration for the [Grouper Provisioning framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework)

## Configuration

> [Scaffolding (start with)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with) is recommended to help you get started with provisioning configuration.

The configName must match the provisioner config name. Example config in grouper-loader.properties is provisioner.<configName>.subjectSourcesToProvision

"target" is the system being provisioned to

LDAP provisioner config

| **Config** | **Example** | **Description** | **Notes** |
| --- | --- | --- | --- |
| class | edu.whatever.MyProvisioner | Class extends the base provisioner class | This class informs configuration decisions. Required. Read-only. |
| hasSubjectLink | true  false | If the subject API is needed to resolve attribute on subject | required, drives requirements of other configurations. defaults to false. |
| hasTargetUserLink | true  false | If subjects need to be resolved in the target before provisioning | defaults to false. required. |
| hasTargetGroupLink | true  false | If groups need to be resolved in the target before provisioning | defaults to false. required. |
| subjectSourcesToProvision | pennperson | subject sources to provision | optional, defaults to all except g:gsa, grouperExternal, g:isa, localEntities. comma separated list. checkboxes. |
| userSearchAttributeName | employeeID | attribute to filter on | required if userAttributes or hasTargetUserLink |
| userSearchAttributeValueFormat | ${subject.id}   ${targetEntity.dn}   ${targetEntity.attributes['uid']} | value for the user search attribute name | required if userAttributes or hasTargetUserLink |
| userAttributeReferredToByGroup | dn | in group memberships, this is the value that refers to the user | optional. show if groupMemberships and hasTargetUserLink  default to dn |
| subjectApiAttributeForTargetUser |  | for subject link, this is the subject api identifier that is needed to look up the target user | required. show if hasSubjectLink |
| groupAttributeReferredToByUser | dn | in user attributes, this is the value that refers to the group | required. show if userAttributes and hasTargetGroupLink. defaults to dn |
| syncMemberToId2AttributeValueFormat | ${targetEntity.attributes['dn']} | main identifier of the user on the target side | show = false |
| syncMemberToId3AttributeValueFormat | ${targetEntity.attributes['uid']} | identifier of the user as referred to by the group | show = false |
| syncMemberFromId2AttributeValueFormat | ${targetEntity.attributes['netId']} | target attribute value that helps look up user | show = false |
| syncMemberFromId3AttributeValueFormat | ${subject.attributes['myLdapId']} | subject attribute value that helps look up user | show = false |
| syncGroupToId2AttributeValueFormat |  |  | show = false |
| syncGroupToId3AttributeValueFormat |  |  | show = false |
| syncGroupFromId2AttributeValueFormat |  |  | show = false |
| syncGroupFromId3AttributeValueFormat |  |  | show = false |
| userSearchAttributes | dn,cn,uid,mail,   samAccountName,uidNumber,   objectclass | attributes to search when getting users | optional. show if userAttributes or hasTargetUserLink. |
| userAttributesMultivalued | someAttr | everything is assumed to be single valued except objectclass and the provisionedAttributeName | optional. show if userAttributes or hasTargetUserLink. |
| createMissingUsers | true or false |  | defaults false, optional. show if userAttributes or hasTargetUserLink |
| createMissingGroups | true or false |  | defaults to true. show if groupMemberships or hasTargetGroupLink |
| groupSearchAttributeName | gidNumber | attribute name to filter on | show if groupMemberships or hasTargetGroupLink  required |
| groupSearchAttributeValueFormat | ${syncGroup.groupIdIndex} | value to filter group on | show if groupMemberships or hasTargetGroupLink  required |
| groupSearchAttributes | cn,gidNumber,samAccountName,objectclass | attributes to get if searching for groups | optional  show if groupMemberships or hasTargetGroupLink |
| groupAttributesMultivalued | someAttr | everything is assumed to be single valued except object class. List attributes in the groupSearchAttribute which are multivalued | optional. show if groupMemberships or hasTargetGroupLink |
| deleteInTargetIfInTargetAndNotGrouper | true or false | if groups in full sync should be deleted if in group all filter and not in grouper   or for attributes delete other attribute not provisioned by grouper | default to false |
| deleteInTargetIfDeletedInGrouper | true or false | if groups that were created in grouper were deleted should it be deleted in ldap?   or for attributes, delete attribute value if deleted in grouper | default to true |
| membershipFields | members  read,admin  update,admin  admin | if provisioning normal memberships or privileges | default to "members" for normal memberships |
| recalculateAllOperations | true or false | If the target should be checked before sending actions. e.g. if an addMember is made to a provisionable group, then check the target to see if the entity is already a member first. | default to false |
| membershipsConvertToGroupSyncThreshold | 500 | If there are this number of incremental memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency.  -1 to not use this feature | grouper-loader.properties  provisionerDefault.membershipsConvertToGroupSyncThreshold  which is 500 |
| scoreConvertToFullSyncThreshold | 10000 | In incremental processing, each provisionable group/entity to sync memberships counts as 10, each provisionable membership to sync counts as 1. If the total score is more than this number, it will convert the incrementals to a a full sync. e.g. 10000 individual memberships to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync | grouper-loader.properties  provisionerDefault.scoreConvertToFullSyncThreshold  which is 10000 |

## Provisioner batching configuration

This applies to Grouper v7.0.3+, v6.1.1+, v4.22.2+

The batching section controls how many objects the provisioning framework sends to the target DAO in each batch. These are advanced settings that generally should not be adjusted. The provisioner DAO (e.g. TeamDynamix, SCIM, LDAP) registers its maximum supported batch sizes as capabilities. The user can optionally reduce these batch sizes via configuration, but cannot increase them beyond the DAO maximum.  
**When to use:** Only adjust these settings if you are experiencing rate limiting or timeout issues with the target system, or if the target system has documented payload size limits. In most cases the defaults set by the provisioner DAO are optimal.

**How It Works**

1. The target DAO registers its maximum batch sizes during provisioner initialization (e.g. TeamDynamix sets insert/delete memberships to 400).
2. The user can optionally configure a batch size in the UI under the "Batching" section.
3. The effective batch size is the **minimum** of the user-configured value and the DAO maximum. This means you can only reduce batch sizes, not increase them beyond what the DAO supports.
4. The effective batch sizes are logged in the daemon log output as `provisionerBatchingXxx` entries (visible when "Log all objects verbose" is enabled in the Advanced section).

**Show Batching Options**

The `provisionerBatchingShow` toggle must be enabled to reveal the individual batch size settings. This keeps the configuration UI clean for typical use cases.

**Default Batch Size**

| **Setting** | `provisionerBatchingDefault` |
| --- | --- |
| **Description** | Default batch size for all operations. Applies to any operation that does not have a specific batch size configured below. The DAO default is typically 20 unless the provisioner overrides it. |
| **Default** | Blank (use provisioner DAO default, typically 20) |
| **Valid values** | Integer >= 1, or blank |

**Membership Batch Sizes**

| **Setting** | **Description** | **When applicable** |
| --- | --- | --- |
| `provisionerBatchingInsertMemberships` | How many memberships to send per batch when adding members to groups in the target. | Only if the provisioner DAO supports inserting multiple memberships at once (e.g. TeamDynamix bulk member endpoint). |
| `provisionerBatchingDeleteMemberships` | How many memberships to send per batch when removing members from groups in the target. | Only if the provisioner DAO supports deleting multiple memberships at once. |
| `provisionerBatchingUpdateMemberships` | How many memberships to send per batch when updating membership attributes in the target. | Only if the provisioner DAO supports updating multiple memberships at once. |
| `provisionerBatchingRetrieveMemberships` | How many memberships to look up per batch when retrieving memberships from the target. | Only if the provisioner DAO supports retrieving multiple memberships at once. |

**Group Batch Sizes**

| **Setting** | **Description** | **When applicable** |
| --- | --- | --- |
| `provisionerBatchingInsertGroups` | How many groups to send per batch when creating groups in the target. | Only if the provisioner DAO supports creating multiple groups at once. |
| `provisionerBatchingDeleteGroups` | How many groups to send per batch when deleting groups in the target. | Only if the provisioner DAO supports deleting multiple groups at once. |
| `provisionerBatchingUpdateGroups` | How many groups to send per batch when updating group attributes in the target. | Only if the provisioner DAO supports updating multiple groups at once. |
| `provisionerBatchingRetrieveGroups` | How many groups to look up per batch when retrieving individual groups from the target. | Only if the provisioner DAO supports retrieving multiple groups at once. |

**Entity Batch Sizes**

| **Setting** | **Description** | **When applicable** |
| --- | --- | --- |
| `provisionerBatchingInsertEntities` | How many entities to send per batch when creating users/entities in the target. | Only if the provisioner DAO supports creating multiple entities at once. |
| `provisionerBatchingDeleteEntities` | How many entities to send per batch when deleting users/entities in the target. | Only if the provisioner DAO supports deleting multiple entities at once. |
| `provisionerBatchingUpdateEntities` | How many entities to send per batch when updating user/entity attributes in the target. | Only if the provisioner DAO supports updating multiple entities at once. |
| `provisionerBatchingRetrieveEntities` | How many entities to look up per batch when retrieving individual users/entities from the target. | Only if the provisioner DAO supports retrieving multiple entities at once. |

**Example: TeamDynamix**

The TeamDynamix provisioner DAO registers insert and delete memberships batch sizes of 400 because the TeamDynamix API has a bulk member endpoint that accepts an array of user IDs. All other operations use the framework default of 20 since the TeamDynamix API processes groups and entities individually.  
**Before this fix:** The TeamDynamix provisioner had a default batch size of 1, which meant 48,937 individual API calls for membership inserts instead of batching them. With the batch size set to 400, this is reduced to approximately 5 API calls (one per group).

If you are experiencing rate limiting with TeamDynamix (60 calls per 60 seconds), you could reduce the retrieve batch sizes:

- Set `provisionerBatchingRetrieveEntities` to a lower value to slow down entity lookups
- Set `provisionerBatchingRetrieveGroups` to a lower value to slow down group lookups

**Debugging**

The effective batch sizes (after applying the minimum of user config and DAO maximum) are logged in the daemon log output. Look for entries like:

- `provisionerBatchingInsertMemberships: 400`
- `provisionerBatchingDefault: 20`
- `provisionerBatchingRetrieveGroups: 20`

These appear in the daemon log whenever the batch size is first used during a provisioning run. Enable "Log all objects verbose" in the Advanced section for detailed provisioning output.

**Validation**

All batch size values must be integers greater than or equal to 1. Blank values mean "use the provisioner default." The UI will show a validation error if an invalid value is entered. The effective batch size also has a floor of 1 as a safety measure.

## Provisioning types

**See Also**

[Grouper Provisioning Configuration Scaffolding (start with)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with)
