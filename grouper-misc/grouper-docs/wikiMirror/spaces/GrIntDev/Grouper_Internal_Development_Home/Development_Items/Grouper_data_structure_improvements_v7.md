---
title: "Grouper data structure improvements v7"
space: GrIntDev
pageId: 48792732
version: 52
lastUpdated: 2026-07-12T07:01:14.657Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792732/Grouper+data+structure+improvements+v7
---

## Grouper groups DDL v7

The goals are:

- Optimize for SELECT/join speed
- Compact storage

Here is a suggestion for how grouper data will be stored in the database (not all tables accounted for)

- Take out individual privileges, individual privs can be internally represented as groups in a private stem. Recommend not to use individual privs. How would GrouperAll work? Same way? Maybe make another type of group "group/role/entity/privilege".  Or it could be "internal" group, if we think we want to use internal groups for other things too...
- Each Grouper data structure (group, stem, etc), will have a "core" table, and a 1-to-1 "more" table (optional). 
  
  - The core table should follow the 80/20 rule and have what is generally queried to make things fast.
  - If the objects are going to the UI or being edited, the "more" table will be needed.
- Tables which are currently shared for multiple reasons (e.g. memberships and privileges, or attributes and permissions) will be split out for single purpose
  
  - An exception to this is the generic metadata table (description, etc), which is 1-to-1 with the "owner" object but does not need SQL foreign key
- If a table has multiple mutually exclusive foreign key cols, these can be split out into an intermediate table (e.g. privilege owners and attribute owners)
- Indexes will be scrutinized and only added as needed. Indexes can be smaller since fewer large primary key and foreign key cols
- "Enum" type columns will be generally one single CHAR type using capital letter (e.g. which type of privilege, or if enabled or not). If there are more than 62 options (alphanumeric cap/lower/digit), or if we want to make things more clear, then do two or three chars.
- The APIs for inserting/updating/deleting objects in Grouper will be batchable (can take N tasks and do them with database batches)
- Linkage tables (e.g. "attribute assign" tables do not need primary keys or other standard cols). A daemon can clean things up
- The Grouper primary key data type will be as follows. Note, this id is not exposed via web service or provisioning or exporting and is internal only. This will be used for foreign keys instead of uuid.
- Use the id_index stragey for primary key ints where a certain number are reserved per JVM. Some might be wasted but thats ok
- TODO take out metadata table and add "more" table to other tables

| **Grouper primary key** |
| --- |
| Database | Type | Notes |
| Oracle | number(38) |  |
| Postgres | bigint |  |
| Mysql | bigint |  |

- The Grouper UUID data type will be binary (risk of text compare issues with institutions which store uuids). The membership all view could have a composite key of two bigints or a 16byte col from a function based on the two 8 byte bigints.

| **Grouper UUID** |
| --- |
| Database | Type | Notes |
| Oracle | raw(16) |  |
| Postgres | bytea |  |
| Mysql | binary(16) |  |

- Grouper timestamps are timestamp type

| **Grouper timestamp** |
| --- |
| Database | Type | Notes |
| Oracle | timestamp |  |
| Postgres | timestamp |  |
| Mysql | timestamp |  |

- id_index

| grouper_v3_group_core table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_group_seq   Primary key of this table   Foreign key to other tables |
| uuid | Grouper UUID | UUID binary data   Functions will convert to hex (but could be case issues) |
| id_index | Bigint | Numeric ID that can be used outside of Grouper |
| parent_stem_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_stem_core.id_int |
| enabled | Char(1) | T \| F |
| extension | Varchar(255) |  |
| name | Varchar(1024) |  |
| alternate_name | Varchar(1024) |  |
| type_of_group | Char(1) |  |
| last_updated | Bigint | millis since 1970 for optimistic locking... increment by 1 if same as before |

Group indexes. Note, do we need a type_of_group index? Note, what type of index is group_enabled_idx (bitmap)?

| grouper_v3_group_core indexes |
| --- |
| **Name** | **Columns** | **Notes** |
| group_id_int_idx | id_int |  |
| group_uuid_idx | uuid |  |
| group_alternate_name_idx | alternate_name |  |
| group_name_idx | name |  |
| group_parent_and_ext_idx | parent_stem_id_int, extension |  |
| group_parent_and_dispext_idx | parent_stem_id_int, display_extension |  |
| group_enabled_idx | enabled |  |
| group_enabled_timestamp_idx | enabled_timestamp_millis |  |
| group_disabled_timestamp_idx | disabled_timestamp_millis |  |
| group_last_membership_idx | last_membership_change |  |
| group_last_imm_membership_idx | last_imm_membership_change |  |

