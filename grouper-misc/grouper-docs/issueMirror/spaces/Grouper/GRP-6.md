---
key: GRP-6
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-6
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-07-12T18:34:52.352+0000
updated: 2007-10-16T15:18:28.294+0000
resolved: 2007-08-09T18:58:47.601+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-6  Subject caching does not appear to be working as intended

While testing my Oracle/JNDI deployment, I am seeing repeated LDAP queries for a subject.  For each of the "SubjectFinder" methods that I repeatedly called a LDAP query was generated.  

## Comments

### blair@example.com - 2007-08-09T18:58:47.558+0000

I just committed a large patch to HEAD.  The Subject resolution and caching code has been completely rewritten.  Most importantly: Subject caching appears to work again and there are a couple of slight tests for it.

Also changed:
* Upgrade to ehcache 1.3.0
* Subject caching now done using ehcache
* Subject caching is now configured via "conf/gouper.ehcache.xml", not "conf/grouper.properties"