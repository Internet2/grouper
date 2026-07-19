---
title: "v2.5 new features"
space: GrIntDev
pageId: 48793863
version: 26
lastUpdated: 2026-07-19T00:33:45.663Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793863/v2.5+new+features
---

## New Features in Grouper 2.5

Grouper 2.5 includes many helpful new features, as listed below, as well as the enhancements provided in Grouper 2.4 patches, such as visualization and reporting.

The upgrade from 2.4.0 to 2.5 is not generally a major upgrade. The database did not change much.

You are required to use a container when running Grouper. This will ensure you have consistent directory structure, the correct version of libraries, and low risk and low effort upgrades. There are [instructions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning) to make using the container as easy as possible.

| Expirable groups | [Groups can have enabled / disabled dates and can be disabled (RBAC feature](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544574/Grouper+enabled+and+disabled+dates)) |
| --- | --- |
| Container required | [Grouper requires a container to run. No tarballs will be distributed.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544489/Grouper+Packaging+and+Versioning)   No more patches, no more confusing upgrades, no more inconsistent environments, no more lengthy installs |
| Container redesign | [One servlet container, easier mounts, one directory structure, fewer processes, maven build, patchless](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation) |
| Grouper installer installs container | REMOVED |
| Improve pagination in WS | [Cursor based paging allows fewer memory problems and paging which does not skip records](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544698/Cursor+based+paging+to+download+large+amounts+of+data+without+missing+records+during+inserts+deletes) |
| Gantt chart for jobs | [See when jobs have executed, job overlap, how long jobs take, success or error](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547702/Grouper+Daemon+-+job+history+chart) |
| Add new web services | [Get audit log Web Service](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548223/Get+Audit+Entries) [Add point in time options for WS get members, get groups, group save, get memberships](https://grouper.atlassian.net/browse/GRP-2180) |
| Attributes on memberships in UI | [Allow direct and indirect attributes on memberships in UI (see JIRA)](https://grouper.atlassian.net/browse/GRP-2434). See wiki documentation [here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545447/Membership+view+advanced+options) |
| WS and UI authentication | [Basic authn stored in database. Passwordless WS authn in future. This is more friendly for containers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) |
| Simple custom UI | [Analyze access for a policy. Allow easy join/leave. One-pager application](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI) |
| Automatic DDL upgrades | [Grouper can automatically adjust the database structure in a controlled way when a new container is run](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548570/DDL+in+Grouper) |
