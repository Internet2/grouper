# AI instructions: mirror the GRP Jira project to Markdown in git

**Goal:** keep a Markdown copy of the Grouper Jira project (GRP on
grouper.atlassian.net) in this repo under `issueMirror/spaces/Grouper/`, one
Markdown file per issue. Used for (a) AI RAG / question answering over Grouper
issue history, (b) change tracking in git, and (c) a permanent record that
outlives any Jira instance.

**When to run:** at each Grouper release (it is incremental, so re-running is
cheap), or any time you want the new issues captured.

This is the issue-side companion of `../wikiMirror/convertToMarkdownAiInstructions.md`.
Read this file fully before starting.

---

## 1. What to produce

- `issueMirror/spaces/Grouper/GRP-<n>.md` -- one file per GRP issue.
- Each file = YAML frontmatter + title + description + comments + attachment
  list. No attachment binaries, no images.
- Flat directory (issues have no hierarchy).

## 2. Source and authentication

- Jira Cloud site: `https://grouper.atlassian.net`
- REST API base: `https://grouper.atlassian.net/rest/api/2`
  **Use v2, not v3.** v2 returns issue descriptions and comment bodies as wiki
  markup *text*; v3 returns ADF (a JSON document tree) that would have to be
  re-serialized and would not match the existing mirror.
- Auth: HTTP Basic with an Atlassian account **email + API token**
  (create at https://id.atlassian.com/manage-profile/security/api-tokens).
  **Never commit the token.**
  - (Claude Code in this environment: creds are in
    `~/.secrets/grouper_confluence.env` as `ATLASSIAN_EMAIL` and
    `ATLASSIAN_API_TOKEN` -- the same token reaches Jira and Confluence.)
- **Listing issues uses `/rest/api/2/search/jql`** with `nextPageToken`
  pagination. The old `/rest/api/2/search` with `startAt` was removed by
  Atlassian and now returns **HTTP 410 Gone**.
- Comments and attachments are **not** returned by search -- fetch each changed
  issue individually with `GET /rest/api/2/issue/<KEY>?fields=...`.

## 3. History: where the existing files came from

The mirror was first rendered from a full export of the **frozen on-prem Jira**
(`todos.internet2.edu`) by `jira_migration/migrate.py markdown`, covering
`GRP-1 .. GRP-7143`. Issues above that were created natively in the cloud.

The cloud copy of the migrated issues was created by a CSV import, then patched
over REST (descriptions, comment bodies, reporters/assignees). Two artifacts of
that import matter here:

1. **Migration boilerplate in the text.** The CSV could not set the real
   reporter/assignee/comment author, so they were stamped into the body: a
   `---- / Original reporter: X / Original assignee: Y / ----` header on the
   description, and an `_[originally by X]_` first line on each comment. The
   mirror strips both back off and uses them to populate the frontmatter, so a
   re-render stays faithful to the on-prem original.

2. **Misaligned CSV fields -- the reason migrated issues are not re-rendered.**
   The gap-filled import CSV drifted by one to three rows, so **1181 cloud
   issues in `GRP-1001 .. GRP-6895` carry another issue's summary** (cloud
   GRP-1009 has on-prem GRP-1006's summary, and so on); `created`,
   `fixVersions` and friends drifted with it. The later REST pass rewrote
   *descriptions* by key, so descriptions are correct while the CSV-carried
   fields are not. Consequence: **for `GRP-1 .. GRP-7143` the existing
   on-prem-derived Markdown is the accurate record and the cloud is not.**
   `jiraToMarkdown.py` therefore skips those keys unless you pass
   `--include-migrated`. Do not pass it until the cloud summaries have been
   repaired from `jira_migration/export/GRP-*.json`.

## 4. Incremental refresh (only changed issues)

- Store each issue's `updated` timestamp in its file's frontmatter.
- On refresh: list all issues with just `updated` (cheap -- no bodies), and
  re-fetch only issues whose file is missing or whose `updated` changed.
- **Ignore the migration bulk-touch.** The 2026-07-18 import restamped
  `updated` on every migrated issue, so an `updated` on or before 2026-07-18 is
  noise, not a real edit (`MIGRATION_TOUCH` in the script).
- Issues are never deleted from the mirror -- a deleted Jira issue is still
  history worth keeping.

## 5. Rendering rules (must match the original render)

Frontmatter, in this order:

```yaml
---
key: GRP-7245
cloud_key:                 # always blank: cloud keys are identical to on-prem
onprem_url:                # blank for cloud-native issues; todos URL for migrated
type: Documentation
status: Resolved
resolution: Unresolved
priority: Medium
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2026-08-13T12:59:12.594-0400
updated: 2026-08-13T12:59:27.377-0400
resolved:
components: []
fixVersions: []
labels: []
links: []
---
```

Body:

- `# <KEY>  <summary>` (two spaces), then the description, or
  `_(no description)_` when empty.
- `## Comments`, then per comment `### <author> - <created>` and the body.
- `## Attachments`, then `- <filename> (<size> bytes) - by <author> on <created>`.
  **Names only -- never download the binaries into this repo.**

### Email addresses (sanitize)

Replace every real email address with `<localpart>@example.com` -- keep the
local part, swap the domain. This repo is public. Do not touch OData
annotations (`foo@odata.bind`) or JDBC hosts (`jdbc:oracle:thin:@host`), which
have no local part. `jiraToMarkdown.py` does this in `redact_emails()` as the
last step, identical to the wiki mirror.

## 6. Do NOT

- Do **not** pre-chunk. One file per issue; the RAG pipeline chunks at
  ingestion time.
- Do **not** download attachment binaries or inline images.
- Do **not** commit the API token.
- Do **not** re-render `GRP-1 .. GRP-7143` from the cloud (section 3.2).

## 7. Running it

```
export ATLASSIAN_EMAIL='you@example.com'
export ATLASSIAN_API_TOKEN='...'
python3 jiraToMarkdown.py --dry-run     # see what would change
python3 jiraToMarkdown.py               # incremental refresh
python3 jiraToMarkdown.py --only GRP-7245,GRP-7250
```

Standard library only -- no `requests`, no `beautifulsoup4`.

## 8. After running

Review `git diff issueMirror/`. Commit with a message prefixed **`issues: sync`**
(a human reviews / commits / pushes -- do not auto-push).

## 9. Known follow-up: repair the cloud summaries

The 1181 misaligned cloud summaries (section 3.2) are fixable: for each key,
`PUT /rest/api/2/issue/<KEY>` with the `summary` from
`jira_migration/export/<KEY>.json`, which is the authoritative on-prem dump.
`created` cannot be corrected over REST; `fixVersions` can. This is a
production write to the community Jira, so it needs an explicit go-ahead
before anyone runs it. Once done, `--include-migrated` becomes safe and the
whole mirror can be re-rendered from a single source.
