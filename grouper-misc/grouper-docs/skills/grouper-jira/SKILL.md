---
name: grouper-jira
description: Create, edit, transition, and resolve issues in the GRP (Grouper)
  open-source project on grouper.atlassian.net via the Jira Cloud REST API with an
  API token. Use whenever the user wants a GRP Jira opened/resolved so they can
  commit a Grouper code change, or to comment on / transition a GRP-#### issue. Also
  trigger when the user names the vanity display URL todos.grouper.internet2.edu,
  which is the SAME Cloud instance as grouper.atlassian.net. This is the Internet2
  Grouper project, NOT a local/institution Jira and NOT the frozen on-prem tracker
  at bare todos.internet2.edu (no dot-grouper-dot).
---

# Grouper Jira (GRP project on grouper.atlassian.net)

The Internet2 Grouper issue tracker migrated from on-prem todos.internet2.edu to
Jira Cloud. It is cloud-only now. For a Grouper code commit, the convention is
one GRP issue, resolved, referenced by the commit.

## Two hostnames, ONE Cloud instance (do not be fooled)
The instance answers on two names, and people refer to it by either:
- `grouper.atlassian.net`  -- the API base (`baseUrl`). USE THIS FOR REST.
- `todos.grouper.internet2.edu` -- a vanity display URL (`displayUrl`) for the
  SAME instance. This is what users see in links and what they will often quote
  at you.

A `todos.grouper.internet2.edu` URL therefore does NOT mean the project moved
back to Jira Server/Data Center. It is still Cloud: `/rest/api/3`, ADF
descriptions, and the transition ids below all apply. Confirmed via
`GET /rest/api/3/serverInfo`, which returns `deploymentType: Cloud`,
`baseUrl: https://grouper.atlassian.net`,
`displayUrl: https://todos.grouper.internet2.edu` (checked Thu Aug 20 2026).

If ever in doubt about which flavor you are talking to, fetch `serverInfo`
first -- it is unauthenticated and settles it in one call. Server/DC would
report a non-Cloud `deploymentType` and would need `/rest/api/2` plus wiki
markup instead of ADF.

Note the OLD on-prem tracker at bare `todos.internet2.edu` (no `.grouper.`) is
a different, frozen thing -- do NOT touch that one. Mind the hostname.

Related: the Confluence wiki has the same arrangement -- `displayUrlConfluence`
is `docs.grouper.internet2.edu`. See the grouper-confluence-edit skill.

## Golden rules
- WRITE (create / transition / comment) via the Jira Cloud REST API with an API
  token, not an MCP server (an institution's Grouper MCP OAuth token often reaches
  only that institution's own Atlassian site, not grouper.atlassian.net). Reading
  is fine via REST too; keep it all on REST for consistency.
- Descriptions use ADF (Atlassian Document Format) on REST v3, not markdown or
  wiki markup. Build a `{type:"doc",version:1,content:[...paragraphs...]}` object.
  Never wrap the whole description in a code fence.
- Prefer ASCII in summaries/descriptions. Avoid literal double quotes inside JSON
  text where you can (say "double-quoted" in words) to dodge escaping bugs.
- Commit message tied to a GRP issue: ONE line only, `GRP-####: summary`. No body.
  Propose it; let the human commit.

## Credentials
Basic auth = Atlassian email + API token. This is the same cross-site token used
by the grouper-wiki-edit skill (it covers both Grouper Jira and Confluence). Keep
it in a file outside git, chmod 600, never echoed:

  # ~/.secrets/grouper_confluence.env
  ATLASSIAN_EMAIL=you@example.edu
  ATLASSIAN_API_TOKEN=xxxxxxxxxxxxxxxx

Load, then Basic-auth every curl:
  set -a; . ~/.secrets/grouper_confluence.env; set +a
  curl -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" ...

REST base: https://grouper.atlassian.net/rest/api/3

## Project facts (GRP)
- Project key: GRP   projectId: 10100
- Issue type ids:
    10044 Improvement
    10045 Task
    10046 Sub-task
    10047 New Feature
    10048 Bug
    10000 Epic
    10119 Documentation
- Transition ids (GRP workflow):
    11 To Do
    21 In Progress
    31 In Review
    41 Done
    51 Open
    61 Reopened
    71 Resolved
    81 Closed
- Re-fetch these if anything looks off (workflows/ids can change):
    GET /rest/api/3/project/GRP                 -> id, issueTypes
    GET /rest/api/3/issue/GRP-####/transitions  -> current transition ids

## Gotcha: resolution field is NOT on the transition screen
Transitioning to Resolved (71) or Done (41) with a `fields.resolution` in the body
returns HTTP 400. Send the transition WITHOUT a resolution:
  {"transition":{"id":"71"}}
Resolution stays null/None; that matches how the migrated GRP issues look. This is
expected, not an error.

## Create an issue
1. Build payload (Improvement shown; pick the issue type that fits -- a docs/UX
   clarification is usually Improvement or Documentation, a broken behavior is Bug):
   {
     "fields": {
       "project": { "key": "GRP" },
       "issuetype": { "id": "10044" },
       "summary": "<one line>",
       "description": { "type":"doc","version":1,"content":[
         {"type":"paragraph","content":[{"type":"text","text":"<para 1>"}]},
         {"type":"paragraph","content":[{"type":"text","text":"<para 2>"}]}
       ]}
     }
   }
2. POST it:
   curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
     -H "Content-Type: application/json" -X POST --data @create.json \
     "https://grouper.atlassian.net/rest/api/3/issue"
   -> returns {"key":"GRP-####", ...}

## Resolve an issue
  curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
    -H "Content-Type: application/json" -X POST \
    --data '{"transition":{"id":"71"}}' \
    "https://grouper.atlassian.net/rest/api/3/issue/GRP-####/transitions"
Verify:
  curl -s -u "$ATLASSIAN_EMAIL:$ATLASSIAN_API_TOKEN" \
    "https://grouper.atlassian.net/rest/api/3/issue/GRP-####?fields=status,resolution"

## Add a comment (ADF body)
  POST /rest/api/3/issue/GRP-####/comment
  {"body":{"type":"doc","version":1,"content":[
    {"type":"paragraph","content":[{"type":"text","text":"<comment>"}]}]}}
