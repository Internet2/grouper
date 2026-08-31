---
title: "Grouper database"
space: Grouper
pageId: 28549120
version: 5
lastUpdated: 2026-07-01T05:42:47.150Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549120/Grouper+database
---

Grouper needs a database for each environment (dev/test/prod). This database is shared by all the Grouper services in that environment (WS/UI/daemon/SCIM/GSH). When picking your database:

1. [Postgres](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555261/Grouper+database+-+Postgres) is recommended
2. Oracle can also be used
3. MySQL can be used but only for small deployments
  
  1. It has performance issues in large deployments
4. HSQL is not supported anymore

## Database tuning

### Analyzing tables to improve query performance

Whenever a lot of changes are made to the data in the Groups Registry database (including Grouper upgrades), you should analyze your database tables to improve query performance. Substitute `table_name` with each table you want analyzed; for Oracle, also substitute `schema` with the database schema for your Groups Registry.

| Database | Analyze syntax |
| --- | --- |
| MySQL | `ANALYZE TABLE table_name` — see the [MySQL ANALYZE TABLE documentation](http://dev.mysql.com/doc/refman/5.5/en/analyze-table.html) |
| PostgreSQL | `ANALYZE table_name` — see the [PostgreSQL ANALYZE documentation](http://www.postgresql.org/docs/8.4/static/sql-analyze.html) |
| Oracle | `exec dbms_stats.gather_table_stats('schema', 'table_name', cascade => TRUE);`   or `EXEC DBMS_STATS.gather_schema_stats('schema');` — see the [Oracle DBMS_STATS documentation](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_stats.htm) |

For Oracle, you can [analyze a subset of the rows](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_stats.htm) if you have a lot of data. This query generates the per-table ANALYZE statements:

```sql
select 'ANALYZE TABLE ' || table_name || ' estimate STATISTICS sample 100000 rows;' as script from user_Tables where table_name like 'GROUPER%'

e.g. ANALYZE TABLE GROUPER_ATTRIBUTES estimate STATISTICS sample 100000 rows;
or, e.g. ANALYZE TABLE GROUPER_ATTRIBUTES compute STATISTICS;
```

MySQL example:

```sql
ANALYZE TABLE grouper_groups;
ANALYZE TABLE grouper_stems;
ANALYZE TABLE grouper_memberships;
ANALYZE TABLE grouper_group_set;
....
```

### Improving queries using histogram statistics

Even with a full set of statistics on tables, columns, and indexes, this is sometimes not enough information for some queries. For example, in a database with 100,000 groups and 100,000 users, a query plan based on memberships may think that there will likely be at most one group per member. So the query plan may be built on the assumption that it can safely do a Nested Loop iteration through the few rows returned. But it is a plausible example that the GrouperAll subject is granted read access to a large number of these groups. This could have an effect on queries for non-wheel users when checking whether the logged in user can read a group. Instead of looping through a few rows, it could be looping through thousands.

With database histograms, values are put into a fixed number of bins. If the column data is heavily skewed toward one value, that value will occupy one or more bins by itself, and the query analysis can use that information to get a rough estimate on the cardinality of a filter on that column.

With Oracle, a first step toward improving these queries is to add a histogram for a single column, e.g.:

```sql
BEGIN
DBMS_STATS.GATHER_TABLE_STATS (
ownname => 'GROUPER'
, tabname => 'GROUPER_MEMBERSHIPS'
, method_opt => 'FOR COLUMNS MEMBER_ID'
);
END;
```

Histograms on more than one column require an extended version of this:

```sql
select dbms_stats.create_extended_stats(null, 'GROUPER_GROUP_SET', '(OWNER_GROUP_ID, FIELD_ID)') from dual;
-- (will return a generated rowid such as SYS_STU$V77C8_NRNA1MJMG#1SPOH$)

exec dbms_stats.gather_table_stats(null, 'GROUPER_GROUP_SET');

select * from user_tab_col_statistics where table_name = 'GROUPER_GROUP_SET';
```

MySQL starting from version 8 has histograms, probably similar to Oracle. See [Histogram statistics in MySQL](https://mysqlserverteam.com/histogram-statistics-in-mysql/).
