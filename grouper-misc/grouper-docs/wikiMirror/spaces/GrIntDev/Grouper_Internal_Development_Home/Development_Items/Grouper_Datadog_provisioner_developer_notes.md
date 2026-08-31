---
title: "Grouper Datadog provisioner developer notes"
space: GrIntDev
pageId: 48792504
version: 5
lastUpdated: 2026-07-12T17:02:36.859Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792504/Grouper+Datadog+provisioner+developer+notes
---

## Log in to UI

- (Log in) [https://us5.datadoghq.com/](https://us5.datadoghq.com/)
- (API) [https://api.us5.datadoghq.com/api/v2](https://api.us5.datadoghq.com/api/v2)
- (API Docs) [https://docs.datadoghq.com/api/latest/](https://docs.datadoghq.com/api/latest/)

## External system

[Grouper Datadog external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548830/Grouper+Datadog+external+system)

Uses a WsBearerToken external system. The accessTokenPassword holds a JSON object with both keys: {"apiKey": "...", "applicationKey": "..."}. The provisioner parses this and attaches DD-API-KEY and DD-APPLICATION-KEY headers to every request.

## Provisioner general

Rate limiting: Datadog returns 429 when rate limited. Response headers include X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset.

Paging: All v2 list endpoints use page[size] (max 100) and page[number] (0-indexed). Response includes meta.page.total_count or meta.page.total_filtered_count.

All v2 endpoints use JSON:API format with data, type, id, attributes, relationships.

User IDs, role IDs, and team IDs are all UUIDs (e.g. "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee").

## User

Crud: select, insert, update, disable (soft delete via disabled=true)

Primary key: id (UUID)

| mock_datadog_user |
| --- |
| Column | Type | Description |
| id | varchar(40) | UUID assigned by Datadog, not null after create, readonly |
| email | varchar(256) | email address, not null, unique, used as "handle" |
| name | varchar(256) | display name |
| title | varchar(256) | job title, nullable |
| disabled | varchar(1) | T or F |
| service_account | varchar(1) | T or F |

Mappable provisioning attributes: id, email, name, title, disabled, serviceAccount

Note: when retrieving users (list all or get single), filter out service accounts (service_account=true) so they are invisible to the provisioner.

## Group (roles and teams)

Roles and teams are both stored in a single mock table with a group_type discriminator. The DatadogGroup bean is used for both.

Group types: "role" (Datadog role), "team" (Datadog team). Team admin/member distinction is on the membership, not the group.

Crud: select, insert, update, delete

Primary key: id (UUID)

| mock_datadog_group |
| --- |
| Column | Type | Description |
| id | varchar(40) | UUID assigned by Datadog, not null after create, readonly |
| name | varchar(256) | role or team name, not null |
| group_type | varchar(20) | "role" or "team" |

Mappable provisioning attributes: id, name, groupType

Note: two groups can have the same name with different group_type values.

For deletes, 404 is accepted as OK (resource already gone).

## Membership (roles and teams)

Role memberships and team memberships are stored in a single mock table. For role memberships the "role" column is null. For team memberships the "role" column is "admin" or "member" (default "member").

Crud: select, insert, delete

Primary key: id (UUID). Unique constraint on group_id + user_id.

| mock_datadog_membership |
| --- |
| Column | Type | Description |
| id | varchar(40) | UUID assigned internally, not null, primary key |
| group_id | varchar(40) | UUID of the role or team (references [mock_datadog_group.id](http://mock_datadog_group.id)) |
| user_id | varchar(40) | UUID of the user (references [mock_datadog_user.id](http://mock_datadog_user.id)) |
| role | varchar(20) | null for role memberships, "admin" or "member" for team memberships |

## List all users

/api/v2/users?page[size]=100&page[number]=0

Start at page[number]=0, increment until all users retrieved. Use meta.page.total_filtered_count to determine total pages.

Can filter by status: ?filter[status]=Active

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/users?page[size]=100&page[number]=0 200 { "data": [ { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "attributes": { "name": "John Doe", "handle": "john.doe@example.com", "email": "john.doe@example.com", "title": "Engineer", "verified": true, "disabled": false, "service_account": false, "status": "Active", "created_at": "2024-01-15T10:30:00.000000+00:00", "modified_at": "2024-06-01T12:00:00.000000+00:00" }, "relationships": { "roles": { "data": [ { "type": "roles", "id": "11111111-2222-3333-4444-555555555555" } ] }, "org": { "data": { "type": "orgs", "id": "aaaaaaaa-1111-2222-3333-444444444444" } } } } ], "meta": { "page": { "total_count": 150, "total_filtered_count": 150 } } }

## Create user (invite)

/api/v2/users

Required: email. Optional: name, title, relationships.roles.

Returns 201 if created. Returns 409 if user with that email already exists. On 409, look up the existing user by email using the list endpoint with filter, then update via PATCH (set disabled=false and include name).

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json POST $SERVICE_URL$/api/v2/users { "data": { "type": "users", "attributes": { "name": "Jane Smith", "email": "jane.smith@example.com", "title": "Developer" } } } 201 { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith", "handle": "jane.smith@example.com", "email": "jane.smith@example.com", "title": "Developer", "verified": false, "disabled": false, "service_account": false, "status": "Pending", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T14:00:00.000000+00:00" } } }

## Get user by email

/api/v2/users?filter=email@[example.com](http://example.com)

Uses the list endpoint with filter param. Returns matching users in data array (may be empty if not found).

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/users?filter=jane.smith@example.com 200 { "data": [ { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith", "handle": "jane.smith@example.com", "email": "jane.smith@example.com", "title": "Developer", "disabled": false, "service_account": false, "status": "Active", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T15:00:00.000000+00:00" } } ], "meta": { "page": { "total_count": 1, "total_filtered_count": 1 } } }

## Get user by ID

/api/v2/users/{user_id}

200 if found. 404 if not found.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/users/bbbbbbbb-cccc-dddd-eeee-ffffffffffff 200 { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith", "handle": "jane.smith@example.com", "email": "jane.smith@example.com", "title": "Developer", "verified": true, "disabled": false, "service_account": false, "status": "Active", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T15:00:00.000000+00:00" }, "relationships": { "roles": { "data": [ { "type": "roles", "id": "11111111-2222-3333-4444-555555555555" } ] }, "org": { "data": { "type": "orgs", "id": "aaaaaaaa-1111-2222-3333-444444444444" } } } } }

## Update user

PATCH /api/v2/users/{user_id}

Only include attributes you want to change. The id in the body must match the path parameter.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json PATCH $SERVICE_URL$/api/v2/users/bbbbbbbb-cccc-dddd-eeee-ffffffffffff { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith-Jones", "title": "Senior Developer" } } } 200 { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith-Jones", "handle": "jane.smith@example.com", "email": "jane.smith@example.com", "title": "Senior Developer", "verified": true, "disabled": false, "service_account": false, "status": "Active", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T16:00:00.000000+00:00" } } }

## Disable user

PATCH /api/v2/users/{user_id}

Same PATCH endpoint as update. Set disabled=true. To re-enable, set disabled=false (also include name when re-enabling on 409 conflict during create).

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json PATCH $SERVICE_URL$/api/v2/users/bbbbbbbb-cccc-dddd-eeee-ffffffffffff { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "disabled": true } } } 200 { "data": { "type": "users", "id": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff", "attributes": { "name": "Jane Smith-Jones", "handle": "jane.smith@example.com", "email": "jane.smith@example.com", "title": "Senior Developer", "verified": true, "disabled": true, "service_account": false, "status": "Disabled", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T17:00:00.000000+00:00" } } }

## List roles

/api/v2/roles?page[size]=100&page[number]=0

Can filter by name: ?filter=roleName

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/roles?page[size]=100&page[number]=0 200 { "data": [ { "type": "roles", "id": "11111111-2222-3333-4444-555555555555", "attributes": { "name": "Datadog Standard Role", "created_at": "2023-01-01T00:00:00.000000+00:00", "modified_at": "2023-01-01T00:00:00.000000+00:00", "user_count": 3 }, "relationships": { "permissions": { "data": [ { "type": "permissions", "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" } ] } } } ], "meta": { "page": { "total_count": 5, "total_filtered_count": 5 } } }

## Create role

POST /api/v2/roles

Required: name. Optional: relationships.permissions.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json POST $SERVICE_URL$/api/v2/roles { "data": { "type": "roles", "attributes": { "name": "My Custom Role" } } } 200 { "data": { "type": "roles", "id": "22222222-3333-4444-5555-666666666666", "attributes": { "name": "My Custom Role", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T14:00:00.000000+00:00", "user_count": 0 }, "relationships": { "permissions": { "data": [] } } } }

## Update role

PATCH /api/v2/roles/{role_id}

The id in the body must match the path parameter.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json PATCH $SERVICE_URL$/api/v2/roles/22222222-3333-4444-5555-666666666666 { "data": { "type": "roles", "id": "22222222-3333-4444-5555-666666666666", "attributes": { "name": "Updated Role Name" } } } 200 { "data": { "type": "roles", "id": "22222222-3333-4444-5555-666666666666", "attributes": { "name": "Updated Role Name", "created_at": "2026-03-19T14:00:00.000000+00:00", "modified_at": "2026-03-19T16:00:00.000000+00:00", "user_count": 0 } } }

## Delete role

DELETE /api/v2/roles/{role_id}

Returns 204 if successful. Returns 404 if not found.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 DELETE $SERVICE_URL$/api/v2/roles/22222222-3333-4444-5555-666666666666 204 (no body)

## Get role users

/api/v2/roles/{role_id}/users?page[size]=100&page[number]=0

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/roles/11111111-2222-3333-4444-555555555555/users?page[size]=100&page[number]=0 200 { "data": [ { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "attributes": { "name": "John Doe", "handle": "john.doe@example.com", "email": "john.doe@example.com", "title": "Engineer", "verified": true, "disabled": false, "service_account": false, "status": "Active" } } ], "meta": { "page": { "total_count": 1, "total_filtered_count": 1 } } }

## Add user to role

POST /api/v2/roles/{role_id}/users

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json POST $SERVICE_URL$/api/v2/roles/11111111-2222-3333-4444-555555555555/users { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } 200 { "data": [ { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "attributes": { "name": "John Doe", "handle": "john.doe@example.com", "email": "john.doe@example.com", "status": "Active" } } ] }

## Remove user from role

DELETE /api/v2/roles/{role_id}/users

Note: this is a DELETE with a request body, which is unusual.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json DELETE $SERVICE_URL$/api/v2/roles/11111111-2222-3333-4444-555555555555/users { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } 200 { "data": [ { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "attributes": { "name": "John Doe", "handle": "john.doe@example.com", "email": "john.doe@example.com", "status": "Active" } } ] }

## List teams

/api/v2/team?page[size]=100&page[number]=0

Can filter by keyword: ?filter[keyword]=teamName

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/team?page[size]=100&page[number]=0 200 { "data": [ { "type": "team", "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "attributes": { "name": "My Team", "handle": "my-team", "summary": "A short summary", "description": "Longer markdown description", "avatar": null, "banner": null, "link_count": 0, "user_count": 5, "created_at": "2023-01-01T00:00:00+00:00", "modified_at": "2023-06-01T00:00:00+00:00" } } ], "meta": { "pagination": { "total": 1, "offset": 0, "limit": 100, "type": "offset" } } }

## Create team

POST /api/v2/team

Required: name, handle. Optional: summary, description.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json POST $SERVICE_URL$/api/v2/team { "data": { "type": "team", "attributes": { "name": "My New Team", "handle": "my-new-team", "description": "Optional description", "summary": "Optional short summary" } } } 201 { "data": { "type": "team", "id": "c1d2e3f4-a5b6-7890-cdef-123456789012", "attributes": { "name": "My New Team", "handle": "my-new-team", "summary": "Optional short summary", "description": "Optional description", "avatar": null, "banner": null, "link_count": 0, "user_count": 0, "created_at": "2026-03-19T14:00:00+00:00", "modified_at": "2026-03-19T14:00:00+00:00" } } }

## Update team

PATCH /api/v2/team/{team_id}

Only include attributes you want to change.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json PATCH $SERVICE_URL$/api/v2/team/c1d2e3f4-a5b6-7890-cdef-123456789012 { "data": { "type": "team", "attributes": { "name": "Updated Team Name", "handle": "updated-team-name", "description": "Updated description" } } } 200 { "data": { "type": "team", "id": "c1d2e3f4-a5b6-7890-cdef-123456789012", "attributes": { "name": "Updated Team Name", "handle": "updated-team-name", "summary": "Optional short summary", "description": "Updated description", "avatar": null, "banner": null, "link_count": 0, "user_count": 0, "created_at": "2026-03-19T14:00:00+00:00", "modified_at": "2026-03-19T16:00:00+00:00" } } }

## Delete team

DELETE /api/v2/team/{team_id}

Returns 204 if successful. Returns 404 if not found.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 DELETE $SERVICE_URL$/api/v2/team/c1d2e3f4-a5b6-7890-cdef-123456789012 204 (no body)

## Get team memberships

/api/v2/team/{team_id}/memberships?page[size]=100&page[number]=0

The role attribute is "admin" or "member" (default).

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 GET $SERVICE_URL$/api/v2/team/a1b2c3d4-e5f6-7890-abcd-ef1234567890/memberships?page[size]=100&page[number]=0 200 { "data": [ { "type": "team_memberships", "id": "membership-uuid", "attributes": { "role": "member" }, "relationships": { "team": { "data": { "type": "team", "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890" } }, "user": { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } } } ], "meta": { "pagination": { "total": 1, "offset": 0, "limit": 100, "type": "offset" } } }

## Add user to team

POST /api/v2/team/{team_id}/memberships

Role can be "member" (default) or "admin".

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json POST $SERVICE_URL$/api/v2/team/a1b2c3d4-e5f6-7890-abcd-ef1234567890/memberships { "data": { "type": "team_memberships", "relationships": { "user": { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } } } } 200 { "data": { "type": "team_memberships", "id": "new-membership-uuid", "attributes": { "role": "member" }, "relationships": { "user": { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } } } }

## Remove user from team

DELETE /api/v2/team/{team_id}/memberships/{user_id}

Returns 204 if successful. Returns 404 if not found.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 DELETE $SERVICE_URL$/api/v2/team/a1b2c3d4-e5f6-7890-abcd-ef1234567890/memberships/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee 204 (no body)

## Update team membership role

PATCH /api/v2/team/{team_id}/memberships/{user_id}

Changes a team member's role between "admin" and "member". Used by the datadogAddTeamAdminMetadata feature to promote or demote team members.

DD-API-KEY: abc123 DD-APPLICATION-KEY: def456 Content-Type: application/json PATCH $SERVICE_URL$/api/v2/team/a1b2c3d4-e5f6-7890-abcd-ef1234567890/memberships/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee { "data": { "type": "team_memberships", "attributes": { "role": "admin" } } } 200 { "data": { "type": "team_memberships", "id": "membership-uuid", "attributes": { "role": "admin" }, "relationships": { "user": { "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" } } } } }
