---
title: "Delete folder"
space: Grouper
pageId: 28545109
version: 12
lastUpdated: 2026-07-01T05:47:45.453Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545109/Delete+folder
---

This wizard is in the Folder → More actions menu → "Delete Folder"

You still need ADMIN on the folder to see this button, even if you are deleting subobjects only.

## Obliterate a folder

To delete a folder and all subobjects you must have (note, if you are a grouper admin you can obviously delete folders):

1. ADMIN on the folder
2. ADMIN on all groups/folders/attributes in that folder and subfolders

You will not see the option to obliterate if you do not have all these things

Only Grouper admins can delete point in time. Other users will not see this option.

Resulting screen:

## Delete certain types of objects in a folder

Note: you need the appropriate ADMIN privileges on any objects you are deleting. The counts should match how many objects you have ADMIN on, though it does not see if the folders are or will be empty for that count.

## Long running task

The delete function runs in a thread and will return in 1 minute. If there are too many objects to delete, it will run in the background. Note: when it is complete, the folder will be deleted but no feedback will be given to the user. Check back to see the progress.
