---
title: "Grouper TrueFoundry provisioner developer notes"
space: GrIntDev
pageId: 48792497
version: 4
lastUpdated: 2026-07-12T17:02:36.393Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792497/Grouper+TrueFoundry+provisioner+developer+notes
---

## Links

- (Log in) https://<domain>.truefoundry.cloud
- (API) [https://app.truefoundry.com/api/svc/v1](https://app.truefoundry.com/api/svc/v1)
- (API Docs) [https://www.truefoundry.com/docs/api-reference](https://www.truefoundry.com/docs/api-reference)
- (SDK Docs) [https://www.truefoundry.com/docs/truefoundry_sdk](https://www.truefoundry.com/docs/truefoundry_sdk)

## External system

[Grouper TrueFoundry external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549505/TrueFoundry+external+system)

Uses a WsBearerToken external system. The accessTokenPassword holds a TrueFoundry access token. The provisioner attaches an Authorization: Bearer header to every request. All requests also require `Accept: application/json` and `Content-Type: application/json` headers.

The API endpoint [https://app.truefoundry.com](https://app.truefoundry.com) is stored as the endpoint in the external system configuration.

## APIs general

Rate limiting: Monitor for 429 responses.

Paging: List endpoints (users, teams) use limit (default 100, max 1000) and offset (0-indexed). Response includes pagination.total, pagination.offset, pagination.limit. There is no paging on team memberships - the full member list is embedded in the team manifest, so all members are returned in one call. This means the provisioner operates in "replace" mode for memberships (replaceGroupMemberships) and is suitable for small to medium deployments.

All endpoints return JSON. Successful responses wrap data in a "data" field (or return {} for operations with no return value).

User IDs and team IDs are strings.

Base path: `[https://app.truefoundry.com/api/svc/v1/](https://app.truefoundry.com/api/svc/v1/)`

## SCIM

TrueFoundry exposes a SCIM v2 endpoint at `/api/svc/v1/scim/v2/{tenantName}/{ssoId}/`. SCIM is not used by this provisioner because SCIM-created users have `isEditable: false` and cannot have roles assigned to them via the UI or API. All provisioning uses the native REST API instead.

## List all users

GET /api/svc/v1/subjects?query=&limit=100&offset=0&showInvalidUsers=true

Must use the subjects endpoint (not /users) as it returns rolesWithResource which includes the actual role assignments. Start at offset=0, increment by limit until all users retrieved. Use totalUsers for paging (not pagination.total). showInvalidUsers=true includes deactivated users.

Note: the response also includes teams, virtualAccounts, and externalIdentities — filter to the users array only. In rolesWithResource, roleId is the internal ID of the role (not the role name) — cross-reference with GET /api/svc/v1/role/list to resolve to a role name.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/subjects?query=&limit=100&offset=0&showInvalidUsers=true 200 { "users": [ { "id": "cmn6b9q042ke801py895t558r", "email": "mchyzer@example.com", "tenantName": "upenn-prod", "roles": ["tenant-admin"], "rolesWithResource": [ { "roleId": "cmmdr4nxr0a4301u07nnlh6kv", "resourceType": "account", "resourceId": "cmmdr4nwp0a3n01u0hcc1614w" } ], "active": true, "isEditable": true, "createdAt": "2026-03-25T17:22:10.276Z", "updatedAt": "2026-03-26T20:55:24.638Z" } ], "totalUsers": 4, "totalTeams": 4, "teams": [...], "externalIdentities": [], "totalExternalIdentities": 0, "virtualAccounts": [...] }

## Register user

POST /api/svc/v1/users/register

Creates a new user by email. Required: email. Returns 200 {} on success — no user ID is returned. After registration, look up the user by email via the search endpoint to retrieve the assigned ID. The user will have a pending registration status until they first log in via SSO, but can be assigned to teams and roles in the meantime. Do not use skipIfUserExists — let it fail so the provisioner knows the user already exists and can handle accordingly (e.g. reactivate).

Authorization: Bearer abc123 Content-Type: application/json POST $SERVICE_URL$/api/svc/v1/users/register { "email": "user@example.com", "sendInviteEmail": false, "skipIfUserExists": true } 200 {}

## Update user display name (SCIM)

PATCH /api/svc/v1/scim/v2/{tenantName}/{ssoId}/Users/{id}

Updates the display name of a natively-registered user via SCIM PATCH. The native user ID is used directly — no separate SCIM ID lookup needed. Important: do not create users via SCIM (only PATCH existing ones) — SCIM-created users have isEditable=false and cannot have roles assigned. SCIM PATCH on a natively-registered user does not convert them to a SCIM user.

Authorization: Bearer abc123 Content-Type: application/json PATCH $SERVICE_URL$/api/svc/v1/scim/v2/{tenantName}/{ssoId}/Users/{id} { "schemas": ["urn:ietf:params:scim:schemas:core:2.0:PatchOp"], "Operations": [ { "op": "replace", "path": "displayName", "value": "John Smith" } ] } 200 {}

## Search user by email

GET /api/svc/v1/subjects?query={email}&limit=25&offset=0&showInvalidUsers=true

Search for a user by email using the query parameter (URL-encoded). Returns rolesWithResource so can be used to check current role assignments for a single user. Returns totalUsers=0 if not found. Note: search by ID does not work — email is the only supported search key.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/subjects?query=oruganty%40upenn.edu&limit=25&offset=0&showInvalidUsers=true 200 { "users": [ { "id": "pt3vuwlxupmefpk8i9cj11du", "email": "oruganty@example.com", "tenantName": "upenn-prod", "roles": [], "rolesWithResource": [ { "roleId": "cmmdr4nxr0a4301u07nnlh6kv", "resourceType": "account", "resourceId": "cmmdr4nwp0a3n01u0hcc1614w" } ], "active": true, "isEditable": true, "createdAt": "2026-03-27T12:37:59.013Z", "updatedAt": "2026-03-29T21:51:44.669Z" } ], "totalUsers": 1, "totalTeams": 0, "teams": [], "externalIdentities": [], "totalExternalIdentities": 0, "virtualAccounts": [], "totalVirtualAccounts": 0 }

## Get user by ID

GET /api/svc/v1/users/{id}

200 if found. 404 if not found. Note: roles are not returned in this response — use the subjects endpoint for user data including role assignments. This endpoint will not be used by the provisioner.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/users/user-123 200 { "data": { "id": "user-123", "email": "user@example.com", "tenantName": "my-tenant", "metadata": { "displayName": "John Doe", "imageURL": "https://example.com/image.jpg", "inviteAccepted": true, "registeredInIdp": true, "groups": ["engineering", "admins"] }, "roles": ["admin", "developer"], "rolesWithResource": [ { "roleId": "role-123", "resourceType": "workspace", "resourceId": "ws-456" } ], "accounts": [ { "accountId": "acc-789", "name": "Production Account" } ], "active": true, "isEditable": true, "createdAt": "2024-01-15T10:30:00Z", "updatedAt": "2024-01-20T14:45:00Z" } } 404 { "statusCode": 404, "message": "Not Found. No user found for the given user ID", "code": "USER_NOT_FOUND" }

## Deactivate user

PATCH /api/svc/v1/users/deactivate

Required: email. Soft-disables the user (sets active=false). Use activate to re-enable.

Authorization: Bearer abc123 Content-Type: application/json PATCH $SERVICE_URL$/api/svc/v1/users/deactivate { "email": "user@example.com" } 200 {} 404 { "statusCode": 404, "message": "Not Found. User with the given email not found.", "code": 404, "details": [] }

## Activate user

PATCH /api/svc/v1/users/activate

Required: email. Re-enables a previously deactivated user (sets active=true).

Authorization: Bearer abc123 Content-Type: application/json PATCH $SERVICE_URL$/api/svc/v1/users/activate { "email": "user@example.com" } 200 {} 404 { "statusCode": 404, "message": "User with the given email not found", "code": "NOT_FOUND" }

## Delete user

DELETE /api/svc/v1/users/{id}

Hard delete. Returns 200 {} on success. Returns 400 if user has active collaborations or team memberships beyond the default "everyone" team. Returns 404 if not found.

Note: this endpoint will not be used by the provisioner — deactivate is used instead since delete is blocked when the user has team memberships.

Authorization: Bearer abc123 DELETE $SERVICE_URL$/api/svc/v1/users/user-123 200 {} 400 { "statusCode": 400, "message": "User has active collaborations or team memberships", "code": "INVALID_REQUEST", "details": [] } 404 { "statusCode": 404, "message": "User not found", "code": "NOT_FOUND", "details": [] }

## Update user roles

PATCH /api/svc/v1/users/roles

Required: email, roles (array of role name strings), resourceType. Assigns a role to a user.

The provisioner only manages roles with `resourceType: "account"` or `resourceType: "tenant"`. All other role types (cluster, workspace, team, mcp-server, etc.) are resource-scoped and not managed by the provisioner. Built-in assignable roles can be identified by `isDefault: true`; custom roles by `isDefault: false`.

Note: role assignment does not work for SCIM-created users (`isEditable: false`). This is one of the reasons the provisioner uses the native API for user creation rather than SCIM.

Authorization: Bearer abc123 Content-Type: application/json PATCH $SERVICE_URL$/api/svc/v1/users/roles { "email": "user@example.com", "roles": ["developer"] } 200 {} 404 { "statusCode": 404, "message": "User with the given email not found" }

## List roles

GET /api/svc/v1/role/list

Returns all roles. No paging — all roles returned in one call. Filter to roles with resourceType "account" or "tenant" for provisioner-managed roles. Use isDefault to distinguish built-in roles (true) from custom roles (false). roleId in rolesWithResource from the subjects endpoint corresponds to the id field here.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/role/list 200 { "data": [ { "id": "cmmdr4nxr0a4301u07nnlh6kv", "name": "member", "resourceType": "account", "accountId": "cmmdr4nwp0a3n01u0hcc1614w", "manifest": { "name": "member", "type": "role", "description": "Role grants member access to the entities in the account.", "displayName": "Member", "permissions": ["cluster:ReadCluster", "environment:ListEnvironments", "..."], "resourceType": "account" }, "isEditable": true, "isDefault": true } ], "pagination": { "total": 38, "offset": 0 } }

## Create or update role

PUT /api/svc/v1/role

Creates a new custom role or updates an existing one (upsert by name). All manifest fields are required: name, displayName, resourceType, description, permissions, type. Role name must be lowercase alphanumeric + hyphens only. Use resourceType "account" for account-scoped roles. Use minimal permissions (e.g. role:ListRoles) as a placeholder — administrators configure actual permissions in the TrueFoundry UI. Returns 200 with the created/updated role including its assigned id.

Authorization: Bearer abc123 Content-Type: application/json PUT $SERVICE_URL$/api/svc/v1/role { "manifest": { "name": "test-role2", "displayName": "Test role 2", "resourceType": "account", "description": "Test role 2 hey", "permissions": ["role:ListRoles"], "type": "role" } } 200 { "data": { "id": "xzbyb3qnmaolwi6leiwof9zm", "name": "test-role2", "resourceType": "account", "accountId": "cmmdr4nwp0a3n01u0hcc1614w", "manifest": { "name": "test-role2", "type": "role", "description": "Test role 2 hey", "displayName": "Test role 2", "permissions": ["role:ListRoles"], "resourceType": "account" }, "isEditable": true, "isDefault": false } }

## Delete role

DELETE /api/svc/v1/role/{id}

Deletes a role by its ID. Returns 200 on success.

Authorization: Bearer abc123 DELETE $SERVICE_URL$/api/svc/v1/role/xzbyb3qnmaolwi6leiwof9zm 200 {}

## List teams

GET /api/svc/v1/teams/user

Returns all teams when called with a tenant-admin token. Uses limit/offset paging.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/teams/user 200 { "data": [ { "id": "a847kssbx44jlb4kz1tvpfjc", "teamName": "test3", "tenantName": "upenn-prod", "accountId": "cmmdr4nwp0a3n01u0hcc1614w", "createdAt": "2026-03-29T03:45:30.714Z", "updatedAt": "2026-03-29T04:31:12.348Z", "members": ["danefett@example.com", "mchyzer@example.com"], "manifest": { "name": "test3", "type": "team", "members": ["mchyzer@example.com", "danefett@example.com"], "managers": ["oruganty@example.com", "danefett@example.com"] }, "metadata": null, "isEditable": true } ], "pagination": { "total": 4, "offset": 0, "limit": 4 } }

## Get team by ID

GET /api/svc/v1/teams/{id}

200 if found. 404 if not found.

Authorization: Bearer abc123 GET $SERVICE_URL$/api/svc/v1/teams/team-123 200 { "data": { "id": "team-123", "description": "Engineering team", "tenantName": "acme-tenant", "accountId": "acc-456", "createdBySubject": { "subjectId": "user-789", "subjectType": "user", "subjectSlug": "john.doe", "subjectDisplayName": "John Doe" }, "members": ["user1@example.com", "user2@example.com"], "createdAt": "2024-01-15T10:30:00Z", "updatedAt": "2024-01-20T14:45:00Z", "manifest": { "type": "team", "name": "engineering-team", "managers": ["admin@example.com"], "members": ["user1@example.com", "user2@example.com"], "ownedBy": { "account": "main-account" } }, "metadata": { "createdByScim": false }, "isEditable": true } } 404 { "statusCode": 404, "message": "Not Found. No team found for the given team ID", "code": "TEAM_NOT_FOUND" }

## Create or update team (with membership)

PUT /api/svc/v1/teams

Creates a new team or updates an existing team. Required: [manifest.name](http://manifest.name), manifest.members (at least one). Optional: manifest.managers, manifest.ownedBy, manifest.description, dryRun.

Key difference from Datadog: membership is managed by providing the complete member list in the manifest. This is the primary way to both create teams and update team membership (replaceGroupMemberships pattern).

Team name constraints: lowercase alphanumeric + hyphens only, 3-36 characters, must start and end with alphanumeric. Names must be unique within the tenant.

Returns 409 if trying to edit the default "everyone" team. Returns 422 for validation failures (empty member list, invalid name, invalid email, etc.).

Authorization: Bearer abc123 Content-Type: application/json PUT $SERVICE_URL$/api/svc/v1/teams { "manifest": { "type": "team", "name": "my-team", "members": ["user1@example.com", "user2@example.com"], "managers": ["manager@example.com"], "ownedBy": { "account": "account-name" } }, "dryRun": false } 200 { "data": { "id": "team-uuid", "description": "Team description", "tenantName": "tenant-name", "accountId": "account-uuid", "createdBySubject": { "subjectId": "user-id", "subjectType": "user", "subjectSlug": "user-slug", "subjectDisplayName": "User Name" }, "members": ["user1@example.com", "user2@example.com"], "createdAt": "2024-01-15T10:30:00Z", "updatedAt": "2024-01-15T10:30:00Z", "manifest": { "type": "team", "name": "my-team", "members": ["user1@example.com", "user2@example.com"], "managers": ["manager@example.com"], "ownedBy": { "account": "account-name" } }, "metadata": { "createdByScim": false }, "isEditable": true } } 409 { "statusCode": 409, "message": "Conflict. Default team 'everyone' cannot be edited", "code": 409, "details": [] } 422 { "statusCode": 422, "message": "Team needs to have at least one member", "code": 422, "details": [] }

## Delete team

DELETE /api/svc/v1/teams/{id}

Returns 200 {} if successful. Returns 404 if not found. Returns 409 if attempting to delete the default "everyone" team.

Authorization: Bearer abc123 DELETE $SERVICE_URL$/api/svc/v1/teams/team-123 200 {} 404 { "statusCode": 404, "message": "Not Found. No Team with provided Id found", "code": 404, "details": [] } 409 { "statusCode": 409, "message": "Conflict. Default team \"everyone\" cannot be deleted", "code": 409, "details": [] }
