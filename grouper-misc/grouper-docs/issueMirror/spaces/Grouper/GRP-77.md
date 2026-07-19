---
key: GRP-77
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-77
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-17T15:03:03.712+0000
updated: 2007-12-17T15:25:24.451+0000
resolved: 2007-12-17T15:25:24.452+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-77  Improve membership listing speed by caching groups associated with Memberships

PrivilegeHelper.canViewMemberships(GrouperSession s, Collection c) iterates across the Collection of Memberships and calls getGroup/getStem each time. Very often the Group/Stem will be the same for each Membership. Currently the API repetitively instantiates the same object - a relatively slow operation, expecially at Bristol where we have a number of custom types and attributes. 

Using Ehcache should greatly improve performance. 

## Comments

### Gary Brown - 2007-12-17T15:25:24.444+0000

Added caching. For a group with 6225 members this improved the response time from ~3 minutes to a few seconds (with the profiler running).