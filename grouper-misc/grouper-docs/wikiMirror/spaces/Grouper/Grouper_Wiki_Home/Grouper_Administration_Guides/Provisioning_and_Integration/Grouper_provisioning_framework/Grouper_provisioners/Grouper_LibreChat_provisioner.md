---
title: "Grouper LibreChat provisioner"
space: Grouper
pageId: 397475863
version: 3
lastUpdated: 2026-11-20T10:17:12.393Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/397475863/Grouper+LibreChat+provisioner
---

Added after v7.9.1 (GRP-7594). The LibreChat provisioner keeps LibreChat groups and their members in sync with Grouper groups, so people can share LibreChat agents, prompts and MCP servers with a group whose membership Grouper keeps current. It uses the LibreChat admin REST API (LibreChat v0.8.5+).

See also: [Writing GSH templates in Java](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549690/Writing+GSH+templates+in+Java) (external system templates, for the LibreChat login) and [Grouper LibreChat provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/396951558/Grouper+LibreChat+provisioner+developer+notes).

## Model

| Grouper | LibreChat | Notes |
| --- | --- | --- |
| Group | Group (source local) | Created, renamed and deleted by Grouper. Only groups whose idOnTheSource starts with the configured prefix are ever read or changed. |
| Entity | User | Read only. LibreChat creates users at their first login. Matched on username (e.g. eppn). |
| Membership | The user in the group's members | Added and removed one call per member. Provisioning type is membershipObjects. |

## Before you turn it on

- A LibreChat user for Grouper (e.g. a service account) with the capabilities ACCESS_ADMIN, READ_GROUPS, MANAGE_GROUPS and READ_USERS. Full ADMIN is not needed.
- A way for Grouper to get a LibreChat token. LibreChat has no API keys: the admin API takes a LibreChat JWT that lasts about 15 minutes. Use a web service external system with "Authentication type" dynamic and a compiled GSH template of type External system that logs in and adds the token (and any cookies a load balancer in front of LibreChat needs). See [Writing GSH templates in Java](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549690/Writing+GSH+templates+in+Java).
- Set the external system's endpoint to the LibreChat base URL (e.g. https://chat.example.edu) and its "Test URL suffix" to api/admin/verify, then click "Test".
- People must log in to LibreChat once before they can be added to a group.
- Only wheel can configure provisioners, GSH templates and external systems.

## Provisioner settings

| Setting | Default | Description |
| --- | --- | --- |
| libreChatExternalSystemConfigId | (required) | Web service external system for LibreChat. |
| libreChatGroupIdOnTheSourcePrefix | grouper: | Groups are created with idOnTheSource = this prefix plus the idOnTheSource attribute. Groups without the prefix are never read, changed or deleted. Use a different prefix for each provisioner pointing at the same LibreChat. |
| libreChatEmptyGroupsInsteadOfDelete | false | Deleting a LibreChat group also deletes everything shared with it. If true, remove all members instead, so the group and its sharing stay but nobody gets access through it. |
| errorHandlingTargetObjectDoesNotExistIsAnError | true | Set to false. A member who has not logged in to LibreChat yet has no user, which is normal; otherwise each such member fails the job. The membership is added on a later sync after they log in. |

## Target attributes

| Object | Attribute | Description |
| --- | --- | --- |
| Group | id | LibreChat group id (select only). Cache it from the target. |
| Group | name | Group name, e.g. from the Grouper display extension. |
| Group | description | Optional, e.g. "Managed in Grouper: ..." so LibreChat admins do not edit it by hand. |
| Group | idOnTheSource | The match key, e.g. from the Grouper group id. Stored in LibreChat with the prefix added and read back with it removed. |
| Entity | id | LibreChat user id (select only). Cache it from the target; memberships use it. |
| Entity | username | The match key: the login name LibreChat gets from SSO (e.g. eppn). Compared case-insensitively. |
| Entity | name, email | Informational only. |

Example (the parts specific to LibreChat):

```text
provisioner.libreChat.class = edu.internet2.middleware.grouper.app.libreChat.LibreChatProvisioner
provisioner.libreChat.libreChatExternalSystemConfigId = libreChat
provisioner.libreChat.provisioningType = membershipObjects
provisioner.libreChat.errorHandlingShow = true
provisioner.libreChat.errorHandlingTargetObjectDoesNotExistIsAnError = false
provisioner.libreChat.targetGroupAttribute.0.name = id
provisioner.libreChat.targetGroupAttribute.1.name = name
provisioner.libreChat.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.libreChat.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayExtension
provisioner.libreChat.targetGroupAttribute.2.name = idOnTheSource
provisioner.libreChat.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.libreChat.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = id
provisioner.libreChat.groupMatchingAttribute0name = idOnTheSource
provisioner.libreChat.targetEntityAttribute.0.name = id
provisioner.libreChat.targetEntityAttribute.1.name = username
provisioner.libreChat.entityMatchingAttribute0name = username
```

Users are read only, so leave "make changes to entities" off and set only "select entities". Translate username from whatever subject attribute matches the LibreChat login name.

## CRUD operations

| Operation | LibreChat admin API |
| --- | --- |
| Retrieve users | GET /api/admin/users (paged), GET /api/admin/users/search?q= (by username) |
| Retrieve groups | GET /api/admin/groups?source=local (paged, filtered to the prefix), GET /api/admin/groups/{id} |
| Retrieve members | GET /api/admin/groups/{id}/members (paged) |
| Insert group | POST /api/admin/groups |
| Update group (name, description) | PATCH /api/admin/groups/{id} |
| Delete group | DELETE /api/admin/groups/{id}, or remove every member when libreChatEmptyGroupsInsteadOfDelete is true |
| Insert / delete membership | POST /api/admin/groups/{id}/members, DELETE /api/admin/groups/{id}/members/{userId} |

## Sync-back

Added after v7.9.1 (GRP-7595). Turn on "Sync users / groups / memberships back into generic grouper tables" (loadEntitiesToGenericGrouperTable, loadGroupsToGenericGrouperTable, loadMembershipsToGenericGrouperTable) to mirror LibreChat into grouper_prov_user, grouper_prov_group and grouper_prov_mship.

- Every LibreChat user and every local group is mirrored (including groups this provisioner does not manage, with their source and idOnTheSource as LibreChat stores them). Memberships are mirrored for managed groups.
- fullSyncMembershipsFromSyncBack and fullSyncGroupsFromSyncBack let a full sync read memberships and groups from the mirror instead of LibreChat. Memberships are the main saving: otherwise each managed group costs one members call per full sync. Groups come from the mirror only when memberships do too.
- Users are always read live (fullSyncUsersFromSyncBack is not supported): LibreChat creates users at first login, outside Grouper, so a cached list would miss new users.
- Cached groups get the same rule as a live read: only managed groups are kept. Members added by hand in LibreChat are only seen on a live read.

## Behavioral notes

- Grouper owns the membership of the groups it manages: anyone added to one by hand in LibreChat is removed on the next full sync.
- Before deleting a group, renaming it, or changing its members, the provisioner re-reads it and refuses if it is no longer managed (e.g. a LibreChat admin changed its idOnTheSource).
- Deleting a LibreChat group also deletes the agents, prompts and MCP servers shared with it. Use libreChatEmptyGroupsInsteadOfDelete to keep them.
