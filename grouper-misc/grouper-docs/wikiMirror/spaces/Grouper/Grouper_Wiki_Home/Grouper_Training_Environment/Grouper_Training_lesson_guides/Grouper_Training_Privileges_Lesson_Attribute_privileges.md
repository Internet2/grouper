---
title: "Grouper Training - Privileges - Lesson: Attribute privileges"
space: Grouper
pageId: 28544423
version: 6
lastUpdated: 2026-07-12T15:26:19.889Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544423/Grouper+Training+-+Privileges+-+Lesson+Attribute+privileges
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Attribute privileges

## Exercise: Show attribute privileges

### Open on browser

Log in as banderson

Navigate to the "etc:attribute" folder

Create attribute definition: *TestFolderFlagDef*

- Type: attribute
- Assign to: folders
- Value: no value (marker)

Create an attribute name in etc:attribute, name: *TestFolderFlag*

Navigate to TestFolderFlagDef

Click on privileges tab

Grant READ to jsmith

Navigate to test folder

More actions → attribute assignments

Assign TestFolderFlag

### Open another browser (not just a tab since cookies are shared)

Log in as jsmith

Navigate to "test" folder

Try to see attribute under More actions → attribute assignments

### banderson browser:

- Navigate to test folder
- Privileges tab
- Grant "attribute read" to jsmith

### jsmith browser:

- Navigate to "test" folder
- Try to see attribute under More actions → attribute assignments

### banderson browser:

- Set attestation for the test folder
- show folder attributes

### jsmith browser:

- show folder attributes

### banderson browser:

- Assign UPDATE to the attribute definition to jsmith

### jsmith browser:

- Can jsmith delete the folder attribute?

### banderson browser:

- grant ATTR_UPDATE to the test folder for jsmith

### jsmith browser:

- Can jsmith delete the folder attribute now?
