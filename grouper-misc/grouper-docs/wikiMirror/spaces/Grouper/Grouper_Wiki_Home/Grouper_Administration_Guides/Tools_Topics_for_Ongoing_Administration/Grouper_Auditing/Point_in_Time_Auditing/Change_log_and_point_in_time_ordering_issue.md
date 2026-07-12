---
title: "Change log and point in time ordering issue"
space: Grouper
pageId: 28555282
version: 2
lastUpdated: 2017-06-15T17:58:36.775Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555282/Change+log+and+point+in+time+ordering+issue
---

### Got this error in change log processing

```
'' : Error invoking compiled command: : Error in
compiled command: java.lang.RuntimeException: java.lang.RuntimeException: Active PITMember with
sourceId=c5c3a39cd7b84aada804b76c4641df5a not found,
```

### See how many PIT members arent there

```
select count(1) from grouper_members gm where not exists
(select * from grouper_pit_members gpm where gm.id = gpm.source_id);

102
```

### Run the change log temp to change log

```
gsh 0%  loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog")

// Error: unable to evaluate command: Sourced file: inline evaluation of: `` loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");''
: Error invoking compiled command: : Error in compiled command: java.lang.RuntimeException: java.lang.RuntimeException: Active PITMember with
sourceId=c5c3a39cd7b84aada804b76c4641df5a not found,

Problem in HibernateSession: HibernateSession (70e04b5f):
new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (658a2984)

gsh 1%  new
edu.internet2.middleware.grouper.misc.SyncPITTables().syncAllPITTables()

Number of entries in grouper_change_log_entry_temp: 3585

For best results, run
loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog") first.
Searching for missing active point in time fields
Found 0 missing active point in time fields

...

 
Searching for point in time attribute assigns that should be inactive
Found 401 active point in time attribute assigns that should be inactive

// Error: unable to evaluate command: Sourced file: inline
evaluation of: `` new edu.internet2.middleware.grouper.misc.SyncPITTables().syncAllPITTables();'' :
Method Invocation syncAllPITTables

// See error log for full stacktrace
// caused by: edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException:
// Problem in HibernateSession: HibernateSession (90497ef): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (3a3e5f05)
// caused by: org.hibernate.StaleObjectStateException:

// Row was updated or deleted by another transaction (or
unsaved-value mapping was incorrect) : [edu.internet2.middleware.grouper.pit.PITAttributeAssign#7b0eb22ac8e546dbaf1187189d5c4f43],

Exception in save: edu.internet2.middleware.grouper.pit.PITAttributeAssign,
edu.internet2.middleware.grouper.hibernate.ByObject@47fdf768:
[edu.internet2.middleware.grouper.pit.PITAttributeAssign#7b0eb22ac8e546dbaf1187189d5c4f43]

gsh 2%
```

### Add a view for change log temp (if not there)

