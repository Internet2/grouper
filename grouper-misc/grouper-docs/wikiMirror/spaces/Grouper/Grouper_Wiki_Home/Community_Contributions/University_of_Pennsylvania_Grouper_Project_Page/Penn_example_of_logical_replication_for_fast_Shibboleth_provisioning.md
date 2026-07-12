---
title: "Penn example of logical replication for fast Shibboleth provisioning"
space: Grouper
pageId: 28544632
version: 8
lastUpdated: 2026-07-01T05:48:19.911Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544632/Penn+example+of+logical+replication+for+fast+Shibboleth+provisioning
---

## Tables and columns

The shib db will not have foreign keys on these tables

#### grouper_members

- id (primary key) varchar(40)
- subject_id varchar(255)
- subject_source varchar(255)
- subject_identifier0 varchar(255)
- internal_id int8
- hibernate_version_number int(8) (new number for inserts/updates)
- Indexes in weblogin:
  
  
  
  - id (primary key)
  - subject_id/subject_source (unique)
  - subject_identifier/subject_source (not unique)
  - internal_id (unique)

#### grouper_sql_cache_mship

- sql_cache_group_internal_id (primary key) int8
- member_internal_id (primary key) int8
- flattened_add_timestamp int8 (micros since 1970)
- Indexes in Weblogin:
  
  
  
  - sql_cache_group_internal_id/member_internal_id (primary key)
  - member_internal_id (not unique)
  - flattened_add_timestamp (not unique)

#### penn_shibboleth_prov_group_cache_ids

- group_cache_internal_id (primary key) int8
- group_name varchar(1024)
- last_changed timestamp
- Indexes
  
  
  
  - group_cache_internal_id (primary key)
  - last_changed (not unique)

#### penn_shibboleth_prov_entity_id_cache (not currently used but the hope is one day Grouper can control which groups go to which entity IDs)

- entity_id (primary key) varchar
- group_cache_internal_id (primary key) int8
- entitlement varchar
- last_changed timestamp
- Indexes
  
  
  
  - entity_id (primary key)
  - last_changed (not unique)

## GSH daemon to cache the group cache IDs in table instead of view

This is to reduce logical replication tables. It does mean it takes a minute for new provisionable groups to get to shibboleth but thats ok.

grouper-loader.properties

```
otherJob.penn_shibboleth_prov_group_cache_ids.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.penn_shibboleth_prov_group_cache_ids.grouperClientTableSyncConfigKey = penn_shibboleth_prov_group_cache_ids
otherJob.penn_shibboleth_prov_group_cache_ids.quartzCron = 41 * * * * ?
otherJob.penn_shibboleth_prov_group_cache_ids.syncType = fullSyncFull
```

grouper.client.properties

```
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.columns = group_cache_internal_id, group_name, group_id_index
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.databaseFrom = grouper
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.databaseTo = grouper
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.primaryKeyColumns = group_cache_internal_id
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.tableFrom = penn_shibboleth_prov_group_cache_ids_v
grouperClient.syncTable.penn_shibboleth_prov_group_cache_ids.tableTo = penn_shibboleth_prov_group_cache_ids

```

penn_shibboleth_prov_group_cache_ids

```
CREATE TABLE penngrouper.penn_shibboleth_prov_group_cache_ids (
	group_cache_internal_id int8 NOT NULL,
	group_name varchar(1024) NOT NULL,
	last_changed timestamp DEFAULT now() NOT NULL,
	group_id_index int8 NULL,
	CONSTRAINT penn_shibboleth_prov_group_cache_ids_pk PRIMARY KEY (group_cache_internal_id)
);

-- Permissions

GRANT SELECT ON TABLE penngrouper.penn_shibboleth_prov_group_cache_ids TO shib_grouperprod;
GRANT SELECT ON TABLE penngrouper.penn_shibboleth_prov_group_cache_ids TO shib_replication_prod;
```

