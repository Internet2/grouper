---
key: GRP-59
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-59
type: New Feature
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-17T16:04:15.004+0000
updated: 2007-11-17T16:07:49.239+0000
resolved: 2007-11-17T16:05:14.608+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-59  Allow default group search to search any group attribute, not just name as at present

See Duke's  Grouper UI Functionality Requests - Group Search Capability

https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+UI+Functionality+Requests

## Comments

### Gary Brown - 2007-11-17T16:05:14.605+0000

The media property:

search.default.any=(false|true|only)

now controls the behavior of the default search:

false=do not search any attribute
true=offer the user the option of searching in name or any attribute
only=default search searches any attribute - no option
