---
title: "Grouper Freshservice agent provisioner developer notes"
space: GrIntDev
pageId: 342982658
version: 2
lastUpdated: 2026-10-31T02:55:27.378Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/342982658/Grouper+Freshservice+agent+provisioner+developer+notes
---

The Freshservice agent provisioner (edu.internet2.middleware.grouper.app.freshServiceAgent.FreshAgentProvisioner) provisions Grouper groups to Freshservice agent groups, and members to Freshservice agents. It was contributed by North Dakota State University (GRP-7539), and rebuilt from the Freshservice requester provisioner so the two are consistent. Read [Grouper Freshservice provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792517/Grouper+Freshservice+provisioner+developer+notes) first, this page only covers what is different for agents.

> Unverified: the request and response samples on this page come from the Grouper mock service and the contributed code, not from calls to a real Freshservice. Replace them with real captures when they are tested against a Freshservice instance.

## Log in to UI

- (Log in) https://somedomainname.freshservice.com
- (Docs) [https://api.freshservice.com/](https://api.freshservice.com/)

## External system

[Freshservice external system documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system). Same external system, authentication, rate limiting, and paging as the requester provisioner. Agents and requesters in the same Freshservice can use the same external system.

## Differences from the requester provisioner

- Endpoints are /api/v2/groups and /api/v2/agents, and json is wrapped in "group" / "groups" / "agent" / "agents". Agents use "email" (requesters use "primary_email")
- Agent groups do not have member endpoints. Members are a "members" array of agent ids on the group, so a membership add or remove is a GET of the group and a PUT of the whole members array (only if it changed). This is last write wins if something else edits the group at the same time
- Freshservice requires at least one role to create an agent, so the provisioner assigns the configured default role to new agents. It does not change roles of existing agents
- Freshservice has one unique email across agents and requesters. If a requester has the email of a new agent, the requester is converted to an agent (same id) with convert_to_agent, then updated
- Freshservice reactivates agents, and converts requesters, as occasional (day pass) agents. If reactivateAsFullTime is true (default) the provisioner then sets occasional=false, which uses a licensed seat
- active cannot be sent on an agent PUT. Agents are deactivated with DELETE and reactivated with /reactivate
- Agent groups do not have a rule_based type, so no groups are skipped
- With provisionRoles=true a Grouper group can be an agent role instead of an agent group (GRP-7546, see Roles below)

## Configuration

In addition to the requester provisioner settings:

| Property | Default | Description |
| --- | --- | --- |
| provisioner.<configId>.defaultAgentRoleId | blank | Freshservice role id assigned to new agents. If blank, an agent create fails since a role is required |
| provisioner.<configId>.defaultAgentRoleAssignmentScope | entire_helpdesk | assignment_scope of the default role: entire_helpdesk, member_groups, specified_groups, assigned_items |
| provisioner.<configId>.reactivateAsFullTime | true | set reactivated and converted agents to full-time (occasional=false) |
| provisioner.<configId>.provisionRoles | false | adds the md_grouper_freshserviceGroupType metadata (group or role) so a Grouper group can provision to an existing role |

## Group

Primary key: id

| mock_freshagent_group |
| --- |
| Column | Type | Description |
| id | bigint | assigned by Freshservice, not null after create, readonly |
| name | varchar(256) | name of group, not null |
| description | varchar(1024) | description, nullable |

Mappable provisioning attributes: id, name, description

## User

Crud: select, insert, update, delete (deactivate)

Primary key: id

| mock_freshagent_user |
| --- |
| Column | Type | Description |
| id | bigint | assigned by Freshservice, not null after create, readonly |
| email | varchar(256) | case insensitive, unique across agents and requesters |
| active | varchar(1) | T or F |
| occasional | varchar(1) | T is occasional (day pass), F is full-time (licensed seat) |
| first_name | varchar(256) |  |
| last_name | varchar(256) |  |
| address | varchar(512) |  |
| external_id | varchar(256) | opaque and unchanging id |
| custom_fields | varchar(4000) | json for custom fields |
| roles_json | varchar(4000) | json roles array, required on create |
| department_id | bigint |  |
| job_title | varchar(256) |  |
| reporting_manager_id | bigint |  |
| work_phone_number | varchar(50) |  |

Mappable provisioning attributes: active, address, departmentId, email, externalId, firstName, id, jobTitle, lastName, occasional, reportingManagerId, workPhoneNumber, and customField_<name>

## Membership

Crud: select, insert, delete

| mock_freshagent_membership |
| --- |
| Column | Type | Description |
| id | bigint | random integer |
| group_id | bigint | id from group |
| user_id | bigint | id from user |

The mock returns these rows as the "members" array of the group, and a PUT of the members array replaces them

## Get groups

/api/v2/groups?per_page=100&page=1. Start at page 1, and increment until fewer than a page of results

```
Authorization: Basic abc123
GET $SERVICE_URL$/api/v2/groups?per_page=100&page=1

{
    "groups": [
        {
            "id": 39000032974,
            "name": "Service desk",
            "description": null,
            "members": [39000111111, 39000222222]
        }
    ]
}
```

## Get group

/api/v2/groups/1234567. 404 if not found

```
GET $SERVICE_URL$/api/v2/groups/1234567

{
    "group": {
        "id": 1234567,
        "name": "Service desk",
        "description": null,
        "members": [39000111111, 39000222222]
    }
}
```

## Create group

```
POST /api/v2/groups
Content-Type: application/json

{ "name": "Service desk", "description": "Service desk agents" }
```

Response 200 with the "group" wrapper. 409 if already exists

## Update group

GET the group, overlay the changed name / description, PUT. Members are not sent, so they are not changed

```
PUT /api/v2/groups/1234567
Content-Type: application/json

{ "name": "Service desk", "description": "New description" }
```

## Delete group

```
DELETE /api/v2/groups/1234567
```

Returns 204 if successful. 404 if already deleted

## Get all agents

/api/v2/agents?per_page=100&page=1. Inactive agents are filtered out unless needed (e.g. to reactivate)

```
GET $SERVICE_URL$/api/v2/agents?per_page=100&page=1

{
    "agents": [
        {
            "id": 39000111111,
            "first_name": "John",
            "last_name": "Smith",
            "email": "jsmith@school.edu",
            "active": true,
            "occasional": false,
            "job_title": "Analyst",
            "department_ids": [39000211201],
            "external_id": "12345678",
            "roles": [ { "role_id": 39000000001, "assignment_scope": "entire_helpdesk" } ],
            "custom_fields": { "pennkey": "jsmith" }
        }
    ]
}
```

## Get agent by ID, email, or attribute

- By id: GET /api/v2/agents/39000111111, "agent" wrapper, 404 if not found
- By email: GET /api/v2/agents?email=jsmith%40school.edu
- By external id or custom field: GET /api/v2/agents?query=external_id:'12345678' (strings quoted, numbers not). More than one result is an error

## Create agent

The provisioner first looks up the email: an existing agent is reactivated (if inactive) and updated, a requester is converted to an agent and updated, otherwise the agent is created. roles is required

```
POST /api/v2/agents
Content-Type: application/json

{
    "first_name": "John",
    "last_name": "Smith",
    "email": "jsmith@school.edu",
    "roles": [ { "role_id": 39000000001, "assignment_scope": "entire_helpdesk" } ]
}
```

Response 201 with the "agent" wrapper. 400 if roles is missing. 409 if the email exists (agent or requester)

## Update agent

GET the agent, remove read-only fields (id, active, created_at, updated_at, has_logged_in, last_login_at, last_active_at, work_schedule_id, department_names, location_name, member_of_pending_approval, observer_of_pending_approval, auto_assign_status_changed_at), overlay the changed fields, PUT. The read-only list needs to be verified against a real Freshservice

```
PUT /api/v2/agents/39000111111
Content-Type: application/json

{ ...current agent minus read-only fields..., "job_title": "Senior analyst" }
```

## Delete agent (deactivate)

```
DELETE /api/v2/agents/39000111111
```

Returns 204. 404 if not found

## Reactivate agent

```
PUT /api/v2/agents/39000111111/reactivate
```

Returns 200 (agent is now occasional), 400 if already active. If reactivateAsFullTime, the provisioner then sends:

```
PUT /api/v2/agents/39000111111
Content-Type: application/json

{ "occasional": false }
```

## Delete agent (forget / permanent)

```
DELETE /api/v2/agents/39000111111/forget
```

Returns 204, 404 if already deleted. Not used by the provisioner

## Convert requester to agent

Used by create when a requester has the email. The requester is looked up with GET /api/v2/requesters?email=... (see the requester page). The agent has the same id as the requester, is occasional, and has the SD Agent role

```
PUT /api/v2/requesters/39000333333/convert_to_agent

{
    "agent": {
        "id": 39000333333,
        "email": "jsmith@school.edu",
        "active": true,
        "occasional": true,
        "roles": [ { "role_id": 39000000002, "assignment_scope": "entire_helpdesk" } ]
    }
}
```

## Add or remove group membership

GET the group, add or remove the agent id in "members", and if it changed, PUT the whole array. Adding to a group that is not there is an error, removing from it is not

```
GET /api/v2/groups/1234567     -> "members": [39000111111]

PUT /api/v2/groups/1234567
Content-Type: application/json

{ "members": [39000111111, 39000222222] }
```

## List group memberships

The "members" array from GET /api/v2/groups/1234567. There is no paging

## Roles

GRP-7546. Roles are read-only in the API (list and view only), so Grouper never creates, updates, or deletes a role. Classes: FreshAgentRole (bean, mock table), FreshAgentSyncObjectMetadata (group type dropdown), and the role branches in FreshAgentTargetDao and FreshAgentApiCommands

- Group type: a role_ prefixed target id, else the groupType target group attribute, else md_grouper_freshserviceGroupType metadata on the Grouper group, else group. The metadata is on a folder or group and cannot be updated (like md_grouper_azureGroupType)
- Target id of a role is role_<roleId> so it cannot collide with an agent group id, and the DAO knows a membership is a role membership from just the group target id. Sync-back records roles as groups with the same role_ id
- retrieveAllGroups returns agent groups and roles. Roles are cached for the run
- insertGroup of a role links to the existing role by name (or id). If not found it throws, the adapter marks just that group as an error, the rest of the run continues, and the job ends in ERROR (errorHandlingProvisionerDaemonShouldFailOnObjectError)
- updateGroup and deleteGroup of a role are no-ops marked provisioned
- Role members are the agents whose roles array has the role. There is no role member endpoint, so all agents are retrieved once per run and cached (cleared on role membership writes and agent creates)
- Add or remove: GET the agent, copy its roles except the one changing, add it back if adding, and PUT only the roles array (other roles keep their assignment_scope and groups). If removing the last role, the defaultAgentRoleId is assigned instead; if that is the role being removed, it fails since agents need a role
- deleteMembershipsOnlyInTrackedGroups (default true) keeps Grouper from removing agents from roles it does not provision

```
GET /api/v2/roles?page=1&per_page=100   -> "roles": [{"id": 101, "name": "Help desk", ...}]
GET /api/v2/roles/101                   -> "role": {"id": 101, ...}

GET /api/v2/agents/39000111111          -> "agent": {"roles": [{"role_id": 1, "assignment_scope": "entire_helpdesk"}], ...}

PUT /api/v2/agents/39000111111
Content-Type: application/json

{ "roles": [{"role_id": 1, "assignment_scope": "entire_helpdesk"}, {"role_id": 101, "assignment_scope": "entire_helpdesk"}] }
```

Mock: table mock_freshagent_role (id, name, description), GET /roles and /roles/{id}, and a PUT of an empty roles array is a 400. Tests insert roles with sql. Tests: testRetrieveAgentRoles, testAddRemoveAgentRole, testFullSyncRoles
