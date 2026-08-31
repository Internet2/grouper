---
title: "Sharing web service logic between grouper-ws and the Grouper UI"
space: Grouper
pageId: 166363145
version: 6
lastUpdated: 2026-08-27T20:23:52.350Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/166363145/Sharing+web+service+logic+between+grouper-ws+and+the+Grouper+UI
---

> This page is a working document for the developers and core team while the change is in flight. Once the task is done it moves to the Grouper Internals / Developer space (GrIntDev).

 

## Why

 The Grouper UI is growing an AI help screen. The screen is optional and provider-agnostic: the institution enables it and configures its own provider, and Grouper ships no default credentials.

 The screen drives Grouper operations through a tool layer: the AI proposes a call, the UI validates it against the session's scope, executes it, and feeds the result back. That tool layer already exists as the MCP tools in grouper-ws. But grouper-ui depends on grouper and grouperClient, not grouper-ws, so today the UI cannot reach any of it.

 

## Proposal

 Move the non-transport parts of grouper-ws into the grouper project, keeping the same package names (`edu.internet2.middleware.grouper.ws.*`). No imports change anywhere.

 

| Moves to grouper | Stays in grouper-ws |
| --- | --- |
| - `coresoap` (128 files) - `rest` (131 files) - the MCP tool classes (34 of 38) - `exceptions`, `query`, `member`, `util` - `GrouperServiceLogic` | - the servlet filter - the `j2ee` package - the servlet-bound security classes - the four MCP servlets, including OAuth - the versioned compatibility trees (v1_6 through v2_5, about 700 files) |

 The unit tests come with it. Today the tests for web service logic live in grouper-ws and run as their own thing; once the logic is in the grouper project, those tests are maintained alongside the rest of the Grouper API unit tests, in the same source tree and the same test run. Only the transport-level tests stay behind with the servlets.

 Some of this has already happened -- `grouper/src/grouper/edu/internet2/middleware/grouper/mcp` holds the config search index, doc search index, and tool log today.

 

## The coupling is thinner than it looks

 `GrouperServiceLogic` is 11,329 lines and touches `HttpServletRequest` exactly once.

 `GrouperServiceJ2ee` is referenced by 30 files outside itself, but of roughly 111 calls, 86 are `retrieveDebugMap` and only 3 touch a servlet type. The rest is per-request context: logged-in subject, act-as subject, request start time.

 So the class does not move. The ThreadLocals and their accessors become a small context class in grouper core; `GrouperServiceJ2ee` stays in grouper-ws as the filter that populates it. That is about seven methods, and the debug-map call sites are a mechanical replace.

 That split is also what makes the UI path possible. `retrieveSubjectLoggedIn` is exactly the value the UI supplies differently -- it has an authenticated UI session, no OAuth token, and no filter in that chain. With a plain context object, the UI populates it from the UI session, grouper-ws populates it from its own path, and `GrouperServiceLogic` never knows which it is running under.

 

## One thing worth a second opinion

 Keeping the same package names means `edu.internet2.middleware.grouper.ws` would exist in two jars, since `GrouperServiceJ2ee` stays behind. Java permits this and we do not foresee a problem, but it is worth a second set of eyes before we commit to it.

 

## Versions

 Do this in v7 only. v4 has no ws/mcp package at all. v6 has one, but this is a structural change with no functional payoff on a maintenance branch.

 The cost is that a v7 fix backported to v6 lands on a path that does not exist there. The mitigation is to make the move a single pure-rename commit with no content edits -- `git mv` and nothing else. Rename detection then resolves it, `cherry-pick -3` and `log --follow` keep working, and path translation stays mechanical. The `GrouperServiceJ2ee` context extraction goes in a separate commit afterward. If renames and edits land together, every backport becomes hand work.
