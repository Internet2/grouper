---
key: GRP-27
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-27
type: Task
status: Closed
resolution: Completed
priority: Minor
reporter: Blair Christensen <blair@example.com>
assignee: Tom Barton  (internet2.edu) <tbarton@example.com>
created: 2007-08-24T15:40:23.914+0000
updated: 2008-06-25T18:07:35.976+0000
resolved: 2008-06-25T18:07:35.979+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-27  Relocate "can*()" methods from "PrivilegeHelper" to "privs.AccessResolver" and "privs.NamingResolver"

The default implementation for each should presumably be in "privs.AccessWrapper" and "privs.NamingWrapper". 

## Comments

### tbarton - 2008-06-25T18:07:35.972+0000

overtaken by events.