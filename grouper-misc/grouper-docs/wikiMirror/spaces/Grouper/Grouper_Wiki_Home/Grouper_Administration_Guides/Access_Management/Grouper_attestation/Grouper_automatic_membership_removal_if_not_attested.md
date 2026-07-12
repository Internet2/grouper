---
title: "Grouper automatic membership removal if not attested"
space: Grouper
pageId: 28549775
version: 13
lastUpdated: 2026-07-12T05:23:16.263Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549775/Grouper+automatic+membership+removal+if+not+attested
---

> This page describes a **workaround**: Grouper does not natively remove memberships when a group is no longer attested. The strategy below uses attestation together with a SQL loader job so that a group's memberships are populated only while the group is attested. The building blocks (attestation and the SQL loader) are available in all currently supported releases.

> **Required privileges:** you need `ADMIN` on the groups involved to configure attestation and the loader job, and a configured loader database connection (the loader reads from the Grouper database in this example).

The idea: a source group holds the real memberships, attestation is required on that group, and a SQL loader copies the source group into an "overall" group only while attestation is still valid. When attestation lapses, the loader empties the overall group, so its memberships are available only while the group is attested.

## How it works

## Set up the source group

The source group that holds the real memberships is group B.

## Set up attestation on the group

Turn on attestation for the group that must stay attested.

## Loader query

This query selects the group only when it is still attested — that is, when its `attestationCalculatedDaysLeft` value is not `0`:

```sql
SELECT gg.name AS subject_identifier, 'g:gsa' AS subject_source_id
FROM grouper_groups gg, grouper_aval_asn_asn_group_v gaaagv
WHERE gg.name = 'test:attestation:autoAttestation:autoAttestationMembers' AND gg.id = gaaagv.group_id
AND gaaagv.attribute_def_name_name1 = 'etc:attribute:attestation:attestation'
AND gaaagv.attribute_def_name_name2 = 'etc:attribute:attestation:attestationCalculatedDaysLeft' AND gaaagv.value_string != '0'
```

## Configure the loader

Set up the loader on the overall group, using the query above. As configured here the loader runs hourly. If a group is shut off and then re-attested, the change takes effect at the top of the next hour, or when someone runs the loader again. If you need this to be more timely, let the Grouper team know and it can be made real-time.

| **Loader setting** | **Value** |
| --- | --- |
| State | `ENABLED` |
| Source type | `SQL` (pull the members from a SQL database; can be SQL or LDAP) |
| Loader type | `SQL_SIMPLE` (the SQL query loads the members of this group) |
| Database name | `grouper` (the server ID configured in `grouper-loader.properties`; `grouper` means use the Grouper registry database connection) |
| SQL query | The query shown above. |
| Schedule type | `CRON` (runs on a schedule; can be CRON or START_TO_START_INTERVAL) |
| Schedule | `0 0 * * * ?` (every hour) |
| Priority | `5` (the default, middle priority) |

## Result

Before attestation, the overall group has no members:

Attest the group and run the loader, and the members appear:
