---
title: "Grouper Training - MCP AI - Lesson 03: Connecting AI clients"
space: Grouper
pageId: 332431362
version: 13
lastUpdated: 2026-10-26T16:22:49.796Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/332431362/Grouper+Training+-+MCP+AI+-+Lesson+03+Connecting+AI+clients
---

In this lesson you connect the Claude app to your GTE through MCP, see what the AI can do with read-only access, and see what it cannot do.

Before you start, complete [Grouper Training - MCP AI - Lesson 00: Pre-work](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work). It adds the `grouper-gte-local` MCP server and the `grouperMcpTraining` folder.

Stay organized: name this lesson's conversations `Lesson 03: ...` and keep them in a sidebar section named `Lesson 03 Connecting`, as in Lesson 01.

## Step 1: start your GTE

1. Start your GTE VM and connect to it with SSH, as in the pre-work. Leave that SSH window open: it is the tunnel to Grouper
2. Reset the GTE to a fresh container, since an earlier lesson already changed it. If you are still in GSH, type `:quit` first, so you see the `student@...` prompt.
  
  In the SSH window:
  
  
  ```bash
  ./gte 101.1.1
  ```
  
  Wait a few minutes for Grouper to start
3. Confirm [https://localhost:8443/grouper](https://localhost:8443/grouper) loads in your browser (accept the security warning)

## Step 2: turn on SQL for MCP

MCP has no SQL access by default. In the UI as banderson, go to "Miscellaneous" -> "Configure" -> "Configuration files" -> "Import", choose `grouper.properties`, and import:

```
grouper.mcp.sql.grouper.grouperDatabase = true
```

`grouper` is the main Grouper database connection. The `sql_select` tool can then run read-only SELECT statements on it, for people who are sysadmins and in [etc:mcp:mcpUsersCanRunSqlReadonly](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpUsersCanRunSqlReadonly), and only when they check "SQL read-only" on the consent page.

In the UI, go to the group [etc:mcp:mcpUsersCanRunSqlReadonly](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpUsersCanRunSqlReadonly) and check that [etc:sysadmingroup](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:sysadmingroup) is a member. If it is not, add it.

## Step 3: sign in to MCP

This is a fresh GTE with a new database, so Claude's registration and login from the old GTE do not work with it. Remove and add the MCP server, then sign in. You do this after every new GTE, so it is good practice. Do this in Claude Code on the command line. In a terminal:

```bash
cd ~/grouperMcpTraining
claude mcp remove -s user grouper-gte-local
claude mcp add --transport http -s user grouper-gte-local https://localhost:8443/grouper-ws/mcp
claude
```

1. Type `/mcp`, select "grouper-gte-local", then select "Re-authenticate"
2. A browser opens. Accept the security warning and log in as banderson / password
3. On the consent page, check "Read-only operations" and "SQL read-only", and approve
4. Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login

If the browser shows "Unknown client_id", the remove and add did not run: run those commands again.

## Step 4: find groups

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Find the groups of instructors for religion
courses.  Give me a table with the course number,
course name, and group name.
```

Expected: 8 instructor groups, for REL101, REL110, REL150, REL201, REL210, REL250, REL301, and REL399, under [basis:sis:course:rel](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=basis:sis:course:rel).

Use the copy button to copy this prompt into the Claude app in the same conversation:

```
Add a column with how many members each group
has.  Use SQL, the sql_select tool, to count them.
```

Claude runs a SELECT on the Grouper database and adds a member count for each group. Expected: most groups have a few members. Ask it to show you the SQL it ran.

If something does not work, copy and paste the error into Claude in the same conversation, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### List the tools

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
What tools do you have from the grouper-gte-local
MCP server?  Group them by what they do, and tell
me which ones I can use with my current access.
```

You should see tools for groups, folders, members, privileges, attributes, audit, and documentation, and that you only have read-only access right now.

### Approve from notifications

While Claude works you can do something else, and approve its requests from your computer's notifications. In the Claude app settings, set "Permission request notifications" to "Banners". On a Mac, also allow notifications for Claude in "System Settings" -> "Notifications", and they collect in Notification Center. When Claude needs approval while the app is in the background, a notification shows: click it to go to the request.
