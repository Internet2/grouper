---
key: GRP-97
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-97
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-03-19T20:47:35.763+0000
updated: 2008-03-20T03:16:15.544+0000
resolved: 2008-03-19T20:50:53.589+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-97  upgrade to hibernate3, remove hibernate2

Hibernate2 is not actively supported anymore, and we need to do data related changes (e.g. transactions, inverse of control), so we will upgrade to hib3.  Since there are so many data layer changes, we will remove support for hib2 so we dont have to do many parallel changes.

## Comments

### mchyzer - 2008-03-19T20:50:53.473+0000

This is fixed.  Grouper cannot be used with hib2 anymore.  It is important that users make 3 changes to make this happen:

1. in grouper.properties

FROM:
dao.factory=edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDAOFactory

TO:
dao.factory=edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory

2. In grouper.hibernate.properties, change any database dialects, e.g.:

FROM:
hibernate.dialect                     = net.sf.hibernate.dialect.MySQLDialect

TO:
hibernate.dialect                     = org.hibernate.dialect.MySQL5Dialect

3. In grouper.hibernate.properties

FROM:
hibernate.cache.provider_class        = net.sf.hibernate.cache.EhCacheProvider

TO:
hibernate.cache.provider_class        = org.hibernate.cache.EhCacheProvider


Also of course you need the new grouper jars, remove the old (if deleted), etc.

Chris

### mchyzer - 2008-03-20T03:16:15.490+0000

There are a couple of more steps too:

https://wiki.internet2.edu/confluence/display/GrouperWG/Hibernate+and+data+layer+updates

    4. You must start using c3p0 database pooling (this is the only one we unit test with grouper with).  This means changing the grouper.hibernate.properties (feel free to set the c3p0 pool settings as you see fit.  Below is a safe version which should perform fine, but you can tune it to err on the side of performance if you like:

# Use DBCP connection pooling
#hibernate.dbcp.maxActive              = 16
#hibernate.dbcp.maxIdle                = 16
#hibernate.dbcp.maxWait                = -1
#hibernate.dbcp.whenExhaustedAction    = 1

# Use c3p0 connection pooling (since dbcp not supported in hibernate anymore)
# http://www.hibernate.org/214.html, http://www.hibernate.org/hib_docs/reference/en/html/session-configuration.html
hibernate.c3p0.max_size 16
hibernate.c3p0.min_size 0
#seconds
hibernate.c3p0.timeout 100
hibernate.c3p0.max_statements 0
hibernate.c3p0.idle_test_period 100
hibernate.c3p0.acquire_increment 1
hibernate.c3p0.validate false

    5. Check your log4j.properties, if you have TRACE log on hibernate, change to ERROR.  If you have net.sf.hibernate, might want to change to org.hibernate.  Otherwise ignore.

log4j.logger.org.hibernate                                       = ERROR, grouper_error
 