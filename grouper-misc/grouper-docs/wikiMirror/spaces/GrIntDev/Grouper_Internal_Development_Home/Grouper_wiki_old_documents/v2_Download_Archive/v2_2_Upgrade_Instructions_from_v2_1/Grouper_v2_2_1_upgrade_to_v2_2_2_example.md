---
title: "Grouper v2.2.1 upgrade to v2.2.2 example"
space: GrIntDev
pageId: 48795862
version: 4
lastUpdated: 2026-07-12T06:46:32.024Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795862/Grouper+v2.2.1+upgrade+to+v2.2.2+example
---

## 

## Grouper API installer upgrade from v2.2.1 to v2.2.2

```
[mchyzer@i2mibuild installer]$ java -jar grouperInstaller.jar 
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/home/mchyzer/2.2.2/installer]: 
What do you want to upgrade?  api, ui, ws, or psp? [api]: 
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper API installed? /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1
Upgrading to grouper API version: 2.2.2
##################################
Download and build grouper packages
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.clientBinary-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.apiBinary-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar
Do you want to set gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]: 
##################################
Copying records from change log temp to change log with command:
  /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
...
stderr: Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: 260483 manifest version: 2.2.1.  However the jar detected is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/grouper/subject.jar, name: subject.jar size: 259419 manifest version: 2.2.1
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../conf
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
edu.internet2.middleware.grouper.GrouperSession: ad79d81852ae4afda35c7ee2e8c29689,'GrouperSystem','application'
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
loader ran successfully: Ran the changeLogTempToChangeLog daemon

##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/lib/grouper/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.client.base.properties
grouper.client.properties has 7 properties that can be removed since the values are the same in grouper.client.base.properties
Would you like to have the 7 redundant properties automatically removed from grouper.client.properties (t|f)? [t]: 
grouper.client.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.client.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/dist/lib/grouper.jar
##################################
Upgrading API config files
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.base.properties
grouper.hibernate.properties has 3 properties that can be removed since the values are the same in grouper.hibernate.base.properties
Would you like to have the 3 redundant properties automatically removed from grouper.hibernate.properties (t|f)? [t]: 
grouper.hibernate.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.hibernate.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper-loader.base.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/subject.base.properties
You should compare /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
  with /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/lib/grouper/subject.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/grouper
  to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/grouper
Upgraded 0 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/jdbcSamples
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../conf
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.hibernate.properties
grouper.hibernate.properties: null@null
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
stderr: NOTE: database table/object structure (ddl) is up to date
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to resolve all group subjects (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.1.0, so you dont have to do this,
  but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.0, so you dont have to do this,
  but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.1, so you dont have to do this,
  but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: 

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[mchyzer@i2mibuild installer]$ 
```

## Grouper UI installer upgrade from v2.2.1 to v2.2.2

