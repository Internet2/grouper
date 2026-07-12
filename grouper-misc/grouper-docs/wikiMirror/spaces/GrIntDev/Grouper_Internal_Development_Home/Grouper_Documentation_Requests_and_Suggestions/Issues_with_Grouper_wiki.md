---
title: "Issues with Grouper wiki"
space: GrIntDev
pageId: 48793000
version: 4
lastUpdated: 2026-07-12T06:45:58.683Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793000/Issues+with+Grouper+wiki
---

| Issue | Description |
| --- | --- |
| Search cannot find result | I2 spaces has too many spaces |
| Search cannot filter by Grouper | Minor issue is cannot anonymously search in Grouper space  More substantial issue the user shouldn't need to filter at all |
| Cannot tune the search results | Since there is more than one wiki, we cannot test searches,   tune the results, and expect that to persist in future (since   too many spaces) |
| Shared plugins, upgrades, etc | Having Grouper in shared I2 spaces means we are dependent   on the community for plugins, versions, upgrades, etc. e.g.   if we want to install a new search plugin for Grouper, it might   not be possible / practical while in spaces |
| Collocating Grouper docs with dev docs | Would be nice to have an "internal" space which is in a different   doc repo than Grouper docs. So both can be public   but the searches in Grouper space would never return internal    docs |
| Labels are global | Labels are not per space, they are global. So when searching by label   (which we want people to do since we will label pages by document   type), we would like to have a succinct and controllable set of labels |

This document is intended to identify the problem, but a solution POC of moving the Grouper user documentation to the cloud seems to address these. An issue with the is finding "cross product" docs (e.g. how to integration Grouper with Comanage). I think this problem is the lesser of two evils and people can search the Grouper docs and the Comanage docs for such information or could use Google. We have the existing problem with Grouper / Shibboleth / MidPoint which are in different doc repos and it has not been identified in the survey as a significant problem. Grouper should have links to all relevant ingration docs.

Another issue is SSO to the cloud. Worst case we have users/passes (for institutions without an EPPN identity provider).
