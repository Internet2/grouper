---
key: GRP-73
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-73
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-11T14:44:47.038+0000
updated: 2008-04-16T10:04:44.042+0000
resolved: 2008-04-16T10:04:44.043+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-73  Improve error checking and logging

Currently the UI does very little error checking and no logging - beyond what gets logged via the API e.g. trying to display a Group which has just been deleted would give a NullPointerException.

Should there be a UI specific log(s) separate from the default ones? What might people want to log as INFO?

## Comments

### Gary Brown - 2008-04-16T10:04:44.024+0000

I'm resolving this for now. There is generic handling of errors with improved logging and display to end users. I still need to put more specific error catching in about half the Struts actions, and work with Chris to remove some extraneous log messages.