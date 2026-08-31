---
title: "Group Rules"
space: Grouper
pageId: 28544856
version: 3
lastUpdated: 2025-09-12T17:52:58.492Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544856/Group+Rules
---

Using group rules (the details for each of the available patterns can be found here: [Grouper rules patterns](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548037/Grouper+rules+patterns)) can be an empowering tool for local-IT or group administrators.

### Using the rules

1. Browse to the group to which you would like to apply the rule
2. Select "Group actions" in the top right corner of the screen
3. Scroll down in the menu and select "Rules"
4. Click "Rules actions" and "Add rule"
5. Choose the rule you wish to use and apply

### Variables in emails

The following variables can be configured in the email messages that are sent to users by these rules. The formatting is this ${safeSubject.%Variable%()}

- getAttributeValue(String attributeName)
- getAttributeValueOrCommaSeparated(String attributeName)
- getDescription()
- getEmailAddress()
- getId()
- getName()
- getSourceId()
- getTypeName()
