---
key: GRP-14
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-14
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Kathryn Huxtable <kathryn@example.com>
created: 2007-07-23T15:07:43.286+0000
updated: 2008-03-19T16:30:56.451+0000
resolved: 2008-03-19T16:30:56.468+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-14  Poor integration with Ldappc: Null DTO error message in Ldappc run causes run to fail

James Cramton @ Brown:

"Finally, LDAPpc isn't satisfied that we've completed the run successfully, we think because it gets a null dto in GrouperSession. I
wonder if the GrouperSession has expired by the time the process finishes?

2007-07-20 06:29:07,230: [edu.internet2.middleware.ldappc.LdappcGrouperProvisioner] Grouper Provision Failed: null dto in class edu.internet2.middleware.grouper.GrouperSession"

## Comments

### blair@example.com - 2007-08-13T19:42:15.861+0000

I haven't been to identify *why* this is happening yet but I have just checked in some code to HEAD that might at least let me better identify the problem when it is triggered.  I am speculating this might be due to some insufficient error handling in Ldappc, partially for lack of a better theory at the moment.  This one will remain open until more information can be gathered.

### gettes - 2007-10-05T14:39:43.332+0000

I get this error message ALL the time now.  ldappc with grouper 1.2.0 and subject api 031 + ldap pooling mod

i had to change the fatal message to a warn so it wouldn't kill ldappc as I am running in -interval mode

### Kathryn Huxtable - 2008-02-06T18:10:59.039+0000

I also get this all the time with Grouper 1.2.1. I'm not impressed at all with the error handling in ldappc, though I can see what they were trying to do. I'll be seeing if I can resolve this in their code or if it needs handling in the Grouper API.

### Kathryn Huxtable - 2008-02-06T19:28:39.477+0000

Okay, this is fixed in CVS. It's not tagged with any release, though, at this point.

It's purely an ldappc problem. That's where I applied the fix.

### Kathryn Huxtable - 2008-03-19T16:30:56.425+0000

Fixed in current HEAD of ldappc source.