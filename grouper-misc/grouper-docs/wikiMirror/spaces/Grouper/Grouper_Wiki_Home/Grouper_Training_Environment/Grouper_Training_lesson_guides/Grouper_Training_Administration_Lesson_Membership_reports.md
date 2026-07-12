---
title: "Grouper Training - Administration - Lesson: Membership reports"
space: Grouper
pageId: 28544396
version: 11
lastUpdated: 2025-04-04T16:06:18.682Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544396/Grouper+Training+-+Administration+-+Lesson+Membership+reports
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

Members to add to locked_by_ciso

```
aadams
jadams3
badams
dadams
```

Report name

```
factStaffLockedOut
```

Description

```
employees locked out
```

Filename

```
employeesLockedOut_$$timestamp$$.csv
```

Viewers group

```
etc:sysadmingroup
```

Quartz cron

```
0 * * * * ?
```

Report query

```
select gmlv1.subject_id from grouper_memberships_lw_v gmlv1 where gmlv1.list_name = 'members' and gmlv1.group_name = 'ref:security:locked_by_ciso' and gmlv1.member_id in (select gmlv2.member_id from grouper_memberships_lw_v gmlv2 where gmlv2.list_name = gmlv1.list_name and gmlv2.group_name = 'ref:role:all_facstaff')
```
