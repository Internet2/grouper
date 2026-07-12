---
title: "Grouper ehcache.xml conversion to grouper.cache.properties"
space: Grouper
pageId: 28554172
version: 9
lastUpdated: 2026-07-01T05:41:00.356Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554172/Grouper+ehcache.xml+conversion+to+grouper.cache.properties
---

In grouper 2.3.0 (unpatched) and previous versions, ehcache was configured with an ehcache.xml (actually in old versions of grouper there were a couple ehcache xml config files).

## Configuration

Sample ehcache.xml config

```
  <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.FindBySubject"
          maxElementsInMemory="5000"
          eternal="false"
          timeToIdleSeconds="5"
          timeToLiveSeconds="10"
          overflowToDisk="false"  
          statistics="false"
  />
```

This needs to be converted to config overlays so Grouper packaging can have defaults and overlays. Also so the Grouper team can add caches and be able to patch the defaults without having to merge with a potentially changed file.

Sample grouper.cache.base.properties config:

```
cache.name.internal_dao_hib3_Hib3GroupDAO.name = edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO
cache.name.internal_dao_hib3_Hib3GroupDAO.maxElementsInMemory = 500
cache.name.internal_dao_hib3_Hib3GroupDAO.eternal = false
cache.name.internal_dao_hib3_Hib3GroupDAO.timeToIdleSeconds = 1
cache.name.internal_dao_hib3_Hib3GroupDAO.timeToLiveSeconds = 1
cache.name.internal_dao_hib3_Hib3GroupDAO.overflowToDisk = false
```

Note, the part that links the properties entries together is the part between "name" and the following dot. In this case "internal_dao_hib3_Hib3GroupDAO". This was automatically converted from the ehcache.xml file by looking at the cache name and chopping off the prefix "edu.internet2.middleware.grouper", though thats not important, it could be "abc" and it would work. However, we should follow this convention going forward.

## Use the xml configuration

If you want to use an xml configuration (generally you wont need to do this) set one of these in the grouper.cache.properties:

```
########################################
## Use old ehcache.xml
########################################

# set ehcache.xml to a filename
grouper.cache.ehcache.xml.filename =

# set ehcache.xml to a resource on the classpath (generally not ehcache.xml)
grouper.cache.ehcache.xml.resource =

```

## Upgrade via Patch

- If you have previously edited the ehcache.xml file
  
  - Get the latest grouperInstaller.jar
  - Backup your ehcache.xml and ehcache.example.xml file
  - Install the 2.3.0#35 API patch (note: Force install that patch since it will say file mismatch)
  - Copy your ehcache.xml and ehcache.example.xml files back to the classpath
  - Delete the empty grouper.cache.properties file
  - Run the grouperInstaller, admin task, upgradeTasks, convert ehcache xml to properties
  - Follow the prompt to identify the location of the ehcache.xml file
  - This will generate the grouper.cache.properties file based on your ehcache.xml edits
  - Examine the grouper.cache.properties file. Diff your ehcache.xml file with your ehcache.example.xml file and make sure those diffs are expected and listed in your grouper.cache.properties file
  - Delete the ehcache.xml and ehcache.example.xml files
  - Force install the patch in your other envs (UI, WS, daemon, etc), and copy the grouper.cache.properties to each env (no need to go through this same process unless you have different cache customizations in each env)

- If you havent edited the ehcache.xml file
  
  - If you have the latest 2.3.0 grouperInstaller as of 11/26/2016
    
    - Install patch 2.3.0#35
    - You will be converted and upgraded
  - If you dont have the latest 2.3.0 grouperInstaller as of 11/26/2016
    
    - Get the latest installer, but if you dont
    - Install patch 2.3.0#35
    - Delete the ehcache.xml and ehcache.example.xml files
    - Until you delete those files you will get a non fatal error on startup reminding you to delete those files

## Upgrade via upgrade

If you upgrade to 2.3.1+ it will walk you through the process of automatically converting your ehcache.xml to grouper.cache.properties

## Upgrade via patch with no ehcache.xml customizations and latest installer

