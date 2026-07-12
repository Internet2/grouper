---
title: "Grouper v2.2.2 upgrade from v2.2.1 on the demo server"
space: GrIntDev
pageId: 48795865
version: 8
lastUpdated: 2026-07-12T06:46:32.984Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795865/Grouper+v2.2.2+upgrade+from+v2.2.1+on+the+demo+server
---

Upgrade the demo server to 2.2.2 using the upgrader

## UI upgrade

```
[mchyzer@i2midev1 ~]$ cd /tmp
[mchyzer@i2midev1 tmp]$ mkdir /tmp/grouper_bak
[mchyzer@i2midev1 tmp]$ cp -R /opt/tomcats/tomcat_d/webapps/grouper_v2_2 /tmp/grouper_bak
[mchyzer@i2midev1 tmp]$ /sbin/service tomcat_d stop
Shutting down tomcat_d Tomcat services: 
Using CATALINA_BASE:   /opt/tomcats/tomcat_d
Using CATALINA_HOME:   /opt/tomcat6base
Using CATALINA_TMPDIR: /opt/tomcats/tomcat_d/temp
Using JRE_HOME:        /opt/javas/java_d
Using CLASSPATH:       /opt/tomcat6base/bin/bootstrap.jar
Waiting for exit...
Waiting for exit...
Waiting for exit...

[appadmin@i2midev1 tmp]$ mkdir /tmp/grouper2.2.2
[appadmin@i2midev1 tmp]$ cd /tmp/grouper2.2.2
[appadmin@i2midev1 tmp]$ ls
grouper2.2                 grouper_bak                    grouper_ehcache_auto_SP1MC1DQ  grouper_ehcache_auto_SP1MJUL3  hsperfdata_root
grouper2.2.2               grouper_bak2                   grouper_ehcache_auto_SP1MJUL1  hsperfdata_appadmin            ssh-usIUK23687
grouper_2_2_autopatch.out  grouper_ehcache_auto_SP1MC1DP  grouper_ehcache_auto_SP1MJUL2  hsperfdata_mchyzer             sudoers
[appadmin@i2midev1 tmp]$ cd grouper2.2.2
[appadmin@i2midev1 grouper2.2.2]$ ls
grouper.apiBinary-2.2.2      grouper.apiBinary-2.2.2.tar.gz  grouper.clientBinary-2.2.2.tar     grouperInstaller.jar
grouper.apiBinary-2.2.2.tar  grouper.clientBinary-2.2.2      grouper.clientBinary-2.2.2.tar.gz
[appadmin@i2midev1 grouper2.2.2]$ rm -rf *
[appadmin@i2midev1 grouper2.2.2]$ history | wget
wget: missing URL
Usage: wget [OPTION]... [URL]...
Try `wget --help' for more options.
[appadmin@i2midev1 grouper2.2.2]$ history | grep wget
  311  wget http://software.internet2.edu/grouper/release/2.2.1/grouperInstaller.jar
  321  wget http://software.internet2.edu/grouper/release/2.2.1/grouper.vootBinary-2.2.1.tar.gz
  379  wget http://software.internet2.edu/grouper/release/2.2.1/grouperInstaller.jar
  431  wget http://software.internet2.edu/grouper/release/2.2.1/grouper.vootBinary-2.2.1.tar.gz
  773  wget http://software.internet2.edu/grouper/release/2.2.1/grouperInstaller.jar
  986  wget http://software.internet2.edu/grouper/release/2.2.2/grouperInstaller.jar
  994  history | grep wget
  995   wget http://software.internet2.edu/grouper/release/2.2.2/grouperInstaller.jar
 1003  history | grep wget
 1004   wget http://software.internet2.edu/grouper/release/2.2.2/grouperInstaller.jar
 1018  history | wget
 1019  history | grep wget
[appadmin@i2midev1 grouper2.2.2]$  wget http://software.internet2.edu/grouper/release/2.2.2/grouperInstaller.jar
--2015-10-15 16:05:46--  http://software.internet2.edu/grouper/release/2.2.2/grouperInstaller.jar
Resolving software.internet2.edu... 207.75.164.52, 2001:48a8:68fe::52
Connecting to software.internet2.edu|207.75.164.52|:80... connected.
HTTP request sent, awaiting response... 200 OK
Length: 2331173 (2.2M) [application/x-java-archive]
Saving to: `grouperInstaller.jar'
100%[=========================================================================================================>] 2,331,173   12.2M/s   in 0.2s
2015-10-15 16:05:47 (12.2 MB/s) - `grouperInstaller.jar' saved [2331173/2331173]
[appadmin@i2midev1 grouper2.2.2]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/tmp/grouper2.2.2]:
What do you want to upgrade?  api, ui, ws, or psp? [api]: ui
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper UI installed? /opt/tomcats/tomcat_d/webapps/grouper_v2_2//opt/tomcats/tomcat_d/webapps/grouper_v2_2/
Error: cant find directory: '/opt/tomcats/tomcat_d/webapps/grouper_v2_2/opt/tomcats/tomcat_d/webapps/grouper_v2_2'
Where is the grouper UI installed? /opt/tomcats/tomcat_d/webapps/grouper_v2_2/
Upgrading to grouper UI version: 2.2.2
Do you want to fix the patch index file (download all patches and see if they are installed?) (recommended) (t|f)? [t]:
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_0.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_1.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_2.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_3.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_4.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_5.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_6.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_7.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_8.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_9.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_10.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_11.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_12.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_13.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_14.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_15.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_16.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_17.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_18.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_19.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_20.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_21.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_22.tar.gz
 - set property: grouper_v2_2_1_ui_patch_3.date from: 2015/03/22 12:46:02 to: 2015/10/15 16:07:10
 - set property: grouper_v2_2_1_ui_patch_3.state from: applied to: skippedTemporarily
Patch grouper_v2_2_1_ui_patch_3 was listed as applied but was changed to skippedTemporarily
 - added to end of property file: grouperInstallerLastFixedIndexFile.date = 2015/10/15 16:07:10
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_0.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_1.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_2.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_3.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_4.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_5.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_6.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_7.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_8.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_9.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_10.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_11.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_12.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_13.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_14.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_15.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_16.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_17.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_18.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_19.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_20.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_21.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_22.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_23.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_24.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_25.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_26.tar.gz
 - set property: grouperInstallerLastFixedIndexFile.date from: 2015/10/15 16:07:10 to: 2015/10/15 16:07:51
Patches for API for version 2_2_1 were in the index file correctly
##################################
Download and build grouper packages
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.clientBinary-2.2.2.tar.gz to file: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.apiBinary-2.2.2.tar.gz to file: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar
Do you want to set gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]:
Making sure gsh.sh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.ui-2.2.2.tar.gz to file: /tmp/grouper2.2.2/grouper.ui-2.2.2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper.ui-2.2.2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper.ui-2.2.2.tar
Copying file: /tmp/grouper2.2.2/grouper.ui-2.2.2/build.properties.template to file: /tmp/grouper2.2.2/grouper.ui-2.2.2/build.properties
Editing /tmp/grouper2.2.2/grouper.ui-2.2.2/build.properties:
 - set property: grouper.folder from: ../grouper to: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2
 - property should.copy.context.xml.to.metainf already was set to: false, not changing file
Downloading from URL: http://software.internet2.edu/grouper/downloads/tools/apache-ant-1.8.2-bin.tar.gz to file: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar.gz
Unzipping: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar.gz
Expanding: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar
Using shell command: bash
##################################
Building UI with command:
/tmp/grouper2.2.2/grouper.ui-2.2.2> bash /tmp/grouper2.2.2/apache-ant-1.8.2/bin/ant dist
.....
stdout: Buildfile: build.xml
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/conf
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/conf/grouperText
dist:
-setup:
-choose-webapp:
[propertyfile] Creating new property file: /tmp/grouper2.2.2/grouper.ui-2.2.2/.lastbuild.properties
     [echo] In setup - do.clean = true   cleanable=${webapp.folder.cleanable}
-doStop:
-doCleanWebappClassFolder:
     [echo] Removing  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-doClean:
     [echo] Removing  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
-resources:
     [echo] In resources - Build folder = /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
-dist-grouper:
     [echo] Creating  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [echo] Copying Grouper configuration files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 18 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-local-log4j:
-fix-grouper-home:
     [echo] Attempting to replace grouper.home with /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/
     [echo] Copying ui resources to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 7 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 3 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-additional-build:
-optional-conf:
-webapp:
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
-compileGrouper:
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/jarBin
    [javac] Compiling 322 source files to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/jarBin
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
      [jar] Building jar: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib/grouper-ui.jar
-additional-build:
     [copy] Copying 66 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [copy] Copying 5 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
-copyContent:
     [echo] Copying core UI files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1219 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [echo] Processing web.xml
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] web.xmls.isempty=:${web.xmls.isempty}:
-merge-xmls:
     [echo] temp.dir : /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] final.web.xmls : ${final.web.xmls}
     [echo] ui.folder : /tmp/grouper2.2.2/grouper.ui-2.2.2
     [echo] webapp.folder : /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] Transforming: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo] /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo] /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo]
     [echo]
     [echo] Base = /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo]  + /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo]  -> /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/web.1.xml
     [echo]
     [echo] Base = /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/web.1.xml
     [echo]  + /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo]  -> /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml
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
Total time: 24 seconds

End building UI
##################################
What is the location of your tomcat server.xml for the UI?  Note, if you dont use tomcat just leave it blank or type 'blank':
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]:
Making sure gsh.sh is in unix format: dos2unix /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh
stderr: dos2unix: converting file /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh
stderr: dos2unix: converting file /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]:
##################################
Copying records from change log temp to change log with command:
  /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
