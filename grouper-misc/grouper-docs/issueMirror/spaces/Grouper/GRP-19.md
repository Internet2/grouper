---
key: GRP-19
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-19
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-08-07T14:49:03.332+0000
updated: 2007-10-16T15:18:27.986+0000
resolved: 2007-08-24T14:59:26.594+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: [has dependent GRP-24]
---

# GRP-19  Refactor privilege resolution

The API's privilege resolution code is currently very dense, convoluted and not always very fast  While I've begun working on refactoring it in some local patches I need to finish doing so and commit those changes back to HEAD.

## Comments

### blair@example.com - 2007-08-07T14:51:19.287+0000

So I don't forget: One optimization would be modifying code like "PrivilegeResolver#internal_canVIEW(Group, Subject)".  Right now it performs individual queries for whether Subject can VIEW Group.  This could lead to 6+ queries being performed.  Instead just query for all privs that Subject has on Group and check the returned results internally.

### blair@example.com - 2007-08-24T14:59:26.584+0000

While I haven't addressed all of the performance concerns (those will be new Jira issues) I've just checked into HEAD code that:
* Switches privilege caching to per session, not per JVM
* Uses ehcache for privilege caching
* Refactored privilege resolution into a base resolver that is wrapped by a number of decorators, each of which provides additional functional (caching, wheel group, GrouperSystem, GrouperAll, etc)