Note: this could be in the core table with references to the dictionary table

| grouper_v3_group_more table |
| --- |
| **Column** | **Type** | **Notes** |
| group_id_int | Grouper primary key | Foreign key to grouper_v3_group_core, 1-to-1   Primary key of this table |
| last_mship_change_millis | Grouper timestamp bigint |  |
| last_imm_mship_change_millis | Grouper timestamp bigint |  |
| display_extension | Varchar(255) |  |
| display_name | Varchar(1024) |  |
| description | Varchar(4000) |  |
| enabled_timestamp_millis | Grouper timestamp bigint |  |
| disabled_timestamp_millis | Grouper timestamp bigint |  |
| metadata_id_int | Bigint | Same value as id_int in grouper_v2_metadata table   Foreign key |
| last_updated | Bigint |  |

We do not need this if descriptions are in the dictionary table

| grouper_v3_metadata table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_metadata_seq |
| description | Varchar(1024) |  |
| the_type | Char(1) | M (membership), etc   Just so we know when "foreign key like" went awry |
| last_updated | Bigint |  |

Convert all composites to JEXL scripted groups?

| grouper_v3_composite table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_metadata_seq |
| owner_group_id_int | Grouper primary key bigint | Value from grouper_v3_group.id_int |
| left_factor_group_id_int | Grouper primary key bigint | Value from grouper_v3_group.id_int |
| right_factor_group_id_int | Grouper primary key bigint | Value from grouper_v3_group.id_int |
| type | Char(1) | I (intersection), C (complement), U (union), X (exclusive or) |
| last_updated | Bigint |  |

| grouper_v3_object_field table |
| --- |
| **Column** | **Type** | **Notes** |
| internal_id | Grouper primary key | Primary key of this table   Foreign key to other tables |
| stem_internal_id | Bigint | Foreign key to stem internal_id |
| group_internal_id | Bigint | Foreign key to group internal_id |
| attribute_def_internal_id | Bigint | Foreign key to attribute def internal_id |
| field_internal_id | Bigint | Foreign key to field internal_id |
| last_updated | Timestamp |  |

| grouper_v3_membership table |
| --- |
| **Column** | **Type** | **Notes** |
| internal_id | Grouper primary key | Primary key of this table   Foreign key to other tables |
| uuid | Grouper UUID | UUID binary data   Functions will convert to hex (but could be case issues) |
| member_internal_id | Bigint | Foreign key to member internal_id |
| object_field_internal_id | Bigint | Foreign key to object_field internal_id |
| enabled_timestamp_millis | Timestamp |  |
| disabled_timestamp_millis | Timestamp |  |
| enabled | Char(1) | T \| F |
| metadata_id_int | Bigint | Same value as id_int in grouper_v3_metadata table |

Note, the "self group sets" will still be in the grouper_v3_group_mship_set table so you can query memberships with no outer join or union. Perhaps there is no unique key so there could be multiples that a full sync could deal with

| grouper_v3_group_mship_set table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key |  |
| owner_object_field_internal_id | Grouper primary key bigint | Foreign key to object_field internal_id |
| member_object_fie_internal_id | Grouper primary key bigint | Foreign key to object_field internal_id |

| grouper_v3_gr_attr_asgn table (composite key) |
| --- |
| **Column** | **Type** | **Notes** |
| group_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_group_core.id_int |
| attr_asgn_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_def_name_core.id_int |

| grouper_v3_attr_asgn_asgn table (composite key) |
| --- |
| **Column** | **Type** | **Notes** |
| attr_asgn_owner_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_asgn.id_int |
| attr_asgn_asgn_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_asgn.id_int |

| grouper_v3_attr_asgn table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_attr_asgn_seq   Primary key of this table   Foreign key to other tables |
| uuid | Grouper UUID | UUID binary data   Functions will convert to hex (but could be case issues) |
| attribute_def_name_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_def_name_core.id_int |
| enabled_timestamp_millis | Grouper timestamp bigint |  |
| disabled_timestamp_millis | Grouper timestamp bigint |  |
| enabled | Char(1) | T \| F |
| metadata_id_int | Bigint | Same value as id_int in grouper_v3_metadata table |
| the_type | Char(1) | G (group), H (group assign), S (stem), T (stem assign) etc   Just so we know when foreign key like went awry |
| last_updated | Bigint |  |

