---
title: "Grouper sources.xml conversion to subject.properties"
space: Grouper
pageId: 28555326
version: 7
lastUpdated: 2026-07-01T05:38:31.493Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555326/Grouper+sources.xml+conversion+to+subject.properties
---

In grouper 2.3.0 (unpatched) and previous versions, the subject API was configured with a sources.xml . This needs to be converted to config overlays in 2.4.0 so Grouper packaging can have defaults and overlays. Also so the Grouper team can manage internal sources without having to merge with a potentially changed file.

## Conversion

Run the grouperInstaller, admin task, upgradeTasks, convert sources xml to properties

Follow the prompt to identify the location of the sources.xml file

This will generate the subject.properties file based on your sources.xml edits

```
[appadmin@i2midev1 patchesAuto]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [install]: admin
What admin action do you want to do (manage, upgradeTask)? : upgradeTask
What upgrade task do you want to do (convertEhcacheXmlToProperties, convertSourcesXmlToProperties)? : convertSourcesXmlToProperties
Note, you need to convert the sources.xml file for each Grouper runtime, e.g. loader, WS, UI.
Note, to use subject sources from subject.properties, you need to be running Grouper 2.3.0+ with API patch 40 installed.
Enter the location of the sources.xml file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml
Enter the location of the subject.base.properties file [/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties]: 
File was written: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties
You should archive your sources.xml and remove it from your project since it is now unused:
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml

```

## Configuration

Sample sources.xml config

```
<?xml version="1.0" encoding="utf-8"?>
<sources>
 <source adapterClass="edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter">
    <id>jdbc</id>
    <name>Example JDBC Source Adapter</name>
     <type>person</type>
     
     <init-param>
       <param-name>sortAttribute1</param-name>
       <param-value>LOGINID</param-value>
     </init-param>
     <init-param>
       <param-name>searchAttribute0</param-name>
       <param-value>searchAttribute0</param-value>
     </init-param>
     <internal-attribute>searchAttribute0</internal-attribute>

     <search>
         <searchType>searchSubject</searchType>
         <param>
             <param-name>sql</param-name>
             <param-value>
select
   s.subjectid as id, s.name as name,
   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,
   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,
   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description,
   (select sa5.value from subjectattribute sa5 where name='email' and sa5.SUBJECTID = s.subjectid) as email
from
   subject s
where
   {inclause}
            </param-value>
         </param>
         <param>
             <param-name>inclause</param-name>
             <param-value>
s.subjectid = ?
            </param-value>
         </param>
     </search>
   </source>
</sources>
```

Sample subject.properties config:

```
#########################################
## Configuration for source id: jdbc
## Source configName: jdbc
#########################################
subjectApi.source.jdbc.id = jdbc

# this is a friendly name for the source
subjectApi.source.jdbc.name = Example JDBC Source Adapter

# type is not used all that much.  Can have multiple types, comma separate.  Can be person, group, application
subjectApi.source.jdbc.types = person

# the adapter class implements the interface: edu.internet2.middleware.subject.Source
# adapter class must extend: edu.internet2.middleware.subject.provider.BaseSourceAdapter
# edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2  :  if doing JDBC this should be used if possible.  All subject data in one table/view.
# edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter   :  oldest JDBC source.  Put freeform queries in here
# edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter   :  used for LDAP
subjectApi.source.jdbc.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter

subjectApi.source.jdbc.param.jdbcConnectionProvider.value = edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider

subjectApi.source.jdbc.param.identifierAttributes.value = LOGINID

# subject identifier to store in grouper's member table.  this is used to increase speed of loader and perhaps for provisioning
# you can have up to max 1 subject identifier
subjectApi.source.jdbc.param.subjectIdentifierAttribute0.value = LOGINID

#searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.
#  Each subject has one and only on ID.  Returns one result when searching for one ID.

# sql is the sql to search for the subject by id should use an {inclause}
subjectApi.source.jdbc.search.searchSubject.param.sql.value = select    s.subjectid as id, s.name as name,    (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,    (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,    (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description,    (select sa5.value from subjectattribute sa5 where name='email' and sa5.SUBJECTID = s.subjectid) as email from    subject s where    {inclause}

# inclause allows searching by subject for multiple ids or identifiers in one query, must have {inclause} in the sql query,
#    this will be subsituted to in clause with the following.  Should use a question mark ? for bind variable
subjectApi.source.jdbc.search.searchSubject.param.inclause.value = s.subjectid = ?

 
# internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
subjectApi.source.jdbc.internalAttributes = searchAttribute0

```

Note, the part that links the properties entries together is the part between "source" and the following dot. In this case "jdbc". This was automatically converted from the sources.xml file by looking at the source name and making sure there are no special chars (replace with underscore).

## Use the xml configuration

If you want to use an xml configuration (generally you wont need to do this) set this in subject.properties (or remove to use the default):

```
########################################
## Use old sources.xml
########################################

# enter the location of the sources.xml.  Must start with classpath: or file:
# blank means dont use sources.xml, use subject.properties
# default is: classpath:sources.xml
# e.g. file:/dir1/dir2/sources.xml
subject.sources.xml.location = classpath:sources.xml

```

## Upgrade via Patch

