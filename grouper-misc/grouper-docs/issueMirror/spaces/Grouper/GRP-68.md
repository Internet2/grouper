---
key: GRP-68
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-68
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-30T16:21:08.029+0000
updated: 2007-12-03T12:03:21.532+0000
resolved: 2007-12-03T12:03:22.351+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-68  Finding groups and displaying privileges for a wheel group Subject gives exception when clicking to view how privileges are derived

Displays the checkboxes but then 'dies' trying to show how privs are derived. Probably need to put in a special case as with GrouperAll

## Comments

### Gary Brown - 2007-12-03T12:03:17.487+0000

Added special case for GrouperSystem derived privs  - which are inherent rather than declared.