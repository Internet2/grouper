---
title: "Grouper ABAC membership cache tables"
space: Grouper
pageId: 28549217
version: 7
lastUpdated: 2026-07-01T05:42:30.425Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549217/Grouper+ABAC+membership+cache+tables
---

In order to make [ABAC](https://en.wikipedia.org/wiki/Attribute-based_access_control) have high performance, we need to be able to translate scripted group jexl's into SQL.

And in order for a SQL to be able to check privileges/memberships/pointInTime efficiently, we need a SQL cache of flattened data.

Certain groups will be "SQL cachable", which means they are needed for high performance queries. Note, the group (or stem or attribute def) is cachable for one or many fields. Fields are either "members" or a privilege.

Examples:

1. Groups that are used as factors in JEXL scripts
2. Overall groups in JEXL scripts
3. Groups used heavily by the API (not necessarily WS)
4. Groups marked as cachable by deployer
5. Grouper internal groups that are known to be queried a lot

Notes:

1. Make sure all the internal id fields are added to main tables (e.g. fields)
2. Full sync will process one at a time

### grouper_sql_cache_group table

| Column | Description | Notes |
| --- | --- | --- |
| internal_id | bigint internal id for this row | primary key |
| group_internal_id | bigint internal id for group, stem, or attribute def | non null |
| field_internal_id | bigint internal id for field |
| membership_size | bigint number of members | required. Included groups as members |
| membership_pit_size | bigint number of pit records for this group | not used |
| enabled_timestamp | timestamp when this can be used | required. Code that uses group cache should not rely on SQL cache if it is "too new" unless it inserted it. |
| disabled_timestamp | timestamp when this should stop being used | Once there is a timestamp here, code should stop using the SQL cache and use normal membership queries |
| created_timestamp | timestamp when this row was inserted | required |
| last_membership_sync | timestamp of last full sync |  |

### grouper_sql_cache_mship table

| Column | Description | Notes |
| --- | --- | --- |
| sql_cache_group_internal_id | bigint internal id for grouper_sql_cache_group | foreign key to sql cache group. |
| member_internal_id | bigint internal id for member |  |
| flattened_add_timestamp | bigint of the most previous flattened add of user to group with this field | required |

### grouper_sql_cache_mship_hst table

| Column | Description | Notes |
| --- | --- | --- |
| sql_cache_group_internal_id | bigint internal id for grouper_sql_cache_group | foreign key to sql cache group. |
| member_internal_id | bigint internal id for member |  |
| start_time | timestamp this flattened add to group | required |
| end_time | timestamp this flattened remove group | required (note current members not needed, right?) |

### Attribute for history cacheable

Note that the non-history cacheable table is populated for every membership and privilege. However, the history table is only populated where it's needed (e.g. based on ABAC) or if manually set by attribute. See grouper_sql_cache_dependency based on dependency category mshipHistory. History is kept for 2 years.

The following attributes can be assigned to a group to enable history caching (e.g. assign sqlCacheableHistoryGroupMembers to enable history caching for the members of the group, assign sqlCacheableHistoryGroupAdmins to enable history caching for the admins of the group):

sqlCacheableHistoryGroupMembers  
sqlCacheableHistoryGroupAttrReaders  
sqlCacheableHistoryGroupAttrUpdaters  
sqlCacheableHistoryGroupAdmins  
sqlCacheableHistoryGroupOptins  
sqlCacheableHistoryGroupOptouts  
sqlCacheableHistoryGroupReaders  
sqlCacheableHistoryGroupUpdaters  
sqlCacheableHistoryGroupViewers

The following attributes can be assigned to an attribute definition to enable history caching:

sqlCacheableHistoryAttributeDefAdmins  
sqlCacheableHistoryAttributeDefAttrReaders  
sqlCacheableHistoryAttributeDefAttrUpdaters  
sqlCacheableHistoryAttributeDefOptins  
sqlCacheableHistoryAttributeDefOptouts  
sqlCacheableHistoryAttributeDefReaders  
sqlCacheableHistoryAttributeDefUpdaters  
sqlCacheableHistoryAttributeDefViewers

The following attributes can be assigned to a stem to enable history caching:

sqlCacheableHistoryStemAdmins  
sqlCacheableHistoryStemAttrReaders  
sqlCacheableHistoryStemAttrUpdaters  
sqlCacheableHistoryStemCreators  
sqlCacheableHistoryStemViewers

### API changes (NOT IMPLEMENTED)

1. In membership finder, if the query parameter does not specify NO_CACHE, and the group/field is cachable, use the cache tables instead of the normal Grouper tables
2. Make sure WS uses the Grouper API that takes advantage of the SQL cache (e.g. get members, get groups, find memberships, privileges, etc)
3. Make sure important operations in most circumstance like loader/provisioner use NO_CACHE when querying memberships
