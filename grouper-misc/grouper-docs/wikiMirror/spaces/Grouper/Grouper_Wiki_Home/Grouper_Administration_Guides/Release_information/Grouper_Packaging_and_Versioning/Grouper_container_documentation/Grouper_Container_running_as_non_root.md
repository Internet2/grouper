---
title: "Grouper Container running as non-root"
space: Grouper
pageId: 28555631
version: 12
lastUpdated: 2026-07-01T05:37:45.778Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555631/Grouper+Container+running+as+non-root
---

## Option 1 - Just run tomcat in container, do web server/authn externally (2.5.28+)

You will start docker as a user and group. So establish that outside of the container. This will just use an existing user. You can create a new user if you like

```
grouperContainer $ id
uid=501(mchyzer) gid=20(staff)
```

In your dockerfile, change tomcat user and group in container to match the uid/gid.

```
# Note, since the gid 20 is in container and not used: "games", it will be removed
RUN groupdel games

RUN /usr/local/bin/changeUid.sh tomcat 501 \
  && /usr/local/bin/changeGid.sh tomcat 20

# chown is needed if copying files, dont want them owned by other than the user
RUN chown -R 501:20 /opt/grouper \
  && chown -R 501:20 /opt/tomee
```

Start container as a user or uid, and tell grouper to start tomcat instead of supervisord

```
$ docker build -t my-grouper-2.5.28 /Users/mchyzer/grouper/docker/grouperContainer

$ docker run --detach --name grouper-ui -u 501 -e GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true --publish 8080:8080 my-grouper-2.5.28:latest ui

```

See the processes inside

```
$ docker exec -it -u 501 grouper-ui /bin/bash

[tomcat@a1c5ce3cb8eb WEB-INF]$ ps -ef
UID        PID  PPID  C STIME TTY          TIME CMD
tomcat       1     0  0 18:27 ?        00:00:00 /bin/bash /usr/local/bin/ui
tomcat       8     1  0 18:27 ?        00:00:00 cat
tomcat      10     1  0 18:27 ?        00:00:00 /bin/sh /usr/local/bin/entrypoint.sh ui
tomcat      11    10  0 18:27 ?        00:00:00 cat
tomcat      12    10  0 18:27 ?        00:00:00 awk -v ENV= -v UT= {printf "supervisord;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
tomcat      14     1  0 18:27 ?        00:00:00 /bin/sh /usr/local/bin/entrypoint.sh ui
tomcat      15    14  0 18:27 ?        00:00:00 cat
tomcat      16    14  0 18:27 ?        00:00:00 awk -v ENV= -v UT= {printf "grouper;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
tomcat      28     1  0 18:27 ?        00:00:00 /bin/bash /usr/local/bin/ui
tomcat      29    28  0 18:27 ?        00:00:00 cat
tomcat      30    28  0 18:27 ?        00:00:00 awk -v ENV= -v UT= {printf "tomee;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
tomcat      40     1 99 18:27 ?        00:00:38 /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/java -Dnop -Djava.util.logging.manager=org.apache.logging.log4j.jul.Lo
tomcat      86     0  0 18:27 pts/0    00:00:00 /bin/bash
tomcat     174    86  0 18:28 pts/0    00:00:00 ps -ef
[tomcat@a1c5ce3cb8eb WEB-INF]$ id
uid=501(tomcat) gid=20(tomcat) groups=20(tomcat)
[tomcat@a1c5ce3cb8eb WEB-INF]$ 

```

## Option 2 - Run all processes as another user with supervisor

This is not really recommended but it is a way to go.

Dockerfile should add a user and group with uid and gid, chown a bunch of files, and all apache to listen on privileged ports. (note, private keys are included here which is a security issue)

```
[root@ip-172-30-3-152 grouperContainer]# more Dockerfile
# this matches the version you decided on from release notes
ARG GROUPER_VERSION=2.5.28

FROM i2incommon/grouper:${GROUPER_VERSION}

# this will overlay all the files from /opt/grouperContainer/slashRoot on to /
COPY slashRoot /

# this means run all processes as the user running the container
ENV GROUPER_RUN_PROCESSES_AS_USERS=false

# create the user to use
RUN groupadd -g 834 i2grouper
RUN useradd -u 834 -g i2grouper i2grouper

# we know certain owners need to change
RUN chown -R i2grouper:i2grouper /opt/tier-support
RUN chown -R i2grouper:i2grouper /opt/grouper
RUN chown -R i2grouper:i2grouper /opt/tomee
RUN chown -R i2grouper:i2grouper /var/log/supervisor
RUN chown -R i2grouper:i2grouper /etc/pki/tls/certs
RUN chown -R i2grouper:i2grouper /etc/pki/tls/private
RUN chown -R i2grouper:i2grouper /etc/httpd/conf.d
RUN chown -R i2grouper:i2grouper /run/httpd
RUN chown -R i2grouper:i2grouper /run/supervisor

# search for more files to change ownership. Note, dont search / since it looks in procs and gives errors.  These dirs should be sufficient
RUN find /var -user shibd -exec chown i2grouper:i2grouper {} \;
RUN find /run -user shibd -exec chown i2grouper:i2grouper {} \;
RUN find /etc -user shibd -exec chown i2grouper:i2grouper {} \;
RUN find /opt -user shibd -exec chown i2grouper:i2grouper {} \;

RUN find /var -user apache -exec chown i2grouper:i2grouper {} \;
RUN find /run -user apache -exec chown i2grouper:i2grouper {} \;
RUN find /etc -user apache -exec chown i2grouper:i2grouper {} \;
RUN find /opt -user apache -exec chown i2grouper:i2grouper {} \;

RUN find /var -user tomcat -exec chown i2grouper:i2grouper {} \;
RUN find /run -user tomcat -exec chown i2grouper:i2grouper {} \;
RUN find /etc -user tomcat -exec chown i2grouper:i2grouper {} \;
RUN find /opt -user tomcat -exec chown i2grouper:i2grouper {} \;

# allow apache to listen on privileged ports in container
RUN setcap 'cap_net_bind_service=+ep' /usr/sbin/httpd
```

