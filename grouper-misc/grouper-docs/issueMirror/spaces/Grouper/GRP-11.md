---
key: GRP-11
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-11
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-07-23T14:59:14.053+0000
updated: 2007-11-17T17:49:14.381+0000
resolved: 2007-08-13T18:04:26.794+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-11  StemFinder#internal_isChild generates spurious error logging

I believe the current ErrorLog'd message is actually more of a debug statement.  Verify that and then either eliminate or modify.

## Comments

### blair@example.com - 2007-07-23T15:00:04.355+0000

Increase "Priority" as this could generate a severe amount of logging output.

### blair@example.com - 2007-08-13T18:04:26.790+0000

Fixed.  

A few details at  http://groupertoolkit.blogspot.com/2007/08/grp-11-eliminate-spurious-error-logging.html