```
[mchyzer@i2mibuild installer]$ java -jar grouperInstaller.jar 
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/home/mchyzer/2.2.2/installer]: 
What do you want to upgrade?  api, ui, ws, or psp? [api]: 
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper API installed? /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1
Upgrading to grouper API version: 2.2.2
##################################
Download and build grouper packages
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.clientBinary-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.apiBinary-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar
Do you want to set gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]: 
##################################
Copying records from change log temp to change log with command:
  /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
...
stderr: Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: 260483 manifest version: 2.2.1.  However the jar detected is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/grouper/subject.jar, name: subject.jar size: 259419 manifest version: 2.2.1
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../conf
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
edu.internet2.middleware.grouper.GrouperSession: ad79d81852ae4afda35c7ee2e8c29689,'GrouperSystem','application'
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
loader ran successfully: Ran the changeLogTempToChangeLog daemon

##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/lib/grouper/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.client.base.properties
grouper.client.properties has 7 properties that can be removed since the values are the same in grouper.client.base.properties
Would you like to have the 7 redundant properties automatically removed from grouper.client.properties (t|f)? [t]: 
grouper.client.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.client.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/dist/lib/grouper.jar
##################################
Upgrading API config files
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.base.properties
grouper.hibernate.properties has 3 properties that can be removed since the values are the same in grouper.hibernate.base.properties
Would you like to have the 3 redundant properties automatically removed from grouper.hibernate.properties (t|f)? [t]: 
grouper.hibernate.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/conf/grouper.hibernate.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper-loader.base.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/subject.base.properties
You should compare /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
  with /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_API_2015_09_23_19_41_03_668/lib/grouper/subject.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/grouper
  to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/grouper
Upgraded 0 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/lib/jdbcSamples
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../conf
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/bin/../logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/grouper.hibernate.properties
grouper.hibernate.properties: null@null
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.1/conf/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
stderr: NOTE: database table/object structure (ddl) is up to date
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to resolve all group subjects (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.1.0, so you dont have to do this,
  but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.0, so you dont have to do this,
  but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.1, so you dont have to do this,
  but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: 

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[mchyzer@i2mibuild installer]$ 
[mchyzer@i2mibuild installer]$ java -jar grouperInstaller.jar 
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/home/mchyzer/2.2.2/installer]: 
What do you want to upgrade?  api, ui, ws, or psp? [api]: ui
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper UI installed? /home/mchyzer/2.2.2/installer/grouperUi
Upgrading to grouper UI version: 2.2.2
##################################
Download and build grouper packages
File exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2, use untarred dir (t|f)? [t]: 
File exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2, use untarred dir (t|f)? [t]: 
Do you want to set gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.ui-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2.tar
Copying file: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/build.properties.template to file: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/build.properties
Editing /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/build.properties: 
 - set property: grouper.folder from: ../grouper to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2
 - property should.copy.context.xml.to.metainf already was set to: false, not changing file
Downloading from URL: http://software.internet2.edu/grouper/downloads/tools/apache-ant-1.8.2-bin.tar.gz to file: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2-bin.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2-bin.tar.gz
Untarred dir exists: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2, use untarred dir (t|f)? [t]: 
Using shell command: bash
##################################
Building UI with command:
/home/mchyzer/2.2.2/installer/grouper.ui-2.2.2> bash /home/mchyzer/2.2.2/installer/apache-ant-1.8.2/bin/ant dist
.....
stdout: Buildfile: build.xml
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/conf
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/conf/grouperText
dist:
-setup:
-choose-webapp:
[propertyfile] Creating new property file: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/.lastbuild.properties
     [echo] In setup - do.clean = true   cleanable=${webapp.folder.cleanable}
-doStop:
-doCleanWebappClassFolder:
     [echo] Removing  /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-doClean:
     [echo] Removing  /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
-resources:
     [echo] In resources - Build folder = /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
-dist-grouper:
     [echo] Creating  /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [echo] Copying Grouper configuration files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 18 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-local-log4j:
-fix-grouper-home:
     [echo] Attempting to replace grouper.home with /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/
     [echo] Copying ui resources to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 7 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 3 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-additional-build:
-optional-conf:
-webapp:
   [delete] Deleting directory /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
-compileGrouper:
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/jarBin
    [javac] Compiling 322 source files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/jarBin
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
      [jar] Building jar: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib/grouper-ui.jar
-additional-build:
     [copy] Copying 66 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [copy] Copying 5 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
-copyContent:
     [echo] Copying core UI files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1219 files to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
     [echo] Processing web.xml
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
     [echo] web.xmls.isempty=:${web.xmls.isempty}:
-merge-xmls:
     [echo] temp.dir : /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
     [echo] final.web.xmls : ${final.web.xmls}
     [echo] ui.folder : /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2
     [echo] webapp.folder : /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
     [copy] Copying 1 file to /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp
     [echo] Transforming: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo] /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo] /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo] 
     [echo] 
     [echo] Base = /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo]  + /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo]  -> /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/web.1.xml
     [echo] 
     [echo] Base = /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/web.1.xml
     [echo]  + /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo]  -> /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml
     [echo] Result: 0
-copy-core-web-xml:
-copyContextXmlToMetaInf:
-copyContextXmlToTomcat:
-html:
-war:
-web:
     [echo] ****************************************************
     [echo] ** The Grouper UI will fail to start if the user  **
     [echo] ** which your application server runs as does not **
     [echo] ** have permission to write to the log files that **
     [echo] ** are configured in log4j.properties. See        **
     [echo] ** build.properties for more information          **
     [echo] ****************************************************
BUILD SUCCESSFUL
Total time: 27 seconds

End building UI
##################################
What is the location of your tomcat server.xml for the UI?  Note, if you dont use tomcat just leave it blank or type 'blank': 
/home/mchyzer/2.2.2/installer/apache-tomcat-6.0.35/conf/server.xml
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]: 
##################################
Copying records from change log temp to change log with command:
  /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh.sh /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
...
stderr: Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: 260483 manifest version: 2.2.1.  However the jar detected is: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/lib/subject.jar, name: subject.jar size: 259419 manifest version: 2.2.1
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/../classes
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/apache-tomcat-6.0.35/logs/grouperUi/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
edu.internet2.middleware.grouper.GrouperSession: 59a3aebdb14041128d39315e228f4e32,'GrouperSystem','application'
loader ran successfully: Ran the changeLogTempToChangeLog daemon

##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/lib/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/grouper.client.base.properties
grouper.client.properties has 7 properties that can be removed since the values are the same in grouper.client.base.properties
Would you like to have the 7 redundant properties automatically removed from grouper.client.properties (t|f)? [t]: 
grouper.client.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/grouper.client.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/lib/grouper.jar
##################################
Upgrading API config files
Found no changes in /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper.base.properties
grouper.hibernate.properties has 3 properties that can be removed since the values are the same in grouper.hibernate.base.properties
Would you like to have the 3 redundant properties automatically removed from grouper.hibernate.properties (t|f)? [t]: 
grouper.hibernate.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/grouper.hibernate.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper-loader.base.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/subject.base.properties
Compare you old ehcache.xml with the new ehcache.xml file: 
  Old file: /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/ehcache.xml
  New file: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/ehcache.xml
  Press <enter> when done

You should compare /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/sources.xml
  with /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/lib/subject.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/grouper
  to: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/lib
Upgraded 0 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/lib
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin/../classes
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/apache-tomcat-6.0.35/logs/grouperUi/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: null@null
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
stderr: NOTE: database table/object structure (ddl) is up to date
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to resolve all group subjects (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.1.0, so you dont have to do this,
  but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.0, so you dont have to do this,
  but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.1, so you dont have to do this,
  but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: 

##################################
Upgrading UI

##################################
Upgrading UI jars
grouper-ui.jar had version null and size 1900112 bytes and is being upgraded to version null and size 1918497 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/lib/grouper-ui.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
  to: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/lib
##################################
Upgrading UI files
Upgrading files from: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/
  to: /home/mchyzer/2.2.2/installer/grouperUi/
  ignoring paths: WEB-INF/lib, WEB-INF/bin/gsh.sh, WEB-INF/web.xml, WEB-INF/bin/gsh, WEB-INF/bin/gsh.bat, WEB-INF/classes
Compared 1323 files and found 0 adds and 0 updates
Backing up: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/web.xml to: /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/web.xml
Copying new file: /home/mchyzer/2.2.2/installer/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml to: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/web.xml
If you customized the web.xml please merge your changes back in 
  New file: /home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/web.xml, bak file:/home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/web.xml
Press the <enter> key to continue

##################################
Upgrading UI config files
grouper.text.en.us.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
grouper-ui.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_UI_2015_09_23_19_57_39_215/WEB-INF/classes/grouper-ui.base.properties
/home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/Owasp.CsrfGuard.properties has not been updated so it was not changed
/home/mchyzer/2.2.2/installer/grouperUi/WEB-INF/classes/Owasp.CsrfGuard.overlay.properties has not been updated so it was not changed
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ui_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ui_patch_0.tar.gz
There are no new UI patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[mchyzer@i2mibuild installer]$ 
```

