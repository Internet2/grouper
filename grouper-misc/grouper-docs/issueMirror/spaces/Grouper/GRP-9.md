---
key: GRP-9
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-9
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Kathryn Huxtable <kathryn@example.com>
created: 2007-07-23T14:55:23.377+0000
updated: 2008-04-02T16:16:51.163+0000
resolved: 2008-04-02T16:16:51.164+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: [has dependent GRP-13]
---

# GRP-9  Group attribute queries generate full table scans

A significant number of queries against the "grouper_attributes" table generate full table scans.  Document methods where this is the case and modify-or-provide-alternate queries that will perform better.

## Comments

### Kathryn Huxtable - 2008-04-02T16:16:51.161+0000

See GRP-13. Same thing.