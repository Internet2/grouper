---
title: "GrouperShell (gsh) Attestation on folders insert / update / delete (AttestationStemSave)"
space: Grouper
pageId: 28548188
version: 6
lastUpdated: 2026-07-12T06:45:08.186Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548188/GrouperShell+gsh+Attestation+on+folders+insert+update+delete+AttestationStemSave
---

Use this class to add/edit/delete attestation on folders.

Sample call

> AttestationStemSave attestationStemSave = new AttestationStemSave(); AttributeAssign attributeAssign = attestationStemSave .assignStem(stem) .addEmailAddress("test@example.com") .assignAttestationType(AttestationType.report) .assignDaysBeforeToRemind(5) .assignDaysUntilRecertify(10) .assignSendEmail(true) .save(); System.out.println(attestationStemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample call to remove attestation from a folder

> new AttestationStemSave() .assignStem(stem) .assignSaveMode(SaveMode.DELETE) .save();

Sample call to update only one attribute

> new AttestationStemSave() .assignStem(stem) .assignReplaceAllSettings(false) .assignSendEmail(true); .save();

## Options:

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/attestation/AttestationStemSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/attestation/AttestationStemSave.html)
