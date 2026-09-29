---
title: "Grouper MCP Recipes"
space: Grouper
pageId: 188579841
version: 3
lastUpdated: 2026-09-28T07:37:16.615Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/188579841/Grouper+MCP+Recipes
---

A recipe tells an AI client how you want a task done here — which tool to use, in what order, and what to avoid. This page is the deployer's reference: every setting, who can change what, and how a recipe quietly rewrites the description of other tools.

## The problem it solves

An MCP client picks a tool by reading tool descriptions. If your institution has a local rule — use this GSH template, never that raw call; ask the owning team first — the client has no way to know. Put the rule in the general instructions and it competes with everything else in the conversation, and loses.

A recipe puts the rule where the client is actually looking: in the description of the tool it is about to call.

## How a client actually receives one

This is the part that surprises people. A recipe reaches a client through **three** different channels, each carrying a different amount of it.

**1. The recipe tool's own description.** Grouper advertises a tool called `recipe`, and lists every recipe you can see — name and summary — inside its description. This is free: the client reads it whether or not it ever calls the tool.

**2. Injected into other tools' descriptions.** Every tool named in a recipe's `toolNames` gets a one-line pointer to that recipe *appended to its own description*. So a client choosing `group_save` reads your rule at the moment it chooses, without fetching anything. Only people who can see the recipe see the pointer — the same tool has a different description for different users.

**3. The body, on demand.** The full text is sent only when a client asks for the recipe by name with `get`, or when one of its `toolNames` tools *fails*. A long body therefore costs nothing until something actually needs it.

That layering is why the summary matters more than the body. The summary is sent to every client that can see the recipe, on every connection. The body is sent almost never.

## Turning it on

| Property | Scope | File |
| --- | --- | --- |
| `grouper.is.mcp` | Global | `grouper.hibernate.properties` |

Note the file — this one lives in `grouper.hibernate.properties`, not `grouper.properties`, which catches people out. With MCP off, the recipes screen is hidden entirely.

Recipes are ordinary Grouper configuration, so they can ship as built-ins, be overridden by setting the same keys, and be turned off with `enabled = false` without losing them. Edit them in the UI under **Miscellaneous → MCP recipes** rather than by hand.

## Where a recipe comes from

Grouper decides how much of a recipe can be changed from where its keys are set. It shows on the recipes screen, and the server enforces it on both the screens and `update` over MCP.

| **Source** | **Where the keys live** | **What can be changed in Grouper** |
| --- | --- | --- |
| Built-in | `grouper.base.properties`, shipped with Grouper | Only `enabled` and `groupNameCanUse`, so you can turn it on for your audience. The wording and tools stay Grouper's, so upgrades keep improving it. Cannot be deleted. |
| Config file | Any other properties file, e.g. your `grouper.properties` | Nothing: it is read only in Grouper and managed in the file, so the file and Grouper never disagree. Cannot be deleted in Grouper. |
| Database | Created on the recipes screen (database config) | Everything, subject to the permissions below. |

## Who can do what

Two separate admin groups, and they are not interchangeable. Everything below is stated in terms of these four, because "admin" on its own is ambiguous here.

| Setting | Scope | Grants |
| --- | --- | --- |
| `grouper.mcp.recipe.groupNameCanAdminInUi` | Global | Administers recipes through the UI screens: add, edit every field, delete. Grants **nothing** over MCP — a member is an ordinary MCP user there. |
| `grouper.mcp.recipe.groupNameCanAdminInMcp` | Global | Reads and updates any recipe over MCP, including the fields that decide who it reaches. Also confers UI administration — this one is the superset. Requires MCP readwrite on top. |
| `groupNameCanUse` | Per recipe | Who can see and read the recipe. There is no global switch for this. |
| `groupNameCanEdit` | Per recipe | Who owns the wording. Members need not administer anything. |

The UI/MCP split is the point: you can let someone curate recipes at a keyboard without their AI client gaining any new reach.

> **No root fallback.** Being a Grouper sysadmin, wheel, or root grants none of this. The checks are pure group membership. Until one of the admin properties names a group, nobody can administer recipes and the UI link is hidden from everyone — including `GrouperSystem`. It is a guardrail against accidents, not a security boundary: anyone who can edit Grouper config can grant themselves the group.

### In the UI

| Action | UI-admin group | MCP-admin group | Recipe's edit group |
| --- | --- | --- | --- |
| See the recipes screen | yes | yes | yes |
| See *every* recipe listed | yes | yes | only its own |
| Create | yes | yes | no |
| Edit name, summary, body | yes | yes | yes |
| Edit enabled, groups, toolNames, priority | yes | yes | read only |
| Delete | yes | yes | no |

