---
title: "GrouperShell (gsh) Attestation on groups insert / update / delete (AttestationGroupSave)"
space: Grouper
pageId: 28548803
version: 6
lastUpdated: 2026-07-01T05:43:28.915Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548803/GrouperShell+gsh+Attestation+on+groups+insert+update+delete+AttestationGroupSave
---

> Use this class to add/edit/delete attestation on groups.
> 
> Sample call
> 
> > AttestationGroupSave attestationGroupSave = new AttestationGroupSave(); AttributeAssign attributeAssign = attestationGroupSave .assignGroup(group) .addEmailAddress("test@example.com") .assignAttestationType(AttestationType.report) .assignDaysBeforeToRemind(5) .assignDaysUntilRecertify(10) .assignSendEmail(true) .save(); System.out.println(attestationGroupSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE
> 
> 
> 
> Sample call to remove attestation from a group
> 
> > new AttestationGroupSave() .assignGroup(group) .assignSaveMode(SaveMode.DELETE) .save();
> 
> 
> 
> Sample call to update only one attribute
> 
> > new AttestationGroupSave() .assignGroup(group) .assignReplaceAllSettings(false) .assignSendEmail(true); .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/attestation/AttestationGroupSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/attestation/AttestationGroupSave.html)
