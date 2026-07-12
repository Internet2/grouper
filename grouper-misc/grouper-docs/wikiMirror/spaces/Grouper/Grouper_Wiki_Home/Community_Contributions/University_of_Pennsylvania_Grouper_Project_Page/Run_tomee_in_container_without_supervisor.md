---
title: "Run tomee in container without supervisor"
space: Grouper
pageId: 28544218
version: 1
lastUpdated: 2020-11-27T19:08:14.447Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544218/Run+tomee+in+container+without+supervisor
---

Temporarily run tomee manually to be able to see crash logs

Turn off the health check (which is a script at penn)

slashRoot\src\script\healthCheck.sh

```
#!/bin/bash

processRunning=`ps -ef | grep jvm | grep tomcat | grep -v grep | wc -l`

if [ "$processRunning" -ge 1 ]; then

  echo "Daemon running, processes: $processRunning"  
  exit 0
fi

echo "Daemon not running"
#exit 1

```

Run without running tomee

Dockerfile

```
ENV GROUPER_RUN_TOMEE=false
```

Start up tomcat

```
[root@018c8c4aeb474679bcd4d0e793977ee7-1492204015 ~]# sudo -u tomcat bash
[tomcat@018c8c4aeb474679bcd4d0e793977ee7-1492204015 root]$ cd /opt/tomee/bin
[tomcat@018c8c4aeb474679bcd4d0e793977ee7-1492204015 bin]$ ./startup.sh 
Using CATALINA_BASE:   /opt/tomee
Using CATALINA_HOME:   /opt/tomee
Using CATALINA_TMPDIR: /opt/tomee/temp
Using JRE_HOME:        /usr/lib/jvm/java-1.8.0-amazon-corretto
Using CLASSPATH:       /opt/tomee/bin/*:/opt/tomee/bin/bootstrap.jar:/opt/tomee/bin/tomcat-juli.jar
Tomcat started.
[tomcat@018c8c4aeb474679bcd4d0e793977ee7-1492204015 bin]$ 
```