Build the container

```
# docker build -t sub-grouper-2.5.28 /opt/grouperContainer
```

Create the i2grouper user and group on host

```
# groupadd -g 834 i2grouper
# useradd -u 834 -g i2grouper i2grouper
```

Run it, note SSL on 443 works

```
# docker run --detach --user i2grouper --mount type=bind,src=/opt/grouperContainer/logs,dst=/opt/grouper/logs --mount type=bind,src=/opt/grouperContainer/slashRoot,dst=/opt/grouper/slashRoot -e SELF_SIGNED_CERT='true' --publish 443:443 --name grouper-ui2 sub-grouper-2.5.28:latest ui
```

Go in as root and see what processes are running, note they are all running as non root (except the shell to go in and check)

```
[root@ip-172-30-3-152 grouperContainer]# docker exec -it --user root grouper-ui2 /bin/bash
[root@4a5c3134f1cd WEB-INF]# ps -ef
UID        PID  PPID  C STIME TTY          TIME CMD
i2group+     1     0  0 07:19 ?        00:00:00 /usr/bin/python /usr/bin/supervisord -c /opt/tier-support/supervisord.conf
i2group+    17     1  0 07:19 ?        00:00:00 cat
i2group+    19     1  0 07:19 ?        00:00:00 /bin/bash /usr/local/bin/ui
i2group+    21    19  0 07:19 ?        00:00:00 cat
i2group+    22    19  0 07:19 ?        00:00:00 awk -v ENV= -v UT= {printf "grouper;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
i2group+    23     1  0 07:19 ?        00:00:00 /bin/bash /usr/local/bin/ui
i2group+    24    23  0 07:19 ?        00:00:00 cat
i2group+    26    23  0 07:19 ?        00:00:00 awk -v ENV= -v UT= {printf "httpd;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
i2group+    27     1  0 07:19 ?        00:00:00 /bin/bash /usr/local/bin/ui
i2group+    29    27  0 07:19 ?        00:00:00 cat
i2group+    30    27  0 07:19 ?        00:00:00 awk -v ENV= -v UT= {printf "shibd;console;%s;%s;%s", ENV, UT, $0; fflush()}
i2group+    31     1  0 07:19 ?        00:00:00 /bin/bash /usr/local/bin/ui
i2group+    33    31  0 07:19 ?        00:00:00 cat
i2group+    34    31  0 07:19 ?        00:00:00 awk -v ENV= -v UT= {printf "tomee;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
i2group+    35     1  0 07:19 ?        00:00:00 /bin/bash /usr/local/bin/ui
i2group+    37    35  0 07:19 ?        00:00:00 cat
i2group+    38    35  0 07:19 ?        00:00:00 awk -v ENV= -v UT= {printf "supervisord;console;%s;%s;%s\n", ENV, UT, $0; fflush()}
i2group+    47     1  0 07:19 ?        00:00:00 httpd -DFOREGROUND
i2group+    48     1 84 07:19 ?        00:00:26 /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/java -Dnop -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -javaagent:/opt/tomee/lib/openej
i2group+    49     1  0 07:19 ?        00:00:00 /usr/sbin/shibd -f -F
i2group+    67    47  0 07:19 ?        00:00:00 httpd -DFOREGROUND
i2group+    68    47  0 07:19 ?        00:00:00 httpd -DFOREGROUND
i2group+    70    47  0 07:19 ?        00:00:00 httpd -DFOREGROUND
i2group+    71    47  0 07:19 ?        00:00:00 httpd -DFOREGROUND
i2group+    72    47  0 07:19 ?        00:00:00 httpd -DFOREGROUND
root       162     0  1 07:19 pts/0    00:00:00 /bin/bash
root       179   162  0 07:19 pts/0    00:00:00 ps -ef
[root@4a5c3134f1cd WEB-INF]# exit
```

Try the UI, it works!
