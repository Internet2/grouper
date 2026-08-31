---
key: GRP-67
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-67
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-30T15:25:46.080+0000
updated: 2007-12-06T14:24:08.307+0000
resolved: 2007-12-05T15:15:12.534+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-67  Missing 'active' Wheel group breaks Grouper

WheelAccessResolver and WheelNamingResolver try to instantiate the wheel group (if active). An exception is thrown if the group has not yet been created.

Should be possible to catch exception, log it, but continue. Need to check whether the Resolvers are created once (in which case crreating the wheel group won't magically cause it to work) or per GrouperSession (or similar, in which case it may start working on creation)



## Comments

### Gary Brown - 2007-12-05T15:15:12.520+0000

I now just log the GroupNotFoundException if the wheel group is configured to be on but does not exist. I believe that if the wheel group is subsequently created it will not work for any existing GrouperSession, but it should work for any new GrouperSession created.