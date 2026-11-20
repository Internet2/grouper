---
title: "Grouper Training - MCP AI - Lesson 01: Introduction"
space: Grouper
pageId: 313720842
version: 28
lastUpdated: 2026-10-25T05:40:19.051Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/313720842/Grouper+Training+-+MCP+AI+-+Lesson+01+Introduction
---

In this lesson you ask Claude Code questions about the Grouper source code. No Grouper server is needed.

> This lesson is done with MCP not connected, in case your institution does not allow AI to connect to Grouper.

Before you start, complete [Grouper Training - MCP AI - Lesson 00: Pre-work](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work).

## Start Claude Code

1. Open the Claude app and click "Code" at the top
2. Start a new chat. In the row under the prompt box, click the folder control, click "Open folder...", and choose the `grouperMcpTraining` folder in your home folder
3. Make sure "Auto" mode is selected in the Claude app
4. Make sure Opus 5.5 and "Medium" effort are selected. If you set the model, effort, and folder first thing in a new conversation, they stick for your new conversations.
5. Allow Claude to finish tasks
  
  1. Do not allow Claude to access data from other apps
  2. Do not allow Claude to access other folders on your computer (copy files/folders into the allowed folder)

## Stay organized

When you do your work through AI, you end up with a lot of conversations, so keep them organized from the start. Rename each conversation to start with the lesson number, e.g. `Lesson 01: GSH script`, and keep each lesson in its own sidebar section, e.g. `Lesson 01 Introduction`. Right-click a conversation in the sidebar to rename it or move it to a section. Do this for every conversation in this training.

## Step 1: ask for a GSH script, with no help

1. Use the copy button to copy this prompt into the Claude app in a new conversation:
  ```
  Write a GSH script to create the group
  test:testGroup.
  ```
2. Try the script. In the SSH window on your GTE VM, start GSH and paste the script in:
  ```bash
  ./gte-gsh
  ```
  
  It will probably not work. That is the point of this step
3. Copy the whole GSH session, the script and any errors, and paste it into the Claude app in the same conversation. Then quit GSH, so you are back at the `student@...` prompt:
  ```
  :quit
  ```

## Step 2: steer Claude in the right direction

Give Claude what it was missing: where the Grouper source is, and what you learned about GSH.

1. Tell Claude about the Grouper source. A `CLAUDE.md` file in a folder holds instructions that Claude reads at the start of every conversation in that folder. Use the copy button to copy this prompt into the Claude app in the same conversation:
  ```
  Create a CLAUDE.md file in this folder that tells
  you about the Grouper source code:
  - The grouper folder is the Grouper v7 source
  - Use it to answer questions about how Grouper
    works, GSH syntax, and config
  - Config defaults are in the *.base.properties
    files, e.g. grouper/grouper/conf/
    grouper.base.properties and
    grouper/grouper/conf/grouper-loader.base.properties
  - The Grouper wiki is mirrored in
    grouper/grouper-misc/grouper-docs/wikiMirror
  - Jira issues are mirrored in
    grouper/grouper-misc/grouper-docs/issueMirror
  - Check the source before you answer, and say
    which file you used
  ```
  
  Claude creates `~/grouperMcpTraining/CLAUDE.md`. Open it and read it: you can edit it yourself at any time, or ask Claude to
2. Make a GSH writing skill from what you learned. Use the copy button to copy this prompt into the Claude app in the same conversation:
  ```
  Create or update my GSH writing skill with what we
  learned from that GSH session, so you get it right
  next time.  Include these GSH rules:
  - Check the Grouper source code in the grouper
    folder to make sure the syntax works in the GSH
    shell
  - GSH is Groovy without semicolons, so in a call
    chained over several lines, end each line with
    the period, otherwise the line is a complete
    statement on its own
  - End every script with a blank line, so it runs
    when pasted without pressing Enter
  Do not give me a new script yet.
  ```

> A common reason the first script fails: `GroupSave` code that is valid Java but not valid in the GSH shell. In GSH, a chained call needs the dot at the end of each line (or everything on one line), otherwise GSH thinks the line is done:
> 
> 
> ```
> new GroupSave().
>   assignName("test:testGroup").
>   assignCreateParentStemsIfNotExist(true).
>   save()
> ```
> 
> Also end the script with a blank line, so the last line runs when you paste it. With the skill, Claude remembers this the next time it writes GSH.

## Step 3: try again, in a new conversation

1. Use the copy button to copy the same prompt into the Claude app in a new conversation:
  ```
  Write a GSH script to create the group
  test:testGroup.
  ```
  
  This time Claude reads `CLAUDE.md` and uses the GSH skill
2. Run `./gte-gsh` and paste in the new script. Then type `:quit` to quit GSH. Check the group in the UI: [test:testGroup](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=test:testGroup)
3. Compare it with the first script. If it still fails, paste the GSH session into Claude in the same conversation, ask it to update the skill, and try again

## Step 4: do a training lesson, quickly

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
help me do this exercise, but quickly.  write one
GSH script that does everything, and tell me how
to copy and paste it into the terminal where I am
connected to the GTE: run the gte-gsh command,
then paste the script.  then tell me how to check
the results in the UI.  my UI is
https://localhost:8443/grouper

https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1
```

You should get one GSH script that:

- Creates the folder [ref:student](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=ref:student) and the group [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students) with its description
- Adds the `ref` type, with data owner `Registrar` and the member description
- Adds the class year groups [basis:sis:prog_status:year:ac:2027](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=basis:sis:prog_status:year:ac:2027) through `ac:2030` as members
- Adds [basis:sis:prog_status:year:cm:2026](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=basis:sis:prog_status:year:cm:2026) with an end date of 12/31/2026 5:00 PM

Run it as described, then type `:quit` to quit GSH. Check [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students) in the UI: the direct members, the indirect members, how many students it has, and the end date on the recently graduated group.

The source code folder has a copy of the wiki, including this lesson (in `grouper/grouper-misc/grouper-docs/wikiMirror`), so Claude Code can do this without a Grouper connection.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### Find a config default

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
What config property limits how many results an
MCP tool returns in one call, and what is its
default?
```

Expected answer: `grouper.mcp.maxPageSize` in `grouper.base.properties`. It is blank by default, which means no limit. It can be set per tool with `grouper.mcp.tool.<toolName>.maxPageSize`.

If the answer is wrong or Claude could not find it, use the copy button to copy this prompt into the Claude app in the same conversation:

```
That was not right.  The config defaults are in
the *.base.properties files in the Grouper source,
e.g. grouper/grouper/conf/grouper.base.properties.
Update CLAUDE.md in this folder so you check the
base config files first for config questions.
Then answer again.
```

### Explain how something works

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Explain how a composite group calculates its
members. Show me the classes involved.
```

You should get a plain explanation of union, intersection, and complement composites, with pointers to the classes in the `grouper` module. Ask a follow-up question about anything that is not clear.

### See what the project tells the AI

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
What does grouper/CLAUDE.md tell you to do?
```

The Grouper project ships its own `CLAUDE.md` with instructions for AI assistants, like the one you made in step 2. The answer should mention:

- The module layout: `grouper`, `grouper-ui`, `grouper-ws`, `grouper-misc`
- How config files layer: `*.base.properties`, overridden by the same file name without `.base`, overridden by database config
- House style, e.g. no Java streams or lambdas
- Commit messages in the format `GRP-XXXX: [Jira title]`

Your institution can do the same for its own repositories, so the AI follows your conventions. The same idea comes back later with MCP recipes.

### Your own question

Ask about something you have run into in your own Grouper, e.g. what a config property does or why a daemon behaves a certain way. Do not paste real institutional data.
