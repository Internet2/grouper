---
name: grouper-wiki-edit
description: >-
  Edit (or create) pages on the Internet2 Grouper wiki -- docs.grouper.internet2.edu /
  grouper.atlassian.net Confluence -- via the Confluence Cloud REST API with an API
  token. Read via MCP; NEVER write via MCP (it strips include macros and corrupts the
  storage format). Use whenever the user asks to edit, update, fix, patch, or create a
  Grouper wiki page, e.g. "fix the broken image on the v5 upgrade page", "update the
  Grouper wiki page", "add a section to the Duo provisioning page", "remove the
  unrestored-unknown-attachment". Do NOT use for an institution's own Confluence (e.g.
  isc-penn.atlassian.net) or the frozen on-prem Internet2 Confluence.
---

# Edit the Grouper wiki (docs.grouper.internet2.edu / grouper.atlassian.net)

## Golden rules
- READ via the Atlassian MCP (search / get page / fetch) -- safe.
- WRITE only via the Confluence Cloud REST API with an API token. NEVER use an
  MCP write/update/comment tool -- it rewrites the body lossily, drops include
  macros (e.g. the Navigation include), and corrupts the storage format. This is
  not a preference: an MCP write silently damages the page.
- Every page must start with the standard Navigation include as its first node.
  On edits, never drop it; if the incoming body lacks it, add it back before the PUT.
- Edit ONE page at a time. No bulk edits without explicit, recent team approval.
- Do the smallest edit that does the job; leave every other tag/macro byte-for-byte.
  Preserve the page title unless asked to change it. Confirm with a human before a
  large or ambiguous edit.
- Be complete but concise. Cover the information fully, but do not pad -- AI
  tends to over-write. Prefer tight prose, tables, and lists over long paragraphs.
- Diagrams: Confluence Cloud does not render SVG inline (it serves it as a
  download). You may author the diagram as SVG locally, but convert it to PNG or
  JPG and attach and embed only the raster. Do not attach the .svg or mention an
  SVG source on the page. Or use the draw.io / diagrams.net app -- and if you do,
  also attach its .drawio/.xml source so a future AI can re-edit it.
- Follow the Grouper style guide, and read it fresh each time (it changes):
  https://docs.grouper.internet2.edu/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide
- Never echo the API token or the loaded env.

## Credentials
Basic auth = Atlassian email + API token. Keep them in a file outside git,
chmod 600, never echoed:

  # ~/.secrets/grouper_confluence.env
  ATLASSIAN_EMAIL=you@example.edu
  ATLASSIAN_API_TOKEN=xxxxxxxxxxxxxxxx

The same token also works for the GRP Jira (see grouper-jira).

To create a token: https://id.atlassian.com/manage-profile/security/api-tokens ->
Create API token, label it (e.g. grouper-wiki-ai), copy it (shown once). Revoke it
from the same screen if it leaks.

Load before a curl, then auth with -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN":
  set -a; . ~/.secrets/grouper_confluence.env; set +a

## Two hosts -- not interchangeable
- REST calls -> https://grouper.atlassian.net/wiki/rest/api (v1, storage format).
  The custom domain docs.grouper.internet2.edu silently drops the Authorization
  header on REST calls, so requests run as Anonymous: reads of the public Grouper
  space look fine, but non-public spaces (e.g. GrIntDev) 404 and every write fails.
- Links for people -> https://docs.grouper.internet2.edu/wiki + _links.webui, e.g.
  https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/<ID>/<Title>
  _links.webui starts at /spaces/... and does NOT include /wiki -- a
  docs.grouper.internet2.edu/spaces/... URL is a dead link. For a section link,
  append the heading anchor, words joined by hyphens: #Sync-back-auth-methods
- The Jira custom domain is a different host (todos.grouper.internet2.edu); Jira
  REST also authenticates on grouper.atlassian.net.

## Navigation include (must be the first node of every page)
<p><ac:structured-macro ac:name="include" ac:schema-version="1"><ac:parameter ac:name=""><ac:link><ri:page ri:space-key="Grouper" ri:content-title="Navigation" /></ac:link></ac:parameter></ac:structured-macro></p>

Keep ri:space-key="Grouper": the Navigation page lives in the Grouper space, and
without it a page in another space (e.g. GrIntDev) resolves the include against its
own space and breaks. Dev-notes pages also add a TOC right after it:
<p><ac:structured-macro ac:name="toc" ac:schema-version="1" /></p>
Omit ac:macro-id and ri:version-at-save on new macros -- Confluence assigns them.

## Edit an existing page
Updates are optimistic-locked: PUT the whole body back with version = current + 1.
Put temp files (body, payload) in a scratch dir, not a repo.

1. GET the current storage body and version:
   curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
     "https://grouper.atlassian.net/wiki/rest/api/content/<PAGEID>?expand=body.storage,version,space"
   Pull version.number, title, space.key, body.storage.value.
2. Edit body.storage.value as text (XHTML with <ac:structured-macro>, <ri:page>,
   ... tags). Change only what you need; keep the Navigation include first. Write
   the new body to a file.
3. PUT with version = current + 1. Build the JSON with jq so the XHTML is escaped
   (never hand-concatenate it):
   jq -n --arg t "<TITLE>" --rawfile b new_body.xhtml --argjson v <NEWVER> \
     '{type:"page",title:$t,space:{key:"<SPACEKEY>"},version:{number:$v},
       body:{storage:{value:$b,representation:"storage"}}}' > payload.json
   curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" -X PUT \
     -H "Content-Type: application/json" --data @payload.json \
     "https://grouper.atlassian.net/wiki/rest/api/content/<PAGEID>"
4. Verify: the response (or a re-GET) shows the version incremented, the change
   landed, and the Navigation include is still first. Give the person the
   docs.grouper.internet2.edu/wiki/... link (see "Two hosts").

## Create a new page
POST to https://grouper.atlassian.net/wiki/rest/api/content with
ancestors:[{id:"<PARENTID>"}], no version, and the Navigation include as the first
node of the body. Confirm the parent with a human first. Re-GET afterwards and
confirm the body begins with the include.
