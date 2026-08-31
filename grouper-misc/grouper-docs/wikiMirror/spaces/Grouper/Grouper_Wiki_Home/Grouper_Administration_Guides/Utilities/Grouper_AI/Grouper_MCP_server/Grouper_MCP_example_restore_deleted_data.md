---
title: "Grouper MCP example: restore deleted data"
space: Grouper
pageId: 28555704
version: 3
lastUpdated: 2026-07-01T05:37:37.705Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555704/Grouper+MCP+example+restore+deleted+data
---

Claude code and MCP can restore deleted data that is stored in PIT. e.g. attribute assignments: yes. group descriptions: no.

Note: this is a contrived example since I was not restoring anything. If you are actually restoring you would use the "etc" folder and not "penn:etc", and you would use the disabled_time and not created_on. Also this is postgres specific, you can translate for your database. Here is [the SQL/GSH for this example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549473/GSH+script+to+restore+a+mistakenly+deleted+attribute+definition).