## Grouper WS installer upgrade from v2.2.1 to v2.2.2

```
[mchyzer@i2mibuild installer]$ java -jar grouperInstaller.jar 
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/home/mchyzer/2.2.2/installer]: 
What do you want to upgrade?  api, ui, ws, or psp? [api]: ws
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper WS installed? /home/mchyzer/2.2.2/installer/grouperWs
Upgrading to grouper WS version: 2.2.2
##################################
Download and build grouper packages
File exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: /home/mchyzer/2.2.2/installer/grouper.clientBinary-2.2.2, use untarred dir (t|f)? [t]: 
File exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2, use untarred dir (t|f)? [t]: 
Do you want to set gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.ws-2.2.2.tar.gz to file: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2.tar.gz
Unzipping: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2.tar.gz
Expanding: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2.tar
Editing /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build.properties: 
 - set property: grouper.dir from: ../grouper to: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2
File exists: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2-bin.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2-bin.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: /home/mchyzer/2.2.2/installer/apache-ant-1.8.2, use untarred dir (t|f)? [t]: 
The Grouper WS has been built in the past, do you want it rebuilt? (t|f) [t]: 
Using shell command: bash
##################################
Building WS with command:
/home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws> bash /home/mchyzer/2.2.2/installer/apache-ant-1.8.2/bin/ant dist
......
stdout: Buildfile: build.xml
checkGrouper:
dist:
distHelper:
compile:
    [javac] Compiling 208 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/src/grouper-ws/edu/internet2/middleware/grouper/ws/query/WsQueryFilterType.java uses unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
    [javac] Compiling 79 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 81 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 93 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 93 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
      [jar] Building jar: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws.jar
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 4 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
     [copy] Copying 24 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/log4j.example.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/log4j.example.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper-loader.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper-loader.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/README.txt to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/README.txt
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.example.xml to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/sources.example.xml
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/subject.base.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/subject.base.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.xml to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/sources.xml
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/server.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/server.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/spy.example.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/spy.example.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.client.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.client.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper-loader.base.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper-loader.base.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.base.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.base.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/morphString.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/morphString.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.hibernate.base.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.hibernate.base.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/log4j.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/log4j.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/subject.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/subject.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/server.example.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/server.example.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouperUtf8.txt to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouperUtf8.txt
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/ehcache.xml to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/ehcache.xml
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.client.base.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.client.base.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/ehcache.example.xml to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/ehcache.example.xml
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/grouper.hibernate.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.hibernate.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/morphString.example.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/morphString.example.properties
     [copy] Copying /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/spy.properties to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/spy.properties
     [copy] Copying 89 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 5 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 26 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws
     [move] Moving 1 file to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
      [jar] Building jar: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws.war
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
   [delete] Deleting directory /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [mkdir] Created dir: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [javac] Compiling 251 source files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [javac] Note: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws-java-generated-client/src/edu/internet2/middleware/grouper/webservicesClient/RampartPwHandlerClient.java uses or overrides a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws-java-generated-client/src/edu/internet2/middleware/grouper/webservicesClient/GrouperServiceStub.java uses unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
     [copy] Copying 255 files to /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
      [jar] Building jar: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws-soap-client.jar
BUILD SUCCESSFUL
Total time: 32 seconds

End building Ws
##################################
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]: 
Making sure gsh.sh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: 
Making sure gsh.sh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh.sh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh
stderr: dos2unix: converting file /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]: 
##################################
Copying records from change log temp to change log with command:
  /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh.sh /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
...
stderr: Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: 260483 manifest version: 2.2.1.  However the jar detected is: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/lib/subject.jar, name: subject.jar size: 259419 manifest version: 2.2.1
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/../classes
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/apache-tomcat-6.0.35/logs/grouperWs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
edu.internet2.middleware.grouper.GrouperSession: 1a762b5f9e6a4828911a9e5fdf5b5915,'GrouperSystem','application'
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
loader ran successfully: Ran the changeLogTempToChangeLog daemon

##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/lib/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/classes/grouper.client.base.properties
grouper.client.properties has 7 properties that can be removed since the values are the same in grouper.client.base.properties
Would you like to have the 7 redundant properties automatically removed from grouper.client.properties (t|f)? [t]: 
grouper.client.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/classes/grouper.client.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/lib/grouper.jar
##################################
Upgrading API config files
Found no changes in /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper.base.properties
grouper.hibernate.properties has 3 properties that can be removed since the values are the same in grouper.hibernate.base.properties
Would you like to have the 3 redundant properties automatically removed from grouper.hibernate.properties (t|f)? [t]: 
grouper.hibernate.properties had redundant properties removed after being backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/classes/grouper.hibernate.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper-loader.base.properties
Found no changes in /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/subject.base.properties
You should compare /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/sources.xml
  with /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/lib/subject.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/grouper
  to: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/lib
Upgraded 0 jar files from: /home/mchyzer/2.2.2/installer/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/lib
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/..
Using GROUPER_CONF: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin/../classes
Using JAVA: /home/mchyzer/software/java/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper.properties
Grouper current directory is: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/bin
log4j.properties read from:   /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /home/mchyzer/2.2.2/installer/apache-tomcat-6.0.35/logs/grouperWs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: null@null
sources.xml read from:        /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'Ù¹ÙºÙ»Ù¼ÙƒÙ„Ù„'
stderr: NOTE: database table/object structure (ddl) is up to date
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.0.0, so you dont have to do this,
  but do you want to resolve all group subjects (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.1.0, so you dont have to do this,
  but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.0, so you dont have to do this,
  but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: 
You are upgrading from after API version 2.2.1, so you dont have to do this,
  but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: 

##################################
Upgrading WS

##################################
Upgrading WS jars
grouper-ws.jar had version null and size 1846208 bytes and is being upgraded to version null and size 1851694 bytes.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/lib/grouper-ws.jar
Upgraded 1 jar files from: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
  to: /home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/lib
##################################
Upgrading WS files
Upgrading files from: /home/mchyzer/2.2.2/installer/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/
  to: /home/mchyzer/2.2.2/installer/grouperWs/
  ignoring paths: WEB-INF/lib, WEB-INF/bin/gsh.sh, WEB-INF/web.xml, WEB-INF/bin/gsh, WEB-INF/bin/gsh.bat, WEB-INF/classes
Compared 148 files and found 0 adds and 8 updates
8 files were backed up to: /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/
WEB-INF/services/GrouperServiceWssec_v2_1.aar.isondeck was updated
WEB-INF/services/GrouperService_v2_2.aar was updated
WEB-INF/services/GrouperServiceWssec_v2_0.aar.isondeck was updated
WEB-INF/services/GrouperService.aar was updated
WEB-INF/services/GrouperServiceWssec.aar.ondeck was updated
WEB-INF/services/GrouperServiceWssec_v2_2.aar.isondeck was updated
WEB-INF/services/GrouperService_v2_1.aar was updated
WEB-INF/services/GrouperService_v2_0.aar was updated
/home/mchyzer/2.2.2/installer/grouperWs/WEB-INF/web.xml has not been updated so it was not changed
##################################
Upgrading WS config files
grouper-ws.base.properties has changes and was upgraded.
  It is backed up to /home/mchyzer/2.2.2/installer/bak_WS_2015_09_23_20_07_31_807/WEB-INF/classes/grouper-ws.base.properties
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ws_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ws_patch_0.tar.gz
There are no new WS patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[mchyzer@i2mibuild installer]$ 
```
