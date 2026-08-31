---
key: GRP-71
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-71
type: Improvement
status: Open
resolution: Unresolved
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2007-12-11T14:13:37.510+0000
updated: 2011-02-16T18:01:18.735+0000
resolved: 
components: [UI]
fixVersions: []
labels: []
links: []
---

# GRP-71  Add act as to UI to act as another user

change the act as grouper system, to act as another user


##################
OLD JIRA

The UI has a feature where wheel group members can 'Act as self' or 'Act as admin', however, this feature is not supported by the API so there are times when privilege checks will return true rather than false. The UI currently checks wheel group membership itself, however, it would be better to have the API provide a public way of determining this (there is a protected method in PrivilegeHelper).

Currently privilege checks / retrieval happen through PrivilegeHelper or WheelNamingResolver / WheelAccessResolver - need to hunt down any others. I think the best approach would be to add methods to GrouperSession so we can check if the session subject 'isWheel', 'isActiveWheel', 'isInactiveWheel' and 'enableWheel','disableWheel'. Currently the Resolvers do not have a reference to a GrouperSession, but this could be changed - the Factory takes a GrouperSession.

I am open to suggestions for other implementations.  
