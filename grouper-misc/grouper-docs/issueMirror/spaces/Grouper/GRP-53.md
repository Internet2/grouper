---
key: GRP-53
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-53
type: Improvement
status: Closed
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-10-31T17:37:30.294+0000
updated: 2011-02-16T18:13:22.396+0000
resolved: 2011-02-16T18:13:04.799+0000
components: [API]
fixVersions: []
labels: []
links: [depends on GRP-136]
---

# GRP-53  Privilege checking in GrouperQuery

Perform privilege checking in GrouperQuery instead of Filter classes.

## Comments

### tbarton - 2007-11-15T03:24:37.635+0000

Reschedule for v1.3.0. We'll explore alternatives to performing the priv checking in GrouperQuery that have been raised on the dev list.

### shilen - 2008-03-19T17:40:44.816+0000

Note that this issue is waiting for the modified Grouper search code worked on by Gary.

### shilen - 2011-02-16T18:13:04.894+0000

Privilege checking was added to the relevant database queries a while back.