---
key: GRP-37
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-37
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-19T14:09:47.852+0000
updated: 2007-10-18T10:40:36.121+0000
resolved: 2007-10-18T10:40:36.122+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-37  Performance of PopulateGroupMembersAction

The method PopulateGroupMembersAction.grouperExecute() makes at least a couple of expensive calls:

Group.getImmediateMemberships()
GrouperHelper.getOneMembershipPerSubjectOrGroup()

The call to getImmediateMemberships() has improved significantly since the 1.2.0 code since it does a bulk retrieval of all memberships.  For a group with about 20,000 members, this call takes about 7 seconds in Duke's environment.  However, getOneMembershipPerSubjectOrGroup() calls Membership.getMember() for each membership and takes about 100 seconds before timing out in the UI.

## Comments

### shilen - 2007-10-16T18:01:57.850+0000

Gary, if you need the Member objects, can you call Group.getMembers()?  That should reduce the number of queries.  Alternatively, I'm actually wondering if it's reasonable for you to just use the member_id column in the grouper_memberships table to get the unique memberships.  If you would like me to make any changes to the API, feel free to send this back to me.

### shilen - 2007-10-16T18:02:23.918+0000

Please see the last comment that I made.

### Gary Brown - 2007-10-18T10:40:36.119+0000

This became more complicated because of a feature I added based on feedback from Brown - the ability to filter memberships by subject source. This feature is dependent on instantiating a Member.

The new feature can be disabled in media.properties - set 'members.filter.by-source' to anything but 'true'. I've now added a limit 'members.filter.limit' so that the feature will disable itself if the list of Memberships is *too* big. I've arbitrarily chosen 500 as the limit. Further, if viewing only immediate memberships and not allowing filtering by source it isn't necessary to call GrouperHelper.getOneMembershipPerSubjectOrGroup - a Subject can only ever be a direct member once.

So, I added a protected method to Membership - getMemberUuid, which can be called by GrouperHelper and use this rather than subject id, as suggested by Shilen.

Membership.getMember() will still be called  for each Membership that is actually shown on screen - up to 50 by default.

NB: Sorting of Membership lists depends on obtaining the Subject (via the Member), so if the 'comparator.sort.limit' media property is set too high there will definitely be performance issues.

I do wonder whether the UI should ever try to deal with 20,000 Memberships/subjects other than for exporting. I can't imagine that anyone will page through the list - especially if it is unsorted. A better option would probably be to 'search' the membership or to find a Subject and work from the Subject Summary page. 