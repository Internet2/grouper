---
key: GRP-55
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-55
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-09T15:42:49.413+0000
updated: 2011-05-11T15:41:07.643+0000
resolved: 2011-05-11T15:41:07.533+0000
components: [API, UI]
fixVersions: [1.6.0]
labels: []
links: []
---

# GRP-55  Enhance API so that Groups can return a Set of Members / Memberships with a 'count' for how many routes each Subject is a member

When listing Group memberships the UI indicates by how many routes a Subject is a member of the group. This requires the UI to process each membership.

The cost of the current approach has been reduced by code changes to reduce instantiation of nested objects where an ID would do, however, it would be cleaner if the API could get the information in a single query.

## Comments

### Gary Brown - 2011-05-11T15:41:07.582+0000

UI updated as part of 447