---
title: "Grouper TrueFoundry provisioner"
space: Grouper
pageId: 28555741
version: 10
lastUpdated: 2026-07-12T15:27:15.030Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555741/Grouper+TrueFoundry+provisioner
---

## External system

[Grouper TrueFoundry external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549505/TrueFoundry+external+system)

## Links

- (Log in) https://<domain>.truefoundry.cloud
- (API) [https://app.truefoundry.com/api/svc/v1](https://app.truefoundry.com/api/svc/v1)
- (API Docs) [https://www.truefoundry.com/docs/api-reference](https://www.truefoundry.com/docs/api-reference)
- (SDK Docs) [https://www.truefoundry.com/docs/truefoundry_sdk](https://www.truefoundry.com/docs/truefoundry_sdk)

## Overview

The TrueFoundry Provisioner manages **users**, **roles**, **teams**, and **team memberships** in a TrueFoundry tenant via the native REST API. http://localhost:8080/whatever

TrueFoundry has two distinct group-like concepts managed by this provisioner:

- **Teams** - groups of users, managed via the team manifest PUT endpoint. Members and managers are specified by email. The provisioner uses replaceGroupMemberships semantics (full member list is sent each time). **TrueFoundry requires at least one member per team at all times.** A configured default team member (service account) is always kept in every team so the team is never empty.
- **Roles** - RBAC roles assigned to individual users. Roles are account-scoped and created in the TrueFoundry UI by administrators. The provisioner assigns existing roles to users; it does not create roles. **Every user must be in exactly one role at all times.** The recommended pattern uses scripted Grouper groups with mutual-exclusion logic to enforce this constraint (see *Setting up role groups* below).

All provisioning is performed via the native REST API. The SCIM v2 endpoint is not used for user creation -- SCIM users have `isEditable: false` and cannot have roles assigned, making SCIM insufficient for full provisioning needs. SCIM PATCH is used only for display name updates on natively-registered users.

The provisioner class is `edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryProvisioner`.

This provisioner works best if you set "replace memberships" to true.

You should add the team manager Grouper group to be a member of the Team member grouper group to have good results.

## Provisioner configuration

The following configuration properties are specific to the TrueFoundry provisioner. These are set via the provisioner configuration in the Grouper loader properties (database or file), with the prefix `provisioner.<configId>.`.

| Config Suffix | Required? | Default | Description |
| --- | --- | --- | --- |
| `trueFoundryExternalSystemConfigId` | Yes |  | The external system config id (WsBearerToken) for TrueFoundry. The accessTokenPassword holds the TrueFoundry access token. |
| `trueFoundryDefaultRole` | No | `read-only-member` | Default role to assign to users when a role membership is removed. TrueFoundry requires every user to have exactly one role, so when a user is removed from a role group, the provisioner assigns this default role rather than leaving them with no role. Set this to the least-privileged role in your TrueFoundry tenant. |
| `trueFoundryDefaultRoleResourceType` | No | `account` | The resourceType to use when assigning the default role. Typically `account`. |
| `trueFoundryIgnoreUserEmails` | No |  | Comma-separated list of user emails to ignore during provisioning. These users will be filtered out of retrieve operations and will not be created, updated, or deleted. Use this to protect admin or service accounts from being modified by the provisioner. |
| `trueFoundryIgnoreRoles` | No |  | Comma-separated list of role names to ignore during provisioning. These roles will be filtered out of retrieve operations and will not be created, updated, or deleted. Use this to protect built-in or administrative roles. |
| `trueFoundryDefaultTeamMemberEmail` | Yes |  | Email address of a service account that is always kept as a member of every team. **TrueFoundry requires at least one member per team at all times.** This email is added as the initial member when a team is created, and is kept (re-added if necessary) when all other members are removed, so no team is ever left empty. Use a dedicated service account such as `[svc-grouper@example.edu](mailto:svc-grouper@example.edu)`. Note: the service team is responsible for ensuring at least one real person remains in each team; if all real members are removed the provisioner will keep only this default member and keep retrying to remove the real members on subsequent syncs. |
| `trueFoundryAddTeamManagerMetadata` | No | `false` | Whether to support team manager metadata. When enabled, adds a metadata field on each provisionable team group that points to a Grouper group whose members should be team managers. |
| `trueFoundryTeamManagerMetadataName` | No | `md_trueFoundryTeamManager` | The metadata attribute name for team manager group path. Only used when `trueFoundryAddTeamManagerMetadata=true`. |
| `trueFoundryScimTenantName` | Yes |  | TrueFoundry tenant name (e.g. `myschool-prod`). Required for SCIM display name updates. To find this value go to **Settings → SSO** in the TrueFoundry UI and locate the SCIM URL. The SCIM URL has the form `[https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId](https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId)}`. The tenant name is the segment directly after `/scim/v2/` (e.g. `myschool-prod` in `.../scim/v2/myschool-prod/abc123`). |
| `trueFoundryScimSsoId` | Yes |  | TrueFoundry SCIM SSO ID. Required for SCIM display name updates. To find this value go to **Settings → SSO** in the TrueFoundry UI and locate the SCIM URL. The SCIM URL has the form `[https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId](https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId)}`. The SSO ID is the segment at the end of the URL after the tenant name (e.g. `abc123` in `.../scim/v2/myschool-prod/abc123`). |

## Naming requirements

Team and role names in TrueFoundry must follow these rules:

- **Must start with a lowercase letter** (a–z). Digits and hyphens are not allowed as the first character.
- **Must end with a lowercase letter or digit** (a–z, 0–9). Hyphens are not allowed as the last character.
- **Middle characters** may be lowercase letters (a–z), digits (0–9), or hyphens (-). No uppercase letters, underscores, spaces, or other special characters.
- **Length** must be between 3 and 36 characters inclusive.

Examples of valid names: `my-team`, `read-only-member`, `ml-platform-v2`.  
Examples of invalid names: `MyTeam` (uppercase), `-team` (starts with hyphen), `team-` (ends with hyphen), `ab` (too short).

The provisioner validates names at provisioning time and will throw an error if a name does not conform.

## Grouper folder structure

The provisioner uses the Grouper folder structure to distinguish between teams and roles. Create a folder hierarchy like the following under your provisioning root:

 myOrg:apps:truefoundry:roles: <-- role groups go here myOrg:apps:truefoundry:teams: <-- team groups go here myOrg:apps:truefoundry:teamManagers: <-- (optional) team manager groups go here Configure the `groupType` target group attribute with a `translationScript` expression that derives the type from the folder path. For example:

${grouperProvisioningGroup.getName().startsWith("myOrg:apps:truefoundry:roles:") ? "role" : "team"}Attach the provisioning attribute to the parent folder (e.g. `myOrg:apps:truefoundry`) with scope `sub` so all groups under `roles` and `teams` are provisioned.

### Setting up role groups

TrueFoundry has four built-in roles that the provisioner can assign to users. The group **extension** must exactly match the TrueFoundry role name.

| TrueFoundry UI name | TrueFoundry role name | Grouper extension | Grouper display extension | Description | Notes |
| --- | --- | --- | --- | --- | --- |
| Member | `member` | `member` | Member | Role grants member access to the entities in the account | Default role for regular users |
| Read-Only Member | `read-only-member` | `read-only-member` | Read-Only Member | Role grants read-only access for all resources | Least-privileged role |
| Team Manager | `team-manager` | **System-managed — do not create a Grouper group for this role.** This role is automatically assigned by TrueFoundry to team managers. The provisioner filters it from retrieval and throws an error if assignment is attempted. |
| Tenant Admin | `tenant-admin` | `tenant-admin` | Tenant Admin | Role grants admin permissions on all entities in the tenant | resourceType is `tenant`. Users can be assigned to this role, but the role itself cannot be created, updated, or deleted via the provisioner. |

The provisioner automatically determines the correct resourceType based on the role name (`tenant-admin` → `tenant`, all others → `account`).

Custom roles created by administrators in the TrueFoundry UI can also be managed by creating additional groups with matching extensions (e.g. `myOrg:apps:truefoundry:roles:customrole1`).

#### One-and-only-one role constraint

**Every user must be in exactly one role group at all times.** If a user is in multiple role groups the provisioner will keep assigning roles in an undefined order, leaving the user in whichever was last processed. If a user is in no role group the provisioner assigns the configured `trueFoundryDefaultRole`.

The recommended pattern uses two sibling folders to enforce mutual exclusion with scripted Grouper groups:

- `roleAssignments:` — contains simple assignment groups, one per role. Administrators add users here directly.
- `roles:` — contains scripted groups, one per role. These are the groups actually marked as provisionable. Each scripted group subtracts higher-priority roles to ensure no user is in two role groups at once.

The priority order (highest to lowest) is: `tenant-admin` > higher custom roles > `member` > `read-only-member`. Higher-priority roles take precedence; a user in `roleAssignments:tenant-admin_assigned` is removed from all lower-priority role groups by the scripting logic.

Example folder layout:

 myOrg:apps:truefoundry:roleAssignments:tenant-admin_assigned <-- direct assignment group myOrg:apps:truefoundry:roleAssignments:subadmin_assigned myOrg:apps:truefoundry:roleAssignments:member_assigned myOrg:apps:truefoundry:roleAssignments:read-only-member_assigned myOrg:apps:truefoundry:roles:tenant-admin <-- scripted, provisionable myOrg:apps:truefoundry:roles:subadmin myOrg:apps:truefoundry:roles:member myOrg:apps:truefoundry:roles:read-only-member Scripted group definitions (using Grouper composite / filter logic):

| Scripted group | Members |
| --- | --- |
| `roles:tenant-admin` | Members of `roleAssignments:tenant-admin_assigned` |
| `roles:subadmin` | Members of `roleAssignments:subadmin_assigned`   AND NOT members of `roles:tenant-admin` |
| `roles:member` | Members of `roleAssignments:member_assigned`   AND NOT members of `roles:tenant-admin`   AND NOT members of `roles:subadmin` |
| `roles:read-only-member` | Members of `roleAssignments:read-only-member_assigned`   AND NOT members of `roles:tenant-admin`   AND NOT members of `roles:subadmin`   AND NOT members of `roles:member` |

With this pattern a user can be added to multiple `roleAssignments` groups (e.g. promoted by accident) but will appear in only one `roles` group — the highest-priority one they qualify for — guaranteeing TrueFoundry receives exactly one role assignment per user.

Attach the provisioning attribute to `myOrg:apps:truefoundry:roles` (not `roleAssignments`) so only the scripted role groups are provisioned.

### Setting up team manager groups (optional)

If `trueFoundryAddTeamManagerMetadata` is enabled, create a manager group for each team under a `teamManagers` folder. Set the `md_trueFoundryTeamManager` metadata on the team group to the path of the corresponding manager group. Members of the manager group who are also members of the team group will be added to the team's `managers` list in TrueFoundry.

For example:

- Team group: `myOrg:apps:truefoundry:teams:engineering`
- Manager group: `myOrg:apps:truefoundry:teamManagers:engineering`
- Metadata on the team group: `md_trueFoundryTeamManager = myOrg:apps:truefoundry:teamManagers:engineering`

## Provisioning attributes

#### User (entity) attributes

TrueFoundry is purely email-based -- there are no display names shown in the product UI. It is recommended to use EPPN (eduPersonPrincipalName) as the email value if it is email-routable for all users, since it works best with SSO and handles users with multiple email addresses correctly.

| Grouper Attribute Name | Type | Required? | TrueFoundry API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String | Yes | n/a (framework) | Entity ID — set to the native TrueFoundry user ID (e.g. `pt3vuwlxupmefpk8i9cj11du`). Retrieved from TrueFoundry and cached by the framework. Used as the SCIM user identifier for display name updates. The email address is stored separately in the `email` attribute and used for all other API calls. |
| `email` | String | Yes | `email` | Email address. Used for all API calls: deactivate, activate, register, role assignment, and team membership. Translate from Grouper `email` field. Recommend using EPPN. |
| `displayName` | String | No | SCIM `displayName` | Full display name. Set/updated via SCIM PATCH using the email as the SCIM user identifier (requires `trueFoundryScimTenantName` and `trueFoundryScimSsoId`). SCIM PATCH on a natively-registered user does not convert them to a SCIM user. |
| `active` | String (T/F) | No | `active` | Whether the user is active. Deactivate via PATCH /users/deactivate; reactivate via PATCH /users/activate. |

#### Team (group) attributes

| Grouper Attribute Name | Type | Required? | TrueFoundry API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String | Yes | `id` | ID assigned by TrueFoundry. **Read-only / select-only.** Cache in an attribute value cache for linking. |
| `name` | String | Yes | `[manifest.name](http://manifest.name)` | Team name. Must meet the naming requirements described below (3–36 characters, starts with a lowercase letter, ends with a lowercase letter or digit, middle characters are lowercase letters, digits, or hyphens). Must be unique within the tenant. |
| `groupType` | String | Yes | n/a (virtual) | Must be set to `team` (typically via a static attribute translation). |
| `managers` | Set<String> (multi-valued) | No | `manifest.managers` | Multi-valued attribute containing email addresses of team managers. Populated automatically when `trueFoundryAddTeamManagerMetadata` is enabled. Only applies to teams. |

#### Role (group) attributes

| Grouper Attribute Name | Type | Required? | TrueFoundry API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String | Yes | `id` | ID assigned by TrueFoundry. **Read-only / select-only.** |
| `name` | String | Yes | `name` | Role name (e.g. `member`, `read-only-member`, or a custom role name). Must meet the naming requirements described below. Used when assigning roles to users. |
| `displayName` | String | No | `manifest.displayName` | Human-readable display name shown in the TrueFoundry UI. Defaults to `name` if blank. |
| `description` | String | No | `manifest.description` | Description of the role. Defaults to `displayName` if blank. |
| `groupType` | String | Yes | n/a (virtual) | Must be set to `role` (typically via a translation script based on the folder path). |

Note: `resourceType` is not a configurable attribute. The provisioner automatically uses `tenant` for the `tenant-admin` role and `account` for all other roles.

## CRUD operations

| Object | Operation | Supported? | Notes |
| --- | --- | --- | --- |
| Entity (User) | Retrieve all | Yes | Paginated via limit/offset. Inactive users filtered by default. |
| Retrieve one | Yes | By email only (search by ID is not supported) |
| Insert | Yes | Upsert: if user exists but is inactive, reactivates. Otherwise registers via POST /users/register then retrieves by email for ID. |
| Update | Yes | Display name via SCIM PATCH (if configured). Activate/deactivate. |
| Delete | Yes | **Soft delete** (deactivate), not permanent removal |
| Team (Group) | Retrieve all | Yes | Paginated via limit/offset |
| Retrieve one | Yes | By id |
| Insert / Update | Yes | Same PUT endpoint for create and update (manifest-based) |
| Delete | Yes | 409 if attempting to delete the default "everyone" team -- treated as non-fatal |
| Role (Group) | Retrieve all | Yes | GET /api/svc/v1/role/list (account and tenant scoped only) |
| Insert / Update / Delete | Yes | Supported but generally roles should be created in the TrueFoundry UI by administrators |
| Team Membership | Insert | Yes | Adds user to team members list via PUT /teams (full replacement) |
| Delete | Yes | Removes user from team members and managers lists via PUT /teams (full replacement) |
| Role Assignment (Membership) | Retrieve all | Yes | Read from the `rolesWithResource` array on each user returned by the subjects endpoint. The `roleId` is cross-referenced with the role list to resolve the role name. |
| Insert | Yes | Assigns role to user via PATCH /users/roles. Replaces any existing role. |
| Delete | No-op | TrueFoundry users always have exactly one role. Assigning a new role (insert) automatically replaces the old one, so explicit delete is unnecessary. |

## Behavioral notes

### Entity create is upsert

When creating a new user, the provisioner first checks if a user with that email already exists (including inactive users). If found:

1. If the existing user is inactive, the provisioner reactivates them via `PATCH /api/svc/v1/users/activate`.
2. Returns the existing user (with their original TrueFoundry user ID).

Only if no existing user is found does the provisioner register a new user via `POST /api/svc/v1/users/register`. Since the register endpoint returns `{}` with no user ID, the provisioner then looks up the user by email to retrieve the assigned ID.

### Entity delete is soft delete (deactivate)

When the provisioner deletes an entity, it calls `PATCH /api/svc/v1/users/deactivate` rather than `DELETE /api/svc/v1/users/{id}`. The user remains in TrueFoundry in a deactivated state and can be reactivated later (e.g., by the upsert flow on a subsequent create). Hard delete is not used because TrueFoundry returns a 400 error if you attempt to hard-delete a user who is still a member of any team. Before issuing a hard delete, the provisioner first removes the user from all teams they belong to (adding the default team member if needed to maintain the minimum-one-member requirement).

### Role assignment is replace-only

TrueFoundry requires every user to have exactly one role. There is no API to "remove" a role -- you can only assign a different one. When the provisioner assigns a role via PATCH /users/roles, it replaces whatever role the user previously had. Therefore, role membership delete is a no-op in the provisioner -- the old role is automatically replaced when the new role is assigned.

Each user must be in exactly one role group in Grouper at any time. Moving a user from one role group to another triggers an insert on the new role, which replaces the old one in TrueFoundry. Use the `roles:` / `roleAssignments:` scripted group pattern described above to enforce mutual exclusion and prevent a user from ever appearing in two role groups simultaneously.

### Ignoring users and roles

Use `trueFoundryIgnoreUserEmails` to specify a comma-separated list of user emails that the provisioner should not touch. These users will be filtered out of all retrieve operations and will not be created, updated, or deleted. This is useful for protecting admin accounts or service accounts.

Use `trueFoundryIgnoreRoles` to specify a comma-separated list of role names that the provisioner should not touch. These roles will be filtered out of role retrieval and will not be created, updated, or deleted. This is useful for protecting built-in administrative roles.

### System-managed "everyone" team

TrueFoundry automatically maintains an "everyone" team containing all tenant users. This team is system-managed and cannot be edited or deleted. The provisioner automatically filters it from team retrieval and will throw an error if creation, update, or deletion is attempted. **Do not create a Grouper group for the "everyone" team.**

### System-managed roles

The `team-manager` role is automatically assigned by TrueFoundry to team managers. The provisioner filters it from role retrieval and throws an error if assignment is attempted. **Do not create a Grouper group for the "team-manager" role.**

The `tenant-admin` role can be assigned to users (by adding them to a `tenant-admin` role group in Grouper), but the role itself cannot be created, updated, or deleted via the provisioner. The provisioner will throw an error if role creation, update, or deletion is attempted for `tenant-admin`.

### Team membership is full replacement

There is no separate add-member or remove-member endpoint for teams. The full membership list is replaced atomically via the team manifest PUT. The provisioner retrieves the current team state, modifies the member/manager lists, and PUTs the updated manifest. This means each membership change requires one GET + one PUT to the team endpoint.

### Team managers

Team membership supports a managers distinction. Enable `trueFoundryAddTeamManagerMetadata` and set the metadata attribute on each provisionable team group to point to a Grouper group whose members should be team managers. Members of that manager group who are also members of the team group will be placed in the `managers` array in the team manifest; all others go in the `members` array.

### SCIM display name updates

Display name updates use the SCIM v2 PATCH endpoint at `/api/svc/v1/scim/v2/{tenantName}/{ssoId}/Users/{nativeId}`. The native TrueFoundry user ID (from the `id` entity attribute) is used as the SCIM user identifier. This requires both `trueFoundryScimTenantName` and `trueFoundryScimSsoId` to be configured. Important: do not create users via SCIM. SCIM-created users have `isEditable=false` and cannot have roles assigned.

### Role types managed by the provisioner

The provisioner only manages roles with `resourceType` of `account` or `tenant`. Other role types (cluster, workspace, team, mcp-server, etc.) are resource-scoped and are not visible to the provisioner.

### Paging

List endpoints (users, teams) use `limit` (default 100, max 1000) and `offset` (0-indexed). Role listing returns all roles in a single call (no paging). Team memberships are embedded in the team manifest so no separate paging is needed for memberships.
