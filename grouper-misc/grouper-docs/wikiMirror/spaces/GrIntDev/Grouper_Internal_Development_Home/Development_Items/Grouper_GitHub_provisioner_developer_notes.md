---
title: "Grouper GitHub provisioner developer notes"
space: GrIntDev
pageId: 171769876
version: 4
lastUpdated: 2026-08-30T08:26:23.822Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/171769876/Grouper+GitHub+provisioner+developer+notes
---

## Log in to UI

- (Log in) [https://github.com/orgs/$ORG$/people](https://github.com/orgs/$ORG$/people)
- (REST docs) [https://docs.github.com/en/rest](https://docs.github.com/en/rest)
- (GraphQL docs) [https://docs.github.com/en/graphql](https://docs.github.com/en/graphql)

## External system

Uses a WsBearerToken external system. Base URL `https://api.github.com` (GraphQL at `https://api.github.com/graphql`). The accessTokenPassword holds a bearer token (fine-grained PAT, classic PAT, or a GitHub App installation token). Every REST request needs three headers:

Accept: application/vnd.github+json Authorization: Bearer $TOKEN$ X-GitHub-Api-Version: 2022-11-28The token identity must be an **org owner** to manage invitations, org membership, and team membership, and an **enterprise owner** to read the SAML identity map (see Provisioner general, Identity resolution).

## Provisioner general

**Rate limiting:** 5000 requests/hour for a user token (higher for a GitHub App). Watch `X-RateLimit-Remaining` and `X-RateLimit-Reset`; back off when remaining is low. GraphQL has a separate point budget on the same headers.

**Paging:** `?per_page=100&page=1`, increment page until an empty array comes back (all 200). Many list endpoints also return a `Link` header with `rel="next"`; either approach works.

**Two kinds of teams.** `GET /orgs/$ORG$/teams` can return two `type` values that use different endpoint families:

| type | slug | path key | endpoint family |
| --- | --- | --- | --- |
| organization | plain, e.g. `identity-services` | slug | `/orgs/$ORG$/teams/$SLUG$/...` |
| enterprise | prefixed `ent:` | numeric id | `/enterprises/$ENTERPRISE$/teams/$ID$/...` |

The provisioner manages **organization teams only**; enterprise teams are treated as read-only / inherited.

**Identity resolution (the key design point).** GitHub list calls return `login`, never email. You invite a person by email, but the resulting login is not derivable from the invitation. When the enterprise enforces SAML SSO, GitHub keeps an authoritative SAML `nameId` to `login` map, readable by an enterprise owner via GraphQL (see Resolve login). The provisioner:

1. invites an unmapped subject by email (bootstrap only), then
2. on a later run, reads the SAML map to learn the login, then
3. drives all org and team membership by login.

The join key is the SAML `nameId`. **Institution TODO:** confirm what your IdP asserts to the GitHub enterprise (eppn such as `pennkey@example.com`, or an opaque id); that value must exist as a Grouper subject attribute and is the match column.

## Group

A group maps to a GitHub organization team. Crud: select only (team insert/update/delete is untested and out of scope).

Primary key: id

| mock_github_team |
| --- |
| Column | Type | Description |
| id | bigint | assigned by GitHub, not null after create, readonly |
| org | varchar(256) | org login this team lives in, not null |
| slug | varchar(256) | url-safe team key, not null (path key for org teams) |
| name | varchar(256) | display name, not null |
| team_type | varchar(32) | organization or enterprise |
| privacy | varchar(32) | closed or secret, nullable |
| description | varchar(1024) | nullable |

Mappable provisionable attributes: id, org, slug, name, description

## User

A user maps to a GitHub account, identified by `login`. Crud: select and delete (from the org). **Insert is asynchronous:** you cannot create an account; you POST an org invitation by email, and the account materializes only after the person accepts and SAML-links out of band.

Primary key: login

| mock_github_user |
| --- |
| Column | Type | Description |
| login | varchar(256) | GitHub username, the stable membership key |
| id | bigint | numeric account id, readonly |
| saml_name_id | varchar(256) | SAML NameID from the enterprise map; the Grouper-subject join key |
| email | varchar(256) | used ONLY for the initial invite; not returned by list calls |
| org_state | varchar(16) | pending or active (org membership state) |

Mappable provisioning attributes: login, email (invite only), samlNameId

## Membership

Team membership. Org membership is a derived prerequisite: it exists iff the subject is in at least one mapped team in that org. First team add triggers the org invite; removing the last team membership triggers org deprovision. Crud: select, insert, delete.

Primary key is config_id, team_id, user_login

| mock_github_membership |
| --- |
| Column | Type | Description |
| id | bigint | random integer |
| team_id | bigint | id from team |
| user_login | varchar(256) | login from user |
| role | varchar(32) | member or maintainer |
| state | varchar(16) | active or pending |

## Get teams

`/orgs/$ORG$/teams?per_page=100&page=1`

Start at page 1, increment until no results. Note each team's `type` (organization vs enterprise), `slug`, and `id`. Watch the org-id-vs-team-id gotcha: an org team also carries the org's own numeric id in `organization_id` - that is NOT the team id.

GET $SERVICE_URL$/orgs/$ORG$/teams?per_page=100&page=1 200 [ { "id": 17937664, "slug": "identity-services", "name": "Identity Services", "privacy": "closed", "type": "organization", "organization_id": 274091482 }, { "id": 17882528, "slug": "ent:usersall", "name": "UsersAll", "type": "enterprise" } ]

## Get team

`/orgs/$ORG$/teams/$SLUG$`

404 if not found.

GET $SERVICE_URL$/orgs/$ORG$/teams/identity-services 200 { "id": 17937664, "slug": "identity-services", "name": "Identity Services", "privacy": "closed", "description": null }

## Get team members

`/orgs/$ORG$/teams/$SLUG$/members?per_page=100&page=1`

Returns logins only (no email).

Returns only **active** members; a membership in `pending` state (see Pending team memberships, below) is not returned.

GET $SERVICE_URL$/orgs/$ORG$/teams/identity-services/members?per_page=100&page=1 200 [ { "login": "someuser", "id": 5720210, "type": "User" } ]

## Get org members

`/orgs/$ORG$/members?per_page=100&page=1`

Accounts already active in the org; does not include pending invitations.

GET $SERVICE_URL$/orgs/$ORG$/members?per_page=100&page=1 200 [ { "login": "someuser", "id": 5720210, "type": "User", "site_admin": false } ]

## Resolve login (SAML nameId to login, GraphQL)

`/graphql`

The identity bridge. Enterprise-owner token required. Returns the SAML `nameId` to `login` map for every enterprise-linked account. The org-level query is rejected when the enterprise enforces SAML ("The Organization's SAML identity provider is disabled when an Enterprise SAML identity provider is available"), so use the enterprise-slug query. Page with `externalIdentities(first:100, after:$CURSOR$)` via `pageInfo.endCursor` / `hasNextPage`.

POST $SERVICE_URL$/graphql Content-Type: application/json { "query": "query($slug:String!){ enterprise(slug:$slug){ ownerInfo { samlIdentityProvider { externalIdentities(first:100){ nodes { samlIdentity { nameId } user { login } } } } } } }", "variables": { "slug": "$ENTERPRISE$" } } 200 { "data": { "enterprise": { "ownerInfo": { "samlIdentityProvider": { "externalIdentities": { "nodes": [ { "samlIdentity": { "nameId": "C66135153" }, "user": { "login": "someuser" } } ] } } } } } }

## Get org membership state

`/orgs/$ORG$/memberships/$LOGIN$`

404 before the login is known/linked; 200 with `state` once it is. `direct_membership` and `enterprise_teams_providing_indirect_membership[]` tell the provisioner whether it owns the org membership (safe to remove) or it is inherited from an enterprise team.

GET $SERVICE_URL$/orgs/$ORG$/memberships/someuser 200 { "state": "active", "role": "member", "direct_membership": true, "enterprise_teams_providing_indirect_membership": [], "user": { "login": "someuser", "id": 310200265 } }

## Invite to org (async user insert)

`/orgs/$ORG$/invitations`

Invite by email. Returns 201 with a pending record: `login` is null, `id` is the invitation id. Optionally assign org teams in the same call with a numeric `team_ids` array (org team ids only). If GitHub already knows the email on an existing account, membership shows `pending` for that login without any acceptance.

POST $SERVICE_URL$/orgs/$ORG$/invitations Content-Type: application/json { "email": "someuser@example.com", "role": "direct_member" } // optional combined team assign: "team_ids": [ 17937664 ] 201 { "id": 77992097, "login": null, "email": "someuser@example.com", "role": "direct_member", "failed_at": null, "team_count": 0 }

## List / cancel invitations

`/orgs/$ORG$/invitations`

`GET` lists pending invites (the "already invited, do not re-invite" signal). Acceptance consumes the invite (the list drops to `[]`). `DELETE /orgs/$ORG$/invitations/$INVITATION_ID$` cancels; returns 204 and is idempotent (204 again if already gone).

GET $SERVICE_URL$/orgs/$ORG$/invitations?per_page=100&page=1 DELETE $SERVICE_URL$/orgs/$ORG$/invitations/77992097 -> 204, idempotent

## Add team membership

`/orgs/$ORG$/teams/$SLUG$/memberships/$LOGIN$`

Requires org membership to already exist. Immediate and idempotent: returns 200 `active` (never pending) when the account is already an org member; re-running returns the same 200. If not yet an org member it returns `pending`.

PUT $SERVICE_URL$/orgs/$ORG$/teams/identity-services/memberships/someuser Content-Type: application/json { "role": "member" } 200 { "state": "active", "role": "member", "url": ".../organizations/274091482/team/17937664/memberships/someuser" }

## Remove team membership

`/orgs/$ORG$/teams/$SLUG$/memberships/$LOGIN$`

Granular: removes only the team membership; the account stays an org member. Returns 204, idempotent (204 again if already removed).

DELETE $SERVICE_URL$/orgs/$ORG$/teams/identity-services/memberships/someuser -> 204, idempotent

## Remove org membership (deprovision)

`/orgs/$ORG$/memberships/$LOGIN$`

Full deprovision: also drops all of that account's team memberships in the org. Returns 204, idempotent. The provisioner does this only when no mapped Grouper group places the subject in the org anymore.

DELETE $SERVICE_URL$/orgs/$ORG$/memberships/someuser -> 204, idempotent

## Enterprise team endpoints (read-only)

Enterprise teams use a different family keyed on numeric id. Reads work with the same token. Writes, and whether the org-invite `team_ids` array accepts an enterprise team id, are UNTESTED; enterprise teams are inherited / read-only.

GET $SERVICE_URL$/enterprises/$ENTERPRISE$/teams?per_page=100 GET $SERVICE_URL$/enterprises/$ENTERPRISE$/teams/$ID$/memberships?per_page=100 PUT $SERVICE_URL$/enterprises/$ENTERPRISE$/teams/$ID$/memberships/$LOGIN$ // untested DELETE $SERVICE_URL$/enterprises/$ENTERPRISE$/teams/$ID$/memberships/$LOGIN$ // untested

## Provisioner implementation

The Grouper provisioner built from these notes is a generic, config-driven provisioner (like Datadog/Okta) - nothing institution-specific. It is **membership-driven**: it does not create GitHub accounts or teams. Teams must pre-exist (organization teams; enterprise teams are read-only). Source lives in `edu.internet2.middleware.grouper.app.github`; provisioning type is `membershipObjects`.

### Object model

| Grouper | GitHub | Key |
| --- | --- | --- |
| Group | Organization team | matched on team `slug` (per org) |
| Subject | Account | matched on `samlNameId` or `login` |
| Group member | Team member (+ derived org member) | login |

### How identity resolves (subject -> login)

GitHub calls key everything by `login`, which is not derivable from a Grouper subject. The provisioner resolves it by pulling the pool of accounts on each sync and letting the framework match:

- `retrieveAllEntities` = the enterprise SAML external-identities map (login + samlNameId, when `githubEnterpriseSlug` is set) UNION the active members of each managed org, deduped by login. The SAML map is enterprise-wide, so it resolves people not yet in any org.
- The deployer configures the entity match attribute: `samlNameId` (SSO enterprises) or `login`. That is the join column - no institution value is hard-coded.

### Write behavior

| Framework op | GitHub action | Notes |
| --- | --- | --- |
| `insertMembership` | PUT team membership by login | team-add also issues the pending org invite (org membership is the derived prerequisite) |
| `deleteMembership` | DELETE team membership | granular; account stays an org member |
| `deleteEntity` | DELETE org membership (each managed org) | full deprovision; drops the account's team memberships in the org |

**Derived org membership:** the provisioner never models the org as its own group. A subject is an org member because it is in at least one mapped team; first team-add invites, last team-removal (or an entity delete) removes from the org. The proven `direct_membership` flag tells the provisioner which org memberships it owns.

### Async boundary

A subject with a discoverable login (SAML-linked in the enterprise map, or already an org member) is fully provisioned, including being invited to the org via team-add. A brand-new external account with no discoverable login has a blank membership target id, so the framework defers that membership (DNE) and retries on a later sync once the account resolves. Bootstrapping such accounts by raw email invitation is a later phase (it is org-scoped and does not fit the framework's global entity model cleanly).

### Pending team memberships

The list-team-members call (`GET /orgs/$ORG$/teams/$SLUG$/members`) returns only members whose team membership state is `active`. A membership GitHub holds in `pending` state is not returned -- most commonly an organization member whose enterprise SSO identity is not linked, so a team-add PUT lands as `pending` rather than `active`. Because the retrieve does not see it, every full sync recomputes that membership as an insert and re-issues the idempotent (200) team-add PUT. The write is harmless and self-resolves once the membership goes active, but it appears as a persistent `Target inserts memberships (1)` and one `addTeamMembership` call per run for that member. The provisioner does not reconcile this. Two ways to suppress the churn if a deployment needs to: (1) also read pending team invitations, `GET /orgs/$ORG$/teams/$SLUG$/invitations` (bulk, one paged call per team; catches invited-but-not-accepted accounts, i.e. non-members), and/or (2) for managed logins the members list did not return, check per-login state, `GET /orgs/$ORG$/teams/$SLUG$/memberships/{login}`, which returns `state: active|pending`. Note (1) only covers pending invitations (non-members); an existing org member with a pending team membership is visible only via (2).

### Configuration

Capabilities are intentionally narrow: retrieve (all/one) groups and entities, retrieve memberships by group, insert/delete membership, delete entity. No group or entity create/update, no sync-back. The managed orgs are listed explicitly in config (a TargetDao cannot cleanly reach the Grouper-side group list, so orgs are not derived from the groups).

provisioner.myGithubProvisioner.class = edu.internet2.middleware.grouper.app.github.GithubProvisioner provisioner.myGithubProvisioner.githubExternalSystemConfigId = myGithubBearerToken provisioner.myGithubProvisioner.githubOrgs = myorg1, myorg2 provisioner.myGithubProvisioner.githubEnterpriseSlug = myenterprise # optional; enables SAML matching provisioner.myGithubProvisioner.provisioningType = membershipObjects # match entities on samlNameId (SSO) or login; match groups on slugThe WsBearerToken external system (`githubExternalSystemConfigId`) holds the token and the `https://api.github.com` endpoint. The token must be an org owner, and an enterprise owner if `githubEnterpriseSlug` is set (to read the SAML map).

## Untested / open items

- Team lifecycle: `POST /orgs/$ORG$/teams` (create), `PATCH`/`DELETE` team - not tested; the provisioner requires teams to pre-exist and syncs membership only.
- Enterprise team write path and enterprise `team_ids` on invite.
- Per-deployment: which IdP-asserted `nameId` value the enterprise sends (confirm before choosing samlNameId matching) - a config choice now, not a code blocker.
