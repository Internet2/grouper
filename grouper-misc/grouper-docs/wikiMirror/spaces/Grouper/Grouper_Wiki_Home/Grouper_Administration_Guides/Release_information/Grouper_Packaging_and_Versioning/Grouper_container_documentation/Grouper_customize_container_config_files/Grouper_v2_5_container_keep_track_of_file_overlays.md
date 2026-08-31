---
title: "Grouper v2.5 container keep track of file overlays"
space: Grouper
pageId: 28560318
version: 5
lastUpdated: 2026-07-01T05:35:50.851Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560318/Grouper+v2.5+container+keep+track+of+file+overlays
---

In v2.5.36+ Grouper will keep track of files that it will edit and see if they have an overlay in the container or via mounted slashRoot.

When the container is built, the original file contents will be copied here:

```
[root@dbcd42683952 originalFiles]# pwd
/opt/tier-support/originalFiles
[root@dbcd42683952 originalFiles]# ls -latr
total 56
-rw-r--r-- 1 root root   291 Oct 18 20:52 web.xml
-rwxr-xr-x 1 root root  1167 Oct 18 20:52 ssl-enabled.conf
-rw-r--r-- 1 root root  1389 Oct 18 20:52 shib.conf
-rw-r--r-- 1 root root  7987 Oct 18 20:52 server.xml
-rwxr-xr-x 1 root root  6659 Oct 18 20:52 log4j.properties
-rw-r--r-- 1 root root 12033 Oct 18 20:52 httpd.conf
-rwxr-xr-x 1 root root   190 Oct 18 20:52 grouper.xml
drwxr-xr-x 2 root root  4096 Oct 18 20:52 .
drwxr-xr-x 1 root root  4096 Oct 20 03:28 ..
[root@dbcd42683952 originalFiles]#
```

Then when the container is started, after the slashRoot is copied, the current files are compared to these copies and env vars are set

```
GROUPER_ORIGFILE_GROUPER_XML=true
GROUPER_ORIGFILE_HTTPD_CONF=true
GROUPER_ORIGFILE_HTTPD_SHIB_CONF=false
GROUPER_ORIGFILE_LOG4J_PROPERTIES=true
GROUPER_ORIGFILE_SERVER_XML=true
GROUPER_ORIGFILE_SHIB_CONF=false
GROUPER_ORIGFILE_SSL_ENABLED_CONF=true
GROUPER_ORIGFILE_WEBAPP_WEB_XML=true
```

As the container is preparing files based on env vars, it will not copy over files or patch files that are not the original file in the container. i.e. if it has an overlay then let it be and do not throw an error. Note that if there is a "sed" replacement that will still take place and if the search string is not there it will be ignored.
