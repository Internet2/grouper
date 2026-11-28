---
title: "Grouper AssetSonar provisioner developer notes"
space: GrIntDev
pageId: 269516825
version: 8
lastUpdated: 2026-10-04T21:00:19.847Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/269516825/Grouper+AssetSonar+provisioner+developer+notes
---

See also: [AssetSonar provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269516802/Grouper+AssetSonar+provisioner) and [AssetSonar external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269582340/Grouper+AssetSonar+external+system).

Behavior of the AssetSonar APIs as established by direct experiment against a live tenant (September and October 2026). EZO documents almost none of it. Each item below explains a piece of code that would otherwise look like pointless defensive programming.

## Code

Package `edu.internet2.middleware.grouper.app.assetSonarProvisioning`. `AssetSonarApiCommands` (HTTP and the quirk guards), `AssetSonarTargetDao` (members only), `AssetSonarProvisioningTranslator` (computes `role_id` and `status`), `AssetSonarProvisioningTargetNativeSync` (sync-back), `AssetSonarMockServiceHandler` (mock name `assetSonar`), tests in `AllAssetSonarProvisionerTests`.

Committed in layers so the base backports cleanly: GRP-7420 the provisioner (no sync-back, no progress labels), GRP-7421 the target progress label (needs GRP-7154), GRP-7422 sync-back (v7 and later), GRP-7423 MCP lookups, GRP-7433 replaced the custom AssetSonar external system with a WsBearerToken one (`httpHeader = token`, `prependBearerTokenPrefix = false`) and moved the role ids to the provisioner; the old one stored its token as `apiToken`, a key name Grouper did not treat as a secret. GRP-7434 retries 502/503/504. GRP-7437 adds entity failsafes (`failsafeMaxOverallPercentEntitiesRemove`, `failsafeMinOverallNumberOfEntities`), the only failsafes that see anything in this entity-only provisioner. GRP-7435 adds `assetSonarExcludeEmails`: excluded members are dropped in `retrieveAllEntities` and `retrieveEntity`, skipped in sync-back capture, and an insert of an excluded email throws (the generic `ignoreIfMatchesValue`, applied as of GRP-7436, would skip such a member silently instead). `readAllPages` sets the label to `retrieving active|inactive members from target: page N of M, X so far` (the API reports `total_pages`); write progress comes from the framework.

## Two APIs, two populations

| Endpoint | Returns |
| --- | --- |
| SCIM `/scim/v2/Users` | login-capable members only (any status), plus records SCIM created |
| REST `/members.api` | active members only (any tier) |
| REST `/members.api?filter=status&filter_val=inactive` | inactive members (any tier) |

In the tenant studied, 4,389 of 4,414 members were the non-login Staff User tier and SCIM returned about 28, so the provisioner is built on REST. The numeric member id is the same in both APIs. SCIM `/Schemas` and `/ServiceProviderConfig` return empty, and SCIM `id` is a number in lists but a string in single-resource responses.

## REST API

Auth is a plain `token` header. Writes are form-encoded `user[...]` parameters, not JSON. Two url shapes, and the member url accepts POST as an update, so do not confuse them:

```text
/members.api        collection: GET lists 25/page, POST creates
/members/<id>.api   member: GET reads one, PUT updates one
```

| Operation | Call | Notes |
| --- | --- | --- |
| list active | `GET /members.api?page=N` | `{"members":[...],"total_pages":N}`, about 317 ms/page |
| list inactive | `GET /members.api?filter=status&filter_val=inactive&page=N` | the word `inactive`, not 0 |
| read one | `GET /members/<id>.api` | bare member object; works for inactive members |
| lookup by email | `GET /members.api?filter=email&filter_val=<email>` | finds inactive members too |
| create | `POST /members.api` | `{"message":"Member created.","member_id":N}` |
| create, email of an active member | same | HTTP 403 `{"errors":{"base":["The email is already taken by a Member"]}}`, no id |
| create, email of an inactive member | same | **HTTP 200** `{"errors":{"base":["The email is already taken by an Inactive Member. To reactivate, ..."]},"status":403}`, no id. Email matching ignores case. |
| create without `user[role_id]` | same | HTTP 400 `{"errors":"Role Id is invalid"}`, checked before the email |
| update | `PUT /members/<id>.api` | partial updates are safe |
| deactivate / reactivate | `PUT` with `user[status]=0` / `1` |  |
| change tier | `PUT` with `user[role_id]=<id>` | reversible |

A full pull of a 4,400-member tenant takes about 90 seconds. There is no bulk write endpoint.

### Filters are silently ignored

Only `filter=email` and `filter=status&filter_val=inactive` work. Everything else returns a normal-looking unfiltered page 1: `status=0`, `status=1`, `state=inactive`, `active=false`, `employee_id`, `role_id`, `creation_source`, `last_name`, `full_name`, `external_id`, `search`, `q`, `query`, `filters[email][value][]`. `/members/search.api` is 404 and `/members/filter.api` is 406 (UI only).

So the code verifies content, never a 200: an email lookup fails if any returned member has another email, and the inactive read fails if any returned member is active.

### user[external_id] nulls the email

A PUT whose only parameter was `user[external_id]` left `external_id` null and wiped `email`, the login identity and match key. An update loop including it would wipe every member's email. `external_id` is written only by AssetSonar's own LDAP integration (an AD distinguished name). The payload rules that follow from this:

