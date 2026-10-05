---
title: "Grouper AssetSonar provisioner"
space: Grouper
pageId: 269516802
version: 10
lastUpdated: 2026-10-04T21:06:38.421Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269516802/Grouper+AssetSonar+provisioner
---

See also: [AssetSonar external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269582340/Grouper+AssetSonar+external+system) and [developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/269516825/Grouper+AssetSonar+provisioner+developer+notes).

Provisions Grouper subjects as **members** of AssetSonar, the IT asset and device inventory product from EZO (the company behind EZOfficeInventory). Added in v7 (GRP-7420). Uses the AssetSonar REST API.

A member is both an account and an asset custodian: equipment is checked out to a member, so member records carry checkout history. The provisioner therefore never deletes a member; removal is a deactivation (`status=0`).

## Model

| Grouper | AssetSonar | What Grouper does |
| --- | --- | --- |
| target entity | member | create, update, deactivate (never delete) |
| target group | none | AssetSonar has no group that governs access |
| target membership | none | the access tier is a field on the member (`role_id`), computed from Grouper groups |

Teams are not provisioned. They describe asset ownership, not access, and the API has no team membership or single-team endpoint; see the developer notes.

## Matching

- Members are matched on **email**, the only attribute AssetSonar enforces uniqueness on.
- Translate `email` from whichever subject attribute holds the address at your institution; this differs by institution, so check that it resolves before turning on changes. A translation from an attribute your source does not have is null for everyone, and with `unprovisionableIfNull` every entity is dropped rather than sent blank (in the tenant studied, a wrong identifier dropped all 1,902).
- Set `caseSensitiveCompare = false` on `email` as cheap insurance. AssetSonar treats emails as unique regardless of case, so case-insensitive matching cannot merge two people, while a case mismatch never matches and every run retries the create and falls back on the 403. In the tenant studied all 4,414 emails were lowercase and both settings gave identical diffs.
- The numeric member id is cached in the sync tables after the first link, so later calls address the member directly.
- `employee_id` is **not** unique and not a match key: AssetSonar accepts a second member with the same `employee_id`.
- `employee_identification_number` is writable and persists; it is a good place for a stable institutional id.
- Do not configure an `external_id` attribute. AssetSonar nulls the member's email when it receives `user[external_id]`, and the provisioner refuses to send it.

## Access tiers

Each member has one tier, a tenant-specific `role_id` set in the provisioner config. The provisioner computes it from Grouper group membership, highest tier wins:

| Tier | Login | Assigned when |
| --- | --- | --- |
| Administrator | yes | subject is in `assetSonarAdministratorGroupName` |
| Agent | yes | subject is in `assetSonarAgentGroupName` |
| Staff User | no | otherwise (the default, asset custodian only) |

- The tier groups are read directly; they do not need to be provisionable.
- A tier whose group is left blank is **unmanaged**: Grouper never assigns it, never moves a member out of it, and never deactivates a member holding it. This protects hand-made Administrators. Configure `assetSonarAdministratorRoleId` anyway so they are recognized.
- A configured tier group that does not exist fails the sync rather than demoting everyone in that tier.

## Activation and removal

- Every subject in scope is sent `status=1`, so a deactivated member who comes back into scope is reactivated in place (same member id, no duplicate).
- A delete sets `status=0`. Members already inactive are left alone.
- The full sync reads inactive members as well as active ones. AssetSonar's default member list hides deactivated members; without them Grouper would treat them as gone and drop their sync rows (and member ids) after `removeSyncRowsAfterSecondsOutOfTarget`.
- If a create collides with an existing email (for example a deactivated member the select did not see), the provisioner looks the member up by email and reactivates it.

## Configuration properties

| Property | Required? | Description |
| --- | --- | --- |
| class | Yes | `edu.internet2.middleware.grouper.app.assetSonarProvisioning.AssetSonarProvisioner` |
| assetSonarExternalSystemConfigId | Yes | The WsBearerToken external system holding the tenant url and REST token (see the AssetSonar external system page). |
| assetSonarStaffUserRoleId | If `role_id` is managed | `role_id` of the Staff User tier, the default. Tenant-specific: read a member known to hold the tier. |
| assetSonarAgentRoleId | If the agent group is set | `role_id` of the Agent tier. Set it anyway so existing Agents are left alone. |
| assetSonarAdministratorRoleId | If the administrator group is set | `role_id` of the Administrator tier. Set it anyway so existing Administrators are never demoted or deactivated. |
| assetSonarAdministratorGroupName | No | Full name of the group whose members get the Administrator tier. Blank = Administrator is unmanaged. |
| assetSonarAgentGroupName | No | Full name of the group whose members get the Agent tier. Blank = Agent is unmanaged. |
| assetSonarSelectInactiveMembers | No | Default `true`. Also read deactivated members on full sync. Leave it on. |
| assetSonarRetryCount | No | Default `3`. Retries after the first attempt when a call gets 502, 503 or 504 (GRP-7434). The AssetSonar gateway sometimes loses the response of a write that committed; retrying is safe. 4xx responses are never retried. |
| assetSonarRetrySleepMillis | No | Default `1000`. Wait before the first retry, doubling each time (1s, 2s, 4s). |
| assetSonarExcludeEmails | No | Emails of members Grouper must never manage (see Excluding non-person accounts). |

Entity-only: groups and memberships are switched off, and subjects in provisionable groups become members. The provisioner sets its membership type itself, so do not set `provisioningType`. The key settings:

