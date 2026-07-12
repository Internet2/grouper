---
title: "Grouper membership reasons"
space: GrIntDev
pageId: 48792718
version: 8
lastUpdated: 2026-07-12T06:45:38.987Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792718/Grouper+membership+reasons
---

A manual group might have members for various reasons.

For example a VPN group might have people in the organization, people in the institution (not in the organization), and people outside of the institution.

John was added to the VPN manual group, and is in the organization in the institution. When he leaves the organization (and/or the institution), then his ad hoc membership should get an end date after a grace period, and the VPN administrator should get an email.

| User | Role when added | User change | Action |
| --- | --- | --- | --- |
| John | In HR org | Leaves the HR org, not the institution | Set an end date on the manual membership 7 days in future. Email the VPN administrator |
| Sally | In HR org | Leaves the institution | Set an end date on the manual membership 2 days in future. Email the VPN administrator |
| Jen | Not in HR org | Leaves the institution | Set an end date on the manual membership 2 days in future. Email the VPN administrator |
| John |  | Back in HR org 3 days later | Remove end date on VPN manual membership |
| Sally |  | Back in institution 5 days later | Since less than 7 days, assume data error and add back to VPN manual group. Email the VPN admin |
| David | Not in institution | No actions can be detected |  |
| Liz | Either HR org or elsewhere in institution | Had a title change | Email the VPN admins and let them know she had a title change |

Report for attestation

Generate a daily report of users to be attested, and send out emails for a monthly attestation of this report

| User | In org | End date in org | In institution | End date in institution | Guest | Has VPN | VPN start date | VPN end date |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| John | F | 2021/02/11 | T |  | F | F | 2018/03/14 | 2021/02/18 |
| Sally | F |  | T |  | F | T | 2017/04/03 |  |
| David | F |  | F |  | T | T | 2016/07/18 |  |

## Configuration setup

VPN manual group

Add attribute with JSON config

```
{
  "membershipReasons": [
    {  "groupName": "org:hr:staff",
       "gracePeriodDaysIfLeave": 7,
       "addBackAgainWithinDaysIfJoin": 14
    },
    {  "groupName": "ref:employee",
       "gracePeriodDaysIfLeave": 2,
       "addBackAgainWithinDaysIfJoin": 7
    },
    {  "stemName": "ref:jobTitles",
       "removeUser": false
    }
  ],
  "allowGuest": true,
  "emailGroupName": "apps:vpn:ref:hrVpnAdmins",
  "emailNotifySubject": "User $$subject.name$$ left $$reasonGroup.name$$, please re-evaluate their access",
  "emailNotifyBody": "User $$subject.name$$ left $$reasonGroup.name$$, please re-evaluate their access.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$",
  "emailRemoveSubject": "User $$subject.name$$ left $$reasonGroup.name$$ $$daysSinceGone$$ days ago and lost VPN access",
  "emailRemoveBody": "User $$subject.name$$ left $$reasonGroup.name$$ $$daysSinceGone$$ days ago and lost VPN access.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$",
  "emailGraceSubject": "User $$subject.name$$ left $$reasonGroup.name$$ and will lose VPN access in $$gracePeriodOrZero$$ days",
  "emailGraceBody": "User $$subject.name$$ left $$reasonGroup.name$$ and will lose VPN access in $$gracePeriodOrZero$$ days.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$",
  "emailAddSubject": "User $$subject.name$$ rejoined $$reasonGroup.name$$ and has VPN access again",
  "emailAddBody": "User $$subject.name$$ rejoined $$reasonGroup.name$$ and has VPN access again.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$", 
}
```

| Setting | Sample value | Description |
| --- | --- | --- |
| membershipReasons.groupName | ref:employee | If they are in this group, that is the reason they have VPN access |
| membershipReasons.gracePeriodDaysIfLeave | 2 | If they have VPN access, and they leave the employee group, give them 2 days grace period in form of end date in future |
| membershipReasons.addBackAgainWithinDaysIfJoin | 7 | If they had VPN access, and lost it, and then went back into the reason group within this amount of time (e.g. from an error), then add them back to the manual group |
| membershipReasons.stemName | ref:jobTitles | If the user has a job title, then that is why they have VPN access |
| membershipReasons.removeUser | false | If the user loses their reason, do not remove them or add a grace period |
| allowGuest | true | If members are allowed in the manual group if they don't have a reason |
| emailGroupName | apps:vpn:ref:hrVpnAdmins | People in this group will get emailed when changes happen. Leave blank if no emails should be sent |
| emailRemoveSubject | User $$subject.name$$ left $$reasonGroup.name$$ $$daysSinceGone$$ days ago and lost VPN access | Email subject sent to admins when access removed |
| emailRemoveBody | User $$subject.name$$ left $$reasonGroup.name$$ $$daysSinceGone$$ days ago and lost VPN access.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$ | Email body sent to admins when access removed |
| emailGraceSubject | User $$subject.name$$ left $$reasonGroup.name$$ and will lose VPN access in $$gracePeriodOrZero$$ days | Email subject sent when grace period enacted |
| emailGraceBody | User $$subject.name$$ left $$reasonGroup.name$$ and will lose VPN access in $$gracePeriodOrZero$$ days.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$ | Email body sent when grace period enacted |
| emailAddSubject | User $$subject.name$$ rejoined $$reasonGroup.name$$ and has VPN access again | Email subject sent when user rejoins reason group in time |
| emailAddBody | User $$subject.name$$ rejoined $$reasonGroup.name$$ and has VPN access again.\n\nClick here to manage the VPN group: $$ownerGroupUrl$$ | Email body sent when user rejoins reason group in time |

## Membership attributes

Grouper will assign effective membership

| Group | Member | Marker attribute | Attribute assignment attribute | Value(s) |
| --- | --- | --- | --- | --- |
| HR VPN | John | grouperMshipReasonMarker | reason | ref:employee   org:hr:staff   ref:jobTitles:hrManager |
| HR VPN | Liz | grouperMshipReasonMarker | reason | ref:employee   org:hr:staff   ref:jobTitles:hrDirector |
| HR VPN | David | grouperMshipReasonMarker | reason | grouperGuest |
