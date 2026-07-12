---
title: "Check Grouper version number"
space: Grouper
pageId: 28545098
version: 10
lastUpdated: 2026-07-01T05:47:46.466Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545098/Check+Grouper+version+number
---

You can see which version of Grouper is running in the UI in the configure screen

This output uses a JEXL expression in the text config (grouper.text.en.us.properties)

```
configurationIndexSubtitle = System settings and wizards to setup the registry. Grouper version: ${edu.internet2.middleware.grouper.misc.GrouperVersion.grouperVersion()}
```

The JEXL expression can be used in other text properties. In the following example, the version is added to the institution name:

```
institutionName.elConfig = Institute of Higher Education (Grouper ${edu.internet2.middleware.grouper.misc.GrouperVersion.grouperVersion()})
```

***NOTE: institutionName is used in multiple locations so your version number will show up as the title for your logo, in your footer, and on the home page as pictured above
