---
title: "Migrate to containers - example"
space: Grouper
pageId: 28560227
version: 19
lastUpdated: 2026-07-12T06:34:14.116Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560227/Migrate+to+containers+-+example
---

This explains how the [demo server](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541850/Grouper+Demo) (not running containers) was upgraded to v2.5.

This server runs multiple versions of Grouper at once. v2.5 will run the UI/WS/Daemon/Scim in one container to save some memory. This should not be done in production.

The demo server has an apache with a domain name and shib, so we will use the external apache (not in container), and point to TomEE inside the container.

[https://grouperdemo.internet2.edu/](https://grouperdemo.internet2.edu/)

## Design decisions

1. Will run tomcat only in container since Apache and Shib are already on host
2. Will run the container as non-root for security reasons since it only has one process
3. Sub-image with Dockerfile will be used so we can adjust the file ownerships in image
4. The only mount needed is the logs directory

## Docker

Lets see what version of docker we are running, make sure starts on boot

```
[root@i2midev6 ~]# docker -v
Docker version 19.03.8, build afacb8b

[root@i2midev6 ~]# sudo systemctl enable docker
Created symlink from /etc/systemd/system/multi-user.target.wants/docker.service to /usr/lib/systemd/system/docker.service.
[root@i2midev6 ~]# /sbin/service docker start
Redirecting to /bin/systemctl start docker.service
[root@i2midev6 ~]# docker ps --all
CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES
[root@i2midev6 ~]# docker images list
REPOSITORY TAG IMAGE ID CREATED SIZE
```

## Get Grouper container

- See which version of Grouper to run
- Pull the image
  
  
  
  | `[root`@i2midev6` ~]# docker pull i2incommon/grouper:``2.5``.XX` |
  | --- |
- Make sure the digest is correct (from release notes page)
  
  
  
  | `[root``@i2midev6` `~]# docker image inspect i2incommon/grouper:``2.5``.XX \| grep i2incommon/grouper``@sha256`   ```"i2incommon/grouper@sha256:b675bb410bf873483497b9b231e7a5db208645e58a3a42a8048381a33b79fd19"` |
  | --- |
- Create a directory to mount files and folder in and out of container. You might have one of these directories that is shared for ws/ui/daemon.
  
  
  
  | [appadmin@i2midev6 container]$ mkdir -p /opt/grouper/2.5/container   [appadmin@i2midev6 container]$ mkdir -p /opt/grouper/2.5/container/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes |
  | --- |

## Import configs to the database

[See this wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560251/Grouper+configuration+in+the+database+migrate+to+on+demo+server)

## Dockerfile

We have a dockerfile so we can change the uid and gid of the user that runs tomcat, so it doesnt run as root

```
# this matches the version you decided on from release notes
ARG GROUPER_VERSION=2.5.29

FROM i2incommon/grouper:${GROUPER_VERSION}

# this will overlay all the files from /opt/grouper/2.5/container on to /
COPY slashRoot /

# [appadmin@i2midev6 bin]$ id
# uid=1870(appadmin) gid=100(users) groups=100(users),10(wheel) context=unconfined_u:unconfined_r:unconfined_t:s0-s0:c0.c1023

# Note, since the gid 100 is in container and not used: "users", it will be removed
# note  since the user "games" has primary gid of 100, need to get rid of that too.  And need to get rid of group games too for some reason

RUN groupdel games \
  && userdel games \
  && groupdel users \
  && /usr/local/bin/changeUid.sh tomcat 1870 \
  && /usr/local/bin/changeGid.sh tomcat 100 \
  && chown -R 1870:100 /opt/grouper \
  && chown -R 1870:100 /opt/tomee
```

Build sub-image command

```
[root@i2midev6 container]# more grouperBuildContainer_v2_5.sh 
#!/bin/bash

docker build -t demo-grouper-2.5 /opt/grouper/2.5/container
```

## Files injected in sub-image

```
[root@i2midev6 slashRoot]# find
.
./opt
./opt/grouper
./opt/grouper/grouperWebapp
./opt/grouper/grouperWebapp/WEB-INF
./opt/grouper/grouperWebapp/WEB-INF/classes
./opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties
./opt/grouper/grouperWebapp/WEB-INF/classes/encrypt.key
./opt/grouper/grouperWebapp/WEB-INF/classes/morphString.properties
```

These files are as thin as possible

```
[root@i2midev6 slashRoot]# more opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties 
hibernate.connection.url = jdbc:mysql://localhost:3306/grouper_v2_5?CharSet=utf8&useUnicode=true&characterEncoding=utf8

hibernate.connection.username    = grouper_v2_5

hibernate.connection.password         = ****

```

## Docker RUN command

```
[root@i2midev6 container]# more grouperRunContainer_v2_5.sh
#!/bin/bash

docker run --net=host \
--detach \
-u 1870 \
--mount type=bind,src=/opt/grouper/2.5/container/logs,dst=/opt/grouper/logs \
-e RUN_SHIB_SP='false' \
-e GROUPER_CHOWN_DIRS=false \
-e GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true \
-e GROUPER_TOMCAT_CONTEXT=grouper_v2_5 \
-e RUN_APACHE='false' \
-e GROUPER_DAEMON=true \
-e GROUPER_WS_GROUPER_AUTH=true \
-e GROUPER_MAX_MEMORY='3g' \
-e GROUPER_TOMCAT_AJP_PORT=8191  \
-e GROUPER_TOMCAT_HTTP_PORT=8190 \
-e GROUPER_TOMCAT_SHUTDOWN_PORT=8192 \
-e GROUPER_SCIM=true \
-e GROUPER_SCIM_GROUPER_AUTH=true \
-e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*' \
-e GROUPER_LOG_TO_HOST=true \
--name grouper_v2_5 demo-grouper-2.5:latest ui-ws
```

| **Run command argument** | **Description** |
| --- | --- |
| docker run | Run the container |
| --net=host | Networking in container is like a host app. mysql localhost database is still accessible to localhost in container even though thats on the host, not in container. |
| --detach | Run in background |
| -u 1870 | Run container as "appadmin" user on host which is uid 1870 |
| --mount type=bind,src=/opt/grouper/2.5/container/logs,dst=/opt/grouper/logs | Mount the log dir from in container to out of container |
| -e RUN_SHIB_SP='false' | Dont run shib in container since its on the host |
| -e GROUPER_CHOWN_DIRS=false | Starting container as non-root, dont chown dirs, this is dont in subimage |
| -e GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true | We have one process here, just run tomcat and not container supervisor |
| -e GROUPER_TOMCAT_CONTEXT=grouper_v2_5 | We will have the tomee context for the webapp match the apache context (first part of URL after domain name) |
| -e RUN_APACHE='false' | Dont run apache we have it in the host |
| -e GROUPER_DAEMON=true | Run the daemon |
| -e GROUPER_WS_GROUPER_AUTH=true | Do Grouper authn for WS |
| -e GROUPER_MAX_MEMORY='3g' | Tomee memory. This is a low amount for daemon/UI/WS/Scim, but the demo server is low activity |
| -e GROUPER_TOMCAT_AJP_PORT=8191 | Because we are net=host, we need this to not conflict with other tomcats on server. Apache connects to the AJP port |
| -e GROUPER_TOMCAT_HTTP_PORT=8190 | Because we are net=host, we need this to not conflict with other tomcats on server |
| -e GROUPER_TOMCAT_SHUTDOWN_PORT=8192 | Because we are net=host, we need this to not conflict with other tomcats on server |
| -e GROUPER_SCIM=true | Run SCIM server |
| -e GROUPER_SCIM_GROUPER_AUTH=true | Do Grouper authn for scim |
| -e GROUPER_AUTO_DDL_UPTOVERSION='v2.5.*' | Auto upgrade DDL in v2.5 |
| -e GROUPER_LOG_TO_HOST=true | Dont log to pipes, log to mounted log dir on host |
| --name grouper_v2_5 | Container name |
| demo-grouper-2.5:latest | Sub-image name from above |
| ui-ws | Run the UI and WS (atleast) |

## Add account for WS and Scim

Add a local entity

Set the password

```
#  docker exec -u tomcat -it grouper_v2_5 /bin/bash
[tomcat@i2midev6 WEB-INF]$ cd bin
[tomcat@i2midev6 bin]$ ./gsh.sh
gsh 0% new GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("test:local").assignPassword("********").save();           

```

Try WS: [https://grouperdemo.internet2.edu/grouper-ws_v2_5/servicesRest/v2_5_000/groups/test%3AtestGroup/members](https://grouperdemo.internet2.edu/grouper-ws_v2_5/servicesRest/v2_5_000/groups/test%3AtestGroup/members) (login with test:local / ******* )  
Try SCIM: [https://grouperdemo.internet2.edu/grouper-ws-scim_v2_5/v2/Groups/](https://grouperdemo.internet2.edu/grouper-ws-scim_v2_5/v2/Groups/) (login with test:local / ******* )

## Apache config

```
/etc/httpd/conf.d/grouper.conf

#match anything that is grouperExternal
<LocationMatch ^/grouper_v2_5[^/]*/grouperExternal/.*>
  AuthType shibboleth
  ShibRequestSetting requireSession 1
  require valid-user

</LocationMatch>

ProxyPass /grouper-ws_v2_5/ ajp://localhost:8191/grouper_v2_5/

ProxyPass /grouper-ws-scim_v2_5/ ajp://localhost:8191/grouper_v2_5/

ProxyPass /grouper_v2_5/ ajp://localhost:8191/grouper_v2_5/

ProxyPass /status_grouper_v2_5/status ajp://localhost:8191/grouper_v2_5/status

```

## Memory on host

First we will shut down some old versions to save some memory.

```
 1009  /sbin/service tomcat_a stop
 1010  /sbin/service tomcat_c stop
 1011  /sbin/service tomcat_d stop
 1012  /sbin/service tomcat_i stop
 1013  /sbin/service tomcat_j stop
 1014  /sbin/service tomcat_k stop
[root@i2midev6 ~]# chkconfig --del tomcat_a
[root@i2midev6 ~]# chkconfig --del tomcat_c
[root@i2midev6 ~]# chkconfig --del tomcat_d
[root@i2midev6 ~]# chkconfig --del tomcat_i
[root@i2midev6 ~]# chkconfig --del tomcat_j
[root@i2midev6 ~]# chkconfig --del tomcat_k

[root@i2midev6 ~]# free
              total        used        free      shared  buff/cache   available
Mem:       14202688     5299732     1079660        9576     7823296     8563136
Swap:       2097148           0     2097148

```

## Database

Lets clone our database from 2.4 to 2.5. This is SQLYog (mysql windows tool), but you can use whatever database tool you want

Create a local database. Preferred is postgres, but could be mysql or oracle too. (e.g. mysql, utf8, bin collation, create a user and password, and grant all to the new database from username and password)

Create a user, and grant all to that database from the user (user: grouper_v2_5)

Clone the 2.4 database with the [Grouper database migration utility](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549706/Grouper+database+migration+utility)
