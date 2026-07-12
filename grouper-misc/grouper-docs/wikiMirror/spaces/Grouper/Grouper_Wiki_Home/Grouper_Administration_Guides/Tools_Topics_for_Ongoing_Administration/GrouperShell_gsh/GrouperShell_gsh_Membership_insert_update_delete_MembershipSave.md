---
title: "GrouperShell (gsh) Membership insert / update / delete (MembershipSave)"
space: Grouper
pageId: 28548330
version: 4
lastUpdated: 2026-07-01T05:44:46.995Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548330/GrouperShell+gsh+Membership+insert+update+delete+MembershipSave
---

Use this class to insert or update or delete a membership

Sample call

> MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(subject); membershipSave.save();

Sample call to delete a membership

> MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(subject).assignSaveMode(SaveMode.DELETE); membershipSave.save();

  
Sample call to add a membership using group name and subject identifier and sourceId. Note, use double quotes to be java compliant

> new MembershipSave().assignGroupName('a:b:c').assignSubjectIdentifier('jsmith').assignSubjectSourceId('myUsers').save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MembershipSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MembershipSave.html)