...
stderr: Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: 260483 manifest version: 2.2.1.  However the jar detected is: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/lib/subject.jar, name: subject.jar size: 259419 manifest version: 2.2.1
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_d/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
Error: Cannot properly read UTF string from resource: grouperUtf8.txt: 'ٹٺٻټكلل'
edu.internet2.middleware.grouper.GrouperSession: 85b3aa40f14340c384c186201d68cc10,'GrouperSystem','application'
loader ran successfully: Ran the changeLogTempToChangeLog daemon
You need to revert all patches to upgrade
################ Checking patch grouper_v2_2_1_ui_patch_21
Patch: grouper_v2_2_1_ui_patch_21: was applied on: 2015/08/16 22:13:18
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21, use untarred dir (t|f)? [t]:
Would you like to revert all patches (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_21 is high risk, is not a security patch
GRP-1089: add grouper admin groups for readonly and viewonly
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LowLevelGrouperCapableAction.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LowLevelGrouperCapableAction.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_21
 - set property: grouper_v2_2_1_ui_patch_21.date from: 2015/08/16 22:13:18 to: 2015/10/15 16:09:35
 - set property: grouper_v2_2_1_ui_patch_21.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_20
Patch: grouper_v2_2_1_ui_patch_20: was applied on: 2015/07/19 20:47:05
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_20 is low risk, is not a security patch
GRP-1152: my group memberships hitting enter doesnt work
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/myGroups/myGroupsMemberships.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_20
 - set property: grouper_v2_2_1_ui_patch_20.date from: 2015/07/19 20:47:05 to: 2015/10/15 16:09:36
 - set property: grouper_v2_2_1_ui_patch_20.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_19
Patch: grouper_v2_2_1_ui_patch_19: was applied on: 2015/07/13 15:30:41
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_19 is low risk, is not a security patch
GRP-1138: add import / export auditing
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport$GrouperImportException.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GuiAuditEntry$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GuiAuditEntry.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GuiAuditEntry.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_19
 - set property: grouper_v2_2_1_ui_patch_19.date from: 2015/07/13 15:30:41 to: 2015/10/15 16:09:37
 - set property: grouper_v2_2_1_ui_patch_19.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_18
Patch: grouper_v2_2_1_ui_patch_18: was applied on: 2015/06/22 08:08:01
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_18 is low risk, is not a security patch
GRP-1137: Group copy with new group extension
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_18
 - set property: grouper_v2_2_1_ui_patch_18.date from: 2015/06/22 08:08:01 to: 2015/10/15 16:09:37
 - set property: grouper_v2_2_1_ui_patch_18.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_17
Patch: grouper_v2_2_1_ui_patch_17: was applied on: 2015/06/03 07:57:04
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_17 is low risk, is not a security patch
This patch fixes GRP-1107 default stem for new ui
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Main$4.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_17
 - set property: grouper_v2_2_1_ui_patch_17.date from: 2015/06/03 07:57:04 to: 2015/10/15 16:09:38
 - set property: grouper_v2_2_1_ui_patch_17.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_16
Patch: grouper_v2_2_1_ui_patch_16: was applied on: 2015/05/04 05:03:07
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_16 is low risk, is not a security patch
GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$RetrieveStemHelperResult.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$StemSearchType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_16
 - set property: grouper_v2_2_1_ui_patch_16.date from: 2015/05/04 05:03:07 to: 2015/10/15 16:09:40
 - set property: grouper_v2_2_1_ui_patch_16.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_15
Patch: grouper_v2_2_1_ui_patch_15: was applied on: 2015/04/29 05:35:15
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_15 is low risk, is not a security patch
GRP-1133: this groups memberships in new ui fails if effective only memberships and wont remove memberships
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.java
Patch successfully reverted: grouper_v2_2_1_ui_patch_15
 - set property: grouper_v2_2_1_ui_patch_15.date from: 2015/04/29 05:35:15 to: 2015/10/15 16:09:41
 - set property: grouper_v2_2_1_ui_patch_15.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_14
Patch: grouper_v2_2_1_ui_patch_14: was applied on: 2015/04/21 01:04:57
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_14 is low risk, is not a security patch
GRP-1131: add button to UI for loader group admins to refresh the group from the system of record
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_14
 - set property: grouper_v2_2_1_ui_patch_14.date from: 2015/04/21 01:04:57 to: 2015/10/15 16:09:42
 - set property: grouper_v2_2_1_ui_patch_14.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_13
Patch: grouper_v2_2_1_ui_patch_13: was applied on: 2015/04/19 15:54:58
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_13 is low risk, is not a security patch
This patch fixes GRP-1124 put composite info on membership list
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/viewGroup.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/grouperExternal/public/assets/css/grouperUi2.css
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_13
 - set property: grouper_v2_2_1_ui_patch_13.date from: 2015/04/19 15:54:58 to: 2015/10/15 16:09:42
 - set property: grouper_v2_2_1_ui_patch_13.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_12
Patch: grouper_v2_2_1_ui_patch_12: was applied on: 2015/03/22 12:46:11
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_12 is low risk, is not a security patch
GRP-1117: grouper new ui tooltips wrap
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/grouperExternal/public/assets/css/grouperUi2.css
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_12
 - set property: grouper_v2_2_1_ui_patch_12.date from: 2015/03/22 12:46:11 to: 2015/10/15 16:09:43
 - set property: grouper_v2_2_1_ui_patch_12.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_11
Patch: grouper_v2_2_1_ui_patch_11: was applied on: 2015/03/22 12:46:10
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_11 is low risk, is not a security patch
GRP-1111: if you leave a group via UI and leaving revokes view privs (or others), dont throw error
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_11
 - set property: grouper_v2_2_1_ui_patch_11.date from: 2015/03/22 12:46:10 to: 2015/10/15 16:09:45
 - set property: grouper_v2_2_1_ui_patch_11.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_10
Patch: grouper_v2_2_1_ui_patch_10: was applied on: 2015/03/22 12:46:09
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_10 is low risk, is not a security patch
GRP-1114: cant assign privs to composite group
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupHeader.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_10
 - set property: grouper_v2_2_1_ui_patch_10.date from: 2015/03/22 12:46:09 to: 2015/10/15 16:09:45
 - set property: grouper_v2_2_1_ui_patch_10.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_9
Patch: grouper_v2_2_1_ui_patch_9: was applied on: 2015/03/22 12:46:08
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_9 is high risk, is a security patch
GRP-1112: problems with 'edit memberships and privileges' button
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/viewGroup.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/editMembership.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/subject/subjectContents.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/externalEntities/invite.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_9
 - set property: grouper_v2_2_1_ui_patch_9.date from: 2015/03/22 12:46:08 to: 2015/10/15 16:09:46
 - set property: grouper_v2_2_1_ui_patch_9.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_8
Patch: grouper_v2_2_1_ui_patch_8: was applied on: 2015/03/22 12:46:07
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_8 is low risk, is a security patch
GRP-1109 problems with inherited privileges rule
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/jsp/stemLinks.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/SaveStemAction.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/SaveStemAction.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_8
 - set property: grouper_v2_2_1_ui_patch_8.date from: 2015/03/22 12:46:07 to: 2015/10/15 16:09:46
 - set property: grouper_v2_2_1_ui_patch_8.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_7
Patch: grouper_v2_2_1_ui_patch_7: was applied on: 2015/03/22 12:46:06
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_7 is medium risk, is a security patch
This patch fixes GRP-1100 grouper new ui not showing unresolvable subjects correctly
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_7
 - set property: grouper_v2_2_1_ui_patch_7.date from: 2015/03/22 12:46:06 to: 2015/10/15 16:09:47
 - set property: grouper_v2_2_1_ui_patch_7.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_6
Patch: grouper_v2_2_1_ui_patch_6: was applied on: 2015/03/22 12:46:05
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_6 is low risk, is a security patch
This patch fixes GRP-1097 grouper logout management
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/index/index.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LogoutAction.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LogoutAction.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/Misc.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/Misc.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_6
 - set property: grouper_v2_2_1_ui_patch_6.date from: 2015/03/22 12:46:05 to: 2015/10/15 16:09:47
 - set property: grouper_v2_2_1_ui_patch_6.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_5
Patch: grouper_v2_2_1_ui_patch_5: was applied on: 2015/03/22 12:46:04
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_5 is low risk, is not a security patch
This patch fixes GRP-1088: attribute def left menu link throws error
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/grouperExternal/public/assets/js/grouperUi.js
Patch successfully reverted: grouper_v2_2_1_ui_patch_5
 - set property: grouper_v2_2_1_ui_patch_5.date from: 2015/03/22 12:46:04 to: 2015/10/15 16:09:48
 - set property: grouper_v2_2_1_ui_patch_5.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_4
Patch: grouper_v2_2_1_ui_patch_4: was applied on: 2015/03/22 12:46:03
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_4 is low risk, is not a security patch
This patch fixes GRP-1087: edit membership page shows large H as icon in title
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceAttributeDefPrivileges.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/editMembership.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceMembership.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceStemPrivileges.jsp
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/tracePrivileges.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_4
 - set property: grouper_v2_2_1_ui_patch_4.date from: 2015/03/22 12:46:03 to: 2015/10/15 16:09:48
 - set property: grouper_v2_2_1_ui_patch_4.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_3
Patch: grouper_v2_2_1_ui_patch_3: was skipped termporarily on: 2015/10/15 16:07:10

################ Checking patch grouper_v2_2_1_ui_patch_2
Patch: grouper_v2_2_1_ui_patch_2: was applied on: 2015/03/22 12:46:01
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_2 is low risk, is not a security patch
This patch fixes GRP-1086: extra system out print in ui
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig.java
Patch successfully reverted: grouper_v2_2_1_ui_patch_2
 - set property: grouper_v2_2_1_ui_patch_2.date from: 2015/03/22 12:46:01 to: 2015/10/15 16:09:49
 - set property: grouper_v2_2_1_ui_patch_2.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_1
Patch: grouper_v2_2_1_ui_patch_1: was applied on: 2015/03/22 12:46:00
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_1 is low risk, is not a security patch
This patch fixes GRP-1082: grouper paging tag2 has one word not externalized
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/tags/GrouperPagingTag2.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/tags/GrouperPagingTag2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_1
 - set property: grouper_v2_2_1_ui_patch_1.date from: 2015/03/22 12:46:00 to: 2015/10/15 16:09:50
 - set property: grouper_v2_2_1_ui_patch_1.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_0
Patch: grouper_v2_2_1_ui_patch_0: was applied on: 2015/03/22 12:45:59
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_0 is low risk, is not a security patch
This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/grouperUi2/index/index.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_0
 - set property: grouper_v2_2_1_ui_patch_0.date from: 2015/03/22 12:45:59 to: 2015/10/15 16:09:51
 - set property: grouper_v2_2_1_ui_patch_0.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_25
Patch: grouper_v2_2_1_api_patch_25: was applied on: 2015/08/30 20:21:28
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_25 is low risk, is not a security patch
GRP-1188: print an error if Grouper is used with invalid version of Java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_25
 - set property: grouper_v2_2_1_api_patch_25.date from: 2015/08/30 20:21:28 to: 2015/10/15 16:09:53
 - set property: grouper_v2_2_1_api_patch_25.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_24
Patch: grouper_v2_2_1_api_patch_24: was applied on: 2015/08/30 16:44:27
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_24 is low risk, is not a security patch
GRP-1183: status servlet gives error if loader job is not configured
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/j2ee/status/DiagnosticType$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_24
 - set property: grouper_v2_2_1_api_patch_24.date from: 2015/08/30 16:44:27 to: 2015/10/15 16:09:54
 - set property: grouper_v2_2_1_api_patch_24.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_23
Patch: grouper_v2_2_1_api_patch_23: was applied on: 2015/08/24 21:10:48
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_23 is medium risk, is not a security patch
GRP-1091: ldap loader display name doesnt change
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$DisplayProperties.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_23
 - set property: grouper_v2_2_1_api_patch_23.date from: 2015/08/24 21:10:48 to: 2015/10/15 16:09:55
 - set property: grouper_v2_2_1_api_patch_23.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_22
Patch: grouper_v2_2_1_api_patch_22: was applied on: 2015/08/16 22:13:03
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_22 is high risk, is not a security patch
GRP-1089: add grouper admin groups for readonly and viewonly
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAttrDefResolver$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAccessResolver$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAccessResolver$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelCache.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelNamingResolver.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAccessResolver$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelNamingResolver.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAttrDefResolver$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAttrDefResolver.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelNamingResolver$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAccessResolver.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAccessResolver.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAttrDefResolver.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelNamingResolver$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelCache.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelAttrDefResolver$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/privs/WheelNamingResolver$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperCheckConfig$CheckGroupResult.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_22
 - set property: grouper_v2_2_1_api_patch_22.date from: 2015/08/16 22:13:03 to: 2015/10/15 16:09:56
 - set property: grouper_v2_2_1_api_patch_22.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_21
Patch: grouper_v2_2_1_api_patch_21: was applied on: 2015/08/01 07:00:15
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_21 is low risk, is not a security patch
GRP-1170: Encountering SourceUnavailableException while running USDU
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/usdu/USDU.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/usdu/USDU.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_21
 - set property: grouper_v2_2_1_api_patch_21.date from: 2015/08/01 07:00:15 to: 2015/10/15 16:09:56
 - set property: grouper_v2_2_1_api_patch_21.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_20
Patch: grouper_v2_2_1_api_patch_20: was applied on: 2015/08/01 07:00:15
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_20 is low risk, is not a security patch
GRP-1171: Show friendly error if importing an old xml export to grouper 2.2
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/xml/importXml/XmlImportGsh.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/xml/importXml/XmlImportGsh.class
Patch successfully reverted: grouper_v2_2_1_api_patch_20
 - set property: grouper_v2_2_1_api_patch_20.date from: 2015/08/01 07:00:15 to: 2015/10/15 16:09:57
 - set property: grouper_v2_2_1_api_patch_20.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_19
Patch: grouper_v2_2_1_api_patch_19: was applied on: 2015/07/29 12:16:01
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_19 is medium risk, is not a security patch
GRP-1153: Audit Log should show Engine/Type as Loader
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/util/GrouperCallable.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/util/GrouperThreadLocalState.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/util/GrouperCallable$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/util/GrouperCallable.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/util/GrouperThreadLocalState.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoader.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoader$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoader.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoader$GrouperLoaderDryRunBean.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/GrouperContext.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/GrouperContext.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.java
Patch successfully reverted: grouper_v2_2_1_api_patch_19
 - set property: grouper_v2_2_1_api_patch_19.date from: 2015/07/29 12:16:01 to: 2015/10/15 16:09:57
 - set property: grouper_v2_2_1_api_patch_19.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_18
Patch: grouper_v2_2_1_api_patch_18: was applied on: 2015/07/21 20:03:24
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_18 is low risk, is not a security patch
GRP-1151: subject api needs ability to use ldap.properties for vt-ldap
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.java
Patch successfully reverted: grouper_v2_2_1_api_patch_18
 - set property: grouper_v2_2_1_api_patch_18.date from: 2015/07/21 20:03:24 to: 2015/10/15 16:09:58
 - set property: grouper_v2_2_1_api_patch_18.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_17
Patch: grouper_v2_2_1_api_patch_17: was applied on: 2015/07/20 19:23:10
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_17 is low risk, is not a security patch
GRP-1091: ldap loader display name doesnt change
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$7.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$9.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$8.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$10.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_17
 - set property: grouper_v2_2_1_api_patch_17.date from: 2015/07/20 19:23:10 to: 2015/10/15 16:09:59
 - set property: grouper_v2_2_1_api_patch_17.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_16
Patch: grouper_v2_2_1_api_patch_16: was applied on: 2015/07/19 20:16:37
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_16 is medium risk, is not a security patch
GRP-1094: Upgrade from v2.2.0 to v2.2.1 recreates views/constraints
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$26.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/DdlVersionable.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperOrgDdl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$28.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperTestDdl$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/SubjectDdl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$18.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperTestDdl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$29.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$24.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/SubjectDdl$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$25.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperOrgDdl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/DdlVersionable.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperTestDdl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperOrgDdl$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$15.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$12.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$23.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$20.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$17.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/SubjectDdl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$21.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$27.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$18$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdl$16.class
Patch successfully reverted: grouper_v2_2_1_api_patch_16
 - set property: grouper_v2_2_1_api_patch_16.date from: 2015/07/19 20:16:37 to: 2015/10/15 16:09:59
 - set property: grouper_v2_2_1_api_patch_16.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_15
Patch: grouper_v2_2_1_api_patch_15: was applied on: 2015/07/19 20:16:30
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_15 is low risk, is not a security patch
GRP-1143: Selective LDAP Provisioning
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/LDAPProvisioningHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/LDAPProvisioningHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/LDAPProvisioningHook$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_15
 - set property: grouper_v2_2_1_api_patch_15.date from: 2015/07/19 20:16:30 to: 2015/10/15 16:10:00
 - set property: grouper_v2_2_1_api_patch_15.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_14
Patch: grouper_v2_2_1_api_patch_14: was applied on: 2015/07/19 20:16:19
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_14 is low risk, is not a security patch
GRP-1140: Virtual attributes for subject name and description
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSubject.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/JDBCSourceAdapter2.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/LdapSourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/helper/DummySubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/helper/DummySubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/GrouperSubject$GrouperSubjectAttributeMap.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/GrouperSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/GrouperSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/InternalSourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/InternalSourceAdapter.class
Patch successfully reverted: grouper_v2_2_1_api_patch_14
 - set property: grouper_v2_2_1_api_patch_14.date from: 2015/07/19 20:16:19 to: 2015/10/15 16:10:00
 - set property: grouper_v2_2_1_api_patch_14.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_13
Patch: grouper_v2_2_1_api_patch_13: was applied on: 2015/07/13 13:08:11
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_13 is low risk, is not a security patch
GRP-1138: add import / export auditing
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/audit/AuditFieldType.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/audit/AuditTypeBuiltin.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/audit/AuditTypeBuiltin.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/audit/AuditFieldType.class
Patch successfully reverted: grouper_v2_2_1_api_patch_13
 - set property: grouper_v2_2_1_api_patch_13.date from: 2015/07/13 13:08:11 to: 2015/10/15 16:10:01
 - set property: grouper_v2_2_1_api_patch_13.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_12
Patch: grouper_v2_2_1_api_patch_12: was applied on: 2015/06/22 08:07:44
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_12 is low risk, is not a security patch
GRP-1137: Group copy with new group extension
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$10.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$12.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$17.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$15.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$4.class

Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$9.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/GroupCopy.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$13.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$Scope.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$11.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$18.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/GroupCopy.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Stem$16.class
Patch successfully reverted: grouper_v2_2_1_api_patch_12
 - set property: grouper_v2_2_1_api_patch_12.date from: 2015/06/22 08:07:44 to: 2015/10/15 16:10:02
 - set property: grouper_v2_2_1_api_patch_12.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_11
Patch: grouper_v2_2_1_api_patch_11: was applied on: 2015/06/03 09:08:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_11 is low risk, is not a security patch
GRP-1139: PSP doesn't support configs with multiple classes in a Grouper Hook definition
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase$ConfigFileType$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase$PropertyValueResult.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase$ConfigFile.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase$ConfigFileType$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/ConfigPropertiesCascadeBase$ConfigFileType.class
Patch successfully reverted: grouper_v2_2_1_api_patch_11
 - set property: grouper_v2_2_1_api_patch_11.date from: 2015/06/03 09:08:51 to: 2015/10/15 16:10:02
 - set property: grouper_v2_2_1_api_patch_11.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_10
Patch: grouper_v2_2_1_api_patch_10: was applied on: 2015/06/03 07:41:35
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_10 is low risk, is not a security patch
GRP-1132: option to auto delete empty loader groups used in other groups
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/GroupTypeTupleIncludeExcludeHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/GroupTypeTupleIncludeExcludeHook.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_10
 - set property: grouper_v2_2_1_api_patch_10.date from: 2015/06/03 07:41:35 to: 2015/10/15 16:10:03
 - set property: grouper_v2_2_1_api_patch_10.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_9
Patch: grouper_v2_2_1_api_patch_9: was applied on: 2015/05/21 15:31:19
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_9 is medium risk, is not a security patch
GRP-1128: Non-english chars dont seem to render correctly.  Note, the grouper.default.fileEncoding changed to UTF-8 from ISO-8859-1.  To make this is a lower risk change, set it back in the grouper.properties
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$DbMetadataBean.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouperUtf8.txt
Patch successfully reverted: grouper_v2_2_1_api_patch_9
 - set property: grouper_v2_2_1_api_patch_9.date from: 2015/05/21 15:31:19 to: 2015/10/15 16:10:03
 - set property: grouper_v2_2_1_api_patch_9.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_8
Patch: grouper_v2_2_1_api_patch_8: was applied on: 2015/05/05 15:50:05
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_8 is low risk, is not a security patch
GRP-1130: grouper loader should have configuration to not make changes (but log error) if too many removes
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_8
 - set property: grouper_v2_2_1_api_patch_8.date from: 2015/05/05 15:50:05 to: 2015/10/15 16:10:04
 - set property: grouper_v2_2_1_api_patch_8.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_7
Patch: grouper_v2_2_1_api_patch_7: was applied on: 2015/05/04 05:02:50
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_7 is low risk, is not a security patch
GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.java
Patch successfully reverted: grouper_v2_2_1_api_patch_7
 - set property: grouper_v2_2_1_api_patch_7.date from: 2015/05/04 05:02:50 to: 2015/10/15 16:10:05
 - set property: grouper_v2_2_1_api_patch_7.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_6
Patch: grouper_v2_2_1_api_patch_6: was applied on: 2015/03/22 12:45:58
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_6 is low risk, is not a security patch
GRP-1126: grouper import xml fails on attribute owner stem id
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_6
 - set property: grouper_v2_2_1_api_patch_6.date from: 2015/03/22 12:45:58 to: 2015/10/15 16:10:05
 - set property: grouper_v2_2_1_api_patch_6.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_5
Patch: grouper_v2_2_1_api_patch_5: was applied on: 2015/03/22 12:45:57
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_5 is high risk, is a security patch
GRP-1112: problems with 'edit memberships and privileges' button
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_5
 - set property: grouper_v2_2_1_api_patch_5.date from: 2015/03/22 12:45:57 to: 2015/10/15 16:10:06
 - set property: grouper_v2_2_1_api_patch_5.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_4
Patch: grouper_v2_2_1_api_patch_4: was applied on: 2015/03/22 12:45:56
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_4 is low risk, is a security patch
GRP-1109 problems with inherited privileges rule
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$12.class
Patch successfully reverted: grouper_v2_2_1_api_patch_4
 - set property: grouper_v2_2_1_api_patch_4.date from: 2015/03/22 12:45:56 to: 2015/10/15 16:10:07
 - set property: grouper_v2_2_1_api_patch_4.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_3
Patch: grouper_v2_2_1_api_patch_3: was applied on: 2015/03/22 12:45:55
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_3 is medium risk, is a security patch
GRP-1100 grouper new ui not showing unresolvable subjects correctly
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.class
Patch successfully reverted: grouper_v2_2_1_api_patch_3
 - set property: grouper_v2_2_1_api_patch_3.date from: 2015/03/22 12:45:55 to: 2015/10/15 16:10:09
 - set property: grouper_v2_2_1_api_patch_3.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_2
Patch: grouper_v2_2_1_api_patch_2: was applied on: 2015/03/22 12:45:53
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_2 is low risk, is not a security patch
This patch fixes GRP-1083: cannot set enabled/disabled dates in ui
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_2
 - set property: grouper_v2_2_1_api_patch_2.date from: 2015/03/22 12:45:53 to: 2015/10/15 16:10:10
 - set property: grouper_v2_2_1_api_patch_2.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_1
Patch: grouper_v2_2_1_api_patch_1: was applied on: 2015/03/22 12:45:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_1 is low risk, is not a security patch
This patch fixes GRP-1096: Use threads for 2.2 upgrade to decrease time of upgrade
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.java
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$1.class
Reverting file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_1
 - set property: grouper_v2_2_1_api_patch_1.date from: 2015/03/22 12:45:51 to: 2015/10/15 16:10:10
 - set property: grouper_v2_2_1_api_patch_1.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_0
Patch: grouper_v2_2_1_api_patch_0: was applied on: 2015/03/22 12:45:29
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_0 is low risk, is not a security patch
This patch fixes GRP-1095: hibernate exception handling masked original exception if roll
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.class
Reverting (deleting) file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.java
Patch successfully reverted: grouper_v2_2_1_api_patch_0
 - set property: grouper_v2_2_1_api_patch_0.date from: 2015/03/22 12:45:29 to: 2015/10/15 16:10:11
 - set property: grouper_v2_2_1_api_patch_0.state from: applied to: reverted
Since patches were reverted, you should delete files in your app server work directory,
  in tomcat it is named 'work'.  Hit <enter> to continue:
##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/lib/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/grouper.client.base.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/lib/grouper.jar
##################################
Upgrading API config files
grouper.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/grouper.base.properties
Found no changes in /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.base.properties
grouper-loader.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/grouper-loader.base.properties
Found no changes in /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/subject.base.properties
Compare you old ehcache.xml with the new ehcache.xml file:
  Old file: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/ehcache.xml
  New file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/ehcache.xml
  Press <enter> when done
grouperUtf8.txt is a new file and is being copied to the application dir: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes
You should compare /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
  with /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/lib/subject.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/grouper
  to: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/lib
Upgraded 0 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/lib
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.2, build date: 2015/09/22 03:44:33, env: <no label configured>
grouper.properties read from: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_d/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
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
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/lib/grouper-ui.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
  to: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/lib
##################################
Upgrading UI files
Upgrading files from: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/
  to: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/
  ignoring paths: WEB-INF/lib, WEB-INF/bin/gsh.sh, WEB-INF/web.xml, WEB-INF/bin/gsh, WEB-INF/bin/gsh.bat, WEB-INF/classes
Compared 1323 files and found 0 adds and 21 updates
21 files were backed up to: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/
Do you want to see the list of files changed (t|f)? [f]:
Backing up: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/web.xml to: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/web.xml
Copying new file: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml to: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/web.xml
Taking out basic authentication from /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/web.xml since it wasnt there before
If you customized the web.xml please merge your changes back in
  Note: basic authentication was removed from the new web.xml to be consistent with the old web.xml
  New file: /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/web.xml, bak file:/tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/web.xml
Press the <enter> key to continue

##################################
Upgrading UI config files
grouper.text.en.us.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
grouper-ui.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_08_43_784/WEB-INF/classes/grouper-ui.base.properties
/opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/Owasp.CsrfGuard.properties has not been updated so it was not changed
/opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/Owasp.CsrfGuard.overlay.properties has not been updated so it was not changed
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ui_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ui_patch_0.tar.gz
There are no new UI patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[appadmin@i2midev1 grouper2.2.2]$ emacs /opt/tomcats/tomcat_d/webapps/grouper_v2_2/
char.jsp         grouper/         grouperUi/       index.html       scripts/         WEB-INF/
error.jsp        grouperExternal/ i2mi/            index.jsp        test/
[appadmin@i2midev1 grouper2.2.2]$ emacs /opt/tomcats/tomcat_d/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
 
add user folders
 
# if true, when a user logs in, a folder will be created and granted to the user if not already there
grouperUi.autoCreateUserFolderOnLogin=true

# you can set a folder for the user.  If the folder is not created then privileges will not be adjusted
# you can use EL here based on the subject, e.g. users:folders:${subject.id} or users:folders:${subject.getAttributeValue('netId')}
grouperUi.autoCreateUserFolderName=userFolders:${subject.getAttributeValue('identifier')}

# if the folders are leaves in the tree, this can be false, if you need to create parents, set to true (be careful)
grouperUi.autoCreateUserFolderCreateParentFoldersIfNotExist = true

[appadmin@i2midev1 grouper2.2.2]$ /sbin/service tomcat_d start

```

## WS upgrade

Use same dir for jars as ui

```
[appadmin@i2midev1 tomcat_i]$ /sbin/service tomcat_i stop
Shutting down tomcat_i Tomcat services:
Using CATALINA_BASE:   /opt/tomcats/tomcat_i
Using CATALINA_HOME:   /opt/tomcat6base
Using CATALINA_TMPDIR: /opt/tomcats/tomcat_i/temp
Using JRE_HOME:        /opt/javas/java_i
Using CLASSPATH:       /opt/tomcat6base/bin/bootstrap.jar
Waiting for exit...
Waiting for exit...
Waiting for exit...
Waiting for exit...
Waiting for exit...
[mchyzer@i2midev1 ~]$ cp -R /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/ /tmp/grouper_bak
[appadmin@i2midev1 grouper2.2.2]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/tmp/grouper2.2.2]:
What do you want to upgrade?  api, ui, ws, or psp? [api]: ws
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper WS installed? /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2
Upgrading to grouper WS version: 2.2.2
Do you want to fix the patch index file (download all patches and see if they are installed?) (recommended) (t|f)? [t]:
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ws_patch_0.tar.gz to file: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0.tar.gz
Expanding: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0.tar
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ws_patch_1.tar.gz
 - added to end of property file: grouperInstallerLastFixedIndexFile.date = 2015/10/15 16:30:31
