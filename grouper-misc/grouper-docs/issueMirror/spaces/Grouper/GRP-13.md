---
key: GRP-13
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-13
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Kathryn Huxtable <kathryn@example.com>
created: 2007-07-23T15:03:45.274+0000
updated: 2008-04-02T16:16:10.425+0000
resolved: 2008-04-02T16:16:10.427+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: [depends on GRP-9]
---

# GRP-13  Poor integration with Ldappc: attribute-matching-queries generates full table scan

Ldappc uses "GroupAttributeFilter" but that internally calls code that will generate a full table scan.

## Comments

### Kathryn Huxtable - 2008-03-27T19:04:02.998+0000

New GroupAttributeExactFilter removes "like" and other wildcarding from the search. There is no need or desire for the attribute query to be case-indistinct. It should be exact and it will be.

This isn't checked in because it's waiting on a tagged version of the Grouper API containing the new filter.

### Kathryn Huxtable - 2008-04-02T16:16:10.418+0000

Checked in snapshot of Grouper API containing needed class.

This can be closed once we have a release of Grouper API 1.3.0.