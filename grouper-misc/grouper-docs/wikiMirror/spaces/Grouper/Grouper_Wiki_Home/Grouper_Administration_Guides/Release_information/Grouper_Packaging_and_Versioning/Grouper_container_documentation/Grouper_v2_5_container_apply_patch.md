---
title: "Grouper v2.5 container apply patch"
space: Grouper
pageId: 28554891
version: 4
lastUpdated: 2026-07-01T05:39:24.554Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554891/Grouper+v2.5+container+apply+patch
---

Generally there are no patches in v2.5.

## Applying patches

Applying patches is an advanced task, be careful.

However if you need to try to adjust code, you can:

1. Make or get a patch from the dev team
  
  1. Generally this is a zip of a code in a package that starts with "edu"
2. You need to mount this or make a subimage
3. The "edu" directory of the patch needs to be in /opt/grouper/grouperWebapp/WEB-INF/classes
  
  1. So that /opt/grouper/grouperWebapp/WEB-INF/classes/edu exists
  2. Pay attention before copying if the edu folder already existed or if any files were there
4. The container needs to be removed and run
5. On the next upgrade of container, you MUST remove these files (but not remove other files already in "edu/" in your container if any exist
6. To revert the patch, just remove the files, and remove container and run again

## Creating patches

This is how Chris creates patches, you can do this however you want as long as the result is the same

1. Create an empty patch dir
2. In the dev env, copy the source "edu" folder to the patch dir
3. Do through and delete all stuff not needed
  
  1. This is done so all directories match. If you create folders and copy files needed in, then you might get something wrong
4. Go to the classes dir in IDE, for the files needed, and copy all files to the right folder
  
  1. Note, you need to get all anonymous inner classes too (with dollar sign in url)
