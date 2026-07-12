---
title: "Grouper loader failsafe - provisioning framework"
space: Grouper
pageId: 28560060
version: 5
lastUpdated: 2026-07-01T05:36:27.717Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560060/Grouper+loader+failsafe+-+provisioning+framework
---

v2.6.6+ allows failsafe options on provisioning

If a full or incremental sync has a failsafe problem, then incrementals will fail until there is an approval. If an incremental fails, events will queue up until approved

## Configure failsafes on provisioning
