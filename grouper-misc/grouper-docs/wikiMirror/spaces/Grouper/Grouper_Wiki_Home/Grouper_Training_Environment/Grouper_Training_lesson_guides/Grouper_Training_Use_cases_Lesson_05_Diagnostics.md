---
title: "Grouper Training - Use cases - Lesson 05: Diagnostics"
space: Grouper
pageId: 28544276
version: 11
lastUpdated: 2026-04-22T01:09:13.466Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544276/Grouper+Training+-+Use+cases+-+Lesson+05+Diagnostics
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Example URL:**

```
https://localhost:8443/grouper-ws/status?diagnosticType=trivial

https://localhost:8443/grouper/status?diagnosticType=trivial
https://localhost:8443/grouper/status?diagnosticType=db
https://localhost:8443/grouper/status?diagnosticType=sources
https://localhost:8443/grouper/status?diagnosticType=all

https://localhost:8443/grouper/status?diagnosticType=daemonJobsOnly&includeOnly=loader_CHANGE_LOG_changeLogTempToChangeLog
```