There is one edit screen, not two. A content owner opens the same form an administrator does and finds the fields they may not change rendered read-only — and the server ignores them on submit rather than trusting the posted form. `allowEditInMcp` is irrelevant in the UI; an edit-group member can always fix wording here.

### Over MCP

Three actions exist: `list`, `get`, `update`. There is deliberately **no create and no delete over MCP, for anyone**.

| Action | Who | `allowEditInMcp` required? |
| --- | --- | --- |
| `list` / `get` | MCP-admin group with readwrite sees everything; everyone else sees only recipes whose use group includes them | no |
| `update` — any field (except enabled) | MCP-admin group, plus MCP readwrite | **no — admins bypass the switch** |
| `update` — name, summary, body only | the recipe's edit group, plus MCP readwrite | **yes** |
| create / delete | nobody | — |

A delegated editor who sends a control field gets an explicit refusal naming that field — refused rather than silently dropped, so a client can't believe it changed something it didn't. The `update` action is only advertised at all when you can actually use it.

| Property | Scope | Default |
| --- | --- | --- |
| `grouper.mcp.recipe.allowEditInMcp` | Global | `false` |

Whether the per-recipe `groupNameCanEdit` delegation works over MCP at all. Off by default, so a deployer opens that path once rather than blanking `groupNameCanEdit` on every recipe to close it. Turning it off does not strand anyone — the UI screen still works.

> **It is not a master off-switch.** The check is `if (!canAdmin && !isAllowEditInMcp())`, so members of the MCP-admin group skip it entirely: with `allowEditInMcp` set to `false` they can still rewrite any recipe over MCP, including `groupNameCanUse` and `toolNames`. To stop all editing over MCP you have to empty `grouper.mcp.recipe.groupNameCanAdminInMcp` as well.

## Per-recipe settings

Every key is prefixed `grouperMcpRecipe.<configId>.` — the config id is your handle for the configuration, and is not what the client sees.

| Key | Required | Notes |
| --- | --- | --- |
| `name` | yes | What the client asks for the recipe by, e.g. `payroll-groups`. Must be unique across recipes — a duplicate would make one of them unreachable. Use words a client would search for, not a title. |
| `summary` | yes | One line stating the rule. The most valuable string in the whole feature — see below. |
| `body` | yes | The detail: what order to do things in, what must be true first, edge cases, what to avoid. Don't repeat the summary. Sent only on `get` or on a tool failure, so there is room to be specific. |
| `groupNameCanUse` | yes | Fails closed. Blank, or naming a group that can't be found, means nobody sees the recipe. Resolved in a root session, so it can name a group the recipe's audience cannot themselves view. |
| `groupNameCanEdit` | no | Blank means nobody owns the wording. Members of this group can change the name, summary and body — nothing else. Who the recipe reaches, which tools it applies to, whether it's enabled and its priority all stay with recipe administrators. |
| `toolNames` | no | Comma-separated MCP tool names, e.g. `group_save, group_delete`. Each gets the one-line pointer added to its description. A name that doesn't match a real tool is reported on the recipes screen rather than failing silently. |
| `enabled` | no (default `true`) | Turn a recipe off without deleting it. A disabled recipe is shown to no client — but stays editable, so you can take one down and rewrite it. |
| `priority` | no (default `100`) | Lower comes first. Decides which recipes get their full summary rather than just their name when there are more than fit, and the order of pointers on a tool that several recipes apply to. The default sits mid-range so you can move one either way without renumbering. Ties break by name, so the same config always produces the same output. |
| `lastEditedBy`, `lastEditedByName`, `lastEditedOn` | read only | Written by Grouper on every edit through the screens or over MCP, and sent to the client alongside the body so a recipe arrives as local advice with a name on it. `lastEditedBy` is a packed subject string, `sourceId::::subjectId`. Blank means the recipe has only ever been edited in a properties file or the database — so read it as who last edited it through Grouper, not as a claim about who wrote the text. |

**Plain text only, and length limits (7.6.0).** Recipe fields cannot be expression language: the recipes screen has no expression language checkbox, and a field that arrives as expression language (e.g. a `.elConfig` key) is refused on save. Name, summary and body have maximum lengths, the same on the screens and over MCP:

| **Field** | **Maximum characters** |
| --- | --- |
| `name` | 100 |
| `summary` | 300 |
| `body` | 20000 |

## Writing a summary that works

Say what to do, not when the rule applies.

|  | Summary |
| --- | --- |
| **Good** | Use the payrollGroupSave template for groups under app:payroll, not group_save |
| **Weaker** | Applies to groups under app:payroll |
| **Worst** | Payroll group operations |