```
/* Formatted on 6/15/2017 11:11:53 AM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW GROUPER_CHANGE_LOG_TEMP_V
(
   CREATED_ON,
   CHANGE_LOG_CATEGORY,
   ACTION_NAME,
   LABEL_STRING01,
   STRING01,
   LABEL_STRING02,
   STRING02,
   LABEL_STRING03,
   STRING03,
   LABEL_STRING04,
   STRING04,
   LABEL_STRING05,
   STRING05,
   LABEL_STRING06,
   STRING06,
   LABEL_STRING07,
   STRING07,
   LABEL_STRING08,
   STRING08,
   LABEL_STRING09,
   STRING09,
   LABEL_STRING10,
   STRING10,
   LABEL_STRING11,
   STRING11,
   LABEL_STRING12,
   STRING12,
   CONTEXT_ID,
   CHANGE_LOG_TYPE_ID
)
   BEQUEATH DEFINER
AS
   SELECT gcle.created_on,
          gclt.change_log_category,
          gclt.action_name,
          gclt.label_string01,
          gcle.string01,
          gclt.label_string02,
          gcle.string02,
          gclt.label_string03,
          gcle.string03,
          gclt.label_string04,
          gcle.string04,
          gclt.label_string05,
          gcle.string05,
          gclt.label_string06,
          gcle.string06,
          gclt.label_string07,
          gcle.string07,
          gclt.label_string08,
          gcle.string08,
          gclt.label_string09,
          gcle.string09,
          gclt.label_string10,
          gcle.string10,
          gclt.label_string11,
          gcle.string11,
          gclt.label_string12,
          gcle.string12,
          gcle.context_id,
          gcle.change_log_type_id
     FROM grouper_change_log_type gclt, grouper_change_log_entry_temp gcle
    WHERE gclt.id = gcle.change_log_type_id;
COMMENT ON TABLE GROUPER_CHANGE_LOG_TEMP_V IS 'Join of change log entry and change log type';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.CREATED_ON IS 'created_on: when this change happened, number of millis since 1970';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.CHANGE_LOG_CATEGORY IS 'change_log_category: category of this change';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.ACTION_NAME IS 'action_name: action of this change';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING01 IS 'label_string01: label of first string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING01 IS 'string01: value of first string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING02 IS 'label_string02: label of second string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING02 IS 'string02: value of second string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING03 IS 'label_string03: label of third string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING03 IS 'string03: value of third string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING04 IS 'label_string04: label of fourth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING04 IS 'string04: value of fourth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING05 IS 'label_string05: label of fifth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING05 IS 'string05: value of fifth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING06 IS 'label_string06: label of sixth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING06 IS 'string06: value of sixth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING07 IS 'label_string07: label of seventh string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING07 IS 'string07: value of seventh string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING08 IS 'label_string08: label of eighth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING08 IS 'string08: value of eighth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING09 IS 'label_string09: label of ninth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING09 IS 'string09: value of ninth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING10 IS 'label_string10: label of tenth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING10 IS 'string10: value of tenth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING11 IS 'label_string11: label of eleventh string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING11 IS 'string11: value of eleventh string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.LABEL_STRING12 IS 'label_string12: label of twelfth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.STRING12 IS 'string12: value of twelfth string';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.CONTEXT_ID IS 'context_id: links this record with an audit record';
COMMENT ON COLUMN GROUPER_CHANGE_LOG_TEMP_V.CHANGE_LOG_TYPE_ID IS 'change_log_type_id: id of this category and name';

```

See which entries are in the change log temp for that id

```
select to_char(created_on), change_log_category, action_name, string01, label_string02, 
string02, label_string03, string03, label_string04, string04, label_string05, string05
 from grouper_change_log_temp_v where
string01 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string02 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string03 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string04 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string05 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string06 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string07 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string08 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string09 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string10 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string11 = 'c5c3a39cd7b84aada804b76c4641df5a'
or string12 = 'c5c3a39cd7b84aada804b76c4641df5a'
order by created_on
```

| 1497469830262000 | privilege | addPrivilege | 3b250c963f29444480f8102e89f371f1 | privilegeName | update | subjectId | ef1563fcdb0d4604b9ed3e45fe1a85f6 | sourceId | g:gsa | privilegeType | access |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1497469850914000 | member | addMember | c5c3a39cd7b84aada804b76c4641df5a | subjectId | ef1563fcdb0d4604b9ed3e45fe1a85f6 | subjectSourceId | g:gsa | subjectTypeId | group | subjectIdentifier0 | penn:isc:XXXXXXXXX |

This happened when time on servers was not correct, and WS is running on multiple servers

```
[appadmin@fastprod ~]$ clusterRun grouperWs date
SERVER fa-01: date
Thu Jun 15 11:50:28 EDT 2017

SERVER a-02: date
Thu Jun 15 11:50:29 EDT 2017

SERVER a-03: date
Thu Jun 15 11:49:56 EDT 2017

SERVER a-04: date
Thu Jun 15 11:50:28 EDT 2017

SERVER a-05: date
Thu Jun 15 11:50:29 EDT 2017
```

Note these are out of order. update the addMember

```
update grouper_change_log_entry_temp set created_on = 14974698302XXXXX - 1 where created_on = 14974698509XXXXX;
 
or
 
update grouper_change_log_entry_temp
set created_on = 14974698302XXXXX
where change_log_type_id = (select id from grouper_change_log_type where 
change_log_category = 'group' and action_name = 'addGroup');
commit;
```
