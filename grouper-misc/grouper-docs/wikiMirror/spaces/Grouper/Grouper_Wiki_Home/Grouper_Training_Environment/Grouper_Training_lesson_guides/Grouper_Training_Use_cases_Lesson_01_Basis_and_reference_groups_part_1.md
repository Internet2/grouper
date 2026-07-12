---
title: "Grouper Training - Use cases - Lesson 01: Basis and reference groups part 1"
space: Grouper
pageId: 28544224
version: 41
lastUpdated: 2026-07-12T15:26:14.082Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Understand the difference between reference groups and basis groups
- Create and manage reference and basis groups
- Implement subject attribute lifecycle requirements

## Exercise: All Students Reference Group

Create an all students reference group to be used in access policy and the “all students” mailing list.

### Create folder ref:student

Create in this folder: `ref`

Folder name: `student`

### Create group ref:student:students

Group name: `students`

Description:

```
This group contains all students for the purpose of access control. Members automatically get access to a broad selection of student services. You can view where this group is in use by selecting "This group's memberships in other groups" under the "More" tab
```

Add ref type to group

Find menu item Group actions → *Types*

Click: Type actions → Edit type settings

Type name: `ref`

Type: Yes, has direct type configuration

Data owner: `Registrar`

Member description: `All student subjects for the purpose of access control`

### Add class years to ref:students

Navigate to group `ref:student:students`

Add the following groups to ref:students

- `basis:sis:prog_status:year:ac:2025`
- `basis:sis:prog_status:year:ac:2026`
- `basis:sis:prog_status:year:ac:2027`
- `basis:sis:prog_status:year:ac:2028`

### Filter for Direct Membership

Filter for: Has direct membership → Apply filter

### Filter for Indirect Membership

Filter for: Has indirect membership → Apply filter

How many students are in the group (look near the bottom)?

### Recently Graduated Students

You suddenly remember that recently graduated students have a 7 month grace period where they retain full access to student services.

Add to students

```
basis:sis:prog_status:year:cm:2024
```

Before submitting, edit the membership and set the end date to

```
12/31/2025 05:00:00 PM
```
