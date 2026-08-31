---
key: GRP-65
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-65
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-20T18:57:07.448+0000
updated: 2007-11-20T18:58:14.160+0000
resolved: 2007-11-20T18:58:14.161+0000
components: [API, UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-65  With 1.2.1 rc1 the UI is no longer correctly indicating direct vs indirect privileges

Changes to GrouperAllAccessResolver return 'correct' privileges but with the wrong 'owner'. This is an oversight from moving where the GrouperAll check is made. In addition WheelAccessResolver was returning (in getPrivileges) wheel privileges without checking membership

## Comments

### Gary Brown - 2007-11-20T18:58:14.153+0000

I now 'fix' the GrouperAll privileges before returning them, and also check for wheel group membership