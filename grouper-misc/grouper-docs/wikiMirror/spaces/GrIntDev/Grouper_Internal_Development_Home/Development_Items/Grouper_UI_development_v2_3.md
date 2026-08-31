---
title: "Grouper UI development v2.3"
space: GrIntDev
pageId: 48792616
version: 21
lastUpdated: 2026-07-12T17:02:39.633Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792616/Grouper+UI+development+v2.3
---

[Kick the tires on the 2.2 UI on the demo server](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48791930/Grouper+demo+Technical+Administration)

### Feature requests for Grouper UI v2.3

| Request name | Description | Requester(s) | Notes | Priority |
| --- | --- | --- | --- | --- |
| External users | Migrate the Lite UI [external users screens](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545569/Grouper+external+subjects) to new UI | Chris | DONE - 2.3.0 API patch 107, UI patch 44 | 1 |
| Attribute definitions and permissions | Migrate the Lite UI attribute screens to new UI | Chris | DONE - 2.3.0 API patch 107, UI patch 44 | 2 |
| Rules editor | Be able to edit [common rules and / or all rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) in UI | Michael Gettes |  | 3 |
| Auditing | Add [user based](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548937/User+Audit+Log) and [overall (point-in-time)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548000/Point+in+Time+Auditing) auditing | Tom Barton | DONE - 2.3.0 API patch 107, UI patch 44 | 4 |
| Attribute handling at objects | It would be really nice to handle attribute usage for groups, memberships and so on the page that handles the object. See the UI being developed at Uppsala University. | Uppsala University     Cru (CCCI) |  | 5 |
| Encapsulated composites | Would be nice if the UI treated include/exclude lists and require groups differently (maybe if they follow a naming convention) so the user doesn't need to know which group is the whitelist etc, the UI makes it easy. Maybe this is in phase 2... If looking at the full group the UI knows to edit the whitelist etc. | Steven C.     Chris |  | 6 |

### Features in the new UI in Grouper v2.2

link to documentation on the Grouper v2.2 UI

| Feature |
| --- |
| User experience designer created look and feel and did user testing |
| Accessible controls which can be controlled by keyboard only |
| Responsive layout is mobile friendly |
| Cross site scripting secure |
| Ajaxy implementation allows for functions to be completed without navigating from page |
| Tree control for folder browsing |
| Objects can be favorited for easy finding from splash page |
| "Services" tag can group objects together into one application |
| Dashboard shows recent activity, tree control, favorites, etc |
| Bulk assignments screen for adding memberships for multiple members to multiple groups |
| Much easier privilege management for folders and groups |
| Can deprovision memberships and privileges easier |
