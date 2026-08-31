---
title: "Grouper container v2.6 change JVM"
space: Grouper
pageId: 28554916
version: 2
lastUpdated: 2026-07-01T05:39:21.576Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554916/Grouper+container+v2.6+change+JVM
---

Generally you should keep the JVM in the Grouper container. But if you are running a different processor architecture or for other reasons you might need to change it. This is available in v2.6.6+ (not released as of 2021/12/16).

This example shows going to openjdk. Note, you should install the devel java which has javac

Dockerfile

```
FROM i2incommon/grouper:2.6.6

# remove current java
RUN rpm -e `rpm -qa | grep java-1.8.0-amazon-corretto-devel`

# install new devel java
RUN yum install -y java-1.8.0-openjdk-devel

# set env var for java and grouper java
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0
ENV GROUPER_JAVA_HOME=/usr/lib/jvm/java-1.8.0
```

Build this

```
docker build -t groupernewjava .
```

Run it and check

```
[root@i2midev6 upgradeJava]# docker run --detach --name grouper-test groupernewjava:latest ui
4810f4e2a855bf12bb30ac47a6d3099f03e010546ad2ced2295635b84ffa7d59
[root@i2midev6 upgradeJava]# docker exec -it grouper-test bash
[root@4810f4e2a855 WEB-INF]# echo $JAVA_HOME
/usr/lib/jvm/java-1.8.0
[root@4810f4e2a855 WEB-INF]# sudo -u tomcat bash
[tomcat@4810f4e2a855 WEB-INF]$ echo $JAVA_HOME
/usr/lib/jvm/java-1.8.0
[tomcat@4810f4e2a855 WEB-INF]$ cd bin
[tomcat@4810f4e2a855 bin]$ ./gsh.sh
Detected Grouper directory structure 'webapp' (valid is api, apiMvn, webapp)
Using GROUPER_HOME:           /opt/grouper/grouperWebapp/WEB-INF
Using GROUPER_CONF:           /opt/grouper/grouperWebapp/WEB-INF/classes
Using JAVA:                   /usr/lib/jvm/java-1.8.0/bin/java
Using CLASSPATH:              /opt/grouper/grouperWebapp/WEB-INF/classes:/opt/grouper/grouperWebapp/WEB-INF/lib/*:/opt/tomee/lib/servlet-api.jar
using MEMORY:                 64m-750m
Grouper starting up: version: 2.6.5, build date: 2021/12/08 07:26:09 +0000, env: <no label configured>
grouper.properties read from: /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/grouper/grouperWebapp/WEB-INF/bin
log4j.properties read from:   /opt/grouper/grouperWebapp/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /tmp/logpipe, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties

```
