---
key: GRP-2
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-2
type: Bug
status: Resolved
resolution: Fixed
priority: Blocker
reporter: Jeff Van Eeuwen <jvaneeuw@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-05-14T14:04:33.482+0000
updated: 2007-05-24T19:41:10.981+0000
resolved: 2007-05-24T19:41:11.036+0000
components: [API]
fixVersions: [HEAD]
labels: []
links: []
---

# GRP-2  Membership remains even though subject removed from group

1. I created a Group A with a single subject X.
2. I created a Group B with a single subject Y.
3. I created a Group C that is union of A and B.  It has members X and Y.
4. I created a Group D with a single subject Group C.  Indirectly it has members X and Y.
5. I removed subject X from Group A.   This resulted in subject X no longer in being member of Group C as expected, but subject X remained a member of D.


## Comments

### blair@example.com - 2007-05-24T19:41:10.717+0000

I just committed some changes to HEAD that should resolve this issue as described in your *excellent* bug report.
