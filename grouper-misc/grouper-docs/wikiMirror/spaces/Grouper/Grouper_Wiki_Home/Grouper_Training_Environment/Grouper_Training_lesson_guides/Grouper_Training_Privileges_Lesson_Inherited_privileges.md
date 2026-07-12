---
title: "Grouper Training - Privileges - Lesson: Inherited privileges"
space: Grouper
pageId: 28545578
version: 7
lastUpdated: 2026-07-12T15:26:44.975Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545578/Grouper+Training+-+Privileges+-+Lesson+Inherited+privileges
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Inherited privileges: the proper way to assign privileges

## Exercise: Setup inherited privileges

### Open browser

Log in as banderson

Navigate to the "test" folder

Create folder `testInheritedPrivs`

Grant create for testAdmins to this folder

Create group in the testInheritedPrivs folder: `inheritPrivGroup`

In folder `testInheritedPrivs,`More tab → Inherited privileges to objects in folder

Add members → test:testAdmins

- Group
- ADMIN
- All levels

Note privileges of test:testInheritedPrivs:inheritPrivGroup

### Open another browser

Log in as jsmith

Navigate to the `testInheritedPrivs` folder

Create group `jsmithGroup2`

View privileges. What do you notice?
