---
title: "Grouper installer task to build the container v2.5"
space: Grouper
pageId: 28554444
version: 11
lastUpdated: 2026-07-12T05:16:31.465Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554444/Grouper+installer+task+to+build+the+container+v2.5
---

This is how the container will be built by the installer in Grouper 2.5

## Use the installer to generate the container files

```
$ GROUPER_VERSION=2.5.8
$ wget https://oss.sonatype.org/service/local/repositories/releases/content/edu/internet2/middleware/grouper/grouper-installer/$GROUPER_VERSION/grouper-installer-$GROUPER_VERSION.jar

$ java -version
java version "1.8.0_181"
Java(TM) SE Runtime Environment (build 1.8.0_181-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.181-b13, mixed mode)

$ vi grouper.installer.properties

download.server.url = https://software.internet2.edu/grouper                                                                                                                                                                            

$ java -jar grouper-installer-$GROUPER_VERSION.jar
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
'patch' an existing installation, 'admin' utilities, 'buildContainer', or 'createPatch' for Grouper developers
(enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch', 'buildContainer', or blank for the default) [install]: buildContainer

```

## Output

```
[appadmin@i2midev6 grouperInstallerContainer]$ cd container/
[appadmin@i2midev6 container]$ ls
tomee  webapp

```
