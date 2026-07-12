---
title: "Grouper 2.3.0 vs 2.2.2 Performance"
space: GrIntDev
pageId: 48792871
version: 3
lastUpdated: 2026-07-12T07:01:18.055Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792871/Grouper+2.3.0+vs+2.2.2+Performance
---

The following is performance results from Grouper API calls using both Grouper 2.3.0 and Grouper 2.2.2. Each call was made several times and an average was taken. Both Grouper instances were prepopulated with equivalent data before performing this test. Default configuration was used with the exception of database settings in grouper.hibernate.properties.

Here's a summary of what was prepopulated:

Groups: 126,801  
 Stems: 105,921  
 Immediate memberships and privileges: 1,074,446  
 Attribute assignments: 125,400  
 Attribute def names: 41,895  
 Permissions: 585,200

This data was mostly added by running edu.internet2.middleware.grouper.helper.LoadData.

Note that these tests were run on a test database server that's shared with other applications so your results will vary... A single Oracle 11g database instance was used for both Grouper versions. Both versions were installed on separate schemas. The test was executed by:

1. Using the Grouper API to add any required data for the operation. For instance, the effective membership add/delete tests required creating a group with 1000 members.
2. Then using the Grouper API to run the operation several times (mostly either 10 times or 100 times) and taking an average of the time.

| Operation | Grouper 2.3.0 (ms) | Grouper 2.2.2 (ms) |
| --- | --- | --- |
| GroupFinder.findByName() | 3 | 4 |
| Group.hasImmediateMember(Subject) | 31 | 31 |
| Group.hasEffectiveMember(Subject) | 30 | 25 |
| Group.hasMember(Subject) | 21 | 19 |
| Group.hasOptin(Subject) | 34 | 23 |
| Group.getPrivs(Subject) | 26 | 33 |
| Group.getUpdaters() | 4 | 3 |
| Group.getEffectiveMembers() | 4 | 3 |
| Group.getEffectiveMemberships() | 5 | 5 |
| Group.getImmediateMembers() | 3 | 3 |
| Group.getImmediateMemberships() | 7 | 5 |
| Group.getMembers() | 3 | 3 |
| Group.getMemberships() | 5 | 5 |
| MemberFinder.findBySubject() | 30 | 26 |
| Member.getEffectiveMemberships() | 8 | 7 |
| Member.getImmediateMemberships() | 7 | 7 |
| Member.getMemberships() | 4 | 4 |
| StemFinder.findByName() | 2 | 2 |
| Stem.getPrivs(Subject) | 40 | 45 |
| Stem.hasCreate(Subject) | 23 | 21 |
| Stem.getStemmers() | 4 | 3 |
| Group create | 193 | 185 |
| Group delete | 117 | 117 |
| Role create | 195 | 204 |
| Attribute def name create | 74 | 76 |
| Assign role permission | 67 | 54 |
| Remove role permission | 32 | 32 |
| Attribute def name delete | 27 | 22 |
| Role delete | 110 | 107 |
| Stem create | 172 | 142 |
| Stem delete | 88 | 81 |
| AttributeDef create (type=perm) | 151 | 133 |
| AttributeDef delete (type=perm) | 81 | 85 |
| Membership add | 50 | 45 |
| Membership delete | 24 | 22 |
| Membership add where member is a group | 70 | 65 |
| Membership delete where member is a group | 29 | 34 |
| Membership add causes composite | 64 | 61 |
| Membership delete causes composite | 38 | 40 |
| Group privilege (update) add | 21 | 21 |
| Group privilege (update) delete | 16 | 18 |
| Stem privilege (create) add | 21 | 23 |
| Stem privilege (create) delete | 17 | 17 |
| Effective membership (1000) add | 22 | 25 |
| Effective membership (1000) delete | 17 | 18 |
