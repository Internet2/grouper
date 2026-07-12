---
title: "Grouper issue tracker move to Atlassian cloud"
space: GrIntDev
pageId: 48793157
version: 5
lastUpdated: 2026-07-12T06:46:09.614Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793157/Grouper+issue+tracker+move+to+Atlassian+cloud
---

From the Internet2 on-prem Jira (Data Center) to a free Atlassian Cloud open-source Premium instance

## Summary

Grouper's issue tracker - the GRP project - is moving from the Internet2 on-prem Jira (Data Center) to Jira in the same free Atlassian Cloud open-source Premium instance at [https://grouper.atlassian.net/jira](https://grouper.atlassian.net/jira) that now hosts the community documentation. This follows the documentation move, which switched on Tuesday June 30, 2026. The target cutover is Thursday July 16, 2026. Moving Jira gives issues a stronger home: better search, tighter integration with the migrated docs, lower-effort upkeep with AI tooling, and a platform that keeps improving. The move also gets us well ahead of the on-prem Data Center end-of-life on March 28, 2029, and the recurring technology problems that come with the on-prem stack.

## Background

- Grouper's issue tracking - bugs, feature requests, and release planning - is a valued community effort. Moving to a modern platform makes issues easier to search, link, and keep current as the product grows.
- Current issues: the GRP project in the Internet2 on-prem Jira.
- New home: Jira in the free Atlassian Cloud open-source Premium instance at [grouper.atlassian.net/jira](https://grouper.atlassian.net/jira), already approved through Atlassian's open-source program and already hosting the migrated documentation.
- Issue keys are preserved: GRP-nnn keys stay the same, so only the domain and URL context path change.

## Plan

- **Thursday July 16, 2026:** the on-prem legacy Jira GRP project will be switched to read-only.
- **Migrate content:** issues, comments, and history are exported from on-prem as CSV and imported to [grouper.atlassian.net](https://grouper.atlassian.net/jira).
- **Attachments:** CSV export does not carry attachment files, so attachments will be migrated separately via the Jira REST API - downloaded from on-prem and re-uploaded to the matching cloud issue by its (unchanged) key.
- **Project:** only the Grouper (GRP) project will be migrated.
- **Preserve issue keys:** GRP-nnn keys are kept, so only the domain and URL context path change.
- **Update documentation links:** because keys are stable and only the base URL changes, we will programmatically rewrite Jira links in the migrated wiki from the on-prem base URL to [grouper.atlassian.net](https://grouper.atlassian.net/jira).
- **Transition:** the existing Internet2 Jira stays available read-only for reference. A note will link to the new site. There is no timeline for this state.

## Why move (in priority order)

1. **Better search.** Substantially stronger issue search than today, making it easier to find bugs, features, and history. Search will be isolated to the Grouper project only, with no unrelated projects or internal content, unlike on-prem search.
2. **Tighter integration with the migrated docs.** With Jira Cloud and Confluence Cloud in one instance, issue links, smart links, and release notes connect cleanly across documentation and issues - something the split on-prem and cloud setup cannot do today.
3. **Lower maintenance effort with MCP + Rovo.** MCP servers and Atlassian's native Rovo AI let us triage, update, reorganize, and de-duplicate issues with a fraction of the human time previously required, so the small dev team can keep the tracker healthy without pulling away from Grouper development.
4. **On-prem reliability and team cost.** The Internet2 on-prem Atlassian stack has added overhead for the team: slow performance, multiple web-application-firewall issues that cause delays and tickets, and very short session timeouts. Releases have sometimes been delayed because Confluence and Jira are needed to publish and track upgrade instructions and the on-prem instances are not always available when needed. On-prem also does not consistently prompt for federated authentication.
5. **Better upgrades.** Atlassian handles upgrades, and new cloud-only features ship continuously, versus the periodic on-prem upgrades we manage today.
6. **Additional benefits.** Permanent stable URLs, mobile access, native automation, tighter Confluence Cloud integration, and native analytics to guide how we manage the tracker.

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Existing Internet2 issue links and bookmarks break. | The Internet2 Jira stays available read-only, with a note and link to the new site. Because issue keys are unchanged, links we control - such as those in the wiki - can be updated with a simple base-URL rewrite. |
| CSV export does not carry attachment files. | Attachments will be migrated separately via the Jira REST API, matching on the unchanged issue keys, and verified after import. |
| Some workflows, custom fields, or plugins do not map identically in Cloud. | We will audit and reconfigure workflows and fields after migration, and simplify where on-prem plugins have no cloud equivalent. |
| User and agent access is capped at 25 licenses. | The open-source Cloud instance includes up to 25 licenses, which we can rotate among community members as they have time to contribute. |
| Rovo AI tokens are limited and could be used up or abused. | Rovo runs on a capped token allowance, but only the licensed users (up to 25) can consume Rovo tokens, so the risk of running out or of abuse is bounded; we will still keep an eye on usage. The search in the cloud (without Rovo) is still better than on-prem since it is constrained to one project and uses newer technology. |
| User management moves off Internet2 Grouper. | We lose the ability to manage users from Internet2 Grouper, but we will look into SCIM provisioning to see if we can retain those automations. |
