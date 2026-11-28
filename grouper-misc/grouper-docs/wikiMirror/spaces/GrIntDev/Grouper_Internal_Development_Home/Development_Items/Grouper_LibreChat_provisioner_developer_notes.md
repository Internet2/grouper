---
title: "Grouper LibreChat provisioner developer notes"
space: GrIntDev
pageId: 396951558
version: 2
lastUpdated: 2026-11-20T10:17:12.815Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/396951558/Grouper+LibreChat+provisioner+developer+notes
---

Developer notes for the LibreChat provisioner, added after v7.9.1. User documentation: [Grouper LibreChat provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/397475863/Grouper+LibreChat+provisioner).

## Links

- [Grouper LibreChat provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/397475863/Grouper+LibreChat+provisioner) (user doc)
- [Writing GSH templates in Java](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549690/Writing+GSH+templates+in+Java) (externalSystem templates, web service dynamic authentication)
- LibreChat admin API source: packages/api/src/admin/groups.ts and users.ts, routes in api/server/routes/admin/ (github.com/danny-avila/LibreChat)

## Jiras

- GRP-7593: web service external system dynamic authentication from a compiled externalSystem GSH template
- GRP-7594: LibreChat provisioner
- GRP-7595: LibreChat provisioner sync-back

## Classes

Package edu.internet2.middleware.grouper.app.libreChat:

| Class | Purpose |
| --- | --- |
| LibreChatProvisioner | Provisioner, membershipObjects |
| LibreChatProvisionerConfiguration | libreChatExternalSystemConfigId, libreChatGroupIdOnTheSourcePrefix (default grouper:), libreChatEmptyGroupsInsteadOfDelete |
| LibreChatProvisioningConfiguration | UI registration (registered in ProvisioningConfiguration) |
| LibreChatTargetDao | Groups CRUD (managed groups only, re-checked before delete, update and member writes), users read only, memberships one call each |
| LibreChatApiCommands | HTTP calls through the web service external system, paging, ids checked before they go into a URL |
| LibreChatGroup, LibreChatUser, LibreChatMembership | Models; mock tables mock_librechat_group, mock_librechat_user, mock_librechat_membership (Hib3DAO) |
| LibreChatMockServiceHandler | Mock admin API at mockServices/libreChat (MockServiceServlet) |
| LibreChatProvisioningTargetNativeSync | Sync-back native beans and capture methods (GRP-7595) |
| LibreChatLog | Debug logging with elapsed time |

## Tests

- AllLibreChatProvisionerTests runs LibreChatProvisionerTest. API-level tests call the mock over HTTP; the full sync and incremental tests are gated by tomcatRunTests().
- Users are read only, so the config leaves makeChangesToEntities off and sets only selectEntities. Setting insertEntities, updateEntities or deleteEntities, even to false, fails validation ("should be refactored with an upgrade task"). The test setup deletes those keys in case an earlier run stored them.
- errorHandlingTargetObjectDoesNotExistIsAnError is false: a member with no LibreChat user is normal (they have not logged in), and otherwise the job ends in ERROR with "entity error DNE".

## API notes

- Auth: no API keys. The admin API takes a LibreChat JWT (about 15 minutes, SESSION_EXPIRY) for a user with ACCESS_ADMIN, READ_GROUPS, MANAGE_GROUPS, READ_USERS. Sites get it with a dynamic external system template. A load balancer in front of LibreChat may require its own session cookie (e.g. an AWS ALB with Cognito returns an HTML 401 without it).
- Lists return {groups|users|members, total, limit, offset}; limit max 200.
- Groups use "_id"; users use "id". Single group responses are {group: {...}}.
- GET /api/admin/users/search?q= is a case-insensitive prefix match on name, email and username, at least 2 characters, at most 50 results; the provisioner filters for an exact username.
- POST /api/admin/groups/{id}/members takes {userId} (native ObjectId only) and is idempotent; DELETE of a member is idempotent too.
- PATCH /api/admin/groups/{id} changes name, description, email, avatar only (not members).
- Groups have a unique index on (idOnTheSource, source, tenantId).
- DELETE /api/admin/groups/{id} also deletes the group's sharing grants (aclentries).

## Error codes seen

| Code | Meaning |
| --- | --- |
| 400 | Invalid id format, name missing, duplicate idOnTheSource |
| 401 (HTML) | A proxy or load balancer in front of LibreChat wants its own login cookie |
| 401 (JSON) | JWT expired or invalid |
| 403 | Missing capability; TOKEN_REUSE_DISABLED from /api/admin/oauth/refresh when OPENID_REUSE_TOKENS is off |
| 404 | Group or user not found |

## Sync-back

GRP-7595. LibreChatProvisioningTargetNativeSync builds the native beans: users (username, name, email) and groups (name, description, source, raw idOnTheSource with the prefix). Reads are captured in LibreChatApiCommands (user list and search, every local group listed before the managed filter, single group reads, each managed group's full member list); writes in LibreChatTargetDao (group create with the created group, group update re-read by the drain, group delete via markSyncBackGroupForDelete since the framework does not mark deletes, each member add and remove).

- Capabilities: canSyncBack, canFullSyncGroupsFromSyncBack, canFullSyncMembershipsFromSyncBack; not entities, because users are created at first login outside Grouper.
- canRetrieveMembershipsAllByGroup (retrieveMembershipsByGroup) is required: without a membership retrieve capability selectMembershipsInGeneral is false and the framework never serves memberships from the cache. It also makes the framework pair groups with memberships (groups from cache only when memberships are).
- retrieveAllData honors isRetrieveEntities / isRetrieveGroups / isRetrieveMemberships and adds debug counters libreChatRetrieveAllUsersApiCall, libreChatRetrieveAllGroupsApiCall, libreChatRetrieveMembershipsApiCall for tests.
- filterTargetGroupsFromSyncBack keeps managed groups and removes the prefix from idOnTheSource; without that a cached group would not match its Grouper group and Grouper would try to create a duplicate. filterTargetMembershipsFromSyncBack keeps members of managed groups.
- Tests: LibreChatProvisioningTargetNativeSyncTest (unit), testSyncBackMirror and testFullSyncFromSyncBackWarmCache.