Patches for WS for version 2_2_1 were in the index file correctly
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23, use untarred dir (t|f)? [t]:

File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25, use untarred dir (t|f)? [t]:
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_26.tar.gz

 - set property: grouperInstallerLastFixedIndexFile.date from: 2015/10/15 16:30:31 to: 2015/10/15 16:31:18
Patches for API for version 2_2_1 were in the index file correctly
##################################
Download and build grouper packages
File exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2, use untarred dir (t|f)? [t]:
Do you want to set gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]:
Making sure gsh.sh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh
stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
Downloading from URL: http://software.internet2.edu/grouper/release/2.2.2/grouper.ws-2.2.2.tar.gz to file: /tmp/grouper2.2.2/grouper.ws-2.2.2.tar.gz
Unzipping: /tmp/grouper2.2.2/grouper.ws-2.2.2.tar.gz
Expanding: /tmp/grouper2.2.2/grouper.ws-2.2.2.tar
Editing /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build.properties:
 - set property: grouper.dir from: ../grouper to: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2
File exists: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/apache-ant-1.8.2, use untarred dir (t|f)? [t]:
The Grouper WS has been built in the past, do you want it rebuilt? (t|f) [t]:
Using shell command: bash
##################################
Building WS with command:
/tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws> bash /tmp/grouper2.2.2/apache-ant-1.8.2/bin/ant dist
......
stdout: Buildfile: build.xml
checkGrouper:
dist:
distHelper:
compile:
    [javac] Compiling 208 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/src/grouper-ws/edu/internet2/middleware/grouper/ws/query/WsQueryFilterType.java uses unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
    [javac] Compiling 79 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 81 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 93 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
    [javac] Compiling 93 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws
      [jar] Building jar: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws.jar
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 4 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
     [copy] Copying 24 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.base.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.base.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/log4j.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/log4j.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/server.example.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/server.example.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.client.base.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.client.base.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/spy.example.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/spy.example.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/log4j.example.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/log4j.example.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouperUtf8.txt to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouperUtf8.txt
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/morphString.example.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/morphString.example.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/morphString.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/morphString.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/ehcache.example.xml to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/ehcache.example.xml
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/README.txt to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/README.txt
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/ehcache.xml to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/ehcache.xml
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.hibernate.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.hibernate.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper-loader.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper-loader.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper-loader.base.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper-loader.base.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/sources.example.xml to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/sources.example.xml
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.hibernate.base.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.hibernate.base.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/grouper.client.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/grouper.client.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/sources.xml to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/sources.xml
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/subject.base.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/subject.base.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/subject.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/subject.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/spy.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/spy.properties
     [copy] Copying /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/server.properties to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/classes/server.properties
     [copy] Copying 89 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 5 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
     [copy] Copying 26 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws
     [move] Moving 1 file to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
     [move] Moving 1 file to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/services
      [jar] Building jar: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws.war
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [javac] Compiling 251 source files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
    [javac] Note: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws-java-generated-client/src/edu/internet2/middleware/grouper/webservicesClient/RampartPwHandlerClient.java uses or overrides a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws-java-generated-client/src/edu/internet2/middleware/grouper/webservicesClient/GrouperServiceStub.java uses unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
     [copy] Copying 255 files to /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/grouper-ws-soap-client
      [jar] Building jar: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws-soap-client.jar
