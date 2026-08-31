---
key: GRP-15
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-15
type: Improvement
status: Closed
resolution: Fixed
priority: Trivial
reporter: Blair Christensen <blair@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-07-24T15:03:14.316+0000
updated: 2009-10-20T17:27:42.853+0000
resolved: 2009-10-20T17:27:42.854+0000
components: [API]
fixVersions: [1.5.0]
labels: []
links: [depends on GRP-12]
---

# GRP-15  Alter query filter workflow to restrict by scope before searching

Currently we identify a candidate list and then apply any required scoping.  It would require some changes in workflow but we could probably improve performance if we first restricted by scope and then filtered out the candidates.

Two comments:
* This potentially moves a lot of query filtering off of the database and into the API.  That isn't necessarily a bad thing.
* We could default to the current behavior if there is no scope or the scope is the root stem.

## Comments

### shilen - 2008-06-25T19:26:05.540+0000

Part of the performance issue with search result scoping was taken care of in the 1.2.1 release when we improved the scoping to use group and stem names rather than the API.  For instance, X:Y:Z is a child of X:Y.

However, we might be able to see more improvements if the database query that searches for the groups does not return back any groups that are not in scope.

### shilen - 2008-11-10T15:22:14.192+0000

I've updated all but three of the query filters to take scope into  consideration when querying the database.  Under most cases, this  improves performance if the search criteria has many matches that are  out of scope.

The three query filters that I haven't done yet don't appear to be  used in any of our code so I'll take care of those later unless  someone really wants them done now.  They
still need to be fixed  because they are public classes that others may use.  Those query filters are GroupMemberFilter, MembershipCreatedAfterFilter, and 
MembershipCreatedBeforeFilter.


### shilen - 2009-01-27T14:54:54.677+0000

Given what's left with this issue, I'm marking down the priority.

### shilen - 2009-10-20T17:27:42.851+0000

I'm closing this since the main issue with the filters were resolved a long time ago.