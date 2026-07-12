---
title: "Display an SSI and server label in UI docker container"
space: Grouper
pageId: 28545325
version: 2
lastUpdated: 2019-11-15T14:50:51.367Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545325/Display+an+SSI+and+server+label+in+UI+docker+container
---

We want to be able to identify which server a UI user is using for the purposes of:

1. Knowing which logs to filter
2. Assurance the correct container is being used when AWS changes versions of containers during a commit/CICD

Note we want this to be secure and not provide an attack vector, so just display the last octet of the IP address, which will identify it on the AWS ECS panel in the AWS UI

### Turn on SSI

In the apache file which is already overridden: grouper-[www.conf](http://www.conf)

```
<Directory "/var/www/html">
    Options +Includes +ExecCGI
</Directory>
```

### Make a script to display last octet

ip4.sh

```
#!/usr/bin/bash                                                                                                                                                                     
ifconfig | grep -A 1 '^eth0' | tail -1 | grep -oE '[0-9]{1,3}' | head -4 | tail -1
```

copy in Dockerfile:

```
COPY /configs-and-secrets/httpd/ip4.sh /opt/grouper/grouper.apiBinary/bin/ip4.sh
RUN chmod +x /opt/grouper/grouper.apiBinary/bin/ip4.sh
RUN dos2unix /opt/grouper/grouper.apiBinary/bin/ip4.sh
```

test it:

```
[root@c6d6d43d837c ~]# /opt/grouper/grouper.apiBinary/bin/ip4.sh 
220
```

### Make an SSI html page

ip4.shtml

```
ip4: <!--#exec cmd="/opt/grouper/grouper.apiBinary/bin/ip4.sh" -->
```

copy in Dockerfile

```
COPY /configs-and-secrets/httpd/ip4.shtml /var/www/html/ip4.shtml
```

### Browser demo

Show in aws
