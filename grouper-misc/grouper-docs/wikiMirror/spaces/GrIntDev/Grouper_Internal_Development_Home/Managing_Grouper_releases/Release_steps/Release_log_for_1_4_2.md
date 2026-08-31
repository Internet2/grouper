---
title: "Release log for 1.4.2"
space: GrIntDev
pageId: 48793767
version: 3
lastUpdated: 2026-07-12T07:02:02.211Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793767/Release+log+for+1.4.2
---

| Action | Initials | Timestamp | Notes |
| --- | --- | --- | --- |
| Merge all CVS folders with previous branches | CH | 5/28/9 1am | NA |
| Run junit test suite on windows/mysql | CH | 5/28/9 2am | Success |
| Make sure all version files are updated:GrouperVersion.java, API misc/version.properties,     client misc/version.properties, conf/grouper.client.example.properties, UI misc/version.properties,     WS misc/version.properties, grouper-ws.example.properties | CH | 5/28/9 5pm | Done |
| Do a clean grouper API build, then an ant dist in grouper API, then an "ant grouper" for WS.     Copy any new grouper/conf configs to grouper-ws/resources | CH | 5/28/9 5pm | Done |
| Tag all (API, UI, WS, QS, client), 1.4 branches as GROUPER_1_4_2 | CH | 5/29/9 1am | Done |
| Build tarfiles on CVS machine:     [mchyzer@ellis bin]$ ./buildGrouper.sh GROUPER_1_4_2 | CH | 5/29/9 1am | Done |
| Copy tars to webserver (webprod0.internet2.edu:     /home/htdocs/middleware.internet2.edu/dir/groups/grouper/ | CH | 5/29/9 1am | Done |
| Run WS related junit tests for Grouper Client. | SP | 5/29/9 2pm | Success |
| Run junit tests for Grouper WS. | SP | 5/29/9 2pm | Success |
| Run junit test suite (API) on mac/oracle 10g | SP | 5/30/9 12pm | Success |
| Created QuickStart tgz | GB | 6/2/9 11am | Done |
| See which config files / jar files / ddl changed since last release,     make sure there are entries in the change log | CH | 6/4/9 | Done |
| update Archives page with info from the current vN.N+Release+Notes page     and links from the Software+Download page. | CH | 6/4/9 | Done |
| update software download, release notes page, product page, grouper home | CH | 6/4/9 | Done |
| run unit tests on mysql linux from binary tarball | CH | 6/4/9 | Done |
| compose release notes email | CH | 6/4/9 | Done |
| run unit tests on postgres windows from source tarball (note, there is a known ddl issue) | CH | 6/4/9 | Done |
| create ui, ws, gc javadoc, sftp to www site | CH | 6/5/9 | Done |
| download, build, and run the quickstart (hsql) | CH | 6/5/9 | Done |
| download, build, and run the UI (hsql) | CH | 6/5/9 | Done |
| download, build, and run the WS (hsql) | CH | 6/5/9 | Done |
| download, build, and run the grouper client (source tarball), hit web service | CH | 6/5/9 | Done |
| download, unzip, run the grouper client (binary tarball), hit web service | CH | 6/5/9 | Done |
