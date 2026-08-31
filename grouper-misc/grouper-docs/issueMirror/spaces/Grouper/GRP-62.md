---
key: GRP-62
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-62
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-17T17:18:27.452+0000
updated: 2007-11-17T17:19:54.253+0000
resolved: 2007-11-17T17:19:54.254+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-62  Reduce cost of instantiating ObjectAsMap wrappers

In general, the UI wraps Grouper objects (Groups, Stems, Subjects, Memberships etc), with a Map. The constructor for some implementations pre-emptively instantiate objects e.g. GroupAsMap instantiates its parent Stem whether or not it is needed.

## Comments

### Gary Brown - 2007-11-17T17:19:54.252+0000

Changed code to use 'lazy instantiation' so that expensive calls are only made if required