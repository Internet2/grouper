---
title: "Grouper hook to not allow group delete if group is provisionable"
space: Grouper
pageId: 28547542
version: 2
lastUpdated: 2026-07-01T05:46:34.562Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547542/Grouper+hook+to+not+allow+group+delete+if+group+is+provisionable
---

This is available in v5.22.5+.

Set this in the grouper.properties (or add hook to existing hooks comma-separated)

```
hooks.group.class=edu.internet2.middleware.grouper.hooks.examples.GroupDoNotDeleteIfProvisionable
```
