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
  ATLASSIAN_EMAIL=you@example.edu
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
