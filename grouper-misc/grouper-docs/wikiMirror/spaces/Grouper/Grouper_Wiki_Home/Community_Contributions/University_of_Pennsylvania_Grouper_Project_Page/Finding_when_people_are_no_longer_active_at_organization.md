---
title: "Finding when people are no longer active at organization"
space: Grouper
pageId: 28544191
version: 4
lastUpdated: 2019-01-29T19:17:04.308Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544191/Finding+when+people+are+no+longer+active+at+organization
---

First make a table with subject_ids of people who are of interest. Note you might want to add subject sources to the queries...

PCDADMIN.TEMP_O365_DISABLED_USERS

| EMPLOYEEID | ACTIVE_ENDS_DATE | ACTIVE |
| --- | --- | --- |
| 12345678 | 1/2/2003 | T/F |

Pick an old group (has lots of history) that means "active", and see when it was created (e.g. oldest memberships in it), in penn's case: penn:community:facultyStudentStaff, made in 2/25/2011.

If people are active fac/stu/staff, set active to T

```
update pcdadmin.temp_o365_disabled_users todu set TODU.ACTIVE = 'T' where TODU.EMPLOYEEID in 
(select distinct GMLV.SUBJECT_ID from grouper_memberships_lw_v gmlv 
where GMLV.GROUP_NAME = 'penn:community:facultyStudentStaff' and GMLV.LIST_NAME = 'members');

```

Now we know everyone else is inactive

```
select count(1) from pcdadmin.temp_o365_disabled_users;
update pcdadmin.temp_o365_disabled_users todu set TODU.ACTIVE = 'F' where TODU.ACTIVE is null;
commit;
```

See which inactive records are not in the PIT for that group... i.e. no records exist. Which means they were inactive before 2/25/2011...

```
update pcdadmin.temp_o365_disabled_users TODU 
set TODU.ACTIVE_ENDS_DATE = TO_TIMESTAMP ('25-Feb-11 00:00:00.000000', 'DD-Mon-RR HH24:MI:SS.FF') where TODU.ACTIVE = 'F' and TODU.ACTIVE_ENDS_DATE is null 
and not exists
(select 1
 from grouper_pit_memberships_all_v gpmav, grouper_pit_members gpm, grouper_pit_fields gpf
where GPMAV.OWNER_GROUP_ID in ( select id from grouper_pit_groups gpg where GPG.NAME = 'penn:community:facultyStudentStaff' )
and GPMAV.MEMBER_ID = GPM.ID and GPF.NAME = 'members' and GPF.ID = GPMAV.FIELD_ID  and GPM.SUBJECT_ID = TODU.EMPLOYEEID and gpmav.membership_end_time is not null);

commit;
```

Check the last active PIT date and update to that

```
update pcdadmin.temp_o365_disabled_users TODU 
set TODU.ACTIVE_ENDS_DATE = (select max (TIMESTAMP '1970-01-01 00:00:00.000'
         + NUMTODSINTERVAL( GPMAV.MEMBERSHIP_END_TIME / 1000000, 'SECOND' )) as end_Date
 from grouper_pit_memberships_all_v gpmav, grouper_pit_members gpm, grouper_pit_fields gpf
where GPMAV.OWNER_GROUP_ID in ( select id from grouper_pit_groups gpg where GPG.NAME = 'penn:community:facultyStudentStaff' )
and GPMAV.MEMBER_ID = GPM.ID and GPF.NAME = 'members' and GPF.ID = GPMAV.FIELD_ID  and GPM.SUBJECT_ID = TODU.EMPLOYEEID ) 
where TODU.ACTIVE = 'F' and TODU.ACTIVE_ENDS_DATE is null and exists 
( select distinct 1 
 from grouper_pit_memberships_all_v gpmav2, grouper_pit_members gpm2, grouper_pit_fields gpf2
where GPMAV2.OWNER_GROUP_ID in ( select id from grouper_pit_groups gpg2 where GPG2.NAME = 'penn:community:facultyStudentStaff' )
and GPMAV2.MEMBER_ID = GPM2.ID and GPF2.NAME = 'members' and GPF2.ID = GPMAV2.FIELD_ID  and GPM2.SUBJECT_ID = TODU.EMPLOYEEID
and gpmav2.membership_end_time is not null  )

```
