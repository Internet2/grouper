---
title: "Grouper attestation testing"
space: Grouper
pageId: 28548659
version: 9
lastUpdated: 2026-07-01T05:43:53.122Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548659/Grouper+attestation+testing
---

As GrouperSystem

Run the attestation unit test.

```
TestRunner.run(new GrouperAttestationJobTest("testGrouperAttestationPrivileges"));
```

Global attestation, click Miscellaneous → Attestation see 4 groups that need attestation (groupA, groupAA, groupAB, groupAC)

Run attestation daemon, should run successfully

Folders and groups with settings

Click on the folder testA:stemA, see attestation

Edit attestation settings, change scope, see that it took effect

Click on folders attestable groups

Folders settings of groups and folders from folder "testa"

## As test.subject.0 (ADMIN of groups)

Note on global, folder, and group menus, there is no option to run attestation daemon

Should not see groupD (no privs)

When looking at global settings, you should see groupE and not groupD

Edit a stem attestation and a group attestation.

Try clear last attestation date

Attest as reviewed

See auditing

## As test.subject.1 (UPDATER of groups)

See the same thing as test.subject.0. Should not see the folder settings since not have ADMIN on that.

## As test.subject.2 (READER of groups)

Look at global screen

Look at settings

Note, no option to run daemon

Cannot edit attestation, clear attestation, or mark as attested.

Can view audit log

## As test.subject.3 (VIEWER of groups)

Cant see anything on folders or groups or global
