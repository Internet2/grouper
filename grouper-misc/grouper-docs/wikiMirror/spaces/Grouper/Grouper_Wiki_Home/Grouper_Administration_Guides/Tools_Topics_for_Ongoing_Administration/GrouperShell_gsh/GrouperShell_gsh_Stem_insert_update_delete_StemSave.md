---
title: "GrouperShell (gsh) Stem insert/update/delete (StemSave)"
space: Grouper
pageId: 28548359
version: 4
lastUpdated: 2026-07-01T05:44:41.875Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548359/GrouperShell+gsh+Stem+insert+update+delete+StemSave
---

Use this class to insert or update a stem

Sample call

> StemSave stemSave = new StemSave(grouperSession).assignName("test") .assignCreateParentStemsIfNotExist(true).assignDisplayName("test") .assignDescription("testDescription"); Stem stem = stemSave.save(); System.out.println(stemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample to delete

> new StemSave(grouperSession).assignUuid(stem.getId()).assignSaveMode(SaveMode.DELETE).save();

To edit just one field (the description) for an existing stem

> new StemSave(grouperSession).assignUuid(stem.getId()) .assignDisplayExtension("test1") .assignAlternateName("newAlternateName") .assignReplaceAllSettings(false).save();

Sample to delete folders in a folder, and be failsafe (log on which ones error)

> String parentStemName = "test:poc"; GrouperSession grouperSession = GrouperSession.startRootSession(); Stem parentStem = StemFinder.findByName(grouperSession, parentStemName, true); for (Stem stem : parentStem.getChildStems()) { try { new StemSave(grouperSession).assignUuid(stem.getId()).assignSaveMode(SaveMode.DELETE).save(); } catch (Exception e) { e.printStackTrace(); } }

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemSave.html)
