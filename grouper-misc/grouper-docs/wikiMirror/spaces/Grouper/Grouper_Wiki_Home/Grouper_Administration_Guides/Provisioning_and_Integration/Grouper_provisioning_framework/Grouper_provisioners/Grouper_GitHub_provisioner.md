---
title: "Grouper GitHub provisioner"
space: Grouper
pageId: 171868186
version: 3
lastUpdated: 2026-08-30T08:26:23.409Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/171868186/Grouper+GitHub+provisioner
---

> Membership-driven GitHub provisioner: manages organization team membership. It does not create GitHub accounts (people are invited) or teams (teams must pre-exist).

## External system

[Grouper GitHub external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/171835422/Grouper+GitHub+external+system)

## Links

- GitHub organization people: https://github.com/orgs/<org>/people
- [GitHub REST API docs](https://docs.github.com/en/rest)
- [GitHub GraphQL API docs](https://docs.github.com/en/graphql)

## Overview

The GitHub provisioner maps Grouper groups to GitHub **organization teams** and group members to **team members**. Organization membership is a derived prerequisite: a person is an org member because they are in at least one mapped team, so adding the first team membership invites them to the org, and removing their last team membership (or a full deprovision) removes them from the org.

| Grouper | GitHub |
| --- | --- |
| Folder (a configured managed org) | Organization |
| Group | Organization team |
| Group member | Team member (and derived org member) |
| Subject | Account (GitHub login) |

The provisioning type is membershipObjects. The managed orgs are listed in the githubOrgs config property; each group carries an org target attribute selecting which org its team lives in. Enterprise teams (slug prefixed ent:) are read-only.

## Provisioning attributes

**Group (team) attributes:**

| Name | Description |
| --- | --- |
| id | the team slug; the target key used for membership URLs (translate from the group extension) |
| name | team display name |
| org | the organization login the team belongs to |
| teamType | organization or enterprise (read only) |
| privacy | closed or secret |
| description | team description |

**Entity (account) attributes:**

| Name | Description |
| --- | --- |
| id | the GitHub login; the target key used for all membership operations |
| samlNameId | the SAML NameID from the enterprise external-identities map (present only when githubEnterpriseSlug is set) |
| email | used only to bootstrap an org invitation; not returned by list calls |
| githubId | the numeric account id (select only) |

## Entity and group matching

Groups are matched on the team **slug** (the id attribute). Entities are matched on **samlNameId** when SAML is in use (the deployer sets samlNameId from the subject attribute the IdP asserts, e.g. eppn), or on **login** otherwise. Because the GitHub login is not derivable from a Grouper subject, both entity link and group link are used: the provisioner reads the enterprise SAML map and the org members, matches the subject to an account, and stores the resulting login for the membership operations.

## CRUD operations

| Operation | GitHub action |
| --- | --- |
| add membership | PUT team membership by login (also invites a known login to the org) |
| remove membership | DELETE team membership (the account stays an org member) |
| delete entity | DELETE org membership in every managed org (full deprovision; drops the account team memberships in the org) |
| retrieve | list teams, team members, org members, org membership state, and the SAML identity map |

Group and entity create/update are NOT supported: teams must pre-exist and accounts are invited rather than created.

## Behavioral notes

### Membership-driven, org membership is derived

The provisioner never models the org as its own group. Org membership follows team membership: the first team-add invites the person to the org; the last team-removal, or a delete entity, removes them. The direct_membership flag on the org membership tells the provisioner whether it owns a given org membership before removing it.

### Async identity (invite then resolve)

A person with a discoverable login (already an org member, or SAML-linked in the enterprise map) is provisioned immediately, including being invited to the org via team-add. A brand-new account whose login cannot be resolved yet has a blank membership key, so the framework defers that membership and retries it on a later sync once the account resolves.

### Enterprise teams are read-only

Enterprise teams use a different endpoint family and are treated as inherited / read-only; the provisioner manages organization teams only.

### Idempotent writes

Add-membership (PUT) is idempotent (200 on repeat), and both removes (DELETE) are idempotent (204 even when the target is already gone), so a full sync can assert desired state without pre-checks.

### Pending team memberships re-insert each sync

GitHub's list-team-members endpoint returns only active members. A team membership that GitHub holds in pending state -- for example an organization member whose enterprise SSO identity is not linked yet -- is not returned by that list, so each full sync recomputes it as an insert and re-issues the (idempotent, 200) team-add PUT. It is harmless and self-resolves once the membership goes active, but it shows as a steady "Target inserts memberships (1)" and one addTeamMembership call every run for that member. To suppress the churn, a deployment can additionally read the pending team invitations (GET /orgs/{org}/teams/{slug}/invitations) and/or check per-login team membership state (GET /orgs/{org}/teams/{slug}/memberships/{login}); see the developer notes.

## Configuration properties

| Property | Description |
| --- | --- |
| githubExternalSystemConfigId | the WsBearerToken external system (token + endpoint) |
| githubOrgs | comma or newline separated list of organization logins this provisioner manages |
| githubEnterpriseSlug | optional enterprise slug; when set, entities are enriched with samlNameId from the enterprise SAML map so subjects can be matched by SSO identity |
| githubIgnoreLogins | logins to never manage (org owners, break-glass accounts, bots) |
| githubIgnoreTeamSlugs | team slugs whose membership is never managed |
