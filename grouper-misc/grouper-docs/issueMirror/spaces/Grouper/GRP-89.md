---
key: GRP-89
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-89
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2008-02-13T14:47:35.006+0000
updated: 2008-04-15T01:09:17.895+0000
resolved: 2008-04-15T01:09:17.896+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-89  Add Foreign Keys

After the hib3 upgrade, add the appropriate foreign keys to the Grouper tables.

## Comments

### shilen - 2008-04-15T01:09:17.887+0000

Used ddlutils to add the foreign keys.  The foreign keys are listed in grouper/src/grouper/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3ForeignKeys.xml.