penn_shibboleth_prov_group_cache_ids_v (has "provisionable" groups based on a READER on the group (this is old school)

```
CREATE OR REPLACE VIEW penngrouper.penn_shibboleth_prov_group_cache_ids_v
AS SELECT DISTINCT gscg.internal_id AS group_cache_internal_id,
    gscmv.group_name,
    gg.id_index AS group_id_index
   FROM grouper_sql_cache_mship_v gscmv,
    grouper_sql_cache_group gscg,
    grouper_fields gf,
    grouper_groups gg
  WHERE gscg.group_internal_id = gscmv.group_internal_id AND gf.name::text = 'members'::text AND gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id AND gscmv.list_name::text = 'readers'::text AND gscmv.subject_id::text = 'grouper/shibdev.net.isc.upenn.edu'::text AND gscmv.subject_source::text = 'servPrinc'::text;

```

## Logical replication

1. Make sure all DDL is in place in the subscriber
  
  
  ```
  CREATE TABLE grouper_members (
  	id varchar(40) NOT NULL, -- db id of this row
  	subject_id varchar(255) NOT NULL, -- subject id is the id from the subject source
  	subject_source varchar(255) NOT NULL, -- id of the source from subject.properties
  	hibernate_version_number int8 NULL, -- hibernate uses this to version rows
  	subject_identifier0 varchar(255) NULL, -- subject identifier of the subject
  	internal_id int8 NOT NULL,
  	CONSTRAINT grouper_members_pkey PRIMARY KEY (id),
  	CONSTRAINT members_internal_id_unique UNIQUE (internal_id)
  );
  CREATE INDEX member_subjectid_idx ON grouper_members USING btree (subject_id, subject_source);
  CREATE INDEX member_subjidentifier0_idx ON grouper_members USING btree (subject_identifier0, subject_source);
  
  CREATE INDEX member_subjectid_idx ON grouper_members USING btree (subject_id, subject_source);
  CREATE INDEX member_subjidentifier0_idx ON grouper_members USING btree (subject_identifier0, subject_source);
  
  COMMENT ON TABLE grouper_members IS 'keeps track of subjects used in grouper.  Records are never deleted from this table';
  
  -- Column comments
  
  COMMENT ON COLUMN grouper_members.id IS 'db id of this row';
  COMMENT ON COLUMN grouper_members.subject_id IS 'subject id is the id from the subject source';
  COMMENT ON COLUMN grouper_members.subject_source IS 'id of the source from subject.properties';
  COMMENT ON COLUMN grouper_members.hibernate_version_number IS 'hibernate uses this to version rows';
  COMMENT ON COLUMN grouper_members.subject_identifier0 IS 'subject identifier of the subject';
  COMMENT ON COLUMN grouper_members.internal_id IS 'Sequential id index integer that can we used outside of Grouper';
  
  CREATE TABLE grouper_sql_cache_mship (
  	sql_cache_group_internal_id int8 NOT NULL, -- internal id of the group/list that this member is in
  	member_internal_id int8 NOT NULL, -- internal id of the member in this group
  	flattened_add_timestamp int8 NOT NULL, -- when this member was last added to this group after not being a member before.  How long this member has been in this group
  	CONSTRAINT grouper_sql_cache_mship_pkey PRIMARY KEY (member_internal_id, sql_cache_group_internal_id)
  );
  CREATE INDEX grouper_sql_cache_mship3_idx ON grouper_sql_cache_mship USING btree (sql_cache_group_internal_id, flattened_add_timestamp);
  COMMENT ON TABLE grouper_sql_cache_mship IS 'Cached memberships based on group and list';
  
  -- Column comments
  
  COMMENT ON COLUMN grouper_sql_cache_mship.sql_cache_group_internal_id IS 'internal id of the group/list that this member is in';
  COMMENT ON COLUMN grouper_sql_cache_mship.member_internal_id IS 'internal id of the member in this group';
  COMMENT ON COLUMN grouper_sql_cache_mship.flattened_add_timestamp IS 'when this member was last added to this group after not being a member before.  How long this member has been in this group';
  
  CREATE TABLE penn_shibboleth_prov_entity_id_cache (
  	  timestamp DEFAULT now() NOT NULL,
  	CONSTRAINT penn_shibboleth_prov_entity_id_cache_pk PRIMARY KEY (entity_id, group_cache_internal_id)
  );
  
  CREATE TABLE penn_shibboleth_prov_group_cache_ids (
  	group_cache_internal_id int8 NOT NULL,
  	group_name varchar(1024) NOT NULL,
  	last_changed timestamp DEFAULT now() NOT NULL,
  	CONSTRAINT penn_shibboleth_prov_group_cache_ids_pk PRIMARY KEY (group_cache_internal_id)
  );
  ```
2. Setup new replication user
  
  
  ```
  [Groupertestdb.cluster-mydb-adminuser]
  create user shib_replication_test with password 'prod-XXXX';
  grant connect on database mydb to shib_replication_test;
  grant rds_replication to shib_replication_test;
  
  [Groupertestdb.penngrouper]
  GRANT SELECT ON TABLE penngrouper.penn_shibboleth_prov_group_cache_ids TO shib_replication_test;
  GRANT SELECT ON TABLE penngrouper.penn_shibboleth_prov_entity_id_cache TO shib_replication_test;
  GRANT SELECT ON TABLE penngrouper.grouper_sql_cache_mship TO shib_replication_test;
  GRANT SELECT ON TABLE penngrouper.grouper_members TO shib_replication_test;
  GRANT USAGE ON mydb TO shib_replication_test;
  GRANT USAGE ON SCHEMA penngrouper TO shib_replication_test;
  ```
3. Make sure there are no updates to Grouper
4. Turn off all Grouper containers
  
  
  
  1. Commit variables.tf in all four modules, set desired, min, max to 0.
  2. Rebuild each module in jenkins
5. Wait for them to shut down
6. Turn on logical replication from writer db
  
  
  ```
  set this in the postgresql.conf: 
  -- Database Config at the Cluster Level, only available via Writer, not Read Replica:
  rds.logical_replication=1
  max_logical_replication_workers=20
  Bounce Writer/Reader
   
  -- shib replication testing:
   
  [penngrouper@mydb=> \dRp+
                           Publication shib_entitlements
      Owner    | All tables | Inserts | Updates | Deletes | Truncates | Via root 
  -------------+------------+---------+---------+---------+-----------+----------
  penngrouper | f          | t       | t       | t       | t         | f
  Tables:
      "penngrouper.grouper_members" (id, subject_id, subject_source, hibernate_version_number, subject_identifier0, internal_id)
      "penngrouper.grouper_sql_cache_mship"
      "penngrouper.penn_shibboleth_prov_entity_id_cache"
      "penngrouper.penn_shibboleth_prov_group_cache_ids"
   
  ```
  
  do this after restart
  
  
  ```
  SHOW rds.logical_replication;
  ON
  SHOW wal_level;
  Logical
  
  -- Monitor Replication?
  SELECT srsubid, srrelid::regclass, srsubstate, srsublsn FROM pg_subscription_rel;
  
  
  ```
7. Bounce db (reader then writer)
8. Create the publication
  
    
  
  ```
  CREATE PUBLICATION shib_entitlements FOR TABLE grouper_members ( id,
      subject_id,
      subject_source,
      hibernate_version_number,
      subject_identifier0,
      internal_id
  );
  ALTER PUBLICATION shib_entitlements add table grouper_sql_cache_mship (sql_cache_group_internal_id,
  	member_internal_id,
  	flattened_add_timestamp);
  ALTER PUBLICATION shib_entitlements add table penn_shibboleth_prov_entity_id_cache (entity_id,
  	group_cache_internal_id,
  	entitlement,
  	last_changed);
  ALTER PUBLICATION shib_entitlements add table penn_shibboleth_prov_group_cache_ids(group_cache_internal_id,
  	group_name,
  	last_changed, group_id_index);
  ```
9. Create subscription  
    
  The subscription needs to specific PG connection info. I recommend using pg_service.conf and ~postgres/.pgpass to store connection details and credentials for database.  
    
  
  ```
   CREATE SUBSCRIPTION groupertestorigin_epe_sub
      CONNECTION 'service=shib_groupertestorigin'
      PUBLICATION shib_entitlements;
  ```
10. Sync data  
  d is 'data load' state
  
  
  ```
  incommunity=# SELECT srsubid, srrelid::regclass, srsubstate, srsublsn FROM pg_subscription_rel;
  -[ RECORD 1 ]------------------------------------------------
  srsubid    | 19692
  srrelid    | penngrouper.grouper_sql_cache_mship
  srsubstate | r
  srsublsn   | 0/68FB7EF0
  -[ RECORD 2 ]------------------------------------------------
  srsubid    | 19692
  srrelid    | penngrouper.grouper_members
  srsubstate | d
  srsublsn   | 
  -[ RECORD 3 ]------------------------------------------------
  srsubid    | 19692
  srrelid    | penngrouper.penn_shibboleth_prov_entity_id_cache
  srsubstate | r
  srsublsn   | 0/68FB4108
  -[ RECORD 4 ]------------------------------------------------
  srsubid    | 19692
  srrelid    | penngrouper.penn_shibboleth_prov_group_cache_ids
  srsubstate | r
  srsublsn   | 0/68FB4108
   
   
  ```
11. Create indexes in weblogin
12. Turn on all Grouper containers
  
  
  
  1. Revert variables.tf in all four modules, set desired, min, max to 0.
  2. Rebuild each module in jenkins
13. announce that maintenance is done
14. Change the resolver query to efficiently query these tables (TODO add this view)

### Check logical subscription

This shows the state of the subscription, but not the state of the subscribed objects.

```
incommunity=# select * FROM pg_stat_subscription;
─[ RECORD 1 ]─────────┬──────────────────────────────
subid                 │ 19692
subname               │ groupertestorigin_epe_sub
pid                   │ 1465
leader_pid            │ \N
relid                 │ \N
received_lsn          │ 0/6C6E7450
last_msg_send_time    │ 2025-05-01 09:40:17.453868-04
last_msg_receipt_time │ 2025-05-01 09:40:17.454355-04
latest_end_lsn        │ 0/6C6E7450
latest_end_time       │ 2025-05-01 09:40:17.453868-04

incommunity=# SELECT srsubid, srrelid::regclass, srsubstate, srsublsn FROM pg_subscription_rel;
 srsubid │                     srrelid                      │ srsubstate │  srsublsn  
─────────┼──────────────────────────────────────────────────┼────────────┼────────────
   19692 │ penngrouper.grouper_sql_cache_mship              │ r          │ 0/68FB7EF0
   19692 │ penngrouper.grouper_members                      │ r          │ 0/68FF5290
   19692 │ penngrouper.penn_shibboleth_prov_entity_id_cache │ r          │ 0/68FB4108
   19692 │ penngrouper.penn_shibboleth_prov_group_cache_ids │ r          │ 0/68FB4108
(4 rows)

```

srsubstate shows the state of each target table ‘d' is pre-completion of dataload and 'r' is replicating. srsublsn is the LSN position (transaction) on the origin DB each table is consistent with.

### Subscriptions object when columns don’t match.

The target and source tables need the same matching set of replicated columns. You can avoid replicating all columns in table (this will avoid writing them into the WAL, sending them over the network, decoding the values, and writing them to disk on the target, and lead to a narrower table with more tuples per page). In this case publication offered the full table, but the target table was a subset of the columns. So we updated the subscription on the origin service (the publication) to match. The subscription picked up the change without any other intervention.

```
alter publication shib_entitlements drop table grouper_members;
alter publication shib_entitlements add table grouper_members ( id,
    subject_id,
    subject_source,
    hibernate_version_number,
    subject_identifier0,
    internal_id
);
```
