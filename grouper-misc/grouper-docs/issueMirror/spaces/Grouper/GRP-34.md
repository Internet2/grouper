---
key: GRP-34
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-34
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2007-09-17T15:48:36.338+0000
updated: 2007-12-13T13:54:41.350+0000
resolved: 2007-10-17T14:44:36.488+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-34  Error initializing Oracle database with Grouper 1.2.1

"ant db-init" results in the following errors.

2007-09-17 11:41:28,563: Unsuccessful: create index membership_member_idx, membership_member_and_list_idx on grouper_memberships (member_id)
2007-09-17 11:41:28,569: ORA-00969: missing ON keyword

2007-09-17 11:41:28,577: Unsuccessful: create index membership_owner_list_and_type_idx, membership_member_and_list_idx on grouper_memberships (list_name, list_type)
2007-09-17 11:41:28,577: ORA-00972: identifier is too long

2007-09-17 11:41:28,599: Unsuccessful: create index membership_owner_list_and_type_idx on grouper_memberships (owner_id, mship_type)
2007-09-17 11:41:28,599: ORA-00972: identifier is too long


## Comments

### shilen - 2007-10-17T14:44:36.485+0000

Changes committed to CVS.

### James Cramton - 2007-12-12T19:56:30.611+0000

Shilen,

What exactly was the change committed to CVS? Since Brown already is running an active DB, we are manually making the schema changes to support the changed memberships for GRP-10. But from what I can tell, the index names are unchanged from the GRP-10 comments: 

membership_member_and_list_idx
and
membership_owner_list_and_type_idx

I just want to make sure the index names we add are inline with the official schema.

### shilen - 2007-12-13T13:54:41.300+0000

James,

There were a couple of errors in the way the indexes were being generated.  The grouper_memberships table should have the following indexes:

create index membership_creator_idx on grouper_memberships (creator_id);
create index membership_depth_idx on grouper_memberships (depth);
create index membership_member_idx on grouper_memberships (member_id);
create index membership_createtime_idx on grouper_memberships (create_time);
create index membership_parent_idx on grouper_memberships (parent_membership);
create index membership_via_idx on grouper_memberships (via_id);
create index membership_uuid_idx on grouper_memberships (membership_uuid);
create index membership_member_list_idx on grouper_memberships (member_id, list_name, list_type);
create index membership_owner_list_type_idx on grouper_memberships (owner_id, list_name, list_type, mship_type);
