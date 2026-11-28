---
title: "Grouper Smartsheet provisioner developer notes"
space: GrIntDev
pageId: 297041929
version: 1
lastUpdated: 2026-10-14T05:04:28.426Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/297041929/Grouper+Smartsheet+provisioner+developer+notes
---

Developer notes for the [Grouper Smartsheet provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/297533449/Grouper+Smartsheet+provisioner) (v4.27.0+, v6.6.0+, v7.9.0+; sync-back and the MCP lookup v7.9.0+ only). Package `edu.internet2.middleware.grouper.app.smartsheet`.

## Links

- (API reference) [https://developers.smartsheet.com/api/smartsheet/openapi](https://developers.smartsheet.com/api/smartsheet/openapi)
- (Error codes) [https://developers.smartsheet.com/api/smartsheet/error-codes](https://developers.smartsheet.com/api/smartsheet/error-codes)
- (Seat type changes) [https://developers.smartsheet.com/api/smartsheet/guides/users/automate-user-seat-type-management/execute-user-upgrades-and-downgrades](https://developers.smartsheet.com/api/smartsheet/guides/users/automate-user-seat-type-management/execute-user-upgrades-and-downgrades)

## Jiras

| Jira | What |
| --- | --- |
| GRP-7485 | Base provisioner: groups, users, memberships, mock, tests |
| GRP-7486 | Member add by id (email fallback), unset flags never sent, link-only mode |
| GRP-7487 | Seat types and account flags from Grouper groups |
| GRP-7488 | MCP `admin_external_system_get` Smartsheet user lookup |
| GRP-7490 | Framework: target DAO can filter objects reconstructed from the sync-back cache |
| GRP-7489 | Sync-back |

GRP-7485 through GRP-7487 are in v4.27.0+, v6.6.0+ and v7.9.0+; GRP-7488 through GRP-7490 are v7.9.0+ only.

## Classes

| Class | Purpose |
| --- | --- |
| SmartsheetProvisioner | Entry point; membershipObjects; wires the DAO, translator, compare and native sync |
| SmartsheetProvisionerConfiguration | Runtime settings; registers `seatType` and the flag attributes automatically when their group settings are used |
| SmartsheetProvisioningConfiguration | UI registration |
| SmartsheetTargetDao | CRUD; the shared user / group / membership rules; the GRP-7490 filter hooks |
| SmartsheetApiCommands | HTTP calls, paging, retry, ignore lists, sync-back capture |
| SmartsheetProvisioningTranslator | Computes seatType and flags with `isInGroup` (records group dependencies so non-provisionable groups reach incremental) |
| SmartsheetProvisioningCompare | seatType compare: VIEWER equals CONTRIBUTOR; a provisional member matches a computed CONTRIBUTOR / GUEST unless downgrades are allowed |
| SmartsheetProvisioningTargetNativeSync | Sync-back capture beans; defaults are every managed attribute in the compared form |
| SmartsheetUser / SmartsheetGroup / SmartsheetMembership | Beans and mock tables (`mock_smartsheet_*`) |
| SmartsheetMockServiceHandler | Mock API at `/grouper/mockServices/smartsheet` |

## Tests

`AllSmartsheetProvisionerTests` runs `SmartsheetProvisionerTest` and `SmartsheetProvisioningTargetNativeSyncTest`. API-level tests hit the mock over HTTP; full / incremental tests are gated by `tomcatRunTests()` and need the mock-services Tomcat with the provisioning daemon off. The mock checks the bearer token against `grouper.wsBearerToken.smartsheetDev.accessTokenPassword` via `grouperTest.exampleSmartsheet.mockExternalSystem.configId`. The sync-back tests are also in `FullSyncFromSyncBackSuite`, and the MCP lookup is tested in `GrouperMcpAdminExternalSystemGetTest`. Mock simplifications: POST /users always adds the user as ACTIVE with a CONTRIBUTOR seat, and adding a member who is not an org user returns 404 / 1020.

## API notes

- **Paging**: list calls use `page` and `pageSize=100` and return `{pageNumber, pageSize, totalPages, totalCount, data}`. Larger page sizes are rejected (1018) once `planId` is passed.
- **Writes** return `{message, resultCode, result}`; resultCode 3 is partial success. **Errors** return `{errorCode, message, refId}`.
- **Ids** are large numbers (e.g. 4583173393803140), held as strings.
- **Users**: `status`, the flags and seat fields are System Admin only. `seatType`, `isInternal` and `provisionalExpirationDate` come only with `planId` (and then users outside the org are listed too); `GET /users/{id}` never has seatType, so single-user reads add a plan-scoped `GET /users?email=`.
- **Add user**: `admin` and `licensedSheetCreator` are always sent (a Group Admin / Resource Viewer is licensed when the license flag is not managed). **Update user** cannot change email or seat type; a flag with no value is left out of the PUT.
- **Seat changes**: upgrade takes MEMBER (or GUEST from CONTRIBUTOR), downgrade takes CONTRIBUTOR (or GUEST from MEMBER / PROVISIONAL_MEMBER). Deactivated users must be reactivated first. Upgrading to the current seat returns 200.
- **Group members**: an array add skips existing members (a single object returns 1129). The provisioner sends `{"id": n}` when it has the user id, else `{"email": ...}`. Add-by-id is documented but rarely shown in examples; verify it on a real tenant the first time.
- **Rate limit**: 429 / 4003, retried with doubling backoff.

## Error codes seen

| errorCode | HTTP | Meaning |
| --- | --- | --- |
| 1002 / 1003 | 401 | Token invalid / expired |
| 1004 | 403 | Not authorized (token user lacks the admin role) |
| 1006 / 1020 / 1106 | 404 | Not found / user not found / group not found |
| 1016 | 403 | User is already a member of the org |
| 1103 | 400 | Group name already exists |
| 1129 | 400 | Already exists (single group member add) |
| 1335 / 1354 | 403 | User to deactivate / reactivate is not in the org |
| 4003 | 429 | Rate limited |

## Sync-back design

- The mirror is the full view: users, groups and each group's member list are captured in `SmartsheetApiCommands` as they are read, before any filtering. Ignored groups' members are read only when memberships are mirrored. Membership adds and removes are write-tracked in the DAO; written users and groups are re-read by the end-of-run drain.
- The DAO applies one user rule (ignored, or deactivated with disable-instead-of-delete) and one membership rule (user and group both present) to live reads and, through the GRP-7490 `filterTarget*FromSyncBack` hooks, to objects reconstructed from the cache. A filtered-out user Grouper provisions is "missing" and is re-read live, which is how a deactivated user is reactivated on a cache-served run.
- `retrieveMembershipsByGroup` (and its capability) is required for the framework to serve memberships from the cache; a single-group read only applies the ignore list, since member lists have no status.
- A user created outside Grouper reaches the mirror only on a live read of users, not on a cache-served run.
