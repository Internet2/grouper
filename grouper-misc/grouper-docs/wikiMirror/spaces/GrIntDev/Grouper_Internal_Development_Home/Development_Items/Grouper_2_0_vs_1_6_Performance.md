---
title: "Grouper 2.0 vs 1.6 Performance"
space: GrIntDev
pageId: 48824685
version: 4
lastUpdated: 2026-07-12T07:02:45.678Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824685/Grouper+2.0+vs+1.6+Performance
---

The following is performance results from Grouper API calls using both Grouper 2.0 and Grouper 1.6. Each call was made several times and an average was taken. Both databases (using Oracle) were prepopulated with equivalent data before performing this test. Here's a summary of what was prepopulated:

 Groups: 126,801  
 Stems: 105,900  
 Immediate memberships and privileges: 1,074,400  
 Attribute assignments: 125,400  
 Attribute def names: 41,800  
 Permissions: 585,200

 This data was mostly added by running edu.internet2.middleware.grouper.helper.LoadData. The version in trunk was run for both the 2.0 and the 1.6 databases.

 Note that these tests were run on a test database server that's shared with other applications so your results will vary... The test was executed by:

 

1. Using the Grouper API to add any required data for the operation. For instance, the effective membership add/delete tests required creating a group with 1000 members.
2. Then using the Grouper API to run the operation several times (mostly either 10 times or 100 times) and taking an average of the time.

 

| Operation | Grouper 2.0 (ms) | Grouper 1.6 (ms) |
| --- | --- | --- |
| GroupFinder.findByName() | 3 | 3 |
| Group.hasImmediateMember(Subject) | 28 | 17 |
| Group.hasEffectiveMember(Subject) | 22 | 18 |
| Group.hasMember(Subject) | 16 | 11 |
| Group.hasOptin(Subject) | 17 | 17 |
| Group.getPrivs(Subject) | 19 | 13 |
| Group.getUpdaters() | 4 | 3 |
| Group.getEffectiveMembers() | 3 | 3 |
| Group.getEffectiveMemberships() | 4 | 4 |
| Group.getImmediateMembers() | 3 | 2 |
| Group.getImmediateMemberships() | 4 | 4 |
| Group.getMembers() | 4 | 2 |
| Group.getMemberships() | 5 | 5 |
| MemberFinder.findBySubject() | 12 | 6 |
| Member.getEffectiveMemberships() | 9 | 10 |
| Member.getImmediateMemberships() | 9 | 7 |
| Member.getMemberships() | 4 | 4 |
| StemFinder.findByName() | 1 | 2 |
| Stem.getPrivs(Subject) | 34 | 17 |
| Stem.hasCreate(Subject) | 17 | 12 |
| Stem.getStemmers() | 3 | 3 |
| Group create | 416 | 317 |
| Group delete | 212 | 193 |
| Role create | 430 | 312 |
| Attribute def name create | 86 | 60 |
| Assign role permission | 91 | 39 |
| Remove role permission | 44 | 15 |
| Attribute def name delete | 38 | 15 |
| Role delete | 207 | 182 |
| Stem create | 206 | 165 |
| Stem delete | 119 | 105 |
| AttributeDef create (type=perm) | 224 | 159 |
| AttributeDef delete (type=perm) | 192 | 149 |
| Membership add | 76 | 59 |
| Membership delete | 53 | 43 |
| Membership add where member is a group | 134 | 127 |
| Membership delete where member is a group | 71 | 60 |
| Membership add causes composite add | 109 | 86 |
| Membership delete causes composite delete | 81 | 70 |
| Group privilege (update) add | 51 | 44 |
| Group privilege (update) delete | 34 | 26 |
| Stem privilege (create) add | 50 | 42 |
| Stem privilege (create) delete | 30 | 26 |
| Effective membership (1000) add | 216 | 169 |
| Effective membership (1000) delete | 162 | 148 |