| grouper_v3_attr_val_st table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_attr_val_seq   Primary key of this table   Foreign key to other tables |
| attr_asgn_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_asgn.id_int |
| last_updated | Bigint |  |
| value_string | Varchar(4000) |  |
| metadata_id_int | Bigint | Same value as id_int in grouper_v3_metadata table |

| grouper_v3_attr_val_in table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_attr_val_seq   Primary key of this table   Foreign key to other tables |
| attr_asgn_id_int | Grouper primary key bigint | Foreign key to id_int in grouper_v3_attr_asgn.id_int |
| last_updated | Bigint |  |
| value_integer | Bigint |  |
| metadata_id_int | Bigint | Same value as id_int in grouper_v3_metadata table |

| grouper_v3_audit table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key | From grouper_v3_audit_seq   Primary key of this table   Foreign key to other tables |
| member_id_int | Member primary key bigint | Foreign key to id_int in grouper_v3_member.id_int |
| timestamp_millis | Bigint | Millis since 1970 when this happened |
| action | Bigint | Foreign key to action table which has category and action  First two chars are category, last one is action  MEI - membership insert   MED - membership delete   MEU - membership update    GRI - group insert   COI - configuration insert   GHE - GSH template execute   GPI - group privilege insert   etc |
| ip_address_id_int | bigint | foreign key to grouper_v3_audit_ipaddress |
| engine | char(1) | U - UI   W - WS   G - GSH   D - Daemon |
| host_id_int | bigint | Foreign key to grouper_v3_audit_host |

| grouper_v3_audit_host table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Bigint |  |
| hostname | varchar(50) | Hostname |

| grouper_v3_audit_ipaddress table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Bigint |  |
| ip_address_v6 | byte(16) | ipv6 |
| ip_address_v4 | byte(4) | ipv4 |

| grouper_v3_audit_group table |
| --- |
| **Column** | **Type** | **Notes** |
| audit_id_int | Audit primary key bigint | Foreign key to grouper_v3_audit.id_int |
| group_id_int | Bigint | Foreign key to grouper_v3_group.id_int |
| relation | char(1) | Describe the relationship to the audit entry, e.g.  O - owner of the group where membership changed   L - left factor of composite |

| grouper_v3_audit_member table |
| --- |
| **Column** | **Type** | **Notes** |
| audit_id_int | Audit primary key bigint | Foreign key to grouper_v3_audit.id_int |
| member_id_int | Bigint | Foreign key to grouper_v3_member.id_int |
| relation | char(1) | Describe the relationship to the audit entry, e.g.  M - member added to group where membership changed |

| grouper_v3_audit_dictionar_rel table |
| --- |
| **Column** | **Type** | **Notes** |
| audit_id_int | Audit primary key bigint | Foreign key to grouper_v3_audit.id_int |
| audit_dictionary_id | bigint | Foreign key to grouper_v3_audit_dictionary |
| relation | char(1) | For any given action there can be dictionary terms associated. e.g.   P - privilege type |

| grouper_v3_audit_dictionary table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key  positive int | From grouper_v3_audit_seq   Primary key of this table   Foreign key to other tables |
| term | varchar(4000) | Some term (unique) e.g.   R - READ privilege |

| grouper_v3_audit_dictionary_large table |
| --- |
| **Column** | **Type** | **Notes** |
| id_int | Grouper primary key  negative int | From grouper_v3_audit_seq   Primary key of this table   Foreign key to other tables |
| term | clob | For values larger than 4k |
| term_size_bytes | int |  |

## Notes

Oracle: use hextoraw and rawtohex to get UUIDs  
Postgres: decode('DEADBEEF', 'hex');  
Mysql: CONV(string, 16, 2)

## Batch updates

For example, create a bunch of stems, all at once

- Create a stem
  
  - (1) insert into grouper_stems
  - (2) insert into grouper_privileges
  - (3) insert into grouper_audit_log
- Create a stem
  
  - (11) insert into grouper_stems
  - (12) insert into grouper_privileges
  - (13) insert into grouper_audit_log
- Create a stem
  
  - (21) insert into grouper_stems
  - (22) insert into grouper_privileges
  - (23) insert into grouper_audit_log