BUILD SUCCESSFUL
Total time: 29 seconds

End building Ws
##################################
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]:
Making sure gsh.sh is in unix format: dos2unix /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh.sh
stderr: dos2unix: converting file /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh
stderr: dos2unix: converting file /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]:
##################################
Copying records from change log temp to change log with command:
  /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh.sh /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
....
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: 2014/11/09 20:03:16, env: GROUPERDEMO_1_6_3
grouper.properties read from: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_i/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
edu.internet2.middleware.grouper.GrouperSession: a506513194d3415da9e43b3c4a73475a,'GrouperSystem','application'
loader ran successfully: Ran the changeLogTempToChangeLog daemon
You need to revert all patches to upgrade
################ Checking patch grouper_v2_2_1_ws_patch_0
Patch: grouper_v2_2_1_ws_patch_0: was applied on: 2015/05/06 15:47:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ws_patch_0, use untarred dir (t|f)? [t]:
Would you like to revert all patches (t|f)? [t]:
Patch grouper_v2_2_1_ws_patch_0 is low risk, is not a security patch
GRP-1135: allow move and copy for groups and folders from WS
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$1$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$11$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$15.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/coresoap/WsGroupToSave.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/coresoap/WsStemToSave.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/coresoap/WsStemToSave.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/coresoap/WsGroupToSave.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$12.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$13$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$4$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ws/GrouperServiceLogic$11$1.class
Patch successfully reverted: grouper_v2_2_1_ws_patch_0
 - set property: grouper_v2_2_1_ws_patch_0.date from: 2015/05/06 15:47:51 to: 2015/10/15 16:32:58
 - set property: grouper_v2_2_1_ws_patch_0.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_8
