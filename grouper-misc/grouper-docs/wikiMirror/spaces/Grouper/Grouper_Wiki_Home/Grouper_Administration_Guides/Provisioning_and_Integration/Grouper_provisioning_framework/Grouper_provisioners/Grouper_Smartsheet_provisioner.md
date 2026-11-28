---
title: "Grouper Smartsheet provisioner"
space: Grouper
pageId: 297533449
version: 2
lastUpdated: 2026-10-14T05:09:04.438Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/297533449/Grouper+Smartsheet+provisioner
---

See also: [Smartsheet external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/297631755/Smartsheet+external+system) and [developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/297041929/Grouper+Smartsheet+provisioner+developer+notes).

Available in Grouper v4.27.0+, v6.6.0+ and v7.9.0+ (GRP-7485). Seat types and account flags from groups are in the same versions (GRP-7487); sync-back is v7.9.0+ only (GRP-7489). Provisions Grouper groups and their members to Smartsheet groups and group members, and manages the Smartsheet organization users, via the Smartsheet API 2.0. Provisioning type is `membershipObjects`.

## Model

| Grouper | Smartsheet | What Grouper does |
| --- | --- | --- |
| target group | group (org-wide, name unique in the org) | Create, rename, change description, delete. Only groups Grouper created are deleted (`deleteGroupsIfGrouperCreated`). |
| target entity | org user | Add to the org, update names and account flags, and deactivate (default) or remove when no longer provisioned. Matched by email. |
| target membership | group member | Added in bulk per group (by Smartsheet user id, email fallback), removed one at a time. |

Groups match on the cached Smartsheet group id, then name (for the first link); users match on the cached user id, then email. So a Grouper group rename is a rename of the same Smartsheet group, and the first sync links to a Smartsheet group or user that already exists.

## Before you turn it on

- **Service account.** The API token belongs to a dedicated System Admin service account, listed in `smartsheetIgnoreUserEmails`.
- **Read-only first.** Run the first full syncs read-only and review what would change.
- **Failsafe.** Turn on the provisioning failsafe (`failsafeUse`, `failsafeMaxOverallPercentEntitiesRemove`, ...).
- **Which users Grouper may deactivate.** `deleteEntitiesIfGrouperCreated` only deactivates users Grouper added. `deleteEntitiesIfNotExistInGrouper` means Grouper owns the whole org: every org user not in the provisioned population is deactivated.
- **Email is the match key.** Use an authoritative email attribute that users cannot change, or a subject could be linked to someone else's Smartsheet account.

## Provisioner settings

| Property | Default | Description |
| --- | --- | --- |
| smartsheetExternalSystemConfigId | (required) | WsBearerToken external system id |
| smartsheetIgnoreUserEmails |  | User emails Grouper never changes (the token service account, other service accounts). Hidden from the compare; a provisioned subject with an ignored email is an error. |
| smartsheetIgnoreGroupNames |  | Group names Grouper never changes |
| disableEntitiesInsteadOfDelete | true | Delete deactivates the user instead of removing them from the org; a later insert reactivates them. |
| smartsheetSendEmailOnAddUser | false | Have Smartsheet email users Grouper adds |
| smartsheetRemoveUserTransferToUserId |  | When removing users, the user id who receives their groups and sheets |
| smartsheetRemoveUserFromSharing | false | When removing users, also remove their access to all org assets |
| smartsheetRetryCount / smartsheetRetrySleepMillis | 5 / 2000 | Retries (doubling backoff, at most 60 seconds) on 429 rate limiting and 502/503/504 |
| smartsheetPlanId |  | Required when seat types are managed (Admin Center, plan details) |
| smartsheetSeatTypeMemberGroupName, ...ContributorGroupName, ...GuestGroupName |  | Grouper groups driving the seat type (see below) |
| smartsheetSeatTypeDefault |  | Seat for a provisioned user in no seat group; blank leaves their seat alone |
| smartsheetDowngradeProvisionalMembers | false | Whether Grouper may downgrade a PROVISIONAL_MEMBER |
| smartsheetAdminGroupName, smartsheetGroupAdminGroupName, smartsheetLicensedSheetCreatorGroupName, smartsheetResourceViewerGroupName |  | Grouper groups driving the account flags (see below) |

## Target attributes

**Group**: `id` (cache it, match on it), `name` (unique in the org, match on it for the first link), `description`.

**Entity (user)**: `id` (cache it, match on it), `email` (match on it for the first link), `firstName`, `lastName`. The account flags and `seatType` are added automatically when their group settings are used.

## Managing users: add, or link only

- **Add users (default).** Grouper adds a provisioned subject with no Smartsheet user to the org, so group sharing is ready right away. With user auto provisioning (UAP) the user is added directly; otherwise Smartsheet invites them (PENDING). On user subscription model plans an added user can count toward seats.
- **Link only** (`insertEntities=false` and `errorHandlingTargetObjectDoesNotExistIsAnError=false`). Grouper never adds users or sends invites. A member with no Smartsheet user yet is skipped quietly and added to their groups by the first full sync after they appear (e.g. after their first SSO login).

