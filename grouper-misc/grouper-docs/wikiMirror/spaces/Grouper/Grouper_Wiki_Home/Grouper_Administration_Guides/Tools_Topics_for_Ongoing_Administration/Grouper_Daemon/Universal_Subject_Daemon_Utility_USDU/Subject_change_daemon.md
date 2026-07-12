---
title: "Subject change daemon"
space: Grouper
pageId: 28554199
version: 7
lastUpdated: 2026-07-01T05:40:56.330Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554199/Subject+change+daemon
---

## Description

Grouper caches subject information in the grouper_members and grouper_sync_member tables. Grouper also can provision subject information to targets.

When subject information changes, and the source is aware, it can notify Grouper to resolve the subject in question and update the grouper_members table. It can also send entity recalc messages to relevant provisioners to adjust the grouper_sync_member table and update targets. If the subject is not resolvable, it can trigger USDU.

## Architecture

You need a change log table in a database to identify subjects to update. This can be accomplished with a database trigger if you have a table of subjects.

## Configuration

grouper-loader.properties

```
#####################################################
## subject change daemon
#####################################################

# set this for subject change daemon
# {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
# otherJob.mySubjectChangeId.class = edu.internet2.middleware.grouper.app.usdu.SubjectChangeDaemon

# cron string
# {valueType: "cron", required: true}
# otherJob.mySubjectChangeId.quartzCron = 

# which source to update subjects for
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.subjectSourceId$", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.subject.provider.SourceManagerOptionValueDriver"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.subjectSourceId = 
 
# database external system config id to hit, default to "grouper"
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.database$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.database = 

# table to sql to, e.g. some_table.  or you can qualify by schema: some_schema.another_table
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.table$", required: true}
# otherJob.mySubjectChangeId.subjectChangeDaemon.table = 
 
# look up subjects by id or identifier
# {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.useSubjectIdOrIdentifier$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
# otherJob.mySubjectChangeId.subjectChangeDaemon.useSubjectIdOrIdentifier =   
 
# subject id column
# {valueType: "string", required: true, showEl: "useSubjectIdOrIdentifier == 'subjectId'",  regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.columnSubjectId$"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.columnSubjectId =   

# if finding subjects by identifier, this is the column that represents the identifier
# {valueType: "string", required: true, showEl: "useSubjectIdOrIdentifier == 'subjectIdentifier'", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.columnSubjectIdentifier$"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.columnSubjectIdentifier = 

# comma separated primary key columns, e.g. col1
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.columnPrimaryKey$", required: true}
# otherJob.mySubjectChangeId.subjectChangeDaemon.columnPrimaryKey = 

# should processed rows by deleted
# {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.deleteProcessedRows$", defaultValue: "false"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.deleteProcessedRows =
 
# name of a column that contains the timestamp of when a row is inserted
# {valueType: "string", required: true,  regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.columnCreateTimestamp$"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.columnCreateTimestamp =
 
# name of a column that contains the timestamp of when a row has been processed if deleteProcessedRows is false
# {valueType: "string", required: true, showEl: "deleteProcessedRows == false", regex: "^otherJob\\.([^.]+)\\.subjectChangeDaemon\\.columnProcessedTimestamp$"}
# otherJob.mySubjectChangeId.subjectChangeDaemon.columnProcessedTimestamp =
 
```

## Design

See this commit for a daemon example

[https://github.com/Internet2/grouper/commit/26bfadfbdb41b4def41830390f68625d1795555f](https://github.com/Internet2/grouper/commit/26bfadfbdb41b4def41830390f68625d1795555f)

This task can run at the same time as USDU to avoid delays when USDU is running since USDU can often take a long time to run. If both processes resolve the subject, that's fine.

Can multiple rows be handled in bulk?

Make sure the daemon total and updates are updated (total is rows handled and updates is number of subjects resolved... some might be skipped)

Skip rows that happened before the last successful USDU started. Check USDU to make sure it doesn't use cached subjects.

Here is an example