Patch: grouper_v2_2_1_api_patch_8: was applied on: 2015/05/06 15:47:49
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_8 is low risk, is not a security patch
GRP-1130: grouper loader should have configuration to not make changes (but log error) if too many removes
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$1.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_8
 - set property: grouper_v2_2_1_api_patch_8.date from: 2015/05/06 15:47:49 to: 2015/10/15 16:33:06
 - set property: grouper_v2_2_1_api_patch_8.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_7
Patch: grouper_v2_2_1_api_patch_7: was applied on: 2015/05/06 15:47:37
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_7 is low risk, is not a security patch
GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.java
Patch successfully reverted: grouper_v2_2_1_api_patch_7
 - set property: grouper_v2_2_1_api_patch_7.date from: 2015/05/06 15:47:37 to: 2015/10/15 16:33:14
 - set property: grouper_v2_2_1_api_patch_7.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_6
Patch: grouper_v2_2_1_api_patch_6: was applied on: 2015/03/24 16:52:52
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_6 is low risk, is not a security patch
GRP-1126: grouper import xml fails on attribute owner stem id
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_6
 - set property: grouper_v2_2_1_api_patch_6.date from: 2015/03/24 16:52:52 to: 2015/10/15 16:33:15
 - set property: grouper_v2_2_1_api_patch_6.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_5
Patch: grouper_v2_2_1_api_patch_5: was applied on: 2015/03/24 16:52:52
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_5 is high risk, is a security patch
GRP-1112: problems with 'edit memberships and privileges' button
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$1.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_5
 - set property: grouper_v2_2_1_api_patch_5.date from: 2015/03/24 16:52:52 to: 2015/10/15 16:33:17
 - set property: grouper_v2_2_1_api_patch_5.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_4
Patch: grouper_v2_2_1_api_patch_4: was applied on: 2015/03/24 16:52:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_4 is low risk, is a security patch
GRP-1109 problems with inherited privileges rule
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$12.class
Patch successfully reverted: grouper_v2_2_1_api_patch_4
 - set property: grouper_v2_2_1_api_patch_4.date from: 2015/03/24 16:52:51 to: 2015/10/15 16:33:17
 - set property: grouper_v2_2_1_api_patch_4.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_3
Patch: grouper_v2_2_1_api_patch_3: was applied on: 2015/03/24 16:52:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_3 is medium risk, is a security patch
GRP-1100 grouper new ui not showing unresolvable subjects correctly
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.class
Patch successfully reverted: grouper_v2_2_1_api_patch_3
 - set property: grouper_v2_2_1_api_patch_3.date from: 2015/03/24 16:52:51 to: 2015/10/15 16:33:18
 - set property: grouper_v2_2_1_api_patch_3.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_2
Patch: grouper_v2_2_1_api_patch_2: was applied on: 2015/03/24 16:52:50
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_2 is low risk, is not a security patch
This patch fixes GRP-1083: cannot set enabled/disabled dates in ui
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_2
 - set property: grouper_v2_2_1_api_patch_2.date from: 2015/03/24 16:52:50 to: 2015/10/15 16:33:19
 - set property: grouper_v2_2_1_api_patch_2.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_1
Patch: grouper_v2_2_1_api_patch_1: was applied on: 2015/03/24 16:52:49
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz, should we use the local file (t|f)? [t]:

Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_1 is low risk, is not a security patch
This patch fixes GRP-1096: Use threads for 2.2 upgrade to decrease time of upgrade
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.java
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$1.class
Reverting file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_1
 - set property: grouper_v2_2_1_api_patch_1.date from: 2015/03/24 16:52:49 to: 2015/10/15 16:33:27
 - set property: grouper_v2_2_1_api_patch_1.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_0
Patch: grouper_v2_2_1_api_patch_0: was applied on: 2015/03/24 16:52:47
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_0 is low risk, is not a security patch
This patch fixes GRP-1095: hibernate exception handling masked original exception if roll
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.class
Reverting (deleting) file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.java
Patch successfully reverted: grouper_v2_2_1_api_patch_0
 - set property: grouper_v2_2_1_api_patch_0.date from: 2015/03/24 16:52:47 to: 2015/10/15 16:33:28
 - set property: grouper_v2_2_1_api_patch_0.state from: applied to: reverted
Since patches were reverted, you should delete files in your app server work directory,
  in tomcat it is named 'work'.  Hit <enter> to continue:
##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/lib/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/classes/grouper.client.base.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/lib/grouper.jar
##################################
Upgrading API config files
grouper.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/classes/grouper.base.properties
Found no changes in /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.hibernate.base.properties
grouper-loader.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/classes/grouper-loader.base.properties
Found no changes in /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/subject.base.properties
grouperUtf8.txt is a new file and is being copied to the application dir: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes
You should compare /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/sources.xml
  with /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/lib/subject.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/grouper
  to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/lib
Upgraded 0 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/lib
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz

There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/gsh.sh -registry -check -noprompt
.
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.2, build date: 2015/09/22 03:44:33, env: GROUPERDEMO_1_6_3
grouper.properties read from: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_i/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
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
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/lib/grouper-ws.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/lib
  to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/lib
##################################
Upgrading WS files
Upgrading files from: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/
  to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/
  ignoring paths: WEB-INF/lib, WEB-INF/bin/gsh.sh, WEB-INF/web.xml, WEB-INF/bin/gsh, WEB-INF/bin/gsh.bat, WEB-INF/classes
