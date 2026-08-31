---
key: GRP-48
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-48
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-09-20T21:01:15.096+0000
updated: 2007-11-01T18:27:22.601+0000
resolved: 2007-11-01T18:27:22.603+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-48  Performance of GroupAttributeFilter.getResults()

There are three expensive calls with GroupAttributeFilter.getResults().

1.  HibernateGroupDAO.findAllByApproximateAttr()
2.  PrivilegeHelper.canViewGroups()
3.  BaseQueryFilter.filterByScope()

For the first, I've noticed in our environment, the substring query is the very least expensive part of this operation since that is just one query.  What's more expensive is that for each result, there's a separate query for the group data in grouper_groups.  We should see some performance improvements if this can be combined into one query that does a join between grouper_attributes and grouper_groups.

For the second, about 50% of the time spent in the call PrivilegeHelper.canViewGroups() is spent executing MemberFinder.findBySubject() within GrouperAccessAdapter.hasPriv().  Caching the Member object should provide improvements.

For the third, assuming the query is executed from the root stem, most of the time is spent in the Group.getParentStem() call within Stem.isChildGroup().  To save time in cases where the query is executed from the root stem, does it make sense to add a check for isRootStem() before calling Group.getParentStem()?

There may be more areas that can be improved here, but these are the main ones I've found so far.

## Comments

### shilen - 2007-10-31T12:13:48.710+0000

The following changes have been committed to SVN now.

1.  Modification to determine how child stems and groups are determined by using the stem and group names.
2.  Caching of findBySubject calls.

### shilen - 2007-11-01T18:27:11.022+0000

Also in SVN now.

3.  Modified findAll* methods in HibernateGroupDAO.java to perform only one database query per call and pre-fetch attributes.  Note that the pre-fetched attributes are not cached using ehcache.