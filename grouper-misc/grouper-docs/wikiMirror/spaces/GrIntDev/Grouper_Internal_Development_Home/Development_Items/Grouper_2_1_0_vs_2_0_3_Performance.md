---
title: "Grouper 2.1.0 vs 2.0.3 Performance"
space: GrIntDev
pageId: 48824675
version: 5
lastUpdated: 2026-07-12T07:02:42.242Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824675/Grouper+2.1.0+vs+2.0.3+Performance
---

The following is performance results from Grouper API calls using both Grouper 2.1.0 and Grouper 2.0.3. Each call was made several times and an average was taken. Both Grouper instances were prepopulated with equivalent data before performing this test. Default configuration was used with the exception of database settings in grouper.hibernate.properties. Here's a summary of what was prepopulated:

 Groups: 126,801  
 Stems: 105,918  
 Immediate memberships and privileges: 1,074,441  
 Attribute assignments: 125,400  
 Attribute def names: 41,880  
 Permissions: 585,200

 This data was mostly added by running edu.internet2.middleware.grouper.helper.LoadData.

 Note that these tests were run on a test database server that's shared with other applications so your results will vary... A single Oracle 11g database instance was used for both Grouper versions. Both versions were installed on separate schemas. The test was executed by:

 

1. Using the Grouper API to add any required data for the operation. For instance, the effective membership add/delete tests required creating a group with 1000 members.
2. Then using the Grouper API to run the operation several times (mostly either 10 times or 100 times) and taking an average of the time.

 

| Operation | Grouper 2.1.0 (ms) | Grouper 2.0.3 (ms) |
| --- | --- | --- |
| GroupFinder.findByName() | 3 | 3 |
| Group.hasImmediateMember(Subject) | 37 | 33 |
| Group.hasEffectiveMember(Subject) | 28 | 23 |
| Group.hasMember(Subject) | 24 | 17 |
| Group.hasOptin(Subject) | 26 | 23 |
| Group.getPrivs(Subject) | 28 | 21 |
| Group.getUpdaters() | 4 | 5 |
| Group.getEffectiveMembers() | 3 | 3 |
| Group.getEffectiveMemberships() | 5 | 4 |
| Group.getImmediateMembers() | 3 | 4 |
| Group.getImmediateMemberships() | 4 | 4 |
| Group.getMembers() | 4 | 4 |
| Group.getMemberships() | 5 | 6 |
| MemberFinder.findBySubject() | 24 | 15 |
| Member.getEffectiveMemberships() | 7 | 10 |
| Member.getImmediateMemberships() | 8 | 8 |
| Member.getMemberships() | 4 | 4 |
| StemFinder.findByName() | 1 | 2 |
| Stem.getPrivs(Subject) | 36 | 23 |
| Stem.hasCreate(Subject) | 21 | 19 |
| Stem.getStemmers() | 3 | 3 |
| Group create | 268 | 323 |
| Group delete | 135 | 157 |
| Role create | 304 | 318 |
| Attribute def name create | 56 | 56 |
| Assign role permission | 64 | 52 |
| Remove role permission | 28 | 33 |
| Attribute def name delete | 21 | 30 |
| Role delete | 113 | 145 |
| Stem create | 142 | 125 |
| Stem delete | 82 | 90 |
| AttributeDef create (type=perm) | 119 | 155 |
| AttributeDef delete (type=perm) | 165 | 164 |
| Membership add | 53 | 59 |
| Membership delete | 28 | 37 |
| Membership add where member is a group | 88 | 95 |
| Membership delete where member is a group | 38 | 56 |
| Membership add causes composite | 74 | 79 |
| Membership delete causes composite | 41 | 55 |
| Group privilege (update) add | 35 | 36 |
| Group privilege (update) delete | 25 | 27 |
| Stem privilege (create) add | 31 | 35 |
| Stem privilege (create) delete | 19 | 27 |
| Effective membership (1000) add | 30 | 38 |
| Effective membership (1000) delete | 20 | 30 |
