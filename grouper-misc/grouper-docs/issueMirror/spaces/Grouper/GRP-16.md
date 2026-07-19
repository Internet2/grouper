---
key: GRP-16
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-16
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Jeff Van Eeuwen <jvaneeuw@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-07-27T17:12:11.743+0000
updated: 2007-11-17T17:34:17.916+0000
resolved: 2007-08-03T14:39:54.190+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-16  Can't update member privilege on self

0.  There exists a subject SubjectA and an empty group GroupA in Grouper.
1.  The Grouper System User (or Grouper Root) grants SubjectA the update privilege to GroupA.
2.  SubjectA logs into Grouper via the UI.
3.  SubjectA views GroupA via the "Manage Groups" functionality in the UI.
4.  SubjectA grants itself the member privilege to GroupA
5.  SubjectA lists the direct members of GroupA. This lists the only direct member SubjectA.
6.  SubjectA clicks on the "is direct member" link to view/modify it's privileges.  This lists only the member privilege.  The update privilege granted by the Grouper System User is not listed here.
7. SubjectA now tries to revoke the member privilege to GroupA via the "saveGroupMember.do" action in the UI.  
8. The "SaveGroupMemberAction" tries to update SubjectA's privileges. 
9. The SaveGroupMemberAction throws a "InsufficientPrivilegeException" when it tries to revoke the update privilege.  Then a NullPointerException is throw when the error handling in SaveGroupMemberAction tries to act on the message returned by InsufficientPrivilegeException.


## Comments

### blair@example.com - 2007-08-02T15:04:31.410+0000

I've asked Gary (who doesn't appear to have a Jira account yet) about this as I *think* it is a UI and not an API issue.  Depending upon his feedback one of us will resolve this.

### Gary Brown - 2007-08-03T14:21:20.754+0000

I got a NullPointerException, but I can see there would be a problem because the code checks all possible privileges regardless of what the user can change. Shouldn't be hard to fix

### Gary Brown - 2007-08-03T14:39:54.156+0000

Fixed by modifying SaveGroupMemberAction.java so that code does not try to revoke Access privileges unless the authenticated user has admin privilege for the group