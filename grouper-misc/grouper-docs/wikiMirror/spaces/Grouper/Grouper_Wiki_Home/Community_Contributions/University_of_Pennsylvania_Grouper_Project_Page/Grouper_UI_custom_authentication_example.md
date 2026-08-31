---
title: "Grouper UI custom authentication example"
space: Grouper
pageId: 28545158
version: 62
lastUpdated: 2026-07-01T05:47:41.434Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545158/Grouper+UI+custom+authentication+example
---

#### 

#### Grouper UI authentication

The Grouper UI will check request attribute REMOTE_USER, or request.getUserPrincipal(), or request.getRemoteUser(), so it should work with common SSO solutions (e.g. Shib, Cosign, etc)

See debug information in logs in log4j.properties

```
log4j.logger.edu.internet2.middleware.grouper.ui.GrouperUiFilter = DEBUG
```
