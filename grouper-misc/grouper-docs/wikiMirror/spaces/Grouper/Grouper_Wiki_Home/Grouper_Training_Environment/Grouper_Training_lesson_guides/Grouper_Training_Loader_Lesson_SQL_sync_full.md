---
title: "Grouper Training - Loader - Lesson: SQL sync full"
space: Grouper
pageId: 28544292
version: 8
lastUpdated: 2026-07-12T15:26:16.652Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544292/Grouper+Training+-+Loader+-+Lesson+SQL+sync+full
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

- Connect to the Grouper training database with DBeaver (free SQL browser), or any Postgres compatible SQL editor
  
  - Grouper
    
    - Host: localhost
    - Port: 8432
    - Database: grouper
    - User: grouper
    - Password: pass
  - HR
    
    - Host: localhost
    - Port: 8432
    - Database: hr
    - User: hr_owner
    - Password: pass
- In the grouper database, create view of group names and net ID's for users in department groups starting with 201
  
  
  ```
  create or replace view hr_depts_201_v as
  select distinct group_name, gm.subject_identifier0 as net_id
  from grouper_memberships_lw_v gmlv, grouper_members gm
  where gmlv.group_name like 'basis:hr:employee:dept:201%'
  and gmlv.list_name = 'members' and gmlv.subject_source = 'eduLDAP'
  and gmlv.member_id = gm.id and gm.subject_identifier0 is not null;
  ```
- In the HR database, create a table
  
  
  ```
  CREATE TABLE hr_depts_201 (
  	group_name varchar NOT NULL,
  	net_id varchar NOT NULL,
  	CONSTRAINT hr_depts_201_pk PRIMARY KEY (group_name,net_id)
  );
  ```
- Log in to your Grouper VM as banderson/password

Configure SQL sync

- Navigate to: Miscellaneous → SQL sync
- Actions → Add SQL sync
- Config ID: hr_depts_201
- Sql sync configuration: SqlSyncConfiguration
- Database from: grouper
- Table from: hr_depts_201_v
- Database to: hr
- Table to: hr_depts_201
- Column names
  
  
  ```
  group_name, net_id
  ```
- Primary key columns
  
  
  ```
  group_name, net_id
  ```
- Submit SQL sync

Daemon job

- Navigate to Miscellaneous → Daemon jobs
- Daemon actions → Add daemon
- Config ID: hr_depts_201
- Daemon type: SQL sync
- Quartz cron:
  
  
  ```
  29 43 * * * ?
  ```
- Sync config key: hr_depts_201
- Sync type: fullSyncFull
- Submit daemon job

Verify result

- Search for daemon: hr_depts_201
- Job actions → Run job now
- See the failure
- Look at the logs to the right
- See grants are missing
- See the hr database external system username: hr_grouper_svc
- Run this database script in the hr database with DBeaver
  
  
  ```
  grant select, insert, update, delete on hr_depts_201 to hr_grouper_svc;
  ```
- Go back to the daemon screen and run the hr_depts_201 daemon again. See it succeed and take less than a second
- Look in the hr_depts_201 table and see the data
