---
key: GRP-46
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-46
type: Bug
status: Resolved
resolution: Fixed
priority: Critical
reporter: James Cramton <jcramton@example.com>
assignee: Tom Zeller <tzeller@example.com>
created: 2007-09-20T20:28:13.623+0000
updated: 2008-04-09T16:55:10.511+0000
resolved: 2008-04-09T16:55:10.517+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-46  Support deleting group membership for subjects who are no longer in the directory

In our pre-release work in Grouper, we discovered that the API must instantiate a subject before it can delete the subject from a group's membership.  There is a major use case at Brown (and certainly elsewhere) where users leave the directory without first being deleted from Grouper. Our provisioning software handled these cases by recognizing the need to remove the user from the group, but it would fail catastrophically and produce an unrecoverable corrupted data condition that made the group unusable. We implemented a solution that uses a local SQL user registry rather than our LDAP registry, (originally, there were performance reasons for this). But our design of the SQL person registry was influenced by the desire to never delete users from the SQL registry, so we could be assured of being able to successfully delete purged users' group membership.

Ideally, there should be an ability to remove a subject's group membership based on just a subject identifier, not a subject instance. I don't have an example of the exception, but it was (unfortunately) one of the most repeatable exceptions we've seen.

## Comments

### James Cramton - 2007-09-20T20:36:04.653+0000

The result of the error was that group members who leave the directory cannot be removed from the group. It follows that we cannot delete groups containing members who cannot be deleted. Both of these issues are potentially show stopping issues, had we not designed the SQL person registry as we did. They will need to be addressed before we can change to an LDAP person registry again, as we would like to do as soon as LDAPpc performance issues are resolved.

### tzeller@example.com - 2008-04-09T16:54:35.182+0000

The unresolvable subject deletion utility (usdu) available with 1.3.0 will delete group members whose subjects no longer exist in a source. Documentation for usdu is currently available on the Grouper Product wiki.