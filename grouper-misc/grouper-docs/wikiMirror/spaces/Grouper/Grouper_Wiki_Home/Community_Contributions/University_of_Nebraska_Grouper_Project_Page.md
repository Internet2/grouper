---
title: "University of Nebraska Grouper Project Page"
space: Grouper
pageId: 28543535
version: 14
lastUpdated: 2026-07-01T05:49:32.978Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543535/University+of+Nebraska+Grouper+Project+Page
---

University of Nebraska is using Grouper in production.

Types of data in Grouper:

- Student data
- Employee data
- Housing, residence halls
- Key managers

See [this page for more info and a presentation on Grouper at UNL](http://idm.unl.edu/grouper).

Grouper Stem Layout and reference groups for employees

| Group IDPath | Group Name | Notes |
| --- | --- | --- |
| unl:ref:hr:employee | Employee | All employees of the University, unl:ref:hr:staff is a direct member. |
| unl:ref:hr:staff | Staff | All staff of the University, unl:ref:hr:esg:B1 is a direct member. |
| unl:ref:hr:esg:B1 | B1 | All Salaried B-line staff of the University |
| unl:ref:hr:orgs:50000081:employee | Office of the Chancellor UNL (Employee) | All employees of the Chancellor’s Office, unl:ref:hr:orgs:50000081:staff is a direct member. |
| unl:ref:hr:orgs:50000081:staff | Office of the Chancellor UNL (Staff) | Staff in the Chancellor’s office, unl:ref:hr:orgs:50000081:esg:B1 is a direct member. |
| unl:ref:hr:orgs:50000081:esg:B1 | Office of the Chancellor UNL (ESG B1) | Salaried B-line Staff in the Chancellor’s Office |
| unl:ref:hr:position:00003276 | Mgr Identity Access Mgmt (Position 00003276 - Brett Bieber) | Immutable unique position #, for the Manager of Identity & Access Management, this group has Brett Bieber as the only member. For this group, it’s been very helpful to include the name of the employee in the name of the group, so that it is returned when someone searches for that person |
| unl:ref:hr:supervisor:00003276 | Mgr Identity Access Mgmt (Supervisor 00003276) | Group of all employees with #3276 as their supervisor (Brett's direct reports are direct members) |
| unl:ref:hr:team:00003276 | Mgr Identity Access Mgmt Team (Position+Supervisor 00003276) | Group with unl:ref:hr:position:00003276 and unl:ref:hr:supervisor:00003276 as direct members. |
| unl:ref:hr:title:Help Center Assistant | Help Center Assistant | Employees with that specific title |

Grouper Stem Layout for Course information

| Group IDPath | Group Name | Notes |
| --- | --- | --- |
| basis:stdnt:nu:courses:UNL:ASC:AECN:109:001:instructors   basis:stdnt:nu:courses:UNL:ASC:AECN:109:001:students   basis:stdnt:nu:courses:UNL:ASC:AECN:109:001:all | WATER IN SOCIETY AECN109 SEC 001 Spring 2019 - students/instructors/all | The groups in this folder all pertain to a specific section (001) of the AECN (Agricultural Economics) 109 course. |
| basis:stdnt:nu:courses:UNL:ASC:AECN:109:all_sections_instructors   basis:stdnt:nu:courses:UNL:ASC:AECN:109:all_sections_students | WATER IN SOCIETY AECN109 SEC 150 Spring 2020 - all_sections_instructors/all_sections_students | The groups in this folder roll-up all of the respective instructors or students for all the sections of AECN 109. |
| basis:stdnt:nu:courses:UNL:ASC:AECN:100_level_instructors   basis:stdnt:nu:courses:UNL:ASC:AECN:100_level_students | AGRICULTURAL ECONOMICS 100 level instructors/students | These groups contain the basis:stdnt:nu:courses:UNL:ASC:AECN:1%:all_sections_instructors/students groups for all of the 1XX level Agricultural Economics courses. |
| basis:stdnt:nu:courses:UNL:ASC:AECN:all_courses_instructors   basis:stdnt:nu:courses:UNL:ASC:AECN:all_courses_students | AGRICULTURAL ECONOMICS all instructors/students | These groups contain the basis:stdnt:nu:courses:UNL:ASC:AECN:%00_level_instructors/students groups as members. |
| basis:stdnt:nu:courses:UNL:ASC:all_subjects_instructors   basis:stdnt:nu:courses:UNL:ASC:all_subjects_students | ARTS AND SCIENCES all subjects instructors/students | These groups contain the basis:stdnt:nu:courses:UNL:ASC:%:all_courses_instructors/students groups |

University of Nebraska presented on their Grouper deployment at the Feb. 14, 2018 IAM Online webinar. See the slides: [https://www.incommon.org/docs/iamonline/20180214_IAMOnline.pdf](https://www.incommon.org/docs/iamonline/20180214_IAMOnline.pdf) and recorded webinar: [http://internet2.adobeconnect.com/p03kw4etow2t/](http://internet2.adobeconnect.com/p03kw4etow2t/)

**See Also**

[University of Nebraska Contribution on Grace Period in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544138/University+of+Nebraska+-+Grace+Period+in+Grouper)

[Grouper on University of Nebraska Lincoln IT Services Website](https://its.unl.edu/services/grouper/)