```
Patch grouper_v2_3_0_api_patch_35 is low risk, is not a security patch
GRP-1417: migrate from grouper.ehcache.xml to hierarchical properties configuration
 - set property: grouper_v2_3_0_api_patch_35.date from: 2016/11/27 18:51:45 to: 2016/11/27 18:54:11
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable and grouperInstaller.autorun.continueAfterPatchStopProcesses>: 
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.base.properties
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties
Deleting file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml
Deleting file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
Patch successfully applied: grouper_v2_3_0_api_patch_35
 - set property: grouper_v2_3_0_api_patch_35.state to: applied
```

## Upgrade via patch with no ehcache.xml customizations and old installer

```
Patch grouper_v2_3_0_api_patch_35 is low risk, is not a security patch
GRP-1417: migrate from grouper.ehcache.xml to hierarchical properties configuration
 - set property: grouper_v2_3_0_api_patch_35.date from: 2016/11/27 16:46:03 to: 2016/11/27 16:48:06
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable and grouperInstaller.autorun.continueAfterPatchStopProcesses>: 
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.base.properties
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties
Patch successfully applied: grouper_v2_3_0_api_patch_35
 - set property: grouper_v2_3_0_api_patch_35.state from: reverted to: applied
 
 
[appadmin@i2midev1 patchesAuto]$ rm -v /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.*
removed `/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml'
removed `/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml'
[appadmin@i2midev1 patchesAuto]$ 
 
 
```

## Upgrade via patch with ehcache.xml customizations and new grouperInstaller

Note that patching will fail

```
Patch grouper_v2_3_0_api_patch_35 is low risk, is not a security patch
GRP-1417: migrate from grouper.ehcache.xml to hierarchical properties configuration
 - set property: grouper_v2_3_0_api_patch_35.date from: 2016/11/27 19:02:25 to: 2016/11/27 19:05:16
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable and grouperInstaller.autorun.continueAfterPatchStopProcesses>: 
Problem applying patch since this patch old file:
  /opt/grouper/2.3/patchesAuto/tarballs/patches/grouper_v2_3_0_api_patch_35/old/classes/ehcache.xml
  is not the same as what the patch expects:
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
  Do you want to force install this patch (t|f)? [f]: 
<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable and grouperInstaller.autorun.forceInstallPatch>: 
Cannot apply patch since this patch file:
  /opt/grouper/2.3/patchesAuto/tarballs/patches/grouper_v2_3_0_api_patch_35/old/classes/ehcache.xml
  is not the same as what the patch expects:
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
 - set property: grouper_v2_3_0_api_patch_35.state to: error
```

Get the latest grouperInstaller.jar

```
[appadmin@i2midev1 patchesAuto]$ rm grouperInstaller.jar
[appadmin@i2midev1 patchesAuto]$ wget --no-check-certificate https://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
```

Backup your ehcache.xml and ehcache.example.xml file

```
[appadmin@i2midev1 patchesAuto]$ cp /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml /tmp
[appadmin@i2midev1 patchesAuto]$ cp /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml /tmp
```

Install the 2.3.0#35 API patch (note: Force install that patch since it will say file mismatch)

```
Patch grouper_v2_3_0_api_patch_35 is low risk, is not a security patch
GRP-1417: migrate from grouper.ehcache.xml to hierarchical properties configuration
 - set property: grouper_v2_3_0_api_patch_35.date from: 2016/11/27 19:05:16 to: 2016/11/27 19:12:08
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...

Problem applying patch since this patch old file:
  /opt/grouper/2.3/patchesAuto/tarballs/patches/grouper_v2_3_0_api_patch_35/old/classes/ehcache.xml
  is not the same as what the patch expects:
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
  Do you want to force install this patch (t|f)? [f]: 
t
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.base.properties
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cfg/GrouperCacheConfig.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.java
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/edu/internet2/middleware/grouper/cache/EhcacheController.class
Applying file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties
Deleting file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml
Deleting file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
Patch successfully applied: grouper_v2_3_0_api_patch_35
 - set property: grouper_v2_3_0_api_patch_35.state from: error to: applied
```

