---
title: "Grouper Training - MCP AI - Lesson 05: Recipes"
space: Grouper
pageId: 332169255
version: 11
lastUpdated: 2026-10-26T16:22:51.846Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/332169255/Grouper+Training+-+MCP+AI+-+Lesson+05+Recipes
---

A recipe tells the AI how your institution wants a task done. Grouper adds a recipe's one line summary to the description of the MCP tools it applies to, so the AI reads it right when it picks that tool. In this lesson you see the AI create a group without a recipe, add a recipe, and see the AI follow it.

Before you start, complete [Grouper Training - MCP AI - Lesson 04: Access levels and scopes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/331677779/Grouper+Training+-+MCP+AI+-+Lesson+04+Access+levels+and+scopes). You should be signed in to MCP with read-write access to the `test` folder.

Stay organized: name this lesson's conversations `Lesson 05: ...` and keep them in a sidebar section named `Lesson 05 Recipes`, as in Lesson 01.

## Step 1: before the recipe

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Create the group test:recipeTest1.
```

The AI creates the group right away, with no description (or with a non-standard description).

## Step 2: add a recipe

As banderson in the UI, go to "Miscellaneous" -> "Configure" -> "Configuration files" -> "Import", choose `grouper.properties`, and import:

```
grouper.mcp.recipe.groupNameCanAdminInUi = etc:sysadmingroup
grouperMcpRecipe.groupDescriptions.name = group-descriptions
grouperMcpRecipe.groupDescriptions.summary = Every new group needs a description: if the user did not give one, ask for it before calling group_save
grouperMcpRecipe.groupDescriptions.body = Do not make up a description.  Ask the user who is in the group and why, and use their words.  A good description says who the members are and what the group is used for.  Ask if there is a ticket number.  Ask if there is a requester to document.
grouperMcpRecipe.groupDescriptions.groupNameCanUse = etc:mcp:mcpUsersReadwrite
grouperMcpRecipe.groupDescriptions.toolNames = group_save
```

- `summary` is the one line the AI sees on the `group_save` tool. It says what to do, not just when the rule applies
- `body` is the detail, sent when the AI asks for the recipe or `group_save` fails
- `groupNameCanUse` is who sees the recipe. If it is blank, nobody does. Here it is everyone who can use MCP read-write ([etc:mcp:mcpUsersReadwrite](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpUsersReadwrite)), since they are the ones who create groups
- `groupNameCanAdminInUi` lets sysadmins see the recipes screen (see the extras)

## Step 3: start a new session

The AI reads the tool descriptions when it connects, and Grouper caches recipes for up to 2 minutes. Wait 2 minutes, then start a new conversation in the Claude app.

## Step 4: after the recipe

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Create the group test:recipeTest2.
```

This time the AI should ask you for a description before it creates the group. Give it one, and check [test:recipeTest2](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=test:recipeTest2) in the UI.

If something does not work, copy and paste the error into Claude in the same conversation, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### Ask which recipes apply

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Which Grouper recipes apply when I create a group?
Show me the full recipe.
```

The AI uses the `recipe` tool and shows `group-descriptions`.

### See it in the UI

Go to "Miscellaneous" -> "MCP recipes". The recipe is listed with the tools it applies to. From here an administrator can add, edit, and delete recipes, and a team named in `groupNameCanEdit` can edit the wording of its own recipes.

### A real world recipe

The instructor shows a recipe from a production Grouper. Look at:

- What problem it solves, and what the AI did wrong before it existed
- Who can use it, and who owns the wording

Think of a task at your institution that AI should do your way, and what its one line summary would say.