- Get the latest grouperInstaller.jar
- Backup your sources.xml file
- Install the 2.3.0#40 API patch
- If you have ever edited the subject.properties file (unlikely), move that file to another location and merge in changes later if applicable
- Run the grouperInstaller, admin task, upgradeTasks, convert sources xml to properties
- Follow the prompt to identify the location of the sources.xml file
- This will generate the subject.properties file based on your sources.xml
- Examine the subject.properties file. Eyeball it to see that your settings from sources.xml are there
- Diff your sources.xml file with your sources.example.xml file and make sure you have never changed the settings for Grouper internal sources (not common), e.g. the g:gsa source, or g:isa, or grouperEntities. If you have, configure those properties in the subject.properties overlay. Maybe alert the Grouper team as well to see if it should be a new default for all.
- Delete the sources.xml and sources.example.xml files (again, keep a backup somewhere for a while)
- Install the patch in your other envs (UI, WS, daemon, etc), and copy the subject.properties to each env (no need to go through this same process unless you have different cache customizations in each env). And delete the sources.xml and sources.example in each env

## Upgrade via upgrade

If you upgrade to 2.3.1+ it will walk you through the process of automatically converting your sources.xml to subject.properties

## Example of using the latest grouperInstaller.jar

```
[appadmin@i2midev1 patchesAuto]$ rm grouperInstaller.jar
[appadmin@i2midev1 patchesAuto]$ wget --no-check-certificate https://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
```

Backup your sources.xml and sources.example.xml file

```
[appadmin@i2midev1 patchesAuto]$ cp /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml /tmp
[appadmin@i2midev1 patchesAuto]$ cp /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.example.xml /tmp
```

Install the 2.3.0#40 API patch (note: Force install that patch since it will say file mismatch)

```
Patch grouper_v2_3_0_api_patch_40 is medium risk, is not a security patch
GRP-1454: migrate from sources.xml to hierarchical properties configuration
 - set property: grouper_v2_3_0_api_patch_40.date from: 2017/01/07 22:43:45 to: 2017/01/07 22:44:38
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable and grouperInstaller.autorun.continueAfterPatchStopProcesses>: 
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/SourceManager$SourceManagerStatusBean.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/NullSourceAdapter.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/NullSourceAdapter.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/SourceManager.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/SourceManager.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/JNDISourceAdapterLegacy.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/JNDISourceAdapterLegacy.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/config/SubjectConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/subject/config/SubjectConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/GrouperSourceAdapter$2.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/subj/InternalSourceAdapter.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/subj/InternalSourceAdapter.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig$1.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/GrouperSourceAdapter.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/GrouperSourceAdapter$3.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/GrouperSourceAdapter$1.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/GrouperSourceAdapter.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.base.properties
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/lib/commons-digester.jar
Patch successfully applied: grouper_v2_3_0_api_patch_40
 - set property: grouper_v2_3_0_api_patch_40.state from: error to: applied
```

At this point, you can use Grouper as you did before, with the sources.xml. but if you want to use the subject.properties, you can continue. You can see its reading from sources.xml in the startup output

```
subject.properties read from: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties
sources.xml read from:        /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml
sources configured in:        sources.xml
sources.xml        groupersource id: g:gsa
sources.xml        groupersource id: grouperEntities
sources.xml        jdbc source id:   jdbc: GrouperJdbcConnectionProvider
```

Run the grouperInstaller, admin task, upgradeTasks, convert sources xml to properties

Follow the prompt to identify the location of the sources.xml file

This will generate the subject.properties file based on your sources.xml edits

```
[appadmin@i2midev1 patchesAuto]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [install]: admin
What admin action do you want to do (manage, upgradeTask)? : upgradeTask
What upgrade task do you want to do (convertEhcacheXmlToProperties, convertSourcesXmlToProperties)? : convertSourcesXmlToProperties
Note, you need to convert the sources.xml file for each Grouper runtime, e.g. loader, WS, UI.
Note, to use subject sources from subject.properties, you need to be running Grouper 2.3.0+ with API patch 40 installed.
Enter the location of the sources.xml file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml
Enter the location of the subject.base.properties file [/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties]: 
File was written: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties
You should archive your sources.xml and remove it from your project since it is now unused:
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml

```

Examine the subject.properties file. Diff your sources.xml file with your sources.example.xml (if exists or get [here](https://github.com/Internet2/grouper/raw/GROUPER_2_3_BRANCH/grouper/conf/sources.example.xml)) file and make sure those diffs are expected and listed in your subject.properties file

```
[appadmin@i2midev1 patchesAuto]$ more /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/properties.properties

[appadmin@i2midev1 patchesAuto]$ wget --no-check-certificate https://github.com/Internet2/grouper/raw/GROUPER_2_3_BRANCH/grouper/conf/sources.example.xml
[appadmin@i2midev1 patchesAuto]$ diff sources.example.xml /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml

```

Delete the sources.xml and sources.example.xml (if exists) files, archiving if you like

```
[appadmin@i2midev1 patchesAuto]$ mv /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.xml ~/sources.xml.20170107.grouper2_3
[appadmin@i2midev1 patchesAuto]$ rm /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.example.xml
rm: cannot remove `/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/sources.example.xml': No such file or directory
[appadmin@i2midev1 patchesAuto]$ 
```

Install the patch in your other envs (UI, WS, daemon, etc), and copy the subject.properties to each env (no need to go through this same process unless you have different cache customizations in each env. Delete the sources.xml and sources.example.xml in other envs too

## Logging

Grouper startup will show the subject.properties info

```
subject.properties read from: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/subject.properties
sources configured in:        subject.properties
subject.properties groupersource id: g:gsa
subject.properties groupersource id: grouperEntities
subject.properties jdbc source id:   jdbc: GrouperJdbcConnectionProvider
```
