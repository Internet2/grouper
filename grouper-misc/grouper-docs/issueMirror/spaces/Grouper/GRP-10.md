---
key: GRP-10
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-10
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Blair Christensen <blair@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-07-23T14:56:30.366+0000
updated: 2007-11-17T17:50:06.988+0000
resolved: 2007-08-09T19:53:35.411+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-10  Modify Membership Indices

Shilen Patel @ Duke:

"So we tried the following.

drop index MEMBERSHIP_FIELD_IDX;
drop index MEMBERSHIP_OWNER_IDX;
drop index MEMBERSHIP_TYPE_IDX;

CREATE INDEX IMS.GROUPER_MEMBERSHIPS_IDX1 ON IMS.GROUPER_MEMBERSHIPS
(OWNER_ID, LIST_NAME, LIST_TYPE, MSHIP_TYPE);
CREATE INDEX IMS.GROUPER_MEMBERSHIPS_IDX2 ON IMS.GROUPER_MEMBERSHIPS
(MEMBER_ID, LIST_NAME, LIST_TYPE);


I've tried these index changes on two database servers and have seen
similar results on both.

The time to delete a group drops from 2.18 seconds to .18 seconds.
The time to delete a stem drops from .40 seconds to .08 seconds.
The time to run Member.getMemberships() for a person with 7 memberships
drops from .55 seconds to .10 seconds."

## Comments

### blair@example.com - 2007-08-09T19:53:35.407+0000

Indices removed from Hibernate mapping:
* membership_owner_idx (owner_id)
* membership_type_idx( mship_type )
* membership_member_idx (member_id)

Indices added to Hibernate mapping:
* membership_member_and_list_idx (member_id, list_name, list_type)
* membership_owner_list_and_type_idx (owner_id, list_name, list_type, mship_type)

These changes don't make much difference on a small, local Grouper instance using HSQLDB but Shilen's numbers suggest it will help larger and less embedded instances of Grouper.