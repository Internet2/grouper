---
key: GRP-82
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-82
type: Improvement
status: Closed
resolution: Fixed
priority: Minor
reporter: blair christensen. <blair@example.com>
assignee: Tom Zeller <tzeller@example.com>
created: 2008-01-17T04:24:56.051+0000
updated: 2008-04-04T14:29:21.195+0000
resolved: 2008-04-04T14:29:21.196+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-82  Test failure due to missing cache(s)

At least one test was failing due to the test ehcache configuration file not having all of the same caches as the production configuration.  This patch:
* Removes "conf/grouper.ehcache.xml"
* Removes "src/test/conf/grouper.ehcache.xml"
* Adds "src/conf/grouper.ehcache.xml"
* Ant's "init.conf" target now builds the two ehcache configuration files with different values for "timeToIdle" and "timeToLive"

Of course I'm not actually seeing an option to include a patch when submitting this issue.  Maybe once I press "Create"?  Or after I create the issue?

## Comments

### blair christensen. - 2008-01-17T04:25:39.256+0000

The patch I was referring to in the description.

### tzeller@example.com - 2008-04-04T14:29:21.193+0000

Patch applied.

## Attachments
- generate_ehcache_configs.patch (14752 bytes) - by blair christensen. on 2008-01-17T04:25:39.227+0000