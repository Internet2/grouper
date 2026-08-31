---
title: "Grouper Dropbox provisioner"
space: Grouper
pageId: 28554223
version: 2
lastUpdated: 2026-07-01T05:40:54.323Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554223/Grouper+Dropbox+provisioner
---

## External system

[Grouper Dropbox external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547380/Dropbox+external+system)

## Links

- (Log in) [https://www.dropbox.com/team/admin](https://www.dropbox.com/team/admin)
- (API) [https://api.dropboxapi.com/2/team](https://api.dropboxapi.com/2/team)
- (API Docs) [https://www.dropbox.com/developers/documentation/http/teams](https://www.dropbox.com/developers/documentation/http/teams)
- (App Console) [https://www.dropbox.com/developers/apps](https://www.dropbox.com/developers/apps)

## Overview

The Dropbox Provisioner manages **team members** (entities), **company-managed groups**, and **group memberships** in a Dropbox Business team via the Dropbox Team API.

The provisioner uses the `membershipObjects` membership model: group memberships are first-class objects, retrieved and written per group. Dropbox concepts managed by this provisioner are:

- **Members** - Dropbox Business team members. Matched by an `external_id` that Grouper sets (e.g. the pennname). Created via /2/team/members/add_v2, updated via set_profile_v2, and removed via /2/team/members/remove.
- **Groups** - company-managed Dropbox groups. Grouper only manages `company_managed` groups (membership is API-managed); `user_managed` groups are left alone. Matched by `group_external_id`.
- **Group memberships** - a member's membership in a group, with an access type of `member` or `owner`.

Optionally, the provisioner can manage each member's **Dropbox admin role** via the admin-role overlay (see *Admin-role overlay* below). This is disabled unless an admin-role folder is configured.

The provisioner class is `edu.internet2.middleware.grouper.app.dropbox.DropboxProvisioner`.

## Provisioner configuration

The following configuration properties are specific to the Dropbox provisioner. These are set via the provisioner configuration in the Grouper loader properties (database or file), with the prefix `provisioner.<configId>.`.

| Config Suffix | Required? | Default | Description |
| --- | --- | --- | --- |
| `dropboxExternalSystemConfigId` | Yes |  | The external system config id (WsBearerToken) for Dropbox. The accessTokenPassword holds the plain Dropbox team API bearer token. |
| `dropboxAdminRoleFolderName` | No |  | Grouper folder (path) whose child groups are admin-role markers, one per built-in Dropbox admin role. Membership in these groups drives each member's Dropbox admin role (highest tier wins). **If blank, admin roles are not pulled or managed at all.** See *Admin-role overlay* below. |
| `dropboxIgnoreUserEmails` | No |  | Comma-separated list of member emails to ignore during provisioning. These members are filtered out of retrieve operations and are never created, updated, or removed. Use this to protect admin or service accounts. |
| `dropboxIgnoreGroupNames` | No |  | Comma-separated list of group names to ignore during provisioning. These groups are filtered out of retrieve operations and are never created, updated, or deleted. Use this to protect built-in or externally-managed groups. |
| `dropboxWipeDataOnRemove` | No | `true` | Whether to wipe the member's Dropbox data when removing them from the team (standard offboarding). Passed as `wipe_data` on /2/team/members/remove. |
| `dropboxKeepAccountOnRemove` | No | `false` | Whether to keep the account (convert to a personal Basic account) when removing a member, rather than fully deleting it. Passed as `keep_account` on /2/team/members/remove. |

## Provisioning attributes

#### Group attributes

Grouper manages company-managed Dropbox groups and matches them by `externalId` (mapped to the Dropbox `group_external_id` field).

| Grouper Attribute Name | Type | Required? | Dropbox API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String | Yes | `group_id` | Native Dropbox group id (e.g. `g:abc123`). **Read-only / select-only**, assigned by Dropbox on create. Used as the GroupSelector for update/delete/membership operations. |
| `name` | String | Yes | `group_name` | Group name shown in the Dropbox admin console. Translate from the Grouper group's display extension or name. |
| `externalId` | String | Yes | `group_external_id` | **Match key.** Grouper sets this to a stable value (e.g. the Grouper group idIndex or name) so groups are matched reliably across syncs. |
| `managementType` | String | No | `group_management_type` (.tag) | Either `company_managed` or `user_managed`. Grouper-created groups are always `company_managed`. User-managed groups are not provisioned. |
| `memberCount` | Integer | No | `member_count` | **Read-only.** Member count from list/get_info responses; not written by Grouper. |
| `adminRole` | String | No | n/a (overlay marker) | On admin-role marker groups only: the built-in Dropbox admin role name this group represents (one of the 8 below). These groups do NOT create Dropbox groups -- see *Admin-role overlay*. |

#### Entity (member) attributes

Grouper matches members by `external_id` and uses the native `team_member_id` for all selector-based operations.

| Grouper Attribute Name | Type | Required? | Dropbox API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String | Yes | `team_member_id` | Native Dropbox team member id (e.g. `dbmid:abc123`). **Read-only / select-only**, assigned by Dropbox. Used as the UserSelectorArg for membership, profile, remove, and admin-role calls. |
| `email` | String | Yes | `email` | Member email address. Used on add (member_email) and as a get_info_v2 selector. Translate from the Grouper subject email/EPPN. |
| `externalId` | String | Yes | `external_id` | **Match key.** Grouper sets this to a stable value (e.g. pennname) so members are matched across syncs. |
| `givenName` | String | No | `name.given_name` | Given (first) name. Sent on add (member_given_name) and update (new_given_name). Defaults to "Unknown" if blank on add. |
| `surname` | String | No | `name.surname` | Surname (last name). Sent on add (member_surname) and update (new_surname). Defaults to "Unknown" if blank on add. |
| `status` | String | No | `status` (.tag) | **Read-only.** One of `active`, `invited`, `suspended`, `removed`. |
| `accountId` | String | No | `account_id` | **Read-only.** Dropbox account id (distinct from team_member_id). |
| `adminRole` | String | No | roles[].name (via set_admin_permissions_v2) | The member's effective Dropbox admin role name (one of the 8 built-in roles) or blank for member_only. Only populated and managed when the admin-role overlay is enabled. Computed by full sync as the highest tier among the member's admin-role group memberships. |

#### Membership notes

A group membership carries an **access type**: `member` (regular member) or `owner` (group owner / manager). The access type is encoded as a Dropbox union (`{".tag": "member"}` or `{".tag": "owner"}`) on add and set_access_type. Memberships are retrieved per group via /2/team/groups/members/list and written via /2/team/groups/members/add and /remove.

## CRUD operations

| Object | Operation | Supported? | Notes |
| --- | --- | --- | --- |
| Entity (Member) | Retrieve all | Yes | POST /2/team/members/list_v2, paged via cursor/has_more + list/continue_v2. Also caches admin role name-to-id pairs seen in responses. |
| Retrieve one | Yes | POST /2/team/members/get_info_v2 by external_id, email, or team_member_id selector |
| Insert | Yes | POST /2/team/members/add_v2 (may return an async job to poll) |
| Update | Yes | POST /2/team/members/set_profile_v2 for profile fields; set_admin_permissions_v2 for admin role (overlay only) |
| Delete | Yes | POST /2/team/members/remove with wipe_data / keep_account per config (may return an async job to poll) |
| Group | Retrieve all | Yes | POST /2/team/groups/list, paged via cursor/has_more + list/continue |
| Retrieve one | Yes | POST /2/team/groups/get_info by group_id |
| Insert / Update | Yes | POST /2/team/groups/create and /2/team/groups/update (selected by group_id) |
| Delete | Yes | POST /2/team/groups/delete (returns LaunchEmptyResult; async job polled to completion) |
| Group Membership | Retrieve all | Yes | POST /2/team/groups/members/list per group, paged via cursor/has_more + members/list/continue |
| Insert | Yes | POST /2/team/groups/members/add with access_type; batched per group (may poll async) |
| Delete | Yes | POST /2/team/groups/members/remove; batched per group (may poll async) |
| Admin role (overlay) | Retrieve | Yes* | Read from roles[] on members/list_v2 and get_info_v2. *Only when an admin-role folder is configured. |
| Set | Yes* | POST /2/team/members/set_admin_permissions_v2 with resolved role ids. *Only when an admin-role folder is configured. |

## Admin-role overlay

Dropbox has 8 built-in admin roles. Rather than modeling these as Dropbox groups (they are not groups), the provisioner offers an **admin-role overlay**: a Grouper folder of marker groups, one per admin role, whose memberships drive each member's Dropbox admin role.

To enable it, set `dropboxAdminRoleFolderName` to a Grouper folder. Create one child group per admin role, with the group **extension** exactly matching the Dropbox admin role name. The 8 roles, in priority order (highest tier first), are:

| Priority | Grouper group extension / Dropbox admin role |
| --- | --- |
| 1 (highest) | `Team_Admin` |
| 2 | `User_Management_Admin` |
| 3 | `Support_Admin` |
| 4 | `Billing_Admin` |
| 5 | `Content_Admin` |
| 6 | `Security_Admin` |
| 7 | `Reporting_Admin` |
| 8 (lowest) | `Compliance_Admin` |

Key behaviors:

- These admin-role marker groups **do NOT create Dropbox groups**. They are read only as a source of admin-role assignment for members.
- Each member's Dropbox admin role is the **highest tier** among the admin-role groups they belong to. The list order above is the hierarchy: `Team_Admin` wins over everything, `Compliance_Admin` is the lowest. A member in `Team_Admin` and `Billing_Admin` becomes `Team_Admin`.
- A member in none of the admin-role groups is set to **member_only** (no admin rights).
- When the admin-role folder is **blank**, admin roles are **not pulled or managed at all** -- the provisioner does not read members' roles[] and never calls set_admin_permissions_v2. Existing admin roles in Dropbox are left untouched.
- The role name is resolved to a Dropbox `role_id` by caching the {role_id, name} pairs seen during member retrieval; set_admin_permissions_v2 is then called with the resolved id (empty list demotes to member_only).

## Behavioral notes

### Membership model

This provisioner uses `membershipObjects`. Group memberships are retrieved per group and written incrementally (add/remove specific members), not as a full-list replacement. Each membership carries an access type of `member` or `owner`.

### Company-managed groups only

Grouper only manages `company_managed` Dropbox groups; their membership is API-managed. Groups created by the provisioner are always company_managed (with `add_creator_as_owner=false` so the API caller is not added as an owner). User-managed groups are filtered out and left alone.

### Member remove behavior

Removing a member calls /2/team/members/remove with `wipe_data` and `keep_account` taken from `dropboxWipeDataOnRemove` (default true) and `dropboxKeepAccountOnRemove` (default false). Adjust these to match your offboarding policy: keep_account=true converts the member to a personal Basic account instead of deleting it.

### Ignoring members and groups

Use `dropboxIgnoreUserEmails` and `dropboxIgnoreGroupNames` (comma-separated) to protect admin accounts, service accounts, or externally-managed groups. Ignored objects are filtered out of all retrieve operations and are never created, updated, or removed.

### Async operations

Several write operations (group delete, member add/remove, group membership add/remove) can return a Dropbox `LaunchEmptyResult` with an `async_job_id`. The provisioner polls the matching job_status endpoint until the job reports `complete` (or throws on `failed`). See the developer notes for endpoint specifics.

### Paging

List endpoints (groups/list, members/list_v2, groups/members/list) use Dropbox cursor paging: an initial call returns up to `limit` items plus a `cursor` and `has_more` flag; the provisioner calls the matching `/continue` endpoint with the cursor until `has_more` is false.
