---
key: GRP-93
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-93
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2008-03-03T13:56:42.688+0000
updated: 2008-03-03T13:59:10.866+0000
resolved: 2008-03-03T13:59:10.867+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-93  For performance reasons allow case-sensitive sorting

Currently the UI lowercases Strings while sorting. This can have an impact on peforrmance if thousands of comparisons are made

## Comments

### Gary Brown - 2008-03-03T13:59:10.864+0000

comparator.sort.lowercase=false in media.properties stops comparison Strings being lowercased when sorting