## Seat types and account flags from Grouper groups

Seat types and account flags are driven by plain Grouper groups named in the provisioner settings, e.g. in a `policy:seatTypesToSmartsheet` folder. The groups do not need to be provisionable: a membership change in one reaches the incremental sync. **Run a full sync after setting or changing a group name** (that is when Grouper starts watching the group). A configured group that does not exist fails the sync.

- **A blank setting means Grouper never touches that seat or flag.**
- **A configured flag is owned by Grouper**: true for members of the group, false for every other provisioned user, including flags set by hand in Smartsheet.
- **Highest seat wins** when a user is in several seat groups: MEMBER, then GUEST, then CONTRIBUTOR. GUEST is only for users outside the org. PROVISIONAL_MEMBER cannot be set by the API.
- **Provisional members** are not downgraded unless `smartsheetDowngradeProvisionalMembers` (an API downgrade is permanent), but are upgraded to MEMBER if in the member group.
- **Default seat.** With `smartsheetSeatTypeDefault=CONTRIBUTOR`, people who leave the member group give their paid seat back. Grouper then manages every provisioned user's seat, so run read-only first.

> These groups are privilege grants. Anyone who can change the membership of the System Admin or Group Admin group (UPDATE on it, or on a group that is a member of it) can grant that Smartsheet privilege. Restrict UPDATE and ADMIN on them to your Smartsheet administrators.

Which settings apply depends on the plan type. On classic plans the license is the licensed sheet creator flag; on user subscription model plans it is the seat type (MEMBER paid, CONTRIBUTOR free) and Smartsheet forces licensedSheetCreator to true. groupAdmin and resourceViewer (access to resource views) are permissions in either model. Smartsheet refuses `licensedSheetCreator=false` for a Group Admin or Resource Viewer, so a licensed sheet creator group must include them.

## Group names

Group names are unique in the whole org and are what people pick when sharing a sheet. The example uses the Grouper `displayExtension`. Two provisioned groups with the same name collide (error 1103). The first sync links a Grouper group to an existing Smartsheet group of the same name and then manages its members; to keep Grouper-managed groups separate, use a prefix, e.g. `${'grouper-' + grouperProvisioningGroup.displayExtension}`.

## Sync-back

v7.9.0+ only. With `loadEntitiesToGenericGrouperTable`, `loadGroupsToGenericGrouperTable` and `loadMembershipsToGenericGrouperTable`, the provisioner mirrors Smartsheet into the generic provisioning tables. The mirror is the full view of Smartsheet, including ignored and deactivated users and ignored groups. `fullSyncUsersFromSyncBack`, `fullSyncMembershipsFromSyncBack` and `fullSyncGroupsFromSyncBack` serve a full sync from the mirror; memberships are the main saving, since a live full sync reads each group's members (one call per group). The same rules (ignore lists, deactivated users) apply to the mirror as to a live read. A user created outside Grouper reaches the mirror on the next live read of users.

## CRUD operations

| Object | Operation | Smartsheet API |
| --- | --- | --- |
| Group | retrieve | GET /2.0/groups (paged), GET /2.0/groups/{id} (with members) |
| Group | insert / update / delete | POST /2.0/groups, PUT /2.0/groups/{id}, DELETE /2.0/groups/{id} |
| User | retrieve | GET /2.0/users (paged), GET /2.0/users?email=, GET /2.0/users/{id} |
| User | insert / update | POST /2.0/users (or reactivate), PUT /2.0/users/{id} |
| User | seat type | POST /2.0/users/{id}/plans/{planId}/upgrade or /downgrade |
| User | delete | POST /2.0/users/{id}/deactivate, or DELETE /2.0/users/{id} |
| Membership | insert / delete | POST /2.0/groups/{id}/members (array), DELETE /2.0/groups/{id}/members/{userId} |

## Behavioral notes

- **Invited users.** PENDING users are treated as present and can be put in groups. DECLINED users are left alone (never re-invited) and listed in the debug log.
- **Deactivate vs remove.** Deactivate keeps the user's sharing and assets and is reversible. It is not available on Smartsheet Gov, for users created by a Smartsheet directory integration (Okta / Microsoft Entra ID; those are managed only in the IdP and should not also be managed by Grouper), or for users whose email is not on the plan's domains. These fail as per-user errors. Remove takes the user out of the org; set `smartsheetRemoveUserTransferToUserId` so their groups and sheets are not orphaned.
- **Users outside the org** (seen when a plan id is set) are never deactivated or removed.
- **Rate limits.** Smartsheet limits requests per token (429, errorCode 4003); the provisioner backs off and retries.
