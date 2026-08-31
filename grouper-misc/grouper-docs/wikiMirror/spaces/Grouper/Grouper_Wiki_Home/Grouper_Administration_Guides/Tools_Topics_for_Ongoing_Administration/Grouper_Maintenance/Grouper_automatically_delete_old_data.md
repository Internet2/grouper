---
title: "Grouper automatically delete old data"
space: Grouper
pageId: 28548887
version: 28
lastUpdated: 2026-07-01T05:43:19.442Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548887/Grouper+automatically+delete+old+data
---

Grouper can automatically purge old data that accumulates over time – audit entries, deleted point-in-time data, and aged-out folders – so the database does not grow without bound. Retention is configured in `grouper-loader.properties` and enforced by the loader's `MAINTENANCE_cleanLogs` daemon job, which runs on its normal schedule. You can also run any cleanup on demand from GrouperShell (GSH).

 Available in v2.3.0+ (patch 97+).

  > **Privileges:** changing retention requires edit access to `grouper-loader.properties` on the Grouper server (a Grouper/server administrator); the change takes effect when the loader reloads its configuration. Running a cleanup by hand requires running GSH with a root session (`GrouperSystem`). The scheduled `MAINTENANCE_cleanLogs` job runs as the loader daemon.

 

## How cleanup runs

 The `MAINTENANCE_cleanLogs` daemon job applies the retention settings below on its schedule. Nothing is deleted unless you set a retention period – every setting defaults to `-1` (keep forever). To run the job once on demand:

 
```
gsh 0% grouperSession = GrouperSession.startRootSession();
gsh 1% loaderRunOneJob("MAINTENANCE_cleanLogs");

loader ran successfully: Deleted 3872 records from grouper_loader_log older than 7 days old.  Deleted 2038 records from grouper_change_log_entry older than 14 days old.  Deleted 0 instrumentation records older than 30 days old.  Configured to not delete records from audit_entry table with null logged in member id  Configured to not delete records from audit_entry table  Configured to not delete records from DeletedPointInTimeObjects
```

 The individual cleanup routines can also be called directly from GSH, as shown in each section below.

 

## Delete audit entries with no logged-in user

 Audit entries with no logged-in user (loader, GSH, etc.) are not especially useful – the equivalent point-in-time data is still retained – so removing the old ones is generally low-risk. Configure in `grouper-loader.properties`:

 
```text
# number of days to retain db rows in grouper_audit_entry with no logged in user (loader, gsh, etc).
# -1 is forever.  suggested is 365 or five years: 1825.  Default is -1
loader.retain.db.audit_entry_no_logged_in_user.days=-1
```

 Call from GSH:

 
```
// delete records older than the configured number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser();

// delete records older than the specified number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser(1825);
```

 
```
logType: maintenanceDeleteOldRecords, deleteOldAuditNotLoggedInDays: -1, elapsed: 0 ms
logType: maintenanceDeleteOldRecords, deleteOldAuditNotLoggedInDays: 9, deleteOldAuditNotLoggedInCount: 0, elapsed: 2 ms
logType: maintenanceDeleteOldRecords, deleteOldAuditNotLoggedInDays: 7, deleteOldAuditNotLoggedInCount: 1, elapsed: 15 ms
```

 

## Delete audit entries

 These are audits of actions people take in the UI or web service (as opposed to entries with no logged-in user). Even at large institutions there are not many, so the default is to keep them forever. Configure in `grouper-loader.properties`:

 
```text
# number of days to retain db rows in grouper_audit_entry.
# -1 is forever.  suggested is -1 or ten years: 3650.  Default is -1
loader.retain.db.audit_entry.days=-1
```

 Call from GSH:

 
```
// delete records older than the configured number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldAuditEntry();

// delete records older than the specified number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldAuditEntry(3650);
```

 

## Delete old deleted point-in-time data

 After you delete an object in Grouper it remains in point-in-time data, so you can still answer questions like who was in a group a year ago. After enough time it may be acceptable to let that go. Configure in `grouper-loader.properties`:

 
```text
# number of days to retain db rows for point in time deleted objects.
# -1 is forever.  suggested is 365 or five years: 1825.  Default is -1
loader.retain.db.point_in_time_deleted_objects.days=-1
```

 Call from GSH:

 
```
// delete records older than the configured number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldDeletedPointInTimeObjects();

// delete records older than the specified number of days
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.deleteOldDeletedPointInTimeObjects(1825);
```

 

## Obliterate old folders

 Optionally, Grouper can automatically obliterate (delete) folders **directly within a given parent folder** once they reach a certain age – for example, a term of courses four years old. Make sure the loader will not just recreate the folder, or you will get churn. This can also delete the associated point-in-time data.

 Each rule has a label you make up (`courses` and `anotherLabel` below are examples). Configure in `grouper-loader.properties`:

 
```text
# number of days after a subfolder (directly in the parent folder) is created that it will be obliterated
loader.retain.db.folder.courses.days=1825
# the parent folder whose direct subfolders are eligible for obliteration
loader.retain.db.folder.courses.parentFolderName=my:folder:for:courses
# whether to also delete the point in time data for the obliterated folder
loader.retain.db.folder.courses.deletePointInTime=true

# add more rules with another label
loader.retain.db.folder.anotherLabel.days=1825
loader.retain.db.folder.anotherLabel.parentFolderName=my:folder:for:something
loader.retain.db.folder.anotherLabel.deletePointInTime=false
```

 Call from GSH (obliterates the folders configured above):

 
```
edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords.obliterateOldStemsDirectlyInStem();
```

 
```
logType: maintenanceDeleteOldRecords, obliterateOldStemsCount: 1, obliterateOldStems.0.stem: test, obliterateOldStems.0.days: 7, obliterateOldStems.0.deletePointInTime: true, obliterateOldStems.0.stem.subFolderCount: 2, obliterateOldStems.0.stem.test2.deleting: true, obliterateOldStems.0.stem.test2.folderCreatedOn: 2018-02-11 14:51:57.096, elapsed: 60744 ms
```

 

## Gauging impact before you enable

 Before turning on a retention period, it is worth counting how many rows it would remove so the first run is not a surprise. Count current rows in `grouper_audit_entry_v` (optionally filtering on `logged_in_subject_id IS NULL` and `created_on`) and in the `grouper_pit_*` tables (filtering on `end_time` for deleted objects), then compare against your chosen retention window.