Process these changes

In a transaction, do three batched SQLs

- (1) insert into grouper_stems, (11) insert into grouper_stems, (21) insert into grouper_stems
- (2) insert into grouper_privileges, (12) insert into grouper_privileges, (22) insert into grouper_privileges
- (3) insert into grouper_audit_log, (13) insert into grouper_audit_log, (23) insert into grouper_audit_log

If there is a problem, then do each unit of work in a transaction and report the error

Note, the loader would not need threads anymore, just do batches of work

Note, rules and hooks would need to be honored. If a create is vetoed still continue with others. Hooked would be able to add work

An interface could tell Grouper which object types are batchable

Get rid of hooks?

Change possibilities of rules?

## Database ideas (legacy wiki)

Lets look at the data design of Grouper and see what gives us the most benefits and is doable in a reasonable amount of time.

Goals are to improve performance, reduce the amount of storage required in the database, reduce the amount of memory required in JVMs

[https://www.percona.com/blog/2019/11/22/uuids-are-popular-but-bad-for-performance-lets-discuss/](https://www.percona.com/blog/2019/11/22/uuids-are-popular-but-bad-for-performance-lets-discuss/)

[https://tomharrisonjr.com/uuid-or-guid-as-primary-keys-be-careful-7b2aa3dcb439](https://tomharrisonjr.com/uuid-or-guid-as-primary-keys-be-careful-7b2aa3dcb439)

| Idea | Description | Notes |
| --- | --- | --- |
| Database function library | Can each database support a basic grouper function library    1. Decode uuid binary to string 2. Convert string uuid to binary 3. Decode basic codes to and from (e.g. field int to name) | If we can have a function library we can    convert views to be the exact same (perhaps)   and reduce database storage and foreign keys |
| Remove dashes in uuids | Old uuids have dashes, lets remove | Need to change all foreign keys and values. Find uuids in attribute values (rules) etc and convert.  This is so we can convert to and from binary. APIs and WS should be backwards compatible |
| UUIDs to binary | UUIDs to binary would change from 34 chars (40 varchar) to 16 bytes  Not sure if a char is always a byte, might be more. | Need to see what queries, joins, and ORM would look like.  Database functions to convert these and Java helper methods to convert in consistent way |
| UUIDs to base64? | If we cant change from UUID varchar to binary, maybe go to base64.  Goes from 34 chars (40 varchar) to 22 chars | Note this is not needed if we can go to binary |
| UUIDs more sequential? | Indexes on UUID might do better if somewhat incrementing | Maybe first 2? bytes could be number of days since 1970, and they would generally keep the index    in the same page for a given period of time |
| Add id index on all tables    (bigint 8 bytes) | Some tables have id index right now. All can. | Same algorithm as now where we reserve ids? Or database function? |
| Change primary key to big int id index | Instead of UUID |  |
| Change foreign keys to id index | Instead of UUID |  |
| Do all tables need a uuid? | See where uuid is needed on all tables. Maybe a bigint would work | Note we want to keep web services backwards compatible, keep uuid for all major    outward facingtables. E.g. do audits and PIT need uuids? |
| Do we need audit cols?   Other cols to remove? | Do we need created_on, created_by, last_updated and updated_by?   Maybe just last updated in a 4 byte number minutes since 1970 gives rough idea of age? | Should auditing tables be responsible for capturing that? Make sure audit purging doesnt delete this. |
| Refactor audit tables | What needs to be stored? Make tables for group or object data? Index tables? | Make queries efficient. Index data. |
| Can we remove indexes? | Can we remove any indexes? |  |
| Convert label keys to one char or int | e.g. If there is a small list of enums, consider just using one char or small int |  |
| Remove lookup tables? | e.g. do we need grouper_field or can that be handled by DB functions and tinyint for col?  e.g. grouper_field_convert_id_name(5) → readers   grouper_field_convert_name_id('readers') → 5   grouper_field_is_access_by_id(5) → true |  |
| Can we use more bitmap indexes? | Do attribute values need bitmaps? |  |
| Harmonize on time columns | Date/Timestamp type or millis/micros from 1970? Be consistent | DB functions can help convert. Does this matter or just for new cols? |
| Move fields to other tables? | Or have multiple ORM mappings. e.g. we dont need group description    and friendly name when calculating memberships |  |

Example: grouper_memberships

| Column | Current size (bytes) | New size (bytes) | Description |
| --- | --- | --- | --- |
| id | 34 | 16 | from varchar to binary |
| id_index | 0 | 8 | new primary key |
| member_id | 34 | 8 | from varchar uuid to bigint id index |
| owner_id | 34 | 8 | from varchar uuid to bigint id index |
| field_id | 34 | 1 | from varchar uuid to a smallint |
| owner_group_id   owner_stem_id   owner_attr_def_id | 34 | 8 | from varchar uuid to bigint id index  these are mutually exclusive so counted once |
| via_composite_id | 34 | 8 | from varchar uuid to bigint id index |
| enabled | 1 | 1 | same |
| enabled_timestamp | 8 | 8 | same |
| disabled_timestamp | 8 | 8 | same |
| mship_type | 12 | 1 | theres only a few types, just represent in one char |
| creator_id | 34 | 0 | not needed, this is in audits |
| create_time | 8 | 0 | not needed, this is in audits |
| hibernate_version_number | 8 | 8 | same |
| context_id | 34 | 16 | from varchar uuid to bigint id index |
| **TOTAL** | 385 bytes | 99 bytes | 74% of the storage eliminated   indexes smaller and faster |

Example: grouper_attribute_assign

| Column | Current size (bytes) | New size (bytes) | Description |
| --- | --- | --- | --- |
| id | 34 | 16 | from varchar to binary |
| id_index | 0 | 8 | new primary key |
| owner_group_id   owner_stem_id   owner_attr_def_id   etc | 34 | 8 | from varchar uuid to bigint id index  these are mutually exclusive so counted once |
| attribute_assign_action_id | 34 | 8 | from varchar uuid to bigint id index |
| attribute_def_name_id | 34 | 8 | from varchar uuid to bigint id index |
| notes | 10 | 10 | can be more but generally blank so    estimate 10. Maybe move to another table? |
| attribute_assign_delegatable | 15 | 1 | theres only a few types, just represent in one char |
| attribute_assign_type | 15 | 1 | theres only a few types, just represent in one char |
| disallowed | 1 | 1 | same |
| enabled | 1 | 1 | same |
| enabled_timestamp | 8 | 8 | same |
| disabled_timestamp | 8 | 8 | same |
| last_updated | 8 | 0 | not needed |
| mship_type | 12 | 1 | theres only a few types, just represent in one char |
| creator_id | 34 | 0 | not needed, this is in audits |
| create_time | 8 | 0 | not needed, this is in audits |
| hibernate_version_number | 8 | 8 | same |
| context_id | 34 | 16 | from varchar uuid to bigint id index |
| **TOTAL** | 298 bytes | 87 bytes | 71% of the storage eliminated   indexes smaller and faster |

Example: grouper_group_set

| Column | Current size (bytes) | New size (bytes) | Description |
| --- | --- | --- | --- |
| id | 34 | 16 | from varchar to binary |
| id_index | 0 | 8 | new primary key |
| owner_group_id   owner_stem_id   owner_attr_def_id   etc | 34 | 8 | from varchar uuid to bigint id index  these are mutually exclusive so counted once |
| owner_group_id_null   owner_stem_id_null   owner_attr_def_id_null   etc | 34 | 8 | from varchar uuid to bigint id index (not null)  these are mutually exclusive so counted once |
| member_group_id   member_stem_id   member_attr_def_id   etc | 34 | 8 | from varchar uuid to bigint id index  these are mutually exclusive so counted once |
| member_id | 34 | 8 | from varchar uuid to bigint id index |
| field_id | 34 | 1 | from varchar uuid to a smallint |
| member_field_id | 34 | 1 | from varchar uuid to a smallint |
| owner_id | 34 | 8 | from varchar uuid to bigint id index |
| mship_type | 16 | 1 | from varchar uuid to a smallint |
| depth | 4 | 4 | same |
| via_group_id | 34 | 8 | from varchar uuid to a smallint |
| parent_id | 34 | 8 | from varchar uuid to a smallint |
| creator_id | 34 | 0 | not needed, this is in audits |
| create_time | 8 | 0 | not needed, this is in audits |
| hibernate_version_number | 8 | 8 | same |
| context_id | 34 | 16 | from varchar uuid to bigint id index |
| **TOTAL** | 512 bytes | 95 bytes | 91% of the storage eliminated   indexes smaller and faster |
