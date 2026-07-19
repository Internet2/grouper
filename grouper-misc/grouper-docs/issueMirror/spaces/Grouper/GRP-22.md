---
key: GRP-22
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-22
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-08-07T16:02:12.739+0000
updated: 2007-11-17T17:48:00.065+0000
resolved: 2007-08-14T17:18:41.800+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-22  Excessive Group member queries

Shilen Patel @ Duke:

"This is regarding the membership queries we discussed in the call. Running Group.getMembers() in the API causes the following query to
execute once per each member.

Hibernate: select hibernatem0_.id as id, hibernatem0_.member_uuid as member_u2_, hibernatem0_.subject_id as subject_id, hibernatem0_.subject_source as subject_4_, hibernatem0_.subject_type as subject_5_ from grouper_members hibernatem0_ where
(hibernatem0_.member_uuid=? )"

Shilen Patel @ Duke:

"This is just a quick follow up to the previous email to mention how this operation can be improved.  As mentioned before, if you want to run
Group.getMembers() on a group with 30,000 members, there will be 30,000 SELECT queries on the grouper_members table.

You can easily reduce that down to 1 query using a join like the following where you're getting all members in a specified group.

select m.id as id, m.member_uuid as member_u2_,  m.subject_id as subject_id, m.subject_source as subject_4_, m.subject_type as subject_5_ from grouper_members m, grouper_memberships ms where owner_id=? and ms.list_name='members' and ms.list_type='list' and
ms.member_id = m.MEMBER_UUID"


## Comments

### blair@example.com - 2007-08-14T17:18:41.749+0000

* I added a new DAO method with what should be a more optimal query.
* I also stopped (pointlessly) performing a privilege check for every membership.  That has been replaced with a single privilege check before retrieving any memberships.