---
title: "GrouperShell (gsh) Privilege inheritance on attribute definitions finder (PrivilegeAttributeDefInheritanceFinder)"
space: Grouper
pageId: 28549512
version: 3
lastUpdated: 2026-07-01T05:41:52.335Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549512/GrouperShell+gsh+Privilege+inheritance+on+attribute+definitions+finder+PrivilegeAttributeDefInheritanceFinder
---

Use this class to find assigned and effective inherited privileges based on assignment to the user or a group the user is in. The inherited privileges could be assigned on the folder or an ancestor folder.

Sample call to retrieve assigned privileges:

```
Set<Privilege> assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("stemName")
        .findAssignedPrivileges();
```

Sample call to retrieve effective privileges:

```
Set<Privilege> effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName("stemName").findEffectivePrivileges();
```
