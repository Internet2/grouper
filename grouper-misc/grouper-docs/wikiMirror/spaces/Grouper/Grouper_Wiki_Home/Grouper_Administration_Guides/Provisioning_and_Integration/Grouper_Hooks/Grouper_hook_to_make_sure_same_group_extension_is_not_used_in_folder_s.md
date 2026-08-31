---
title: "Grouper hook to make sure same group extension is not used in folder(s)"
space: Grouper
pageId: 28547415
version: 2
lastUpdated: 2026-07-01T05:46:49.459Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547415/Grouper+hook+to+make+sure+same+group+extension+is+not+used+in+folder+s
---

This is available in Grouper v4.5.0+ or v5.3.0+

## Configure

Note, the first time you configure this you need to bounce or redeploy Grouper.

grouper.properties

```
groupUniqueExtensionInFolderHook.config1.folderNames = a:b, c:d:e
groupUniqueExtensionInFolderHook.config1.caseSensitive = false

groupUniqueExtensionInFolderHook.myConfig2.folderNames = f:g
```

Each config is a different realm to check for unique extension. "config1" and "myConfig2" are arbitrary config ids. You can use the root stem if you like (single colon). Case sensitive refers to the case of the extension, not the folder. The folders listed will check that folder and subfolders. New groups or moved or renamed groups will be vetoed. Race conditions will be caught by daemon

## Daemon

grouper-loader.properties

Daemon is enabled by default. It will fail if there is a problem. Emails can be optional configured to notify on error

```
# Put in comma separated email addresses to send errors to.  Use a:b:c@grouper to send to a group (email addresses must be configured in the subject source).
# {valueType: "string"}
otherJob.findBadGroupsUniqueExtensionInFolder.mailToOnError = 
```
