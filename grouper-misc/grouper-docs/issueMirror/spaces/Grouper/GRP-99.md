---
key: GRP-99
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-99
type: New Feature
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-03-20T03:11:11.446+0000
updated: 2008-03-20T03:11:35.080+0000
resolved: 2008-03-20T03:11:35.081+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-99  add transactions and inverse of control to grouper

Database management is sensitive since unclosed resources can cause slow resource leaks which can be difficult to troubleshoot.  Inverse of control is a technique which makes DB access a little unusual, but completely solves this problem, and also ensures proper and consistent error handling.

Transactions are usual so units of work can be grouper together and they can commit or rollback atomically.

Both are added to grouper.

https://wiki.internet2.edu/confluence/display/GrouperWG/Hibernate+and+data+layer+updates

## Comments

### mchyzer - 2008-03-20T03:11:35.079+0000

This is done