---
key: GRP-44
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-44
type: New Feature
status: Resolved
resolution: Fixed
priority: Trivial
reporter: James Cramton <jcramton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-20T19:05:52.244+0000
updated: 2007-11-17T17:29:12.782+0000
resolved: 2007-11-17T17:29:12.786+0000
components: [API, UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-44  List all ACLs on a group, either direct or inherited

In troubleshooting privilege issues in Grouper, it would be very handy to have the ability to display all direct or inherited ACLs on a group or person. Brown wrote a command line program to identify the direct ACLs, but it does not elucidate the indirect ACLs. Having this ability in the UI would help resolve issues without contacting back office nerds like me.

## Comments

### Gary Brown - 2007-09-25T13:31:48.792+0000

Duke have asked for something similar.i.e. for a given subject and a 'group search' display all ACLs for the subject on the group search results.

Does that sound like it would work for you? I would expect to add this as an option to the Subject Summary screen.

### James Cramton - 2007-09-25T14:43:35.441+0000

yes, an option on the subject summary page seems like the way to go, since this could be an expensive operation.  And having a 'display all groups with acl X sounds useful, too.

### Gary Brown - 2007-11-17T17:29:12.781+0000

Unfortunately searching can be slow, but selecting the option on the Subject summary page takes you to the advanced group search page. Privileges are listed next to the subsequent search results