1. Never send `user[external_id]` (the code throws if asked to).
2. Always send `user[email]` on updates.
3. Never send a blank value; skip the attribute, so a source data gap is a no-op.

### The 403 fallback

The default list hides deactivated members, so a create can collide with a member the select did not see, and the refusal carries no id. For an inactive member that refusal is an HTTP 200 with `"status":403` in the body, so the client treats any 2xx whose JSON body has `errors` as the status in the body (GRP-7434; before that the fallback never ran for inactive members). The DAO then looks the member up by email and reactivates it by id with `user[status]=1`. Selecting inactive members makes this a safety net; both exist because the inactive filter is undocumented.

### 502 retries

Under sustained writing the gateway answers about one call in 200 with `502` and `<h1>Incomplete response received from application</h1>`, but the write has committed (the next sync found nothing to do). Every call is retried on 502, 503 or 504, never 4xx, with backoff (`assetSonarRetryCount` 3, `assetSonarRetrySleepMillis` 1000, doubling). A retried update is idempotent; a retried create of a committed member gets the duplicate-email 403 and goes through the lookup-and-reactivate fallback. Retries log at WARN; only exhausting them is an error. The mock reproduces this: a row in `mock_asset_sonar_fault` makes the next N writes for an email commit and then answer 502.

### Sync-table retention

`removeSyncRowsAfterSecondsOutOfTarget` (7 days) drops sync rows for objects no longer provisionable and not in the target. Selecting only active members would make deactivated members look absent and lose their member ids, which is why `assetSonarSelectInactiveMembers` defaults to true.

## Matching

Email is the only attribute with enforced uniqueness (the 403 above). `employee_id` holds the account name in an LDAP-fed tenant but is not unique: creating a member with an existing `employee_id` and a new email succeeds and forks the record.

## Teams and other groupings

Probed in October 2026 and not provisioned:

- `GET /teams.api` lists teams (`id`, `name`, `path`, `description`, `sequence_num`), paged, with no members.
- `/teams/<id>.api` and `/teams/<id>/members.api` return 404.
- The only link is `team_id` on the member, a scalar string, so a member is on at most one team and membership can only be read by scanning every member. A subject in two provisioned groups would flip between teams.
- `path` suggests nesting, which is uncharacterized.

Teams govern asset ownership, not access, and the tenant studied had no members on a team. If needed, model a team as another computed member attribute like `role_id`, after probing team create and assignment. Nothing else is group-like: `/custom_roles.api` is empty, `/groups.api` is UI only (406), and `department`, `location_id` and `manager_id` are bookkeeping.

## Provenance fields

- `creation_source` is `ldap`, `manual` or `scim`; an API-created member is `manual`.
- `created_by_id` is the member id of the token owner that created the record, the only reliable marker of API-created members.
- `created_by_scim` means SCIM has touched the record, not that SCIM created it.
- `auto_sync_with_ldap` is true even on manual and API-created members; not an ownership signal.
- `deactivated_at` and `inactive_by_id` are set by SCIM deactivation but not REST. `status` is authoritative.

## Sync-back

- **Read capture** is from the raw member JSON at two seams in `AssetSonarApiCommands`: `parseMembersPage` (every active and inactive page, and the `filter=email` lookup) and `retrieveMemberById`. `target_user_id` is the numeric member id.
- **Writes** return no member (create returns only `member_id`, update only a message), so the DAO calls `recordTargetNativeUserWrite(id, null)` after create, the 403 reactivate, update and deactivate. The end-of-run drain re-reads those through `retrieveEntity`, searching on the matching attribute (email), which captures them at the read seam.
- **Deactivation is a write, not a delete.** The member still exists with `status=0`, so it is never marked for delete and stays in the mirror, just like inactive members captured from the inactive pages.
- **Default capture list** is exactly the managed attributes, all `type: string`. `role_id` and `status` are numbers in the JSON but the provisioner compares strings; captured as numbers, every cache-served member would look changed every run.
- **fullSyncUsersFromSyncBack**: the DAO sets `canFullSyncEntitiesFromSyncBack`. On the per-axis path the adapter skips `retrieveAllEntities` when users come from the cache, and seeds them from `grouper_prov_user`; members missing from the cache are read individually by email. Cache-first: an empty cache falls back to the normal page walk. A member created in AssetSonar outside Grouper is not in the cache, so it is not seen (and not deactivated) until a normal full sync; `testSyncBackCaptureAndFullSyncFromCache` asserts exactly that.

## Mock service

`AssetSonarMockServiceHandler` reproduces the awkward behaviors so the tests mean something: the `token` header, form-encoded bodies (JSON is rejected), unknown filters ignored, only the two real filters honored, the default list excluding `status=0`, 403 with no id on a duplicate email (any case, any status), `user[external_id]` nulling the email, and POST to the member url acting as an update.

## Product issues reported or worth reporting to EZO

- `user[external_id]` nulls the email and sets nothing.
- Unrecognized query parameters are ignored rather than rejected.
- SCIM `/Schemas` and `/ServiceProviderConfig` return empty; SCIM `id` type is inconsistent.
- `deactivated_at` and `inactive_by_id` are not set by REST deactivation.
- No bulk endpoint.
- Some errors come back as HTTP 200 with the real status in the body (e.g. a create for an email held by an inactive member).
- The gateway answers 502 for some writes that committed (about 0.5% under sustained writing).
