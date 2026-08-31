---
title: "Grouper dependency SQL caching"
space: Grouper
pageId: 28547441
version: 4
lastUpdated: 2026-07-01T05:46:46.567Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547441/Grouper+dependency+SQL+caching
---

In order to know where objects are used in Grouper for various reasons, those relationships can be cached in the database

## Uses for dependency caching

1. For scripted ABAC groups, when incremental changes occur, the daemon needs to know which scripts use which groups (script depends on factor group)
2. For scripted ABAC groups, when incremental changes occur, the daemon needs to know which scripts use which attributes (data fields)
3. For renames of groups, the uses of groups in rules (attributes), configuration, etc need to be known

## DDL

Note, there is a foreign key from dependency to depend_type, but otherwise any internal ids can be used. The API will be efficient and gracefully handle unique indices

| grouper_sql_cache_depend_type |
| --- |
| Column | Type | Description |
| internal_id | bigint | id of this row |
| name | varchar | name of this dependency (alphanumeric camel case) |
| description | varchar | describe the dependency and columns |
| owner_type | varchar | Single character has the type of owner of this dependency   G means group   D means data field   etc |
| dependent_type | varchar | G means group   D means data field   etc |
| created_on | timestamp |  |

| grouper_sql_cache_dependency |
| --- |
| Column | Type | Description |
| internal_id | bigint | id of this row |
| dependency_owner_internal_id | bigint | id of the owner of the dependency (could be a group or config or user or attribute) |
| depend_type_internal_id | varchar | name of this dependency (alphanumeric camel case) |
| dependent_internal_id | varchar | id of the object that is dependent on the owner |
| created_on | timestamp |  |

## Management

When something starts using another thing that is managed in this table, it will insert into this table. When something stops using another thing it will delete from this table.

There will be a nightly daemon to manage data in the table if it is corrupt.

## Discussion

Renames?

- Store english names or identifiers?
  
  - Current strategy is names
- If a script is missing a part, keep members static
- Add memberships for recreated groups too? to be consistent?
- Renames (or moves) should have a confirm screen with list of dependencies
- Button in group actions to see dependencies (redact things not allowed to see)
