---
title: "Grouper issue tracker moved to Atlassian cloud"
space: Grouper
pageId: 65241091
version: 6
lastUpdated: 2026-07-19T01:57:39.878Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/65241091/Grouper+issue+tracker+moved+to+Atlassian+cloud
---

The Grouper issue tracker - the GRP project - has moved from the Internet2 on-prem Jira (Data Center) to Jira in the Atlassian cloud at [grouper.atlassian.net](https://grouper.atlassian.net/browse/GRP), joining the community documentation that moved there earlier. The migration completed on 18 July 2026.

Every issue kept its original issue key, so GRP-1234 is still GRP-1234. Issue numbering and ordering are preserved end to end.

## Why we migrated

- It puts the issue tracker on the same platform as the community documentation, which already moved to the Atlassian cloud.
- The on-prem instances will eventually go away, so the tracker would need to be migrated regardless - doing it now avoids a forced, rushed move later.
- The on-prem Confluence has had reliability problems.
- The cloud gets new features and continuous updates.
- It gives developers and power users consistent access, alongside the documentation.
- Other benefits: MCP, Rovo, and improved automation.

## Why now, and why we did not announce sooner

We had planned to run a small trial migration first and announce once we were confident. In practice, building a faithful migration that preserves every issue key - without Atlassian's Cloud Migration Assistant, which we were not able to use - grew into a much larger effort and took far longer than expected. Rather than keep delaying, we have cut over now.

We cannot run both trackers in read-write mode at the same time: issue keys have to be migrated in order to keep the numbering intact (GRP-1234 must stay GRP-1234). So the on-prem tracker is now read-only, and will remain available in read-only mode; all new work happens in the cloud.

## What was migrated

This was a large piece of work. The migration brought across:

- Every issue, GRP-1 through GRP-7143, each with its original issue key preserved exactly
- Original created, updated, and resolved dates
- Reporters and assignees, matched to real cloud accounts where the person has one
- Every comment, with its original author and date
- Statuses, resolutions, priorities, issue types, components, and affected and fix versions
- Attachments - all that still existed on the old server
- Rich-text formatting for descriptions and comments, restored after import
- Issue links and subtask relationships
- New cloud configuration to receive it all: matching workflow statuses, a Critical priority, a Documentation issue type, and the full set of released versions and components

Where someone who reported or was assigned an issue does not have a cloud account, their name is recorded in the issue description, so there is still a full record of who reported and worked each issue.

## The markdown archive

The migration also produced a complete markdown archive of every issue - all 7,034 of them - stored in git as a permanent, searchable, plain-text record. It is not that large of an addition to Grouper git.

## Protecting contributor privacy

To avoid exposing harvestable email addresses, personal emails throughout the migrated issues, comments, and the markdown archive have been redacted: each address keeps its local name but has its domain replaced with example.com - for example, jane@school.edu becomes jane@example.com. Display names are kept, so attribution is preserved.

## Reporting bugs and getting help

Everything in the cloud tracker is public and can be read by anyone, without an account. There are fewer accounts in the cloud instance, though, so the best way to report a bug, ask a question, or interact on an issue is Slack - the incommon-grouper channel in Internet2's Slack, another grouper channel, or a direct message to the developers. See [Contact Information](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541872/Contact+Information) for how to join. Slack includes the Grouper developers and experienced users, and is the fastest way to get a response.

## The old on-prem tracker

The previous on-prem tracker at todos.internet2.edu is now read-only and will stay available in read-only mode. To enforce this, editing privileges were removed, and the on-prem project was renamed from "Grouper" to "Grouper (read-only - migrated to grouper.atlassian.net/jira)".

Because issue numbers were preserved, an old link such as todos.internet2.edu/browse/GRP-1234 still points to the same issue, which is also at [grouper.atlassian.net](https://grouper.atlassian.net/browse/GRP) under the same number. Feel free to adjust your own links to the cloud tracker if you like. We have also swept the Grouper wiki: all 6,361 links to the on-prem tracker, across 161 pages, have been repointed to the cloud tracker at grouper.atlassian.net.
