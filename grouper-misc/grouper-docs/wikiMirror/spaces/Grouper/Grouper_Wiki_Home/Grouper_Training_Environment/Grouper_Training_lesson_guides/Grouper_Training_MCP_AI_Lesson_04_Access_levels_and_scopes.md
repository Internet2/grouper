---
title: "Grouper Training - MCP AI - Lesson 04: Access levels and scopes"
space: Grouper
pageId: 331677779
version: 9
lastUpdated: 2026-10-26T16:22:50.289Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/331677779/Grouper+Training+-+MCP+AI+-+Lesson+04+Access+levels+and+scopes
---

In this lesson you see the layers that decide what the AI may do: your Grouper privileges, the MCP access groups, the scopes you grant on the consent page, and the limits MCP always enforces. Then you see that every change is audited as you.

Before you start, complete [Grouper Training - MCP AI - Lesson 03: Connecting AI clients](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/332431362/Grouper+Training+-+MCP+AI+-+Lesson+03+Connecting+AI+clients). You should be signed in to MCP with read-only access.

Stay organized: name this lesson's conversations `Lesson 04: ...` and keep them in a sidebar section named `Lesson 04 Access levels`, as in Lesson 01.

## Step 1: try a change with read-only access

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Create the group test:mcpWriteTest with the
description "Created by AI over MCP".
```

This should be refused: on the consent page you granted only read-only access, so the AI cannot make changes, even though banderson can make this change in the UI.

## Step 2: grant read-write access

Do this in Claude Code on the command line. In a terminal, start Claude Code in the training folder:

```bash
cd ~/grouperMcpTraining
claude
```

1. Type `/mcp`, select "grouper-gte-local", then select "Re-authenticate"
2. Log in as banderson / password
3. On the consent page, check "Read-only operations", "Read-write operations", and "SQL read-only"
4. Under "Limit read-write scope", in "Folders", enter `test` and click "Add folder", then enter `ref` and click "Add folder". The AI can then change groups only in the `test` and `ref` folders
5. Approve
6. Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login
7. In the Claude app, go back to the conversation from step 1

## Step 3: try the change again

Use the copy button to copy this prompt into the Claude app in the same conversation as step 1:

```
try again
```

Claude picks up the new scope in the conversation you already have, so you do not have to start over when you change scopes.

This time it should work. Check the group in the UI: [test:mcpWriteTest](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=test:mcpWriteTest). The audit shows banderson made the change, because the AI acts as you.

## Step 4: tell Claude to use SQL

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Add this to my user memory, ~/.claude/CLAUDE.md:
when you use the grouper-gte-local MCP server,
use SQL (the sql_select tool) when you can, e.g.
for membership counts and reports.  Do not call
sql_get_schema, just run SELECT statements on the
Grouper views, e.g. grouper_memberships_lw_v.
```

Claude adds it to your memory, so it uses SQL for counts in every conversation from now on.

## Step 5: do a real lesson through MCP

In the introduction, AI wrote a GSH script for [Use cases - Lesson 01: Basis and reference groups part 1](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1) and you ran it. This time Claude does the lesson itself, through the MCP tools.

The lesson creates the folder [ref:student](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=ref:student) and changes groups in it, which your read-write scope from step 2 allows.

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Do the exercise in Grouper Training - Use cases -
Lesson 01: Basis and reference groups part 1 for
me, using the Grouper MCP tools:
https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1
Then answer the questions in the lesson.
```

Expected: Claude creates [ref:student](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=ref:student) and [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students) with its description and the `ref` type, adds the class year groups `ac:2027` to `ac:2030`, adds `cm:2026` with an end date of 12/31/2026 5:00 PM, and tells you how many students are in the group. Check [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students) in the UI. Later lessons do the same exercise with Grouper's AI assistant and with Claude driving the UI, so note the student count to compare.

If something does not work, copy and paste the error into Claude in the same conversation, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### Try a change outside your scope

Ask it to create a group in a folder outside your read-write scope, e.g. `app:mcpWriteTest`. It should be refused, because you limited read-write to the `test` and `ref` folders.

### Protected folders

Some folders should never be changed by AI, even by a Grouper sysadmin with read-write access. As banderson in the UI, go to "Miscellaneous" -> "Configure" -> "Configuration files" -> "Import", choose `grouper.properties`, and import:

```
grouper.mcp.protectedFolders = ref:security
```

Wait a minute for the setting to take effect, then:

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Add banderson as a member of
ref:security:locked_by_ciso.
```

This should be refused, even though your read-write scope includes `ref` and banderson is a sysadmin: MCP never changes a protected folder. You can still make the change yourself in the UI.

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Add banderson as a member of
etc:mcp:mcpUsersAdminReadWrite.
```

This should be refused too: the `etc` folder is always protected, so AI can never change who has access to MCP, including its own access.

### Every change is audited

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Show me the audit history for
test:mcpWriteTest.  Who made each change, and
when?
```

The group you created in step 3 shows banderson, not "the AI": the AI acts as you, and you are accountable for the changes you approve.

Do not just trust the AI: check the audit log yourself. Open the [audit page for ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewAudits&groupName=ref:student:students), from step 5, and see who made each change and when.