The first lets the client act immediately. The second only says that something applies, so it must open the full recipe first — and often it won't bother. The third gives it nothing to act on.

Keep it to one line. Every client that can see the recipe receives this text on every connection.

## Limits on what a client receives

These keep recipes from crowding out everything else in a client's context. Recipes past a cap are chosen by `priority`.

| **Property** | **Default** | **Notes** |
| --- | --- | --- |
| `grouper.mcp.recipe.maxSummariesInDescription` | `10` | How many recipes get their summary in the recipe tool's description. Recipes past the cap are still listed by name, so a client can ask for one it has seen mentioned. |
| `grouper.mcp.recipe.maxPointersPerTool` | `3` | How many recipe pointers one tool's description gets. Past this the rest are summarised as a count. Also caps how many recipes are sent back when that tool fails. |
| `grouper.mcp.recipe.maxBodyCharsOnError` | `4000` | When a tool fails, recipe bodies about it are sent back with the error. Past this many characters a body is cut off and the client is told to read the rest with the recipe tool. 0 or less means no limit. |

## Caching, and why your change hasn't appeared

Three separate delays sit between an edit and a client seeing it.

| Property | Default | Notes |
| --- | --- | --- |
| `grouper.mcp.recipe.cacheSeconds` | `120` | Recipes and per-subject permission answers are cached this long. A group membership change can take two minutes to affect recipe behaviour. Setting it to 0 or less turns caching off, which is useful while working out why a recipe is or isn't appearing, and wasteful left on. |
| `grouper.mcp.recipe.cacheMaxSize` | `5000` | Per-user entries grow with the number of distinct people on a node, so the cache is bounded by count as well as age. |

The other two delays:

- **Config file reload** — `grouper.config.secondsBetweenUpdateChecks` defaults to **600**. A `grouper.properties` edit can take ten minutes. Database config, which is what the UI writes, doesn't wait on this.
- **The client's tool list** — MCP advertises tools once, at connection. A client connected before you changed a recipe keeps serving its user the old summary, and won't see a newly-permitted action, until it reconnects.

When testing permissions, restart Tomcat rather than waiting. It clears the caches outright and takes less time than reasoning about which one bit you.

## A worked example

Written as properties; the UI writes the same keys to database config.

```
grouperMcpRecipe.payrollGroups.enabled          = true
grouperMcpRecipe.payrollGroups.name             = payroll-groups
grouperMcpRecipe.payrollGroups.summary          = Groups under app:payroll are owned by the \
    payroll team. Ask them before you change membership, and never delete one.
grouperMcpRecipe.payrollGroups.body             = Groups under app:payroll drive pay runs, \
    so a wrong membership has a real world effect...
grouperMcpRecipe.payrollGroups.groupNameCanUse  = app:payroll:etc:payrollStaff
grouperMcpRecipe.payrollGroups.groupNameCanEdit = app:payroll:etc:payrollOwners
grouperMcpRecipe.payrollGroups.toolNames        = group_save, group_delete, group_add_member
grouperMcpRecipe.payrollGroups.priority         = 100
```

The effect: members of `payrollStaff` see the recipe listed by their client, and see a pointer to it appended to the descriptions of `group_save`, `group_delete` and `group_add_member`. Members of `payrollOwners` can additionally rewrite the wording — in the UI always, over MCP only if `allowEditInMcp` is on. Nobody else sees that the recipe exists.

## Things worth knowing before you deploy

- **There is no staging state.** Set a use group and the recipe is live to those clients. To hold one back, use `enabled = false` or leave the use group blank. Both hide it from every client; recipe administrators still see it on the recipes screen, where Enabled shows No.
- **Recipe text reaches AI clients verbatim.** Treat the body as published to everyone in the use group. Don't put anything there you wouldn't want pulled into a model's context.
- **An edit group writes prompts for its audience.** Members of a recipe's `groupNameCanEdit` choose text that is put in front of every AI client in its use group, in tool descriptions and on errors. Pick that group as carefully as you would pick who can change the tools themselves.
- **Denying the recipe tool does not turn recipes off.** With `recipe` in `grouper.mcp.tools.deny`, clients cannot list or get recipes and no pointers are added to tool descriptions, but when a tool named in a recipe fails, the recipe body is still sent back with the error. To stop a recipe, use `enabled = false`.
- **The advertised schema is not the boundary.** A client can attempt actions and fields the tool description never offered it; the server-side checks are what actually refuse them.
- **A recipe can point at nothing.** A tool renamed by an upgrade, or a deleted group, leaves a recipe that looks fine and does nothing. The recipes screen reports these under "Some recipes are not doing anything" — it's the only place they surface.
