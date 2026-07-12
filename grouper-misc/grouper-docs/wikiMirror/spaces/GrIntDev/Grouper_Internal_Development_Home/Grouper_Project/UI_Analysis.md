---
title: "UI Analysis"
space: GrIntDev
pageId: 48792470
version: 8
lastUpdated: 2026-07-12T17:02:35.383Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792470/UI+Analysis
---

## Grouper UI Analysis

The purpose of this UI analysis is to gather input, feedback, ideas and functional enhancements for the Grouper User Interface. This effort is currently (January '08) spearheaded by the University of Pennsylvania.

The motivation of this initiative is to improve the overall baseline UI to better coincide with the needs of a large scale deployment where end users (in mass) will be able to intuitively navigate and interact with the grouper functionality for which they have permissions.

### Approach

The approach taken was to create a wire-frame model from the perspective of an end user who may want to search and find a group to join (opt in). Administrative functionality was then added without disrupting the consistency of the interface. The idea being that the interface remains the same and the amount of functionality exposed to the user is dependent on their underlying permissions.

### Wire Frame

The [wire frame](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792470/UI+Analysis) is in PDF format and represents, conceptually, a different approach to a grouper front end

### Proposed UI Modifications

A UI Subgroup was formed from interested members of the development community to review and discuss changes that would have a low technical implementation effort but provide a high impact to site usability.

This proposed UI redesign is in PDF format and details a variety of visual enhancements to the grouper user interface

The proposed changes to the default grouper terms that are used by the user interface focus on more intuitive and common terminology

### Initial Findings

Initial evaluation of the existing out-of-the-box user interface shows a number of issues that make it somewhat cumbersome to use.

- Buttons and navigation are not used in a consistent manner
- Inconsistent or inappropriate terminology for users to understand
- The use of 'Saved' (in navigation) implies persisted across session while clipboard or workspace implies session specific
- Cancel is used as a back button
- No context sensitive help
- Delete doesn't have a confirmation dialog
- Make names more friendly (Terms like 'stem' or 'extension' don't convey an intuitive meaning across broad user groups)
- Search Results don't show enough identifiable data to distinguish between two people named 'Ed Smith'

A [navigation flow diagram](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792470/UI+Analysis) shows the interaction between pages and functions. This was helpful in determining that search is a core utility that precedes most other site actions. This leads to the following conclusions:

- Basically all operations are performed against search results
- Abstract out search to its own utility-
- Create utility/tool bar for Help, browse, search, log in/out
- Simplify screen flow overall
- Search results should show more attribute data in grid/table format and sortable by column.

### UI Modifications to Baseline

The UI subgroup was amiable to a series of modifications that are thought to make the baseline user interface significantly stronger. The modifications will be deployed as part of the Spring 2008 release of v1.3.

The scope of what will be in v1.3 is largely related to easier to understand terminology and help text. The help text will be in the format of tooltips and infodots.

Details of what is implemeted can be found on the UI Modifications page and the supporting technical documentation pages for [tooltips](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824700/Tooltips+and+Terms)and [infodots](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824689/Infodot+help)

Questions or comments?  [Contact us](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541872/Contact+Information).
