---
key: GRP-30
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-30
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Blair Christensen <blair@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-08-24T15:47:23.551+0000
updated: 2007-11-17T17:46:54.991+0000
resolved: 2007-11-17T17:46:54.994+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: [depends on GRP-29, depends on GRP-28]
---

# GRP-30  Optimize query logic in "PrivilegeHelper#can*()" methods

Right now methods such as "canView()" might internally call "hasPrivilege()" for up to 6 different privileges.    We would probably be better served by instead calling "privs.AccessResolver#getPrivileges(Group, Subject)" and then checking the results within the method call.  

## Comments

### blair@example.com - 2007-08-24T15:48:30.903+0000

I think the results might be inconsistent if we do not properly account for GrouperAll.

### Gary Brown - 2007-11-17T17:46:54.990+0000

I added a new internal method to retrieve all 'memberships' for a Group and subject, and changed other code to call it