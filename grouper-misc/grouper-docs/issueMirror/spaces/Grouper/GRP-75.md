---
key: GRP-75
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-75
type: New Feature
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-13T09:41:52.047+0000
updated: 2007-12-13T09:50:47.099+0000
resolved: 2007-12-13T09:50:47.101+0000
components: []
fixVersions: []
labels: []
links: []
---

# GRP-75  Allow sites to configure the default subject source when searching for Subjects

Currently 'All' is selected. My original motivation for being able to change this is that group searching can be slow - very slow on one of our dev databases, and most times users want to search for people.

## Comments

### Gary Brown - 2007-12-13T09:50:47.097+0000

subject.search.default-source=all added to media.properties. Change all to desired source id if you prefer a different default