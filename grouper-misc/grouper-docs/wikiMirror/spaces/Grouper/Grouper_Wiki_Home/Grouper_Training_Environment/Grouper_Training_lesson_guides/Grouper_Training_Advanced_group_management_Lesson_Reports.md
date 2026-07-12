---
title: "Grouper Training - Advanced group management - Lesson: Reports"
space: Grouper
pageId: 28544539
version: 3
lastUpdated: 2026-07-12T15:26:23.425Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544539/Grouper+Training+-+Advanced+group+management+-+Lesson+Reports
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

## SQL: Top HR depts:

```
SELECT G.name AS group_name, COUNT(DISTINCT V.member_id) AS member_count
  FROM grouper_memberships_all_v V
  JOIN grouper_groups G ON V.owner_group_id = G.id
 WHERE V.mship_type = 'immediate'
   AND G.name LIKE 'basis:hr:employee:dept:%'
 GROUP BY G.name
 ORDER BY member_count DESC
 LIMIT 10
```

## GSH: Nicely format the members of the sysadmin group and when they were added

```
Group group = GroupFinder.findByName("etc:sysadmingroup", true)

GrouperReportData grouperReportData = gsh_builtin_gshReportRuntime.grouperReportData

grouperReportData.headers = ['Row', 'ID', 'UID', 'Full Name', ' First Name', 'last Name', 'Email']
grouperReportData.data = new ArrayList<String[]>()

group.members.eachWithIndex { it, i ->
    String[] row = [
            i+1,
            it.subject.getAttributeValue('employeenumber'),
            it.subject.getAttributeValue('uid'),
            it.subject.getAttributeValue('cn'),
            it.subject.getAttributeValue('givenname'),
            it.subject.getAttributeValue('sn'),
            it.subject.getAttributeValue('mail'),
    ]

    grouperReportData.data << row
}
```
