---
title: "Grouper documentation move to Atlassian cloud"
space: GrIntDev
pageId: 48793162
version: 5
lastUpdated: 2026-07-12T06:46:10.232Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793162/Grouper+documentation+move+to+Atlassian+cloud
---

From the Internet2 on-prem Confluence (Data Center) to a free Atlassian Cloud open-source Premium instance

## Summary

Grouper's community documentation moved from the Internet2 on-prem Confluence (Data Center) to a free Atlassian Cloud open-source Premium instance at [https://grouper.atlassian.net/](https://grouper.atlassian.net/). We migrated to give it a stronger home: better search, lower-effort upkeep with AI tooling, and a platform that keeps improving. The move also gets us well ahead of the on-prem Data Center end-of-life on March 28, 2029, and the recurring technology problems that come with the on-prem stack. Data-migration testing has proven the move viable. We switched on Tuesday June 30, 2026. Grouper Jira is hopefully soon to follow.

## Background

- Grouper's documentation is a valued community effort. Moving to a modern platform is an opportunity to make it even better - easier to search, simpler to keep current, and clearer to navigate as the product grows.
- Current docs: [spaces.at.internet2.edu/spaces/Grouper/overview](https://spaces.at.internet2.edu/spaces/Grouper/overview)
- New home: a free Atlassian Cloud open-source Premium instance at [grouper.atlassian.net](https://grouper.atlassian.net/), already approved through Atlassian's open-source program.

## Plan

- **Tuesday June 30, 2026:** on-prem legacy Confluence will be switched to read-only.
- **Migrate content:** from Internet2 to [grouper.atlassian.net](http://grouper.atlassian.net).
- **Space:** only the Grouper space will be migrated.
- **Transition:** the existing Internet2 space stays available read-only for reference. A banner at the top of each page will explain the migration and link to the new site. There is no timeline for this state.
- **Potential deep links:** we will attempt to programmatically add links to on-prem pages to deep link to the cloud site. The link format changes from on-prem to cloud so this is not a trivial process.

## Why move (in priority order)

1. **Better search.** Substantially stronger than today, making it easier for users to find guidance and troubleshooting - a common complaint in Grouper documentation surveys. Search will be isolated to Grouper product documentation only, with no unrelated spaces or internal content, unlike on-prem search.
2. **Lower maintenance effort with MCP + Rovo.** MCP servers and Atlassian's native Rovo AI let us generate, update, reorganize, and de-duplicate pages with a fraction of the human time previously required, so the small dev team can keep docs current without pulling away from Grouper development.
3. **On-prem reliability and team cost.** The Internet2 on-prem Atlassian stack has added overhead for the team: slow performance, multiple web-application-firewall issues that cause delays and tickets, and very short session timeouts. Releases have sometimes been delayed because Confluence and Jira are needed to publish upgrade instructions and the on-prem instances are not always available when needed. On-prem also does not consistently prompt for federated authentication.
4. **Better upgrades.** Atlassian handles upgrades, and new cloud-only features ship continuously, versus the periodic on-prem upgrades we manage today.
5. **Additional benefits.** Permanent stable URLs, live co-editing, mobile editing, tighter Jira Cloud integration, and native page analytics (views and engagement data) to guide the docs revamp.

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Existing Internet2 links and bookmarks break. | The Internet2 space stays available read-only, with a note and link to the new site at the top of each page. We will also attempt to add deep links from on-prem pages to their cloud equivalents. |
| Some macros and page layouts do not render identically in Cloud. | We know some include macros break, but we can fix them programmatically; we will also audit pages after migration and correct any other unsupported macros. |
| Links to Internet2 Jira break until Grouper Jira also migrates. | We plan to migrate Grouper Jira soon after, and will update key cross-links during the interim. |
| Editor access is capped at 25 licenses. | The open-source Cloud instance includes up to 25 editor licenses, which we can rotate among community members as they have time to contribute. |
| Rovo AI tokens are limited and could be used up or abused. | Rovo runs on a capped token allowance, and the free open-source plan does not provide a lot, so we will watch usage to guard against abuse. The search in the cloud (without Rovo) is still better than on-prem since it is constrained to one space and uses newer technology. |
| User management moves off Internet2 Grouper. | We lose the ability to manage users from Internet2 Grouper, but we will look into SCIM provisioning to see if we can retain those automations. |
