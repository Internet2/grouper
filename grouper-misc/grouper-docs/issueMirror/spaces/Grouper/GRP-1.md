---
key: GRP-1
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-1
type: Bug
status: Resolved
resolution: Fixed
priority: Blocker
reporter: Jeff Van Eeuwen <jvaneeuw@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-05-14T14:02:59.383+0000
updated: 2007-05-22T15:23:11.558+0000
resolved: 2007-05-22T15:23:11.682+0000
components: [API]
fixVersions: [HEAD]
labels: []
links: []
---

# GRP-1  Memberships not updated when composite group modified

1. I created a group A and added two direct members Subject X and Subject Y.
2. I created a group B and added one direct member Subject Y.
3. I created a composite group C that is A\B (the complement of B within A).  It has a single indirect member X.
4. I created a group D and added a single direct member of group C. Group D also shows Subject X as an indirect member.
5. I modified the composite group C so that C is now A U B (A union B).  Group C now has two indirect members Subjects X and Y.
6. I looked at the member list of Group D.  Group C is still a direct member, but it is the only member of D.  I had also expected to see Subjects X and Y as indirect members.


## Comments

### blair@example.com - 2007-05-22T15:23:11.223+0000

HEAD has been updated to include a test and fix for this issue.  

Thanks for the excellent description of the problem!