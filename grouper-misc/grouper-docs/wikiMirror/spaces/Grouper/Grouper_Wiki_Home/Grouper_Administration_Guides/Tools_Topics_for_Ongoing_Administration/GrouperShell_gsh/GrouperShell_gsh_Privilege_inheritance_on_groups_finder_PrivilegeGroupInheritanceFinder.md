---
title: "GrouperShell (gsh) Privilege inheritance on groups finder (PrivilegeGroupInheritanceFinder)"
space: Grouper
pageId: 28549519
version: 2
lastUpdated: 2026-07-01T05:41:51.360Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549519/GrouperShell+gsh+Privilege+inheritance+on+groups+finder+PrivilegeGroupInheritanceFinder
---

Use this class to find assigned and effective inherited privileges based on assignment to the user or a group the user is in. The inherited privileges could be assigned on the folder or an ancestor folder.

Sample call to retrieve assigned privileges:

```
Set<Privilege> assignedPrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("stemName")
        .findAssignedPrivileges();
```

Sample call to retrieve effective privileges:

```
Set<Privilege> effectivePrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName("stemName").findEffectivePrivileges();
```
