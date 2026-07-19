---
title: "Using AI to edit the Grouper wiki"
space: Grouper
pageId: 63143938
version: 11
lastUpdated: 2026-07-19T22:06:23.211Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/63143938/Using+AI+to+edit+the+Grouper+wiki
---

> For wiki editors -- Grouper developers and community power users -- using an AI assistant to read, edit, and rearrange this wiki. Assumes you already have edit access (see [How to use the Grouper wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541532/How+to+use+the+Grouper+wiki)).

 

## How AI reaches the wiki

 AI assistants such as Claude connect through the Model Context Protocol (MCP). Two paths, **not** interchangeable:

 

- **Read via the Atlassian MCP** -- safe (search, open, summarize).
- **Edit via the Confluence REST API** with an OAuth2 / API token. **Never edit via MCP:** its write path drops *include* macros -- including the *Navigation* include at the top of every page -- and corrupts the storage format. The REST API preserves it.

 The assistant authenticates as you and has no access you lack.

 

## You need an AI skill

 An assistant will not get the storage format, macros, and conventions right on its own. Give it a reusable **skill** (or your client's equivalent) that tells it how to edit Grouper pages: read via MCP, write only via REST API + token, keep the *Navigation* include first. Point it at the [Grouper style guide](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide) -- a living document, so have it read the current version each time, not a pasted snapshot.

 

### Example skill

 The canonical, maintained copy lives in the `Internet2/grouper` repo at [grouper-misc/grouper-docs/skills](https://github.com/Internet2/grouper/tree/GROUPER_7_BRANCH/grouper-misc/grouper-docs/skills), so the community can share and improve it in git. Pull it, drop the `grouper-wiki-edit` folder into your client's skills directory (for Claude, `~/.claude/skills/`), and contribute changes back via pull request.

 The block below is a snapshot and may lag behind git -- treat the repository version as authoritative:

 
```markdown
---
name: grouper-wiki-edit
description: Edit pages on grouper.atlassian.net Confluence via the REST API with
  an API token. Read via MCP; NEVER write via MCP (it strips include macros and
  corrupts the storage format).
---

# Edit the Grouper wiki (grouper.atlassian.net)

## Golden rules
- READ via the Atlassian MCP (search / get page) -- safe.
- WRITE only via the Confluence Cloud REST API with an API token. NEVER use an
  MCP write/update tool -- it drops include macros (e.g. the Navigation include)
  and corrupts the storage format.
- Every page must start with the standard Navigation include as its first node.
- Edit ONE page at a time. No bulk edits without explicit, recent team approval.
- Be complete but concise. Cover the information fully, but do not pad -- AI
  tends to over-write. Prefer tight prose and lists over long paragraphs.
- Diagrams: Confluence Cloud does not render SVG inline. Author/edit the diagram
  as SVG, convert to PNG or JPG, embed the raster, and attach BOTH the .svg
  source and the .png/.jpg (matching names) so a future AI can re-edit the SVG.
  Or use the draw.io / diagrams.net app -- and if you do, also attach its
  .drawio/.xml source so a future AI can re-edit it.
- Follow the Grouper style guide, and read it fresh each time (it changes):
  https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide

## Credentials
Basic auth = Atlassian email + API token. Keep them in a file outside git,
chmod 600, never echoed:

  # ~/.secrets/grouper_confluence.env
  ATLASSIAN_EMAIL=you@example.com
  ATLASSIAN_API_TOKEN=xxxxxxxxxxxxxxxx

Load before a curl, then auth with -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN":
  set -a; . ~/.secrets/grouper_confluence.env; set +a
REST base: https://grouper.atlassian.net/wiki/rest/api

## Navigation include (must be the first node of every page)
<p><ac:structured-macro ac:name="include" ac:schema-version="1"><ac:parameter ac:name=""><ac:link><ri:page ri:content-title="Navigation" /></ac:link></ac:parameter></ac:structured-macro></p>

## Edit an existing page
1. GET the current storage body and version:
   curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
     ".../rest/api/content/<PAGEID>?expand=body.storage,version,space"
2. Edit body.storage.value as text. Change only what you need; preserve every
   other macro/tag byte-for-byte; keep the Navigation include first. Write the
   new body to a file.
3. PUT with version = current + 1 (use jq so the XHTML body is escaped):
   jq -n --arg t "<TITLE>" --arg b "$(cat new_body.xhtml)" --argjson v <NEWVER> \
     '{type:"page",title:$t,space:{key:"Grouper"},version:{number:$v},
       body:{storage:{value:$b,representation:"storage"}}}' > payload.json
   curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" -X PUT \
     -H "Content-Type: application/json" --data @payload.json \
     ".../rest/api/content/<PAGEID>"
4. Re-GET and confirm the version incremented and the Navigation include is first.

## Create a new page
POST to .../rest/api/content with ancestors:[{id:"<PARENTID>"}], no version, and
the Navigation include as the first node of the body. Confirm the parent with a
human first.
```

 

## Get the API token

 Editing uses your Atlassian email + an **API token** (basic auth). Create one at [id.atlassian.com/manage-profile/security/api-tokens](https://id.atlassian.com/manage-profile/security/api-tokens) (Create API token -- copy the value, shown once). Store both in a file your skill reads, `chmod 600`, out of git:

 
```bash
# ~/.secrets/grouper_confluence.env  (never commit)
ATLASSIAN_EMAIL=you@example.com
ATLASSIAN_API_TOKEN=xxxxxxxxxxxxxxxxxxxx
```

 The token is a password: never paste it into a page, prompt, or chat; revoke it from the same screen if it leaks.

 

## Connect

 

1. Use an MCP-capable client (Claude desktop or web app, Claude Code).
2. Add the Atlassian MCP connector: `https://mcp.atlassian.com/v1/mcp`
3. Authenticate via OAuth; pick the `grouper.atlassian.net` site.
4. Set up the API token (above) for writes.
5. Open a page to confirm the connection before changing anything.

 

## What you can do

 

- **Read / search:** find pages by title, label, text, or CQL; summarize; list `needsDocUpdate` pages.
- **Edit:** fix typos, links, and stale version numbers; rewrite or restructure a section.
- **Rearrange:** re-parent or reorder pages; split or merge.

 

## One page at a time

 > Edit **one page at a time.** No bulk edits without explicit, recent Grouper-team approval -- they are hard to review, easy to get wrong at scale, and disruptive to revert.

 

## Working safely

 

- **Review before saving.** Have the assistant show the change first; version history lets you revert.
- **Keep every macro/include.** The *Navigation* include stays first; preserve all others exactly.
- **Small edits** over wholesale rewrites -- easier to review and revert.
- **Complete but not wordy.** Cover the topic fully, but AI over-writes; keep full information, minimal words.
- **Confirm structure changes.** Moves and deletes affect others' links -- clear the plan first.
- **Flag, don't guess.** If uncertain, add the `needsDocUpdate` label plus a comment rather than inventing content.

 

## Diagrams

 Confluence Cloud does **not** render SVG inline (it sanitizes SVG and serves it as a download). So:

 

- Author/edit as SVG, convert to PNG or JPG, and embed the raster.
- Attach **both** the `.svg` source and the `.png`/`.jpg` (matching names, e.g. `flow.svg` + `flow.png`) so a future AI can re-edit the source.
- Or use **draw.io** (diagrams.net), which renders natively -- and if you do, also attach its `.drawio`/`.xml` source, since an AI cannot read the app's internal storage.

 

## Example prompts

 
```text
Update the supported-version list on "Grouper container documentation" to add
v5. Show me the change before saving, and keep the Navigation include at the top.

List pages in the Grouper space labeled needsDocUpdate, with the comment that
says what needs updating.

Move page "X" to be a child of "How to use the Grouper wiki"; confirm the move.

Draft a new child page titled "Y" from these notes: ... Don't publish until I
have read it.
```

 

## Limits

 

- The assistant sees only what you can see.
- It edits as you -- your name is on the version history.
- Wiki only; Jira automation is not covered here yet.
