---
key: GRP-98
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-98
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-03-19T20:53:16.594+0000
updated: 2008-07-30T06:59:02.134+0000
resolved: 2008-07-30T06:59:02.137+0000
components: [API]
fixVersions: [1.4.0]
labels: []
links: []
---

# GRP-98  remove the unique id cols in many tables, in favor of keeping the uuid col

An aspect that Tom mentioned about this is that there might be utility in having a unique id for each row which doesnt change.  I would like to discuss this point.

Here is a wiki page about the issue and solution:

https://wiki.internet2.edu/confluence/display/GrouperWG/Hibernate+ID%27s+and+versioning

## Comments

### mchyzer - 2008-07-30T06:59:02.083+0000

This is done

https://wiki.internet2.edu/confluence/display/GrouperWG/Hibernate+ID%27s+and+versioning