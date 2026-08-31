---
title: "SFTP a delimited file and sync to SQL"
space: Grouper
pageId: 28548946
version: 11
lastUpdated: 2026-07-01T05:43:07.776Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548946/SFTP+a+delimited+file+and+sync+to+SQL
---

> The **SFTP delimited file to SQL** job pulls a delimited (CSV/pipe/etc.) file from an SFTP endpoint, parses it, and syncs the rows into a SQL table. It is a loader "other job" (`edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob`) that runs on the Grouper daemon. Available in **v2.6.10+** (GRP-4130, 2022).

  

## Overview

 This job will SFTP a file from an endpoint, unpack the contents as a delimited file, and sync it with a SQL table. You can then use that SQL table to sync to a group, to another table, as a subject source, as input to the attribute resolver, and so on.

 The job is configured either through the Grouper UI daemon-job wizard or directly in `grouper-loader.properties`. It relies on two external systems configured elsewhere in Grouper: an **SFTP external system** (where to fetch the file) and a **database external system** (which database holds the target table, defaulting to `grouper`).

 

## Configure in the Grouper UI

 Add the job from the daemon-job configuration wizard ("Miscellaneous" → the daemon/other-job configuration screens) and fill in the SFTP source, target database, table, columns, primary key, and delimiter:

 

 

## External system configuration for SFTP

 The job's SFTP config id points at an SFTP external system that defines the host, credentials, and paths. See **Grouper Sftp files** for how to define one. The external system looks like this:

 

 

## Sample file

 A pipe-delimited source file with a header row looks like this:

 
```text
[mchyzer@flash ~]$ ls /home/mchyzer/someFile.txt
/home/mchyzer/someFile.txt
[mchyzer@flash ~]$ more someFile.txt
NYUID|Application|NET_ID
N11234127|BRC|abc134
N11234127|GiveAVioletAward|abc134
N11234127|iLearn_Blatant|abc134
N11234127|Workday|abc134
N11234497|BRC|def245
N11234497|GiveAVioletAward|def245
N11234497|iCims|def245
N11234497|iLearn_Blatant|def245
[mchyzer@flash ~]$ 
```

 

## SQL table for this example

 The job syncs the parsed rows into a target table whose columns match the file's columns:

 

 DDL (MySQL in this case, but it could be any supported database):

 
```sql
CREATE TABLE my_sftp_sync_table (
  nyuid varchar(100),
  application varchar(100),
  net_id varchar(100)
)
```

 

## Daemon output

 When the job runs, the daemon log reports the rows inserted, updated, and deleted:

 

 

## Configuration in grouper-loader.properties

 The UI wizard writes these same properties; you can set them directly instead. Replace `sftpToSqlJobId` with your own config id. The following keys are read by `GrouperSftpToSqlJob` (confirmed in `grouper-loader.base.properties`):

 

| Property (under `otherJob.<jobId>.`) | Required | Default | Description |
| --- | --- | --- | --- |
| `class` | yes |  | Set to `edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob` to enable the job. |
| `quartzCron` | yes |  | Cron string for the schedule. |
| `sftpToSql.sftp.configId` | yes |  | SFTP external system config id to fetch the file from. Leave blank if the file is already local on the daemon server. |
| `sftpToSql.sftp.fileNameRemote` | yes |  | Remote file to fetch, e.g. `/data01/whatever/MyFile.csv`. |
| `sftpToSql.errorIfRemoteFileDoesNotExist` | no | `false` | Whether a missing remote file is treated as an error. |
| `sftpToSql.deleteFile` | no | `false` | Whether to delete the file from the Grouper daemon server after processing. |
| `sftpToSql.database` | no | `grouper` | Database external system config id holding the target table. |
| `sftpToSql.table` | yes |  | Target table, e.g. `some_table`, or schema-qualified: `some_schema.another_table`. |
| `sftpToSql.columns` | yes |  | Comma-separated columns to sync, e.g. `col1, col2, col3`. |
| `sftpToSql.columnsPrimaryKey` | yes |  | Comma-separated primary key columns, e.g. `col1`. |
| `sftpToSql.hasHeaderRow` | no | `false` | Whether the file has a header row. |
| `sftpToSql.separator` | yes |  | Field separator in the file, e.g. `\|`. |
| `sftpToSql.escapedSeparator` | no |  | Escaped separator (cannot contain the separator). |

  
```text
#####################################################
## sftp delimited file and sync to SQL table
## "sftpToSqlJobId" is the key of the config, change that for your csv file job
#####################################################

# set this to enable the report
# {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
# otherJob.sftpToSqlJobId.class = edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob

# cron string
# {valueType: "cron", required: true}
# otherJob.sftpToSqlJobId.quartzCron =

# sftp config id (from grouper.properties) if sftp'ing this file somewhere, otherwise blank
# https://spaces.at.internet2.edu/display/Grouper/Grouper+Sftp+files
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.sftp\\.configId$", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.file.SftpGrouperExternalSystem"}
# otherJob.sftpToSqlJobId.sftpToSql.sftp.configId =

# remote file to sftp to if sftp'ing, e.g. /data01/whatever/MyFile.csv
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.sftp\\.fileNameRemote$", required: true}
# otherJob.sftpToSqlJobId.sftpToSql.sftp.fileNameRemote =

# if it should be an error if the remote file doesnt exist
# {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.ignoreIfRemoteFileDoesNotExist$", defaultValue: "false"}
# otherJob.sftpToSqlJobId.sftpToSql.errorIfRemoteFileDoesNotExist =

# if the file should be deleted from the grouper daemon server after sending it
# {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.deleteFile$", defaultValue: "false"}
# otherJob.sftpToSqlJobId.sftpToSql.deleteFile =

# database external system config id to hit, default to "grouper"
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.database$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem"}
# otherJob.sftpToSqlJobId.sftpToSql.database =

# table to sql to, e.g. some_table.  or you can qualify by schema: some_schema.another_table
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.table$", required: true}
# otherJob.sftpToSqlJobId.sftpToSql.table =

# comma separated columns to sync to, e.g. col1, col2, col3
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.columns$", required: true}
# otherJob.sftpToSqlJobId.sftpToSql.columns =

# comma separated primary key columns, e.g. col1
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.columnsPrimaryKey$", required: true}
# otherJob.sftpToSqlJobId.sftpToSql.columnsPrimaryKey =

# if there is a header row
# {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.hasHeaderRow$", defaultValue: "false"}
# otherJob.sftpToSqlJobId.sftpToSql.hasHeaderRow =

# separator in file
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.separator$", required: true}
# otherJob.sftpToSqlJobId.sftpToSql.separator =

# escaped separator (cannot contain separator)
# {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.escapedSeparator$"}
# otherJob.sftpToSqlJobId.sftpToSql.escapedSeparator = 
```
