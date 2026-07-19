---
key: GRP-24
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-24
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-08-15T16:13:13.019+0000
updated: 2007-11-17T17:51:47.417+0000
resolved: 2007-08-24T15:00:19.580+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: [depends on GRP-19]
---

# GRP-24  Intermittent test failure: Test_uc_WheelGroup.test_fromNotAMemberOfTheWheelGroupToAMemberOfTheWheelGroup

I've started to experience intermittent failure of this test over the past few days.  I'm not sure of the origin yet.  It fails with the message: "now can CREATE".  

I probably won't look at this until after I've resolved GRP-19.

## Comments

### blair@example.com - 2007-08-15T16:13:35.037+0000

I *think* it depends on GRP-19.

### blair@example.com - 2007-08-24T15:00:19.577+0000

I reordered the privilege resolver ordering.  By having the wheel resolver run before the caching resolver we work around this issue.  Is this ideal?  No.  It restores the old behavior, however, which is a good thing.