---
title: "Grouper Freshservice agent provisioner"
space: Grouper
pageId: 341966857
version: 8
lastUpdated: 2026-10-31T03:34:03.929Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/341966857/Grouper+Freshservice+agent+provisioner
---

> The info on this page applies to Grouper v4.28.0+, v6.7.0+, and v7.10.0+. The provisioner was contributed by North Dakota State University.

## External system

[Grouper Freshservice external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system)

## Links

- Freshservice URL: https://somedomainname.freshservice.com
- [Freshservice API docs](https://api.freshservice.com/)
- [Grouper Freshservice requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner) (provisions requesters, the people who submit tickets)

## Overview

The Freshservice agent provisioner manages **agents**, **agent groups**, and **agent group memberships** in Freshservice via the Freshservice REST API v2. Agents are the IT staff who work tickets. It is built the same way as the [Grouper Freshservice requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner), so the configuration is nearly identical. This page covers the differences.

The provisioning type is `membershipObjects`:

- **Entities** - Freshservice agents
- **Groups** - Freshservice agent groups
- **Memberships** - agents in agent groups

Use group and entity link (target attribute value caches) for the Freshservice-assigned numeric ids.

The provisioner class is `edu.internet2.middleware.grouper.app.freshServiceAgent.FreshAgentProvisioner`.

A Freshservice email is either a requester or an agent, not both. If you run both provisioners, subtract the agent population from the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner).

The API key needs to be from an account that can manage agents and groups (generally an administrator).

## Provisioner settings

| Config | Default | Description |
| --- | --- | --- |
| freshserviceExternalSystemConfigId |  | Required. The Freshservice external system. |
| defaultAgentRoleId |  | Freshservice requires a role to create an agent. This numeric role id is assigned to new agents. If blank, creates fail unless the agent already has a role. |
| defaultAgentRoleAssignmentScope | entire_helpdesk | Scope of the default role: entire_helpdesk, member_groups, specified_groups, or assigned_items. |
| reactivateAsFullTime | true | Freshservice reactivates agents (and converts requesters) as occasional agents. If true, the provisioner then sets them to full-time, which uses a licensed seat. Set to false to leave them occasional. |
| provisionRoles | false | If true, a Grouper group can provision to an existing Freshservice agent role instead of an agent group. See "Agent roles" below. |

To find a role id, use the Freshservice UI (Admin, Roles) or GET /api/v2/roles.

## Provisioning attributes

### Agent group attributes

Same as the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner): id (Freshservice-assigned, select-only, cache it), name (required), description.

### Agent (entity) attributes

Same as the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner) (id, firstName, lastName, email, externalId, jobTitle, workPhoneNumber, departmentId, reportingManagerId, address, active, and customField_*), with these differences:

| Grouper attribute | Freshservice field | Description |
| --- | --- | --- |
| email | email | The agent API field is email (the requester API uses primary_email). Used for matching and the upsert check on create. |
| occasional | occasional | Boolean. True for an occasional agent, false for full-time. Optional. |

Custom fields work the same as the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner), but use the agent fields defined in Freshservice.

## Matching

Same as the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner) (id first, then an opaque unchanging id such as externalId or a custom field, else email), against /api/v2/agents:

- id: GET /api/v2/agents/{id}
- email: GET /api/v2/agents?email=value
- externalId or customField_<name>: GET /api/v2/agents?query=...

## CRUD operations

| Object | Operation | Notes |
| --- | --- | --- |
| Agent | Insert | Upsert: reactivates an existing deactivated agent, converts a requester with the same email to an agent, else creates. Then updates the provisioned fields. |
| Agent | Update | GET-then-PUT, only changed fields are sent. Read-only fields are stripped. Active cannot be updated (see below). |
| Agent | Delete | Soft delete (deactivation) with DELETE /api/v2/agents/{id} |
| Agent group | Insert, update, delete | /api/v2/groups. Delete is permanent. |
| Agent role | Retrieve, link | Roles are read-only in the API, so they are never created, updated, or deleted. A role group links to an existing role. |
| Membership | Retrieve, insert, delete | The members array on the group (see below) |

## Behavior differences from the requester provisioner

### Memberships are the group members array

Agent groups have no member endpoints. Members are a "members" array of agent ids on the group. To add or remove a member, the provisioner GETs the group, changes the array, and PUTs the whole array back. Retrieved members only have the agent id.

### Create converts requesters

If no agent has the email but a requester does, the provisioner converts that requester with PUT /api/v2/requesters/{id}/convert_to_agent, keeping the Freshservice id and history.

### Occasional after reactivate or convert

Freshservice makes reactivated and converted agents occasional. With reactivateAsFullTime=true (the default) the provisioner then sets occasional=false.

### Active is not updated directly

Freshservice rejects active on an agent update. Agents are deactivated with DELETE and reactivated with PUT /api/v2/agents/{id}/reactivate.

### No agent or rule-based filtering

Unlike the [requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner), nothing is filtered out of agent or group lists.

## Agent roles

With provisionRoles=true, a Grouper group can provision to a Freshservice agent role instead of an agent group. Requested by NDSU.

One provisioner does both at the same time, the group type of each group decides. For example, mark the folder test:freshservice:roles as "Agent role" and its groups link to roles, while groups in test:freshservice:groups (no metadata) are agent groups.

- **Group type**: set the "Freshservice group type" provisioning metadata to "Agent role" on a folder (applies to the groups under it) or a group. The default is "Agent group". It cannot be changed once provisioned. Instead of metadata, you can translate a groupType target group attribute to group or role.
- **The role must exist**: the Freshservice API cannot create roles. A role group matches an existing role by name (or id). If no role matches, that group is skipped with an error, everything else is provisioned, and the job ends in error. Create the role in Freshservice and the next sync links it.
- **Roles are not changed**: name and description changes are not sent, and deleting a role group in Grouper leaves the role in Freshservice.
- **Memberships**: a role membership is the roles array on the agent. The provisioner GETs the agent, adds or removes the role, and PUTs the roles back. Other roles on the agent are kept. New role assignments use defaultAgentRoleAssignmentScope.
- **Last role**: agents need at least one role. Removing the last role assigns defaultAgentRoleId instead, or fails that membership if the role being removed is the default role.
- **Names**: agent groups and roles are matched together, so keep names unique across both (or match by id).
- **Roles Grouper does not manage**: keep deleteMembershipsOnlyInTrackedGroups=true (the default) so agents are not removed from roles that are not provisioned from Grouper.

In the target and the sync tables, a role id is prefixed with role_ (e.g. role_123) so it does not collide with an agent group id.

## Example configuration

Use the [requester example configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner), and change the class and add the agent settings:

```
provisioner.myFreshserviceAgents.class = edu.internet2.middleware.grouper.app.freshServiceAgent.FreshAgentProvisioner
provisioner.myFreshserviceAgents.freshserviceExternalSystemConfigId = myFreshservice
provisioner.myFreshserviceAgents.defaultAgentRoleId = 12345
provisioner.myFreshserviceAgents.defaultAgentRoleAssignmentScope = entire_helpdesk
provisioner.myFreshserviceAgents.reactivateAsFullTime = true
provisioner.myFreshserviceAgents.provisionRoles = true
```

## Grouper development team testing

Mock service URL path: /grouper/mockServices/freshAgent. See the [Grouper Freshservice external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system) page for the mock setup.