Compared 148 files and found 0 adds and 8 updates
8 files were backed up to: /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/
WEB-INF/services/GrouperServiceWssec_v2_2.aar.isondeck was updated
WEB-INF/services/GrouperService_v2_1.aar was updated
WEB-INF/services/GrouperService_v2_0.aar was updated
WEB-INF/services/GrouperService_v2_2.aar was updated
WEB-INF/services/GrouperServiceWssec.aar.ondeck was updated
WEB-INF/services/GrouperServiceWssec_v2_0.aar.isondeck was updated
WEB-INF/services/GrouperServiceWssec_v2_1.aar.isondeck was updated
WEB-INF/services/GrouperService.aar was updated
Backing up: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml to: /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/web.xml
Copying new file: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/web.xml to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml
Taking out basic authentication from /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml since it wasnt there before
If you customized the web.xml please merge your changes back in
  Note: basic authentication was removed from the new web.xml to be consistent with the old web.xml
  New file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml, bak file:/tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/web.xml
Press the <enter> key to continue

##################################
Upgrading WS config files
grouper-ws.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/classes/grouper-ws.base.properties
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ws_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ws_patch_0.tar.gz
There are no new WS patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[appadmin@i2midev1 grouper2.2.2]$
[appadmin@i2midev1 grouper-ws_v2_2]$ /sbin/service tomcat_i start
Starting tomcat_i Tomcat services:
Using CATALINA_BASE:   /opt/tomcats/tomcat_i
Using CATALINA_HOME:   /opt/tomcat6base
Using CATALINA_TMPDIR: /opt/tomcats/tomcat_i/temp
Using JRE_HOME:        /opt/javas/java_i
Using CLASSPATH:       /opt/tomcat6base/bin/bootstrap.jar
[appadmin@i2midev1 grouper-ws_v2_2]$

```

## Upgrade loader

Note, the demo server runs the loader as a clone of a UI, so in this case just upload the loader UI (no tomcat runs for it, just on the file system)

```
[appadmin@i2midev1 grouper-ws_v2_2]$ /sbin/service grouper_v2_2_loader stop
Grouper loader is running with processId: 31511, waiting for exit...
Grouper loader is stopped
[appadmin@i2midev1 grouper-ws_v2_2]$
[mchyzer@i2midev1 ~]$ cp -R /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/ /tmp/grouper_bak/grouper_gsh
WEB-INF/services/GrouperServiceWssec_v2_1.aar.isondeck was updated
WEB-INF/services/GrouperService.aar was updated
Backing up: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml to: /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/web.xml
Copying new file: /tmp/grouper2.2.2/grouper.ws-2.2.2/grouper-ws/build/dist/grouper-ws/WEB-INF/web.xml to: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml
Taking out basic authentication from /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml since it wasnt there before
If you customized the web.xml please merge your changes back in
  Note: basic authentication was removed from the new web.xml to be consistent with the old web.xml
  New file: /opt/tomcats/tomcat_i/webapps/grouper-ws_v2_2/WEB-INF/web.xml, bak file:/tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/web.xml
Press the <enter> key to continue

##################################
Upgrading WS config files
grouper-ws.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_WS_2015_10_15_16_32_16_986/WEB-INF/classes/grouper-ws.base.properties
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ws_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ws_patch_0.tar.gz
There are no new WS patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2
[appadmin@i2midev1 grouper2.2.2]$ clear
[appadmin@i2midev1 grouper2.2.2]$
[appadmin@i2midev1 grouper2.2.2]$ java -jar grouperInstaller.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) [install]: upgrade
You should backup your files and database before you start.  Press <enter> to continue.

##################################
Gather upgrade information
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [/tmp/grouper2.2.2]:
What do you want to upgrade?  api, ui, ws, or psp? [api]: ui
Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:
Where is the grouper UI installed? /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2
Upgrading to grouper UI version: 2.2.2
Do you want to fix the patch index file (download all patches and see if they are installed?) (recommended) (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_3, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7, use untarred dir (t|f)? [t]:

File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_17, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_18, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_19, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_20, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_21, use untarred dir (t|f)? [t]:

Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_ui_patch_22.tar.gz

 - set property: grouper_v2_2_1_ui_patch_3.date from: 2015/05/05 15:57:55 to: 2015/10/15 16:48:48
 - set property: grouper_v2_2_1_ui_patch_3.state from: applied to: skippedTemporarily
Patch grouper_v2_2_1_ui_patch_3 was listed as applied but was changed to skippedTemporarily
 - added to end of property file: grouperInstallerLastFixedIndexFile.date = 2015/10/15 16:48:48
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_11, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_12, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_13, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_14, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_15, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_16, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_17, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_18, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_19, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_20, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_21, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_22, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_23, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_24, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_25, use untarred dir (t|f)? [t]:
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_26.tar.gz
 - set property: grouperInstallerLastFixedIndexFile.date from: 2015/10/15 16:48:48 to: 2015/10/15 16:49:08
Patches for API for version 2_2_1 were in the index file correctly
##################################
Download and build grouper packages
File exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper.clientBinary-2.2.2, use untarred dir (t|f)? [t]:
File exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2, use untarred dir (t|f)? [t]:
Do you want to set gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh

Making sure gsh is executable with command: chmod +x /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]: Making sure gsh.sh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh

stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh
stderr: dos2unix: converting file /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/bin/gsh to UNIX format ...
File exists: /tmp/grouper2.2.2/grouper.ui-2.2.2.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper.ui-2.2.2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper.ui-2.2.2, use untarred dir (t|f)? [t]:
Editing /tmp/grouper2.2.2/grouper.ui-2.2.2/build.properties:
 - property grouper.folder already was set to: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2, not changing file
 - property should.copy.context.xml.to.metainf already was set to: false, not changing file
File exists: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/apache-ant-1.8.2-bin.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/apache-ant-1.8.2, use untarred dir (t|f)? [t]:
The Grouper UI has been built in the past, do you want it rebuilt? (t|f) [t]:

Using shell command: bash
##################################
Building UI with command:
/tmp/grouper2.2.2/grouper.ui-2.2.2> bash /tmp/grouper2.2.2/apache-ant-1.8.2/bin/ant dist
........
stdout: Buildfile: build.xml
dist:
-setup:
-choose-webapp:
[propertyfile] Updating property file: /tmp/grouper2.2.2/grouper.ui-2.2.2/.lastbuild.properties
     [echo] In setup - do.clean = true   cleanable=${webapp.folder.cleanable}
-doStop:
-doCleanWebappClassFolder:
     [echo] Removing  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-doClean:
     [echo] Removing  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/dist
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
-resources:
     [echo] In resources - Build folder = /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
-dist-grouper:
     [echo] Creating  /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [echo] Copying Grouper configuration files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 18 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-local-log4j:
-fix-grouper-home:
     [echo] Attempting to replace grouper.home with /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/
     [echo] Copying ui resources to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 7 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes/resources
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
     [copy] Copying 3 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/classes
-additional-build:
-optional-conf:
-webapp:
   [delete] Deleting directory /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
-compileGrouper:
    [mkdir] Created dir: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/jarBin
    [javac] Compiling 322 source files to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/jarBin
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
      [jar] Building jar: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib/grouper-ui.jar
-additional-build:
     [copy] Copying 66 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
     [copy] Copying 5 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
-copyContent:
     [echo] Copying core UI files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1219 files to /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [echo] Processing web.xml
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] web.xmls.isempty=:${web.xmls.isempty}:
-merge-xmls:
     [echo] temp.dir : /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] final.web.xmls : ${final.web.xmls}
     [echo] ui.folder : /tmp/grouper2.2.2/grouper.ui-2.2.2
     [echo] webapp.folder : /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [copy] Copying 1 file to /tmp/grouper2.2.2/grouper.ui-2.2.2/temp
     [echo] Transforming: /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo] /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo] /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo]
     [echo]
     [echo] Base = /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/50.web.core.xml
     [echo]  + /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/60.web.ajax.xml
     [echo]  -> /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/web.1.xml
     [echo]
     [echo] Base = /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/web.1.xml
     [echo]  + /tmp/grouper2.2.2/grouper.ui-2.2.2/temp/99.web.core-filters.xml
     [echo]  -> /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml
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
Total time: 42 seconds

End building UI
##################################
What is the location of your tomcat server.xml for the UI?  Note, if you dont use tomcat just leave it blank or type 'blank':
End download and build grouper packages

##################################
Do you want to set existing gsh script to executable (t|f)? [t]:
Making sure gsh.sh is executable with command: chmod +x /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh
Making sure gsh is executable with command: chmod +x /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh
Do you want to run dos2unix on gsh.sh (t|f)? [t]:
Making sure gsh.sh is in unix format: dos2unix /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh
stderr: dos2unix: converting file /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh to UNIX format ...
Making sure gsh is in unix format: dos2unix /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh
stderr: dos2unix: converting file /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh to UNIX format ...
Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [t]:
##################################
Copying records from change log temp to change log with command:
  /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/gshChangeLogTempToChangeLog.gsh
..
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.1, build date: null, env: <no label configured>
grouper.properties read from: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_d_gsh/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Type help() for instructions
edu.internet2.middleware.grouper.GrouperSession: 7b35362dd98f4487afe351e0661fd01b,'GrouperSystem','application'
loader ran successfully: Ran the changeLogTempToChangeLog daemon
You need to revert all patches to upgrade
################ Checking patch grouper_v2_2_1_ui_patch_16
Patch: grouper_v2_2_1_ui_patch_16: was applied on: 2015/05/05 15:58:05
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_16, use untarred dir (t|f)? [t]:

