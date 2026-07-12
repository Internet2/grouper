---
title: "Grouper failsafes"
space: Grouper
pageId: 28554941
version: 11
lastUpdated: 2026-07-01T05:39:18.417Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554941/Grouper+failsafes
---

Failsafe settings are revamped in v2.6.6+.

## Global settings

These settings are in grouper-loader.properties. Note, jobs can be configured to override defaults with the job-specific settings.

## Failsafe problems in logs

## Approve failsafe button on daemon screen

## The next run (manual or scheduled) will run after approval

## Testing data

```
select col1 as SUBJECT_ID from testgrouper_loader where col1 in ('test.subject.0', 'test.subject.1');

select concat('loader:groups:', REPLACE(col1, '.', '_')) as group_name, col1 as SUBJECT_ID from testgrouper_loader where col1 in ('test.subject.0', 'test.subject.1') or 1=1

-- mysql
CREATE TABLE testgrouper_loader (
  id varchar(255) NOT NULL,
  hibernate_version_number bigint NOT NULL,
  col1 varchar(255) DEFAULT NULL,
  col2 varchar(255) DEFAULT NULL,
  col3 varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO testgrouper_loader (id,hibernate_version_number,col1,col2,col3) VALUES 
('206a83a4173d4e13a61f7094d0424c11',0,'test.subject.1',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf0',0,'test.subject.8',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf2',0,'test.subject.9',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf3',0,'test.subject.0',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf4',0,'test.subject.2',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf5',0,'test.subject.3',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf6',0,'test.subject.4',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf7',0,'test.subject.5',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf8',0,'test.subject.6',NULL,NULL)
,('d27f7943ffd2456d8c7bbe69504cdbf9',0,'test.subject.7',NULL,NULL)
;
commit;
```

## pre v2.6.6: Failsafe to not remove too many members by mistake

You can configure the loader to not make changes if too many members are to be removed. The use case is if the source for the loader groups gets blanked out accidentally, it shouldn't remove everyone. However, if groups are supposed to drastically change, it means a user needs to manually change this flag, run the sync, and change it back.

```
# if the loader should check to see too many users were removed, if so, then error out and
# wait for manual intervention
loader.failsafe.use = false

# if a group has a size less than this (default 200), then make changes including blanking it out
loader.failsafe.minGroupSize = 200

# if a group with more members than the loader.failsafe.minGroupSize have more than this percent (default 30) 
# removed, then log it as error, fail the job, and don't actually remove the members
# In order to run the job, an admin would need to change this param in the config,
# and run the job manually, then change this config back
loader.failsafe.maxPercentRemove = 30
```
