---
key: GRP-29
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-29
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Blair Christensen <blair@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-08-24T15:43:37.364+0000
updated: 2011-02-28T13:28:35.436+0000
resolved: 2011-02-28T13:28:35.339+0000
components: [API]
fixVersions: []
labels: []
links: [has dependent GRP-30]
---

# GRP-29  Adding caching to "privs.NamingResolver#get()" methods

This may also require changes to how/when GrouperAll resolution is performed.  GrouperAll resolution is not currently done in these methods but it probably should be.
