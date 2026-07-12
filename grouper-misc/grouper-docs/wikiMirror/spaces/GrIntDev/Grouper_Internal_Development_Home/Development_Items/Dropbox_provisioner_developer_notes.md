---
title: "Dropbox provisioner developer notes"
space: GrIntDev
pageId: 48792475
version: 9
lastUpdated: 2026-07-12T17:02:35.893Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792475/Dropbox+provisioner+developer+notes
---

## Links

- (Log in) [https://www.dropbox.com/team/admin](https://www.dropbox.com/team/admin)
- (API) [https://api.dropboxapi.com/2/team](https://api.dropboxapi.com/2/team)
- (API Docs) [https://www.dropbox.com/developers/documentation/http/teams](https://www.dropbox.com/developers/documentation/http/teams)
- (App Console) [https://www.dropbox.com/developers/apps](https://www.dropbox.com/developers/apps)

## External system

[Grouper Dropbox external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549505/TrueFoundry+external+system)

Uses a WsBearerToken external system. The accessTokenPassword holds a single plain Dropbox team API bearer token (NOT a JSON blob). The provisioner attaches an `Authorization: Bearer <token>` header to every request, plus `Accept: application/json` and `Content-Type: application/json`. The authorization header is excluded from logging.

The endpoint `[https://api.dropboxapi.com](https://api.dropboxapi.com)` is stored in the external system configuration (read from `grouper.wsBearerToken.<configId>.endpoint`, not hardcoded). In tests this points at the mock service path `.../mockServices/dropbox`. The url suffix (e.g. `/2/team/groups/list`) is appended to the endpoint.

## APIs general

Every Dropbox Team API call is an **HTTP POST** with a JSON request body and a JSON response body. Calls with no arguments still send a body (use `{}`). There are no query parameters; all inputs go in the JSON body.

Rate limiting: monitor for HTTP 429 (too_many_requests) responses, which include a `Retry-After` hint.

Paging: list endpoints return up to `limit` items along with a `cursor` and a `has_more` boolean. When `has_more` is true, call the matching `/continue` endpoint with `{"cursor": ".."}` until `has_more` is false.

Unions: many Dropbox fields are tagged unions encoded as a JSON object with a `.tag` discriminator, e.g. `{".tag": "company_managed"}`, `{".tag": "team_member_id", "team_member_id": "dbmid:.."}`.

Async: some write operations return a `LaunchEmptyResult` -- either `{".tag": "async_job_id", "async_job_id": ".."}` or `{".tag": "complete"}`. When an async_job_id is returned, poll the matching `job_status/get` endpoint until `{".tag": "complete"}` (throw on `{".tag": "failed"}`). The mock service returns synchronous `complete` results, so tests do not exercise polling.

Identifiers: group ids are prefixed `g:`, team member ids are prefixed `dbmid:`, and account ids are prefixed `dbid:`. All are strings.

Base path: `[https://api.dropboxapi.com/2/team/](https://api.dropboxapi.com/2/team/)`

## Shared object shapes

TeamMemberProfile: { "team_member_id": "dbmid:abc123", "email": "jsmith@example.edu", "external_id": "jsmith", "account_id": "dbid:xyz789", "status": { ".tag": "active" }, "name": { "given_name": "John", "surname": "Smith", "display_name": "John Smith" } } TeamMemberRole: { "role_id": "pid_dbtmr:Team_Admin", "name": "Team_Admin", "description": ".." } GroupFullInfo: { "group_name": "ml-platform", "group_id": "g:abc123", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "member_count": 4, "members": [ GroupMemberInfo, ... ] } GroupMemberInfo: { "profile": TeamMemberProfile, "access_type": { ".tag": "member" } } GroupSelector (by group_id): { ".tag": "group_id", "group_id": "g:abc123" } UserSelectorArg (by team_member_id): { ".tag": "team_member_id", "team_member_id": "dbmid:abc123" } also accepts { ".tag": "email", "email": ".." } and { ".tag": "external_id", "external_id": ".." }

## List groups

POST /2/team/groups/list

Returns up to `limit` company-managed and user-managed groups. Page with the returned cursor via /2/team/groups/list/continue while `has_more` is true.

Authorization: Bearer abc123 Content-Type: application/json POST $SERVICE_URL$/2/team/groups/list { "limit": 1000 } 200 { "groups": [ { "group_name": "ml-platform", "group_id": "g:abc123", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "member_count": 4 } ], "cursor": "ABc...", "has_more": false }

## List groups (continue)

POST /2/team/groups/list/continue

Continues a groups/list page using the cursor from the previous response. Same response shape.

POST $SERVICE_URL$/2/team/groups/list/continue { "cursor": "ABc..." } 200 { "groups": [ ... ], "cursor": "..", "has_more": false }

## Get group info

POST /2/team/groups/get_info

Returns an ARRAY of items, one per requested group id. A found item is `{".tag": "group_info", ...GroupFullInfo}` (including the embedded members array). A missing id is `{".tag": "id_not_found", "id_not_found": "g:.."}`.

POST $SERVICE_URL$/2/team/groups/get_info { ".tag": "group_ids", "group_ids": ["g:abc123"] } 200 [ { ".tag": "group_info", "group_name": "ml-platform", "group_id": "g:abc123", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "member_count": 1, "members": [ { "profile": { "team_member_id": "dbmid:abc", "email": "jsmith@example.edu", "status": {".tag":"active"} }, "access_type": { ".tag": "member" } } ] } ]

## Create group

POST /2/team/groups/create

Body is DropboxGroup.toCreateJson(). Grouper-created groups are always company_managed with add_creator_as_owner=false. Returns the new GroupFullInfo (including the assigned group_id).

POST $SERVICE_URL$/2/team/groups/create { "group_name": "ml-platform", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "add_creator_as_owner": false } 200 { "group_name": "ml-platform", "group_id": "g:abc123", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "member_count": 0 }

## Update group

POST /2/team/groups/update

Body is DropboxGroup.toUpdateJson(). The group is selected by group_id; only the supplied new_* fields change. Returns the updated GroupFullInfo.

POST $SERVICE_URL$/2/team/groups/update { "group": { ".tag": "group_id", "group_id": "g:abc123" }, "new_group_name": "ml-platform-v2", "new_group_external_id": "ml-platform", "return_members": false } 200 { "group_name": "ml-platform-v2", "group_id": "g:abc123", "group_external_id": "ml-platform", "group_management_type": { ".tag": "company_managed" }, "member_count": 4 }

## Delete group

POST /2/team/groups/delete

Selected by group_id. Returns a LaunchEmptyResult: either an async_job_id to poll, or complete.

POST $SERVICE_URL$/2/team/groups/delete { ".tag": "group_id", "group_id": "g:abc123" } 200 { ".tag": "async_job_id", "async_job_id": "job-123" } -- or -- { ".tag": "complete" }

## Group job status

POST /2/team/groups/job_status/get

Polls an async group job (e.g. from delete or members/add/remove). Poll until complete; throw on failed.

POST $SERVICE_URL$/2/team/groups/job_status/get { "async_job_id": "job-123" } 200 { ".tag": "in_progress" } -- then eventually -- { ".tag": "complete" } -- or -- { ".tag": "failed", "failed": ".." }

## List group members

POST /2/team/groups/members/list

Lists members of one group (selected by group_id). Page via /2/team/groups/members/list/continue while has_more is true. Each member has a profile and an access_type (member or owner).

POST $SERVICE_URL$/2/team/groups/members/list { "group": { ".tag": "group_id", "group_id": "g:abc123" }, "limit": 1000 } 200 { "members": [ { "profile": { "team_member_id": "dbmid:abc", "email": "jsmith@example.edu", "status": {".tag":"active"} }, "access_type": { ".tag": "owner" } } ], "cursor": "..", "has_more": false }

## Add group members

POST /2/team/groups/members/add

Adds members (each with an access_type union) to a group. Returns a GroupMembersChangeResult; may include an async_job_id to poll via groups/job_status/get.

POST $SERVICE_URL$/2/team/groups/members/add { "group": { ".tag": "group_id", "group_id": "g:abc123" }, "members": [ { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "access_type": { ".tag": "member" } } ], "return_members": false } 200 { "group_info": { "group_id": "g:abc123", "member_count": 5, "..." : "..." } } -- optionally with -- { "group_info": { ... }, "async_job_id": "job-456" }

## Remove group members

POST /2/team/groups/members/remove

Removes members (selected by UserSelectorArg) from a group. Returns a GroupMembersChangeResult; may include an async_job_id to poll.

POST $SERVICE_URL$/2/team/groups/members/remove { "group": { ".tag": "group_id", "group_id": "g:abc123" }, "users": [ { ".tag": "team_member_id", "team_member_id": "dbmid:abc" } ], "return_members": false } 200 { "group_info": { "group_id": "g:abc123", "member_count": 4 } }

## Set group member access type

POST /2/team/groups/members/set_access_type

Changes one member's access_type between member and owner.

POST $SERVICE_URL$/2/team/groups/members/set_access_type { "group": { ".tag": "group_id", "group_id": "g:abc123" }, "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "access_type": { ".tag": "owner" }, "return_members": false } 200 [ { ".tag": "group_info", "group_id": "g:abc123", "..." : "..." } ]

## List members

POST /2/team/members/list_v2

Lists all team members. Each entry has a profile and a roles array (the admin roles). Page via /2/team/members/list/continue_v2 while has_more is true. During retrieval the provisioner caches the {role_id, name} pairs from roles[] so admin-role names can later be resolved to ids. Set include_removed=false to skip removed members.

POST $SERVICE_URL$/2/team/members/list_v2 { "limit": 1000, "include_removed": false } 200 { "members": [ { "profile": { "team_member_id": "dbmid:abc", "email": "jsmith@example.edu", "external_id": "jsmith", "account_id": "dbid:xyz", "status": { ".tag": "active" }, "name": { "given_name": "John", "surname": "Smith", "display_name": "John Smith" } }, "roles": [ { "role_id": "pid_dbtmr:Team_Admin", "name": "Team_Admin", "description": ".." } ] } ], "cursor": "..", "has_more": false }

## List members (continue)

POST /2/team/members/list/continue_v2

POST $SERVICE_URL$/2/team/members/list/continue_v2 { "cursor": ".." } 200 { "members": [ ... ], "cursor": "..", "has_more": false }

## Get member info

POST /2/team/members/get_info_v2

Looks up members by UserSelectorArg (external_id, email, or team_member_id). Returns members_info entries: a found entry is `{".tag": "member_info", "profile": ..., "roles": [..]}`; a missing one is `{".tag": "id_not_found"}`.

POST $SERVICE_URL$/2/team/members/get_info_v2 { "members": [ { ".tag": "external_id", "external_id": "jsmith" } ] } 200 { "members_info": [ { ".tag": "member_info", "profile": { "team_member_id": "dbmid:abc", "email": "jsmith@example.edu", "external_id": "jsmith", "status": { ".tag": "active" }, "name": { "given_name": "John", "surname": "Smith" } }, "roles": [ { "role_id": "pid_dbtmr:Team_Admin", "name": "Team_Admin" } ] } ] }

## Add member

POST /2/team/members/add_v2

Body new_members holds DropboxUser.toAddJson() entries. Returns either a synchronous complete result with per-member outcomes, or an async_job_id to poll via /2/team/members/add/job_status/get_v2. A success entry contains the new TeamMemberProfile (with the assigned team_member_id).

POST $SERVICE_URL$/2/team/members/add_v2 { "new_members": [ { "member_email": "jsmith@example.edu", "member_given_name": "John", "member_surname": "Smith", "member_external_id": "jsmith", "send_welcome_email": false } ], "force_async": false } 200 { ".tag": "complete", "complete": [ { ".tag": "success", "success": { "profile": { "team_member_id": "dbmid:new", "email": "jsmith@example.edu", "status": { ".tag": "active" } }, "role": ".." } } ] } -- or -- { ".tag": "async_job_id", "async_job_id": "job-789" }

## Add member job status

POST /2/team/members/add/job_status/get_v2

Polls an async member-add job until complete (returns the complete[] results) or failed.

POST $SERVICE_URL$/2/team/members/add/job_status/get_v2 { "async_job_id": "job-789" } 200 { ".tag": "in_progress" } -- or -- { ".tag": "complete", "complete": [ { ".tag": "success", "success": { "profile": { ... } } } ] }

## Set member profile

POST /2/team/members/set_profile_v2

Body is DropboxUser.toSetProfileJson(fieldNames): the member is selected by team_member_id; only supplied new_* fields change. Returns a TeamMemberInfoV2 wrapping the updated profile.

POST $SERVICE_URL$/2/team/members/set_profile_v2 { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "new_email": "jsmith@example.edu", "new_external_id": "jsmith", "new_given_name": "John", "new_surname": "Smith" } 200 { "profile": { "team_member_id": "dbmid:abc", "email": "jsmith@example.edu", "external_id": "jsmith", "status": { ".tag": "active" }, "name": { "given_name": "John", "surname": "Smith" } } }

## Remove member

POST /2/team/members/remove

Removes a member (selected by UserSelectorArg). wipe_data and keep_account come from the provisioner config. Returns a LaunchEmptyResult; poll /2/team/members/remove/job_status/get if an async_job_id is returned.

POST $SERVICE_URL$/2/team/members/remove { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "wipe_data": true, "keep_account": false } 200 { ".tag": "complete" } -- or -- { ".tag": "async_job_id", "async_job_id": "job-987" }

## Remove member job status

POST /2/team/members/remove/job_status/get

POST $SERVICE_URL$/2/team/members/remove/job_status/get { "async_job_id": "job-987" } 200 { ".tag": "complete" }

## Suspend / unsuspend member

POST /2/team/members/suspend and POST /2/team/members/unsuspend

Suspend temporarily disables a member's access (account retained); unsuspend re-enables it. Both return an empty body on success.

POST $SERVICE_URL$/2/team/members/suspend { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "wipe_data": false } 200 {} POST $SERVICE_URL$/2/team/members/unsuspend { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" } } 200 {}

## Set admin permissions (admin-role overlay)

POST /2/team/members/set_admin_permissions_v2

Body is DropboxUser.toSetAdminPermissionsJson(roleIds): selects the member by team_member_id and sets new_roles to the resolved Dropbox role ids. An empty new_roles list demotes the member to member_only. The role ids are resolved from the {role_id, name} cache populated during members/list_v2 retrieval. Only called when the admin-role overlay is enabled.

POST $SERVICE_URL$/2/team/members/set_admin_permissions_v2 { "user": { ".tag": "team_member_id", "team_member_id": "dbmid:abc" }, "new_roles": [ "pid_dbtmr:Team_Admin" ] } 200 { "team_member_id": "dbmid:abc" }

## List member roles (admin-role overlay)

POST /2/team/members/list_member_roles

Returns the admin roles currently assigned to one member. There is no standalone "list all roles" endpoint; the catalog of available admin roles and their ids is built by caching the {role_id, name} pairs seen in members/list_v2 and get_info_v2 responses during full sync.

POST $SERVICE_URL$/2/team/members/list_member_roles { "team_member_id": "dbmid:abc" } 200 { "roles": [ { "role_id": "pid_dbtmr:Team_Admin", "name": "Team_Admin", "description": ".." } ] }

## Admin roles

Dropbox has 8 built-in team admin roles, ordered highest-privilege first:

1. Team_Admin
2. User_Management_Admin
3. Support_Admin
4. Billing_Admin
5. Content_Admin
6. Security_Admin
7. Reporting_Admin
8. Compliance_Admin

When a member belongs to more than one admin-role group, the provisioner resolves to the highest tier in this list (DropboxUser.highestAdminRole) and assigns exactly that role via set_admin_permissions_v2. A member in no admin-role group is set to member_only (empty new_roles).

## Error and return codes

Dropbox returns HTTP 200 for successful calls. Common error codes:

| HTTP | Meaning | Notes |
| --- | --- | --- |
| 400 | Bad request | Malformed request body; the response is a plain-text message rather than a tagged error. |
| 401 | Invalid / expired token, or missing scope | Verify the token and that the team app has the required member and group scopes. |
| 403 | Access denied / wrong app type | Token is not a team-scoped token, or lacks member-admin rights for admin-role calls. |
| 409 | Endpoint-specific error (union) | The body is a tagged-union error, e.g. `{".tag": "group_already_exists"}`, `{".tag": "external_id_already_in_use"}`, `{".tag": "user_not_found"}`. |
| 429 | Rate limited | Respect the `Retry-After` header and back off. |
| 500/503 | Server error | Retry with backoff. |

For endpoint-specific failures Dropbox uses tagged-union `error` objects with an `error_summary` string. The provisioner inspects the `.tag` to decide whether a condition is fatal (e.g. it can treat "already exists" / "not found" as non-fatal where appropriate) or should be raised.