```properties
provisioner.myAssetSonarProvisioner.operateOnGrouperEntities = true
provisioner.myAssetSonarProvisioner.operateOnGrouperGroups = false
provisioner.myAssetSonarProvisioner.operateOnGrouperMemberships = false
provisioner.myAssetSonarProvisioner.selectAllEntities = true
provisioner.myAssetSonarProvisioner.hasTargetEntityLink = true
provisioner.myAssetSonarProvisioner.entityMatchingAttributeCount = 1
provisioner.myAssetSonarProvisioner.entityMatchingAttribute0name = email
provisioner.myAssetSonarProvisioner.entityAttributeValueCacheHas = true
provisioner.myAssetSonarProvisioner.entityAttributeValueCache0has = true
provisioner.myAssetSonarProvisioner.entityAttributeValueCache0source = target
provisioner.myAssetSonarProvisioner.entityAttributeValueCache0type = entityAttribute
provisioner.myAssetSonarProvisioner.entityAttributeValueCache0entityAttribute = id
# a delete is a deactivation (status=0); start with deletes off, see Rollout
provisioner.myAssetSonarProvisioner.customizeEntityCrud = true
provisioner.myAssetSonarProvisioner.deleteEntities = false
```

Recommended for bulk writes to this product: `errorHandlingShow = true` and `errorHandlingProvisionerDaemonShouldFailOnObjectError = false`, so a few failed members (e.g. a 502 that outlasts the retries) are reported without failing the run. Both are needed: the second is only read when `errorHandlingShow` is true.

### Entity attributes

| Attribute | Notes |
| --- | --- |
| id | The member id. No translation; cache it (see above). |
| email | Match key and login identity. Sent on every update. |
| first_name, last_name | Names. |
| employee_id | Free text (in an LDAP-fed tenant, the account name). Not unique. |
| employee_identification_number | Suggested home for an institutional id. |
| role_id | No translation; computed from the tier groups. **Configure it whenever inserts are on**: AssetSonar refuses a create without `role_id` (400 "Role Id is invalid"). |
| status | No translation; computed (1 in scope, 0 removed). |

Blank values are never sent: a gap in the source leaves the AssetSonar value as it is rather than blanking it.

### Excluding non-person accounts

A tenant fed by LDAP or by hand accumulates members that are not people: equipment logins (microscopes, scanners, imaging workstations), per-lab shared accounts and service accounts. List their emails in `assetSonarExcludeEmails` (GRP-7435), separated by commas, spaces or new lines, case-insensitive:

```properties
provisioner.myAssetSonarProvisioner.assetSonarExcludeEmails = microscope1@example.com, lab-account@example.com, biochem@example.com
```

- Excluded members are never read, so they are never updated or deactivated, even with `deleteEntitiesIfNotExistInGrouper`, and they are not captured for sync-back.
- A provisioned person whose email matches an excluded account is reported as an error, instead of taking over and renaming that account. Some lab accounts carry institutional addresses, so this can happen even with deletes off.
- `targetEntityAttribute.N.ignoreIfMatchesValue` on email (applied as of GRP-7436) also leaves these accounts alone, but skips a matching provisioned person silently. Prefer the exclude list so that collision is reported as an error.
- Review the list with a person: in the tenant studied, some accounts that looked like local directory accounts were real people whose account name was only capitalized differently.

## Sync-back

Supports sync-back into `grouper_prov_user` (v7, GRP-7422). Turn on `loadEntitiesToGenericGrouperTable`. Every member read is captured (active and inactive pages, lookups by email and id), and every write (create, reactivate, update, deactivate) is re-read at the end of the run, since AssetSonar writes do not return the member.

- Default captured attributes are the managed ones: `email`, `first_name`, `last_name`, `employee_id`, `employee_identification_number`, `role_id`, `status`. `role_id` and `status` are stored as strings, as the provisioner compares them.
- `nativeAttributesEntities` can capture other member fields (e.g. `role_name`, `creation_source`), but it replaces the defaults, so list those seven too, with `"type": "string"` on `role_id` and `status`.
- `fullSyncUsersFromSyncBack = true` serves the full sync from the cache instead of paging every active and inactive member (about 90 seconds for 4,400). Members missing from the cache are read individually. A member added in AssetSonar directly is not seen until it is in the cache, so run a normal full sync now and then.

## Rollout

Deactivation at scale is the risk. In the tenant studied, an LDAP feed had added members for four years and removed none; a first full sync with deletes on would have deactivated about 1,700 members at once.

Timings from that 4,414-member tenant:

| Run | Result |
| --- | --- |
| Full read of the target | 178 page calls, about 79 seconds |
| Read-only full sync | about 87 seconds |
| First full sync with changes on | 366 inserts, 1,513 updates, 5m23s |
| Next full sync | 0 inserts, 0 updates, 0 deletes, 83 seconds (converged, no churn) |

To review the diff, set `logCompareCalculations = true` (under `showAdvanced`). The output goes to the provisioning log (`/opt/grouper/logs/provisioning.log` in the container), not the daemon log or the daemon job message. Every line is prefixed with the provisioner instance id, so grep by that to isolate one run.

1. Deploy with `makeChangesToEntities = false`, run a full sync, and export the diff.
2. Review the would-be deactivations with the service owner.
3. Fix target emails first: a member whose email is wrong cannot be matched and produces a duplicate instead of an update.
4. Enable inserts and updates.
5. Enable deletes (deactivation) last, once the population has been stable across several runs.
6. Keep an entity failsafe on, so a bad run that would deactivate many members stops for approval: `showFailsafe = true`, `failsafeUse = true`, `failsafeMaxOverallPercentEntitiesRemove = 5` (v7.7.0+, GRP-7437). The group and membership failsafes see nothing in this provisioner. See [Grouper provisioning failsafe](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555489/Grouper+provisioning+failsafe).
