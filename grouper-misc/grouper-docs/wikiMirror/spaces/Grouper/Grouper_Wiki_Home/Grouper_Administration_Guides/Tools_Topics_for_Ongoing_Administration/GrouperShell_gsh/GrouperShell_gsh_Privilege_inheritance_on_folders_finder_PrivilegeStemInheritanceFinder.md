---
title: "GrouperShell (gsh) Privilege inheritance on folders finder (PrivilegeStemInheritanceFinder)"
space: Grouper
pageId: 28549525
version: 2
lastUpdated: 2026-07-01T05:41:50.379Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549525/GrouperShell+gsh+Privilege+inheritance+on+folders+finder+PrivilegeStemInheritanceFinder
---

Use this class to find assigned and effective inherited privileges based on assignment to the user or a group the user is in. The inherited privileges could be assigned on the folder or an ancestor folder.

Sample call to retrieve assigned privileges:

```
Set<Privilege> assignedPrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("stemName")
        .findAssignedPrivileges();
```

Sample call to retrieve effective privileges:

```
Set<Privilege> effectivePrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName("stemName").findEffectivePrivileges();
```