Would you like to revert all patches (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_16 is low risk, is not a security patch
GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
This patch requires all processes that user Grouper to be stopped.
  Please stop these processes if they are running and press <enter> to continue...

Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$RetrieveStemHelperResult.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$StemSearchType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$3.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Stem$2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_16
 - set property: grouper_v2_2_1_ui_patch_16.date from: 2015/05/05 15:58:05 to: 2015/10/15 16:51:16
 - set property: grouper_v2_2_1_ui_patch_16.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_15
Patch: grouper_v2_2_1_ui_patch_15: was applied on: 2015/05/05 15:58:04
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_15, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_15 is low risk, is not a security patch
GRP-1133: this groups memberships in new ui fails if effective only memberships and wont remove memberships
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.java
Patch successfully reverted: grouper_v2_2_1_ui_patch_15
 - set property: grouper_v2_2_1_ui_patch_15.date from: 2015/05/05 15:58:04 to: 2015/10/15 16:51:18
 - set property: grouper_v2_2_1_ui_patch_15.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_14
Patch: grouper_v2_2_1_ui_patch_14: was applied on: 2015/05/05 15:58:03
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_14, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_14 is low risk, is not a security patch
GRP-1131: add button to UI for loader group admins to refresh the group from the system of record
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$6.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer$5.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_14
 - set property: grouper_v2_2_1_ui_patch_14.date from: 2015/05/05 15:58:03 to: 2015/10/15 16:51:25
 - set property: grouper_v2_2_1_ui_patch_14.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_13
Patch: grouper_v2_2_1_ui_patch_13: was applied on: 2015/05/05 15:58:02
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_13, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_13 is low risk, is not a security patch
This patch fixes GRP-1124 put composite info on membership list
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/viewGroup.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/grouperExternal/public/assets/css/grouperUi2.css
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_13
 - set property: grouper_v2_2_1_ui_patch_13.date from: 2015/05/05 15:58:02 to: 2015/10/15 16:51:26
 - set property: grouper_v2_2_1_ui_patch_13.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_12
Patch: grouper_v2_2_1_ui_patch_12: was applied on: 2015/05/05 15:58:01
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_12, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_12 is low risk, is not a security patch
GRP-1117: grouper new ui tooltips wrap
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/grouperExternal/public/assets/css/grouperUi2.css
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_12
 - set property: grouper_v2_2_1_ui_patch_12.date from: 2015/05/05 15:58:01 to: 2015/10/15 16:51:26
 - set property: grouper_v2_2_1_ui_patch_12.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_11
Patch: grouper_v2_2_1_ui_patch_11: was applied on: 2015/05/05 15:58:01
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_11, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_11 is low risk, is not a security patch
GRP-1111: if you leave a group via UI and leaving revokes view privs (or others), dont throw error
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$RetrieveGroupHelperResult.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group$5.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_11
 - set property: grouper_v2_2_1_ui_patch_11.date from: 2015/05/05 15:58:01 to: 2015/10/15 16:51:27
 - set property: grouper_v2_2_1_ui_patch_11.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_10
Patch: grouper_v2_2_1_ui_patch_10: was applied on: 2015/05/05 15:58:00
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_10, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_10 is low risk, is not a security patch
GRP-1114: cant assign privs to composite group
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupHeader.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_10
 - set property: grouper_v2_2_1_ui_patch_10.date from: 2015/05/05 15:58:00 to: 2015/10/15 16:51:28
 - set property: grouper_v2_2_1_ui_patch_10.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_9
Patch: grouper_v2_2_1_ui_patch_9: was applied on: 2015/05/05 15:57:59
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_9, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_9 is high risk, is a security patch
GRP-1112: problems with 'edit memberships and privileges' button
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/group/viewGroup.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/editMembership.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/subject/subjectContents.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/externalEntities/invite.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiGroup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiMembershipContainer.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_9
 - set property: grouper_v2_2_1_ui_patch_9.date from: 2015/05/05 15:57:59 to: 2015/10/15 16:51:29
 - set property: grouper_v2_2_1_ui_patch_9.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_8
Patch: grouper_v2_2_1_ui_patch_8: was applied on: 2015/05/05 15:57:58
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_8, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_8 is low risk, is a security patch
GRP-1109 problems with inherited privileges rule
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/jsp/stemLinks.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/SaveStemAction.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/SaveStemAction.class
Patch successfully reverted: grouper_v2_2_1_ui_patch_8
 - set property: grouper_v2_2_1_ui_patch_8.date from: 2015/05/05 15:57:58 to: 2015/10/15 16:51:29
 - set property: grouper_v2_2_1_ui_patch_8.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_7
Patch: grouper_v2_2_1_ui_patch_7: was applied on: 2015/05/05 15:57:57
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_7, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_7 is medium risk, is a security patch
This patch fixes GRP-1100 grouper new ui not showing unresolvable subjects correctly
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiSubject$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_7
 - set property: grouper_v2_2_1_ui_patch_7.date from: 2015/05/05 15:57:57 to: 2015/10/15 16:51:30
 - set property: grouper_v2_2_1_ui_patch_7.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_6
Patch: grouper_v2_2_1_ui_patch_6: was applied on: 2015/05/05 15:57:57
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_6, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_6 is low risk, is a security patch
This patch fixes GRP-1097 grouper logout management
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/index/index.jsp
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LogoutAction.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/actions/LogoutAction.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/Misc.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/Misc.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper-ui.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_6
 - set property: grouper_v2_2_1_ui_patch_6.date from: 2015/05/05 15:57:57 to: 2015/10/15 16:51:30
 - set property: grouper_v2_2_1_ui_patch_6.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_5
Patch: grouper_v2_2_1_ui_patch_5: was applied on: 2015/05/05 15:57:56
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_5, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_5 is low risk, is not a security patch
This patch fixes GRP-1088: attribute def left menu link throws error
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/grouperExternal/public/assets/js/grouperUi.js
Patch successfully reverted: grouper_v2_2_1_ui_patch_5
 - set property: grouper_v2_2_1_ui_patch_5.date from: 2015/05/05 15:57:56 to: 2015/10/15 16:51:31
 - set property: grouper_v2_2_1_ui_patch_5.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_4
Patch: grouper_v2_2_1_ui_patch_4: was applied on: 2015/05/05 15:57:55
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_4, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_4 is low risk, is not a security patch
This patch fixes GRP-1087: edit membership page shows large H as icon in title
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceAttributeDefPrivileges.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/editMembership.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceMembership.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/traceStemPrivileges.jsp
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/membership/tracePrivileges.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_4
 - set property: grouper_v2_2_1_ui_patch_4.date from: 2015/05/05 15:57:55 to: 2015/10/15 16:51:31
 - set property: grouper_v2_2_1_ui_patch_4.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_3
Patch: grouper_v2_2_1_ui_patch_3: was skipped termporarily on: 2015/10/15 16:48:48

################ Checking patch grouper_v2_2_1_ui_patch_2
Patch: grouper_v2_2_1_ui_patch_2: was applied on: 2015/05/05 15:57:54
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_2, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_2 is low risk, is not a security patch
This patch fixes GRP-1086: extra system out print in ui
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouperClient/config/GrouperUiTextConfig.java
Patch successfully reverted: grouper_v2_2_1_ui_patch_2
 - set property: grouper_v2_2_1_ui_patch_2.date from: 2015/05/05 15:57:54 to: 2015/10/15 16:51:32
 - set property: grouper_v2_2_1_ui_patch_2.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_1
Patch: grouper_v2_2_1_ui_patch_1: was applied on: 2015/05/05 15:57:53
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_1, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_1 is low risk, is not a security patch
This patch fixes GRP-1082: grouper paging tag2 has one word not externalized
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/tags/GrouperPagingTag2.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ui/tags/GrouperPagingTag2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
Patch successfully reverted: grouper_v2_2_1_ui_patch_1
 - set property: grouper_v2_2_1_ui_patch_1.date from: 2015/05/05 15:57:53 to: 2015/10/15 16:51:32
 - set property: grouper_v2_2_1_ui_patch_1.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_ui_patch_0
Patch: grouper_v2_2_1_ui_patch_0: was applied on: 2015/05/05 15:57:53
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_ui_patch_0, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_ui_patch_0 is low risk, is not a security patch
This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/grouperUi2/index/index.jsp
Patch successfully reverted: grouper_v2_2_1_ui_patch_0
 - set property: grouper_v2_2_1_ui_patch_0.date from: 2015/05/05 15:57:53 to: 2015/10/15 16:51:33
 - set property: grouper_v2_2_1_ui_patch_0.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_10
Patch: grouper_v2_2_1_api_patch_10: was applied on: 2015/06/03 07:37:29
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_10, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_10 is low risk, is not a security patch
GRP-1132: option to auto delete empty loader groups used in other groups
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/GroupTypeTupleIncludeExcludeHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/GroupTypeTupleIncludeExcludeHook.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_10
 - set property: grouper_v2_2_1_api_patch_10.date from: 2015/06/03 07:37:29 to: 2015/10/15 16:51:33
 - set property: grouper_v2_2_1_api_patch_10.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_9
Patch: grouper_v2_2_1_api_patch_9: was applied on: 2015/06/03 05:51:32
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_9, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_9 is medium risk, is not a security patch
GRP-1128: Non-english chars dont seem to render correctly.  Note, the grouper.default.fileEncoding changed to UTF-8 from ISO-8859-1.  To make this is a lower risk change, set it back in the grouper.properties
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/GrouperStartup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$DbMetadataBean.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/ddl/GrouperDdlUtils.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouperUtf8.txt
Patch successfully reverted: grouper_v2_2_1_api_patch_9
 - set property: grouper_v2_2_1_api_patch_9.date from: 2015/06/03 05:51:32 to: 2015/10/15 16:51:34
 - set property: grouper_v2_2_1_api_patch_9.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_8
Patch: grouper_v2_2_1_api_patch_8: was applied on: 2015/05/05 15:57:52
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_8, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_8 is low risk, is not a security patch
GRP-1130: grouper loader should have configuration to not make changes (but log error) if too many removes
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader/GrouperLoaderType$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper-loader.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_8
 - set property: grouper_v2_2_1_api_patch_8.date from: 2015/05/05 15:57:52 to: 2015/10/15 16:51:34
 - set property: grouper_v2_2_1_api_patch_8.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_7
Patch: grouper_v2_2_1_api_patch_7: was applied on: 2015/05/05 15:57:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_7, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_7 is low risk, is not a security patch
GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectStemHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefHook.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectGroupHook.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hooks/examples/UniqueObjectAttributeDefNameHook.java
Patch successfully reverted: grouper_v2_2_1_api_patch_7
 - set property: grouper_v2_2_1_api_patch_7.date from: 2015/05/05 15:57:51 to: 2015/10/15 16:51:35
 - set property: grouper_v2_2_1_api_patch_7.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_6
Patch: grouper_v2_2_1_api_patch_6: was applied on: 2015/05/05 15:57:51
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_6, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_6 is low risk, is not a security patch
GRP-1126: grouper import xml fails on attribute owner stem id
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/attr/assign/AttributeAssign$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_6
 - set property: grouper_v2_2_1_api_patch_6.date from: 2015/05/05 15:57:51 to: 2015/10/15 16:51:35
 - set property: grouper_v2_2_1_api_patch_6.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_5
Patch: grouper_v2_2_1_api_patch_5: was applied on: 2015/05/05 15:57:50
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_5, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_5 is high risk, is a security patch
GRP-1112: problems with 'edit memberships and privileges' button
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/provider/SubjectImpl.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/subject/SubjectUtils.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipPathGroup$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/entity/EntitySourceAdapter.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_5
 - set property: grouper_v2_2_1_api_patch_5.date from: 2015/05/05 15:57:50 to: 2015/10/15 16:51:36
 - set property: grouper_v2_2_1_api_patch_5.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_4
Patch: grouper_v2_2_1_api_patch_4: was applied on: 2015/05/05 15:57:49
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar.gz, should we use the local file (t|f)? [t]: Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_4, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_4 is low risk, is a security patch
GRP-1109 problems with inherited privileges rule
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/rules/RuleThenEnum$12.class
Patch successfully reverted: grouper_v2_2_1_api_patch_4
 - set property: grouper_v2_2_1_api_patch_4.date from: 2015/05/05 15:57:49 to: 2015/10/15 16:51:36
 - set property: grouper_v2_2_1_api_patch_4.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_3
Patch: grouper_v2_2_1_api_patch_3: was applied on: 2015/05/05 15:57:49
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_3, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_3 is medium risk, is a security patch
GRP-1100 grouper new ui not showing unresolvable subjects correctly
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/membership/MembershipSubjectContainer.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject$LazySubjectType.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/UnresolvableSubject.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/subj/LazySubject.class
Patch successfully reverted: grouper_v2_2_1_api_patch_3
 - set property: grouper_v2_2_1_api_patch_3.date from: 2015/05/05 15:57:49 to: 2015/10/15 16:51:39
 - set property: grouper_v2_2_1_api_patch_3.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_2
Patch: grouper_v2_2_1_api_patch_2: was applied on: 2015/05/05 15:57:48
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_2, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_2 is low risk, is not a security patch
This patch fixes GRP-1083: cannot set enabled/disabled dates in ui
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$6.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$16.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$15.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$18.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$22.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$17.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$5.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$13.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$14.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$20.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$9.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$7.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$11.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$10.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$12.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$8.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$19.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/Group$21$1.class
Patch successfully reverted: grouper_v2_2_1_api_patch_2
 - set property: grouper_v2_2_1_api_patch_2.date from: 2015/05/05 15:57:48 to: 2015/10/15 16:51:40
 - set property: grouper_v2_2_1_api_patch_2.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_1
Patch: grouper_v2_2_1_api_patch_1: was applied on: 2015/05/05 15:57:46
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_1, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_1 is low risk, is not a security patch
This patch fixes GRP-1096: Use threads for 2.2 upgrade to decrease time of upgrade
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$4.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncStemSets$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/SyncPITTables.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes$3.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.java
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$2$1.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO$1.class
Reverting file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.base.properties
Patch successfully reverted: grouper_v2_2_1_api_patch_1
 - set property: grouper_v2_2_1_api_patch_1.date from: 2015/05/05 15:57:46 to: 2015/10/15 16:51:40
 - set property: grouper_v2_2_1_api_patch_1.state from: applied to: reverted

################ Checking patch grouper_v2_2_1_api_patch_0
Patch: grouper_v2_2_1_api_patch_0: was applied on: 2015/05/05 15:57:42
File exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar.gz, should we use the local file (t|f)? [t]:
Unzipped file exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0.tar, use unzipped file (t|f)? [t]:
Untarred dir exists: /tmp/grouper2.2.2/grouper_v2_2_1_api_patch_0, use untarred dir (t|f)? [t]:
Patch grouper_v2_2_1_api_patch_0 is low risk, is not a security patch
This patch fixes GRP-1095: hibernate exception handling masked original exception if roll
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.class
Reverting (deleting) file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/edu/internet2/middleware/grouper/hibernate/HibernateSession.java
Patch successfully reverted: grouper_v2_2_1_api_patch_0
 - set property: grouper_v2_2_1_api_patch_0.date from: 2015/05/05 15:57:42 to: 2015/10/15 16:51:41
 - set property: grouper_v2_2_1_api_patch_0.state from: applied to: reverted
Since patches were reverted, you should delete files in your app server work directory,
  in tomcat it is named 'work'.  Hit <enter> to continue:
##################################
Upgrading grouper client
grouperClient.jar had version 2.2.1 and size 4217426 bytes and is being upgraded to version 2.2.2 and size 4217612 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/lib/grouperClient.jar
grouper.client.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/grouper.client.base.properties
##################################
Upgrading API
grouper.jar had version 2.2.1 and size 5794964 bytes and is being upgraded to version 2.2.2 and size 5890206 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/lib/grouper.jar
##################################
Upgrading API config files
grouper.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/grouper.base.properties
Found no changes in /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.base.properties
grouper-loader.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/grouper-loader.base.properties
Found no changes in /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/subject.base.properties
Compare you old ehcache.xml with the new ehcache.xml file:
  Old file: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/ehcache.xml
  New file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/ehcache.xml
  Press <enter> when done
grouperUtf8.txt is a new file and is being copied to the application dir: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes
You should compare /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
  with /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/conf/sources.xml
Press <enter> to continue after you have merged the sources.xml

##################################
Upgrading API jars
subject.jar had version 2.2.1 and size 259419 bytes and is being upgraded to version 2.2.2 and size 261985 bytes.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/lib/subject.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/grouper
  to: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/lib
Upgraded 0 jar files from: /tmp/grouper2.2.2/grouper.apiBinary-2.2.2/lib/jdbcSamples
  to: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/lib
##################################
Patch API

################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

##################################
Upgrading DB (registry)

##################################
Checking API database version with command: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/gsh.sh -registry -check -noprompt

.
stdout: Using GROUPER_HOME: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/..
Using GROUPER_CONF: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin/../classes
Using JAVA: /opt/java6/bin/java
using MEMORY: 64m-750m
Grouper starting up: version: 2.2.2, build date: 2015/09/22 03:44:33, env: <no label configured>
grouper.properties read from: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/bin
log4j.properties read from:   /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/tomcats/tomcat_d_gsh/logs/grouper_v2_2/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: grouper_v2_2@jdbc:mysql://localhost:3306/grouper_v2_2_utf8?CharSet=utf8&useUnicode=true&characterEncoding=utf8
sources.xml read from:        /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/sources.xml
sources.xml groupersource id: g:gsa
sources.xml groupersource id: grouperEntities
sources.xml jdbc source id:   jdbc: GrouperJdbcConnectionProvider
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
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/lib/grouper-ui.jar
Upgraded 1 jar files from: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/lib
  to: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/lib
##################################
Upgrading UI files
Upgrading files from: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/
  to: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/
  ignoring paths: WEB-INF/lib, WEB-INF/bin/gsh.sh, WEB-INF/web.xml, WEB-INF/bin/gsh, WEB-INF/bin/gsh.bat, WEB-INF/classes
Compared 1323 files and found 0 adds and 21 updates
21 files were backed up to: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/
Do you want to see the list of files changed (t|f)? [f]:
Backing up: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/web.xml to: /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/web.xml
Copying new file: /tmp/grouper2.2.2/grouper.ui-2.2.2/dist/grouper/WEB-INF/web.xml to: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/web.xml
Taking out basic authentication from /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/web.xml since it wasnt there before
If you customized the web.xml please merge your changes back in
  Note: basic authentication was removed from the new web.xml to be consistent with the old web.xml
  New file: /opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/web.xml, bak file:/tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/web.xml
Press the <enter> key to continue

##################################
Upgrading UI config files
grouper.text.en.us.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/grouperText/grouper.text.en.us.base.properties
grouper-ui.base.properties has changes and was upgraded.
  It is backed up to /tmp/grouper2.2.2/bak_UI_2015_10_15_16_49_57_672/WEB-INF/classes/grouper-ui.base.properties
/opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/Owasp.CsrfGuard.properties has not been updated so it was not changed
/opt/tomcats/tomcat_d_gsh/webapps/grouper_v2_2/WEB-INF/classes/Owasp.CsrfGuard.overlay.properties has not been updated so it was not changed
################ Checking patch grouper_v2_2_2_api_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_api_patch_0.tar.gz
There are no new API patches to install

################ Checking patch grouper_v2_2_2_ui_patch_0
Patch doesnt exist yet (not an error): http://software.internet2.edu/grouper/release/2.2.2/patches/grouper_v2_2_2_ui_patch_0.tar.gz
There are no new UI patches to install

##################################
Looking for conflicting jars

Grouper is upgraded from 2.2.1 to 2.2.2

[appadmin@i2midev1 tmp]$ /sbin/service grouper_v2_2_loader start
Starting Grouper loader service...
Grouper loader is running...
[appadmin@i

```