Copy your ehcache.xml and ehcache.example.xml files back to the classpath

```
[appadmin@i2midev1 patchesAuto]$ cp /tmp/ehcache.xml /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes
[appadmin@i2midev1 patchesAuto]$ cp /tmp/ehcache.example.xml /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes
```

Delete the empty grouper.cache.properties file

```
[appadmin@i2midev1 patchesAuto]$ more /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties 
#
# Copyright 2016 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Grouper Cache Configuration
# $Id: grouper.hibernate.example.properties,v 1.9 2009-08-11 20:18:09 mchyzer Exp $
#

# The grouper cache config uses Grouper Configuration Overlays (documented on wiki)
# By default the configuration is read from grouper.cache.base.properties
# (which should not be edited), and the grouper.cache.properties overlays
# the base settings.  See the grouper.cache.base.properties for the possible
# settings that can be applied to the grouper.cache.properties
[appadmin@i2midev1 patchesAuto]$ rm /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties  
[appadmin@i2midev1 patchesAuto]$ 
```

Run the grouperInstaller, admin task, upgradeTasks, convert ehcache xml to properties

Follow the prompt to identify the location of the ehcache.xml file

This will generate the grouper.cache.properties file based on your ehcache.xml edits

```
[appadmin@i2midev1 patchesAuto]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [install]: admin
What admin action do you want to do (manage, upgradeTask)? : upgradeTask
What upgrade task do you want to do (convertEhcacheXmlToProperties)? : convertEhcacheXmlToProperties
Note, you need to convert the ehcache.xml file for each Grouper runtime, e.g. loader, WS, UI.
Note, you need to be running Grouper 2.3.0 with API patch 35 installed.
Enter the location of the ehcache.xml file: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
Enter the location of the grouper.cache.base.properties file [/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.base.properties]: 
Enter the location of the grouper.cache.properties file (to be created)  [/opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties]: 
File was written: /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties
```

Examine the grouper.cache.properties file. Diff your ehcache.xml file with your ehcache.example.xml file and make sure those diffs are expected and listed in your grouper.cache.properties file

```
[appadmin@i2midev1 patchesAuto]$ more /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/grouper.cache.properties
# Copyright 2016 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Grouper Cache Configuration
#

# The grouper cache config uses Grouper Configuration Overlays (documented on wiki)
# By default the configuration is read from grouper.cache.base.properties
# (which should not be edited), and the grouper.cache.properties overlays
# the base settings.  See the grouper.cache.base.properties for the possible
# settings that can be applied to the grouper.cache.properties

cache.name.internal_dao_hib3_Hib3MemberDAO_FindBySubject.maxElementsInMemory = 10000

cache.name.internal_dao_hib3_Hib3MembershipDAO.maxElementsInMemory = 20000
cache.name.internal_dao_hib3_Hib3MembershipDAO.timeToIdleSeconds = 60
cache.name.internal_dao_hib3_Hib3MembershipDAO.timeToLiveSeconds = 60

[appadmin@i2midev1 patchesAuto]$ diff /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml
90c90
<           maxElementsInMemory="10000"
---
>           maxElementsInMemory="5000"
101c101
<           maxElementsInMemory="20000"
---
>           maxElementsInMemory="10000"
103,104c103,104
<           timeToIdleSeconds="60"
<           timeToLiveSeconds="60"
---
>           timeToIdleSeconds="5"
>           timeToLiveSeconds="5"
[appadmin@i2midev1 patchesAuto]$ 
```

Delete the ehcache.xml and ehcache.example.xml files

```
[appadmin@i2midev1 patchesAuto]$ rm /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.xml
[appadmin@i2midev1 patchesAuto]$ rm /opt/tomcats/tomcat_f/webapps/grouper_v2_3/WEB-INF/classes/ehcache.example.xml 
```

Force install the patch in your other envs (UI, WS, daemon, etc), and copy the grouper.cache.properties to each env (no need to go through this same process unless you have different cache customizations in each env

## Logging

Add logging to see whats going on

```
log4j.logger.edu.internet2.middleware.grouper.cache.EhcacheController = DEBUG
```
