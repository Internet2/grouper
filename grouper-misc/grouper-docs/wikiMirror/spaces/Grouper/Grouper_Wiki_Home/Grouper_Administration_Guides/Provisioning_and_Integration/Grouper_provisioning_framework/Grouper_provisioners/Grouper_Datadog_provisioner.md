---
title: "Grouper Datadog provisioner"
space: Grouper
pageId: 28555435
version: 4
lastUpdated: 2026-07-12T15:27:13.656Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555435/Grouper+Datadog+provisioner
---

> The info on this page applies to releases after 3/20/2026 (TODO add versions)

## External system

[Grouper Datadog external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548830/Grouper+Datadog+external+system)

## Links

- Datadog UI: [https://us5.datadoghq.com/](https://us5.datadoghq.com/) (varies by site)
- [Datadog API docs](https://docs.datadoghq.com/api/latest/)
- [API and Application Keys](https://docs.datadoghq.com/account_management/api-app-keys/)

## Overview

The Datadog Provisioner manages **users**, **roles**, **teams**, **role memberships**, and **team memberships** in a [Datadog](https://www.datadoghq.com/) organization via the [Datadog API v2](https://docs.datadoghq.com/api/latest/).

Datadog uses JSON:API format for all v2 endpoints. All IDs are UUIDs.

The provisioning type is `membershipObjects`, meaning Grouper independently manages three object types in Datadog:

- **Entities** -- Datadog users (people who can log in)
- **Groups** -- Datadog roles or teams (depending on provisioner configuration)
- **Memberships** -- Associations between users and roles, or between users and teams

Because Datadog assigns its own UUIDs to users, roles, and teams, the provisioner uses **group and entity link** (target attribute value caches) to track the Datadog-assigned IDs for each Grouper object.

The provisioner class is `edu.internet2.middleware.grouper.app.datadog.DatadogProvisioner`.

Service accounts (users with `service_account=true`) are automatically filtered out of all user retrieval operations. They are invisible to the provisioner.

## Provisioning attributes

Advice

- Provisioning type is hardcoded to membershipObjects
- Use group and entity link (since there are UUIDs in the target for groups and entities that need to be looked up)

#### User (entity) attributes [API](https://docs.datadoghq.com/api/latest/users/)

The provisioner supports the following entity attributes. Deleting an entity through the provisioner performs a soft-delete (disable via `disabled=true`), not a permanent removal.

| Grouper Attribute Name | Type | Required? | Datadog API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (UUID) | Yes | `id` | Datadog-assigned user UUID. **Read-only / select-only.** Do not translate from Grouper. Cache it in an attribute value cache for linking. |
| `email` | String | Yes | `email` | Email address. Also used as the user's "handle" in Datadog. Used for entity matching and for the upsert check on create (409 conflict). |
| `name` | String | No | `name` | Display name of the user. |
| `title` | String | No | `title` | Job title. |

#### Role (group) attributes [API](https://docs.datadoghq.com/api/latest/roles/)

| Grouper Attribute Name | Type | Required? | Datadog API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (UUID) | Yes | `id` | Datadog-assigned role UUID. **Read-only / select-only.** Cache it in an attribute value cache for linking. |
| `name` | String | Yes | `name` | The display name of the role in Datadog. |

#### Team (group) attributes [API](https://docs.datadoghq.com/api/latest/teams/)

| Grouper Attribute Name | Type | Required? | Datadog API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (UUID) | Yes | `id` | Datadog-assigned team UUID. **Read-only / select-only.** Cache it in an attribute value cache for linking. |
| `name` | String | Yes | `name` | The display name of the team. |
| `handle` | String | Yes | `handle` | URL-friendly slug. Must be unique across the org. Typically derived from the team name (lowercase, hyphens). |
| `summary` | String | No | `summary` | Short summary of the team. |
| `description` | String | No | `description` | Longer description (supports markdown). |
| `admins` | Set<String> (multi-valued) | No | n/a (virtual) | Multi-valued attribute containing Datadog user UUIDs of team admins. Not sent to the Datadog API directly; instead, changes to this attribute trigger `PATCH /api/v2/team/{id}/memberships/{userId}` to promote/demote members. Populated automatically by the `DatadogProvisioningTranslator` when `datadogAddTeamAdminMetadata` is enabled. Only applies to teams, not roles. |

### Entity and group matching

Matching tells the provisioner how to find existing Datadog objects that correspond to Grouper objects.

#### Entity matching

Generally you should have id as the first search/match attribute, then email as a fallback.

| Searching/Matching Attribute | Datadog Lookup Method | Notes |
| --- | --- | --- |
| `id` | `GET /api/v2/users/{id}` | Direct lookup by Datadog-assigned UUID. Fastest method. |
| `email` | `GET /api/v2/users?filter=email@[example.com](http://example.com)` | Search by email address using list endpoint with filter. Returns matches in the data array. |

## CRUD operations

The provisioner supports the following operations.

| Object | Operation | Supported? | Notes |
| --- | --- | --- | --- |
| Entity (User) | Retrieve all | Yes | Retrieves all users, filtering out service accounts |
| Retrieve one | Yes | By id or email |
| Insert | Yes | Invite user. Upsert behavior on 409 conflict (re-enable and update) |
| Update | Yes | PATCH with only changed attributes |
| Delete | Yes | **Soft delete** (PATCH with disabled=true), not permanent removal |
| Role (Group) | Retrieve all | Yes | Paginated list of all roles |
| Retrieve one | Yes | By id |
| Insert | Yes | Returns 200 (not 201) |
| Update | Yes | PATCH with changed attributes |
| Delete | Yes | Permanent deletion (204) |
| Team (Group) | Retrieve all | Yes | Paginated list of all teams |
| Retrieve one | Yes | By id |
| Insert | Yes | Returns 201 |
| Update | Yes | PATCH with changed attributes |
| Delete | Yes | Permanent deletion (204) |
| Role Membership | Retrieve by role | Yes | Lists all users in a role (paginated) |
| Insert | Yes | Adds user to role |
| Delete | Yes | Removes user from role. Note: uses DELETE with a request body (unusual) |
| Team Membership | Retrieve by team | Yes | Lists all members of a team (paginated). Each membership has a "role" attribute (admin or member) |
| Insert | Yes | Adds user to team with default role "member" |
| Update role | Yes | PATCH to change a member's role between "admin" and "member". Used by the admin metadata feature. |
| Delete | Yes | Removes user from team (204) |

## Behavioral notes

### Entity create is upsert

When creating (inviting) a new user, the provisioner POSTs to `/api/v2/users`. If Datadog returns a 409 conflict (user with that email already exists), the provisioner:

1. Looks up the existing user by email using `GET /api/v2/users?filter=email@[example.com](http://example.com)`
2. PATCHes the user to set `disabled=false` (re-enable) and updates the name

Only if no conflict occurs does the POST create succeed as a new invite.

### Entity delete is soft delete (disable)

When the provisioner deletes an entity, it PATCHes the user with `disabled=true` rather than calling DELETE. The user remains in Datadog in a disabled state and can be re-enabled later (e.g., by the upsert flow on a subsequent create).

### Service accounts are excluded

Datadog service accounts (identified by `service_account=true` in the API response) are automatically filtered out of all user retrieval operations (list all and get single). They are invisible to the provisioner.

### Role create returns 200

Unlike most REST APIs, Datadog's create role endpoint returns HTTP 200 (not 201) on success. The provisioner handles this.

### Remove user from role uses DELETE with body

Removing a user from a role uses `DELETE /api/v2/roles/{role_id}/users` with a JSON request body containing the user ID. This is an unusual API pattern (DELETE with a body) but is how the Datadog API works.

### Team membership roles and admin metadata

Each team membership in Datadog has a "role" attribute that is either "admin" or "member". When the provisioner adds a user to a team, the default role is "member".

#### How it works

Enable the `datadogAddTeamAdminMetadata` configuration option on the provisioner. This adds a metadata field called `md_adminGroupName` (labeled "Team admin group name") on each provisionable team group. Set this metadata value to the Grouper group path of an admin group (e.g., `my:folder:teamAdmins`). Members of that admin group who are also members of the team group will be promoted to the "admin" role in Datadog.

The `admins` target group attribute must be configured as a multi-valued attribute. The `DatadogProvisioningTranslator` automatically populates this attribute by resolving the admin group's members to their Datadog user UUIDs (from entityAttributeValueCache0). No JEXL translation expression is needed for this attribute.

#### Admin group membership requirement

Members of the admin group must also be members of the provisionable team group to receive the admin role in Datadog. The provisioner only manages team membership for members of the provisionable group -- the admin metadata only controls whether existing team members are promoted to admin or kept as regular members. A recommended pattern is to add the admin group as a member of the team group so that all admins are automatically team members.

#### Multiple provisioner runs needed

When a new team admin user is first provisioned, the admin role promotion requires two provisioner runs:

1. **First run**: Creates the team in Datadog and adds the user as a team member with the default "member" role. The translator populates the `admins` attribute on the Grouper side.
2. **Second run**: The provisioner compares the Grouper-side `admins` attribute against the target and detects the difference. It then calls `PATCH /api/v2/team/{teamId}/memberships/{userId}` to promote the user to "admin".

Once a user is already a team member, subsequent changes to the admin group (adding or removing admin members) take effect on the next provisioner run.

### JSON:API format

All Datadog v2 endpoints use JSON:API format. Request and response bodies use the structure:

{ "data": { "type": "users", "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "attributes": { "name": "John Doe", "email": "john.doe@example.com" } } }List endpoints return `data` as an array. Single-object endpoints return `data` as an object.

### Paging

All list endpoints use `page[size]` (max 100) and `page[number]` (0-indexed). The provisioner pages through results until fewer than `page[size]` results are returned.
