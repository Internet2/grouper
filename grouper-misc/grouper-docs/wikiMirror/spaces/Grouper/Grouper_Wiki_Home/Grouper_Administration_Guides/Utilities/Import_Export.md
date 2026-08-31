---
title: "Import-Export"
space: Grouper
pageId: 28545561
version: 37
lastUpdated: 2026-07-01T05:47:05.749Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545561/Import-Export
---

> [https://www.youtube.com/watch?v=4HzxLb6M03E](https://www.youtube.com/watch?v=4HzxLb6M03E) This topic is discussed in the ["Grouper API - Part 2" training video](https://www.youtube.com/watch?v=4HzxLb6M03E).

 > Grouper includes a GrouperShell (gsh) tool that exports the registry to XML and imports it back into another database, run as `gsh -xmlexport` and `gsh -xmlimport`. It is a long-standing core tool, present in all current supported releases (confirmed in v4, v6, and v7).

  The Grouper registry can be exported to XML and imported back into another database. There are a few variations, including the child pages below.

 

 > This is a server-side administrative operation. It runs from [GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) on the Grouper server, so it requires filesystem access to the Grouper install. The gsh session runs as `GrouperSystem` (root), so it can export and import the entire registry.

 

## What the XML tools are for

 Exported XML can be used for:

 

- backups — for example, before an upgrade
- switching database backends, including to an upgraded schema (required by new Grouper API versions) in the same database
- moving or syncing a folder of Grouper to another environment
- initializing a new, *empty* registry to a known state — useful for demos, testing, and system recovery

 Imported XML adds to, or updates, existing folders, groups, and group types. A whole or partial registry can be exported and then imported at a specified folder (or the root folder, if none is given) in the target instance. Folders, groups, and group types are created if not already present, or updated if they already exist, depending on the import options. Any tool that can produce XML in the correct format can be used as a loader.

 For reporting or for provisioning to other systems, use Grouper web services or SQL rather than the XML export.

 

## Limitations

 The XML import/export does not cover the entire data model. It does **not** include external subjects, entities, point-in-time data, the change log, the `grouper_ddl` table (which is tied to the schema), or the `subject` / `subjectattribute` quickstart tables (which are not part of Grouper itself). Audit entries *are* exported. Because the coverage is partial, the tool is suited to making a backup within a version and **should not** be used to migrate from one Grouper version to another during an upgrade. To move or sync data between environments, use the Grouper database migration utility (linked above).

 A few behaviors to be aware of:

 

- Object metadata (uuid, created date, and so on) is kept in sync, but if an object already exists in the target it keeps its existing uuid rather than the imported one.
- To load subject (membership) data, the target instance must be configured with the same subject sources. The export does not include subject registries, and subjects that cannot be resolved are logged and otherwise ignored.
- If you are not using the default privilege interface in `grouper.properties`, privileges are not handled automatically — export and import them yourself.

 

## Usage

 The tools are run from gsh. The original (legacy) XML format is still available as `-xmlexportold` and `-xmlimportold`.

 

### Export

 Run `gsh -xmlexport` with no arguments (or `-h`) to see the options:

 
```text
gsh -xmlexport

Usage:
args: -h,            Prints this message
args:
      [-noprompt] filename
e.g.  gsh -xmlexport f:/temp/prod.xml
e.g.  gsh -xmlexport -stems a:b:c,d:e:f f:/temp/prod.xml

  -includeComments,  Put comments about foreign keys in XML
  -stems,            Only include objects in these comma separated stems or object names
  -objectNames,      Only include objects in these comma separated object names or stems
  -excludeAudits,    Put comments about foreign keys in XML
  -noprompt,         Do not prompt user to confirm the export
  filename,          The file to import
```

  
```text
C:\mchyzer\grouper\trunk\grouper\bin>gsh -xmlexport whatever.xml
Using GROUPER_HOME: C:\mchyzer\grouper\trunk\grouper\bin\..
Using GROUPER_CONF: C:\mchyzer\grouper\trunk\grouper\bin\../conf
Using JAVA: java
using MEMORY: 64m-512m
This db user 'grouper' and url 'jdbc:mysql://localhost:3306/grouper' are allowed to be changed in the grouper.properties
Continuing...
Grouper starting up: version: 1.6.0, build date: 2010/02/09 02:24:03, env: <no label configured>
grouper.properties read from: C:\mchyzer\grouper\trunk\grouper\conf\grouper.properties
Grouper current directory is: C:\mchyzer\grouper\trunk\grouper\bin
log4j.properties read from: C:\mchyzer\grouper\trunk\grouper\conf\log4j.properties
Grouper is logging to file: C:\mchyzer\grouper\trunk\grouper\bin\..\logs\grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: C:\mchyzer\grouper\trunk\grouper\conf\grouper.hibernate.properties
grouper.hibernate.properties: grouper@jdbc:mysql://localhost:3306/grouper
sources.xml read from: C:\mchyzer\grouper\trunk\grouper\conf\sources.xml
sources.xml groupersource id: g:gsa
sources.xml jdbc source id: jdbc: GrouperJdbcConnectionProvider
Starting: 163 records in the DB to be exported
DONE: 02:32:54: exported 163 records to: C:\mchyzer\grouper\trunk\grouper\bin\whatever.xml
C:\mchyzer\grouper\trunk\grouper\bin>
```

  

### Import

 Before importing, turn off include/exclude and require-groups in `grouper.properties` (these are off by default):

 
```text
grouperIncludeExclude.use = false
grouperIncludeExclude.requireGroups.use = false
```

 Run `gsh -xmlimport` with no arguments (or `-h`) to see the options:

 
```text
gsh -xmlimport

Usage:
args: -h,            Prints this message
args:
      [-recordReport]
      [-noprompt] filename
e.g.  gsh -xmlimport f:/temp/prod.xml

  -recordReport,     Print a file which lists each insert/update
                     In addition to import
  -noprompt,         Do not prompt user to confirm the database that
                     will be updated
  filename,          The file to import
```

  
```text
C:\mchyzer\grouper\trunk\grouper\bin>gsh -xmlimport whatever.xml -recordReport
Using GROUPER_HOME: C:\mchyzer\grouper\trunk\grouper\bin\..
Using GROUPER_CONF: C:\mchyzer\grouper\trunk\grouper\bin\../conf
Using JAVA: java
using MEMORY: 64m-512m
This db user 'grouper' and url 'jdbc:mysql://localhost:3306/grouper' are allowed to be changed in the grouper.properties
Continuing...
Grouper starting up: version: 1.6.0, build date: 2010/02/09 02:24:03, env: <no label configured>
grouper.properties read from: C:\mchyzer\grouper\trunk\grouper\conf\grouper.properties
Grouper current directory is: C:\mchyzer\grouper\trunk\grouper\bin
log4j.properties read from: C:\mchyzer\grouper\trunk\grouper\conf\log4j.properties
Grouper is logging to file: C:\mchyzer\grouper\trunk\grouper\bin\..\logs\grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: C:\mchyzer\grouper\trunk\grouper\conf\grouper.hibernate.properties
grouper.hibernate.properties: grouper@jdbc:mysql://localhost:3306/grouper
sources.xml read from: C:\mchyzer\grouper\trunk\grouper\conf\sources.xml
sources.xml groupersource id: g:gsa
sources.xml jdbc source id: jdbc: GrouperJdbcConnectionProvider
grouper import: reading document: C:\mchyzer\grouper\trunk\grouper\bin\whatever.xml, version: 1.6.0
XML file contains 163 records
02:34:58: Beginning import: database contains 155 records
Ending import: processed 163 records
Ending import: database contains 163 records
Ending import: 8 inserts, 1 updates, and 154 skipped records
DONE: 02:34:59: imported 163 records from: C:\mchyzer\grouper\trunk\grouper\bin\whatever.xml
Wrote record report log to: C:\mchyzer\grouper\trunk\grouper\bin\grouperImportRecordReport_2010_02_09__02_34_58_685.txt

C:\mchyzer\grouper\trunk\grouper\bin>more C:\mchyzer\grouper\trunk\grouper\bin\grouperImportRecordReport_2010_02_09__02_34_58_685.txt
Update: Group: 197c460aff064eb6876b63d500c5ee22, etc:userReceiver
Insert: AttributeDefNameSet: 3e6915e7b4f144b38fe7e5143a60c9b4,
Insert: AuditEntry: f7be69a260514b6db7c3982e997cc012
Insert: AuditEntry: e8bc311da27c468281c4d8867305a998
Insert: AuditEntry: de69f0556d4648169b94ffcb7936cf77
Insert: AuditEntry: faa8130871e549e3947f2d3afaeae460
Insert: AuditEntry: f31a5288f8564b2c8e41a5f693a4f914
Insert: AuditEntry: e5a2c9ef662c483691bd92f8e65d1daa
Insert: AuditEntry: f2227db7415e44659f61e1703a02c81c

C:\mchyzer\grouper\trunk\grouper\bin>
```

  

## See also

 The XML import/export above is an admin tool for the whole registry. A group's owner can also import, export, and change the membership of their own group from the Grouper UI; this is covered in the [Grouper UI training video (part 2)](http://www.youtube.com/watch?v=CuGez-fmqGo).
