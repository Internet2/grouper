---
title: "Grouper changes v2.2"
space: GrIntDev
pageId: 48793454
version: 16
lastUpdated: 2026-07-12T06:46:15.400Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793454/Grouper+changes+v2.2
---

> [http://www.youtube.com/watch?v=dE67V38Jes0](http://www.youtube.com/watch?v=dE67V38Jes0) This topic is discussed in the ["Grouper Minor Upgrade" training video](http://www.youtube.com/watch?v=dE67V38Jes0).

This document lists instructions for people with existing groups installations on how to upgrade to newer versions of Grouper (or Grouper related products). If you notice something missing please let us know. The instructions are in descending order based on date/release. You will find instructions below for Grouper, Grouper-ws, Grouper-ui, etc. It is assumed if you are running grouper-ui that you will perform both the grouper upgrade notes, and the grouper-ui upgrade notes. It is understood that you will get the new source/javadoc/etc files, this document addresses configurations, jars, etc.

#### Grouper installer

- v2.2.2: Get the latest grouperInstaller.jar

#### Grouper

- v2.2.2: Update the grouperClient.jar
- v2.2.2: Update the subject.jar
- v2.2.2: Update the grouper.jar
- v2.2.2: Merge the ehcache.example.xml with your ehcache.xml
- v2.2.2: Replace grouper.base.properties
- v2.2.2: Replace grouper-loader.base.properties
- v2.2.2: Add grouperUtf8.txt
- v2.2.2: Replace gsh.bat and gsh.sh
- v2.2.2: (and every upgrade) clear out grouperPatchStatus.properties
- v2.2.0: Update hibernate.jar -- Contains patch for [https://hibernate.atlassian.net/browse/HHH-4959](https://hibernate.atlassian.net/browse/HHH-4959)

#### Grouper UI

- v2.2.2: Update the grouper-ui.base.properties
- v2.2.2: Update the grouper.text.en.us.base.properties
- v2.2.2: Update the grouper-ui.jar
- v2.2.2: Update various jsp's and js's etc
- v2.2.0: Merge the web.ajax.xml and web.core.xml if you made changes
- v2.2.0: The status servlet is available in the UI. If you want to take advantage of this, you might need to map a new URL if your diagnostics app (e.g. nagios) cannot authenticate. on the demo server this is what was changed in the apache config, then bounce apache, and you can see status here: [https://grouperdemo.internet2.edu/grouperstatus_v2_2/status?diagnosticType=sources](https://grouperdemo.internet2.edu/grouperstatus_v2_2/status?diagnosticType=sources)

```
FROM:

ProxyPass /grouper_v2_2/ ajp://localhost:8111/grouper_v2_2/

TO: (note, new config is above old config since more specific, to avoid apache warning)

# unauthenticated status
ProxyPass /grouperstatus_v2_2/status ajp://localhost:8111/grouper_v2_2/status

ProxyPass /grouper_v2_2/ ajp://localhost:8111/grouper_v2_2/

```

- v2.2.0: There are 2 error servlets as well

```
ProxyPass /grouper_v2_2/grouperExternal/public/UiV2Public.index ajp://localhost:8111/grouper_v2_2/grouperExternal/public/UiV2Public.index

ProxyPass /grouper_v2_2/grouperExternal/public/UiV2Public.postIndex ajp://localhost:8111/grouper_v2_2/grouperExternal/public/UiV2Public.postIndex

```

- v2.2.0: Add the csrfguard jar
- v2.2.0: Add the csrfguard properties files

#### Grouper WS

- v2.2.2: update the grouper-ws.jar
- v2.2.2: update the services aar's
- v2.2.0: The status servlet moved to the API, if you use this, migrate any configs from the WS config to the API config. Note, the config keys did not change.

#### Subject API

- v2.2.2: update the subject.jar
- v2.2.2: merge the sources.example.xml with your sources.xml
- v2.2: If you implement a custom source which does not extend the BaseSourceAdapter, you will need to recompile your source with change for realms

#### Grouper Client

- v2.2.2: update grouper.client.base.properties

#### See Also

[Upgrading from Grouper 2.1 from 2.2](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793443/v2.2+Upgrade+Instructions+from+v2.1)
