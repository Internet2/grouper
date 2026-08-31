---
key: GRP-69
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-69
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-05T11:06:51.092+0000
updated: 2007-12-05T15:08:13.460+0000
resolved: 2007-12-05T15:08:13.461+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-69  Some JUnit tests fail

A number of tests, related to privileges, fail due to changes in the way some data is retrieved and cached. 

One test can be changed to expect the current behavior. Other tests fail because privilege information is now cached for up to 2 minutes. One solution is to choose much shorter time to live and time to idle cache settings when running tests, and to add short pauses - 2 seconds - so that cached privileges expire. 

## Comments

### Gary Brown - 2007-12-05T15:08:13.435+0000

I have tweaked the build script so that a test friendly version of grouper.ehcache.xml is placed at the start of the classpath. Several tests now have a 2 second wait so that previously retrieved privileges expire before we test that new ones are working 