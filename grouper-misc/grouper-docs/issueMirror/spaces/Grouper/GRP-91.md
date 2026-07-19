---
key: GRP-91
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-91
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Shilen Patel <shilen@example.com>
created: 2008-02-24T06:48:58.520+0000
updated: 2008-04-15T01:13:34.606+0000
resolved: 2008-04-15T01:13:34.608+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-91  tune the grouper subject tables

The subject and subject attribute tables in grouper could use some tuning.  These tables are examples of what subject tables could be, but in case people use these in prod (are people already?) we should give them tuned examples...  here are some issues:

1. subjectattribute has primary key of id/name/value, and I think it should just be id/name.
2. if that is the case, then I think the table needs no indexes besides the primary key index
3. Queries like this could have a much better explain plan (and more easily understandable):

FROM (3576):
select
   subject.subjectid as id, subject.name as name,
   lfnamet.lfname as lfname, loginidt.loginid as loginid,
   desct.description as description
from
   subject
   left join (select subjectid, value as lfname from subjectattribute
     where name='name') lfnamet
     on subject.subjectid=lfnamet.subjectid
   left join (select subjectid, value as loginid from subjectattribute
     where name='loginid') loginidt
     on subject.subjectid=loginidt.subjectid
   left join (select subjectid, value as description from subjectattribute
      where name='description') desct
     on subject.subjectid=desct.subjectid
where
   (lower(name) like concat('%',concat('a','%')))
   or (lower(lfnamet.lfname) like concat('%',concat('a','%')))
   or (lower(loginidt.loginid) like concat('%',concat('a','%')))
   or (lower(desct.description) like concat('%',concat('a','%')));

TO (545):
select
   subject.subjectid as id, subject.name as name,
   lfnamet.value as lfname, loginidt.value as loginid,
   desct.value as description
from
   subject
   left join subjectattribute lfnamet
     on subject.subjectid=lfnamet.subjectid
   left join subjectattribute loginidt
     on subject.subjectid=loginidt.subjectid
   left join subjectattribute desct
     on subject.subjectid=desct.subjectid
where
   lfnamet.value = 'name' AND
   loginidt.value = 'loginid' AND
   desct.value = 'description' AND
   ((lower(subject.name) like concat('%',concat('a','%')))
   or (lower(lfnamet.value) like concat('%',concat('a','%')))
   or (lower(loginidt.value) like concat('%',concat('a','%')))
   or (lower(desct.value) like concat('%',concat('a','%'))));

4. Queries like this could also be rewritten for a bettter explain plan (and more easily understandable)

FROM:
  select
   subject.subjectid as id, subject.name as name,
   lfnamet.lfname as lfname, loginidt.loginid as loginid,
   desct.description as description
from
   subject
   left join (select subjectid, value as lfname from subjectattribute
     where name='name') lfnamet
     on subject.subjectid=lfnamet.subjectid
   left join (select subjectid, value as loginid from subjectattribute
     where name='loginid') loginidt
     on subject.subjectid=loginidt.subjectid
   left join (select subjectid, value as description from subjectattribute
      where name='description') desct
     on subject.subjectid=desct.subjectid
where
   subject.subjectid = ?  
   
TO:
 select
   s.subjectid as id, s.name as name,
   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,
   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,
   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description
from
   subject s
where
   s.subjectid = 'a';

5. For queries with "lower" in them, should we use the searchvalue of subjectattribute column (which is pre-lowered, right)?


## Comments

### mchyzer - 2008-04-03T18:41:02.289+0000

might be a nice to have to change "qsuob" sample source to something more sensible... (in quickstart)...

### shilen - 2008-04-15T01:13:34.604+0000

1.  The primary key was left to allow multi-valued attributes.
2.  I've added an index on the value column to improve performance.  The other indexes were removed (besides the one for the primary key).
3.  I used the following query instead.  

select
   s.subjectid as id, s.name as name,
   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,
   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,
   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description
from 
   subject s
where             
   s.subjectid in (
      select subjectid from subject where lower(name) like concat('%',concat(?,'%')) union
      select subjectid from subjectattribute where searchvalue like concat('%',concat(?,'%'))
   )     

4.  I made this update.
5.  I made this update.

I also modified the source id to "example" and added a foreign key on the subjectId column.