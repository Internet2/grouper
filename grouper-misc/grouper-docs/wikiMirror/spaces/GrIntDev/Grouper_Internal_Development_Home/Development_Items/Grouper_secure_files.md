---
title: "Grouper secure files"
space: GrIntDev
pageId: 48792692
version: 7
lastUpdated: 2026-07-12T06:45:37.606Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792692/Grouper+secure+files
---

This is a future enhancement to make files available to download through the UI (v2.5.48+)

Grouper can provide a file for a user or a group via UI link

There are some attributes on a subject or a group (future) that can allow a file to be retrieved from the UI.

| Attribute | Sample value | Description |
| --- | --- | --- |
| grouperSecureFileMarker |  | assignable to group (future) or individuals, multi-assign |
| grouperSecureFileId | abc123 | the uuid used from the browser to retrieve the file, will download the file from the browser after checking security |
| grouperSecureFileCreatedOnMillis | 12345 | millis since 1970 that this was created |
| grouperSecureFileDeleteMinutesAfterDownload | 60 | number of minutes to delete the file after download. If zero, then delete after successful download |
| grouperSecureFileDeleteOnMillis | 12346 | millis since 1970 that this should be deleted |
| grouperSecureFileLastDownloadOnMillis | 123456 | time this was last downloaded |
| grouperSecureFileDbPointer | abc123 | uuid of the grouper_file record |
| grouperSecureFileSentToType | user\|group | to keep track of each user downloading the file |

Attributes will not be stored in PIT

Audit when downloading a file

URL to download a file: [https://url.to.grouper/grouper/grouperUi/app/UiV2Main.index?operation=UiV2File.download&secureFileId=abc123](https://url.to.grouper/grouper/grouperUi/app/UiV2Main.index?operation=UiV2File.download&secureFileId=abc123)

Link to delete after download?

## Daemon

1. Look for grouperSecureFileDeleteOnMillis in past, remove that attribute assignment
2. Look for files older than X (30 days?) with no grouperSecureFileDeleteOnMillis, assume orphaned, delete those
3. Look for grouper files (table) with system name: grouperSecureFile, which do not have any pointers to it, and delete those

## UI to see files (future)

See a list of files when in the UI with download links, and maybe delete options
