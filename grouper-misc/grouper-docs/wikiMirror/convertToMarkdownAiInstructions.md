# AI instructions: mirror the Grouper Confluence wiki to Markdown in git

**Goal:** keep a Markdown copy of the Grouper Confluence wiki (grouper.atlassian.net)
in this repo under `misc/wiki/`, one Markdown file per page. Used for (a) AI RAG /
question answering over the public Grouper docs, and (b) change tracking of both
spaces in git.

**When to run:** periodically -- e.g. at each Grouper release, or whenever the
wiki has changed materially. It is incremental, so re-running is cheap.

Any AI (Claude or otherwise) should be able to follow this file and produce a
correct, up-to-date export. Read it fully before starting.

---

## 1. What to produce

- `misc/wiki/Grouper/...`   -- the public "Grouper" space
- `misc/wiki/GrIntDev/...`  -- the "Grouper Internal Development" space
- One `.md` file per **current** page. Directories mirror the page tree
  (a page's file sits under its parent pages' directories).
- Each file = YAML frontmatter + the page's **content** as Markdown.
- **Content only.** No attachments, no images, no navigation menus, no
  table-of-contents, no "children" lists, no breadcrumbs.

Both spaces are public content (GrIntDev is login-gated in Confluence only to keep
it out of the public wiki search -- it is not secret), so both may live in this
public repo. RAG ingestion normally points at `misc/wiki/Grouper/` only; GrIntDev
is kept for change tracking.

---

## 2. Source and authentication

- Confluence Cloud site: `https://grouper.atlassian.net/wiki`
- REST API base (v1, storage format): `https://grouper.atlassian.net/wiki/rest/api`
- Auth: HTTP Basic with an Atlassian account **email + API token**
  (create at https://id.atlassian.com/manage-profile/security/api-tokens).
  The same token reaches both spaces. **Never commit the token.**
  - (Claude Code in this environment: creds are in
    `~/.claude/secrets/grouper_atlassian_net_confluence.env` as `ATLASSIAN_EMAIL`
    and `ATLASSIAN_API_TOKEN`.)

## 3. Get a GOOD update from Atlassian (fetch correctly)

- Fetch the **`body.storage`** format (Confluence storage XHTML). It is the
  canonical source and preserves structure/macros.
  **Do NOT scrape rendered HTML** -- rendered image URLs are short-lived Media-API
  tokens that expire, and rendered HTML includes menus/chrome you do not want.
- List pages per space, paginated:
  `GET /rest/api/content?spaceKey=<KEY>&type=page&status=current&limit=50&start=<N>`
  with `expand=body.storage,version,ancestors,history.lastUpdated,_links`.
- The stable page URL is **pageId-based**:
  `https://grouper.atlassian.net/wiki/spaces/<SPACEKEY>/pages/<pageId>/<Title+Slug>`
  Resolution is by **pageId only** -- the title slug and even the space key in the
  path are cosmetic, so links never break on rename or move. Use each page's
  `_links.webui` for the exact current slug.

## 4. Incremental refresh (only changed pages)

- Store each page's `version` (integer) in its file's frontmatter.
- On refresh: list all pages with their current `version` (cheap -- no bodies), and
  only re-fetch + rewrite pages whose `version` increased.
- Delete `.md` files for pages that no longer exist (status not current / removed).
- Git `diff`/`status` then shows exactly what changed.

---

## 5. Conversion rules: Confluence storage XHTML -> Markdown

### KEEP (render as Markdown)
- Headings `<h1>`..`<h6>`, paragraphs, `<strong>`/`<em>`, inline `<code>`.
- Lists (`<ul>`/`<ol>`/`<li>`), nested lists.
- Tables (`<table>`/`<tr>`/`<th>`/`<td>`) as GitHub Markdown tables.
- Code blocks: `<ac:structured-macro ac:name="code">` and `"noformat"` ->
  fenced ``` blocks (use the `language` parameter if present).
- Callouts: `<ac:structured-macro ac:name="info|note|warning|tip|panel">` ->
  Markdown blockquote (`> ...`).
- `expand` macro -> just emit its body content.
- `<ac:layout*>` wrappers -> unwrap (emit children).

### STRIP ENTIRELY (navigation / dynamic / non-content)
- `<ac:structured-macro>` whose `ac:name` is one of:
  `include` (this is the top nav bar), `toc`, `children`, `anchor`,
  `recently-updated`, `recently-updated-dashboard`, `livesearch`, `search-box`,
  `contentbylabel`, `gliffy`, `view-file`, `align`, `unmigrated-wiki-markup`,
  `unmigrated-inline-wiki-markup`, and other AUI/`aui*` widgets.
- `<ac:image>` and standalone `<ri:attachment>` -> remove (no images/attachments).
- Do not emit the page's top navigation menu, table of contents, or child-page
  list. Content only.

### LINKS
- Internal link `<ac:link><ri:page ri:content-title="X" [ri:space-key="Y"] /> ...`:
  resolve `X` (in space `Y`, else the source page's space) to the target page's
  **absolute** URL and emit `[text](url)`, where `url` =
  `https://grouper.atlassian.net/wiki/spaces/<space>/pages/<pageId>/<slug>`.
  Link text = the `<ac:link-body>` / `<ac:plain-text-link-body>` content, else `X`.
  If `X` cannot be resolved to a current page, emit the **text only** (no dead link).
- Attachment/user links (`ri:attachment`, `ri:user`) -> emit the text only, drop link.
- External `<a href="...">text</a>` -> keep as `[text](href)` unchanged.
- Resolving internal links requires a title->pageId map of all current pages in
  both spaces (build it from the page list in step 3).

### Entities / whitespace
- Decode HTML entities (`&nbsp;` -> space, `&amp;` -> `&`, etc.).
- Collapse runs of blank lines to at most one.

### Email addresses (sanitize -- same convention as the GRP jira mirror)
- Replace every real email address with `<localpart>@example.com`: keep the local
  part, swap the domain to `example.com`. This keeps real addresses out of this
  public repo while preserving readable placeholders.
- Do **not** touch non-email `@` tokens: OData annotations (`foo@odata.bind`,
  `@odata.id`, `@odata.context`, `@odata.type`) and JDBC/URL hosts
  (`jdbc:oracle:thin:@host...`, which has no local part) are left as-is.
- `wikiToMarkdown.py` does this in `redact_emails()` as the last step of the
  Markdown finalizer, so every sync stays clean.

---

## 6. Frontmatter (YAML) at the top of every file

```yaml
---
title: Grouper Deployment Guide
space: Grouper
pageId: 28541813
version: 72
lastUpdated: 2026-07-01T14:22:00.000Z   # history.lastUpdated.when
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide
---
```

- `url` uses the full pageId form **with** the slug (human-readable; slug is
  cosmetic so it will not rot). Refresh the slug from `_links.webui` on each sync.

## 7. File layout and naming

- Mirror the ancestor hierarchy:
  `misc/wiki/<SPACE>/<ancestorTitle>/.../<PageTitle>.md`
  (top-level pages sit directly under `misc/wiki/<SPACE>/`).
- **Sanitize every path segment (dir and file):** replace each run of
  non-alphanumeric characters with a single underscore `_`, strip leading/trailing
  underscores, and **preserve case**. (e.g. `Grouper - Loader GSH` ->
  `Grouper_Loader_GSH`; `v2.6 (renamed to v4)` -> `v2_6_renamed_to_v4`.)
- **Collisions must FAIL, not overwrite.** If two different pages sanitize to the
  same file path, stop with an error naming both pages; the fix is to rename one of
  them in the wiki (upstream), then re-run. (A segment that sanitizes to empty also
  fails.)
- **Renames/moves preserve git history.** Track each page by its stable `pageId`
  (from frontmatter). On re-run, if a page's file already exists at a different
  path (title or ancestors changed), `git mv` it to the new path instead of writing
  a new file + deleting the old one. Deleted pages -> `git rm`.

## 8. Do NOT

- Do **not** pre-chunk into fragments. One file per page. Chunking (and the
  "chunk N of M" metadata) is done by the RAG ingestion pipeline at ingestion
  time, not here -- pre-chunking bakes in a strategy and makes git diffs noisy.
- Do **not** include images, attachments, navigation menus, TOC, or breadcrumbs.
- Do **not** commit the API token or any secret.

## 9. After running

- Review `git diff misc/wiki/`. Commit with a message prefixed **`wiki: sync`**
  (a human reviews/commits/pushes -- do not auto-push).
- RAG ingestion reads the frontmatter (`url`, `lastUpdated`) and stamps it onto
  each chunk, so answers can cite the live wiki URL and note staleness.

---

## 10. Reference implementation

A working storage-XHTML -> Markdown converter (Python / BeautifulSoup) that
implements the rules above may be committed alongside this file (e.g.
`wikiToMarkdown.py`). An AI may run/adapt it, or re-derive an equivalent from the
rules in section 5. Verify output on a few representative pages (one text-heavy,
one table-heavy, one with code/callout macros, one with many internal links)
before committing a full sync.

---

## 11. Sitemap for the public docs site (Google indexing)

The same `wikiToMarkdown.py` run also emits **`sitemap.xml`** next to itself (in
`wikiMirror/`). This is the Google sitemap for the public docs custom domain
`https://docs.grouper.internet2.edu`.

- **Public space only.** Only `Grouper` is listed; `GrIntDev` (internal) is
  excluded, so internal pages are never advertised to search engines.
- **URLs use the custom domain** (`docs.grouper.internet2.edu/wiki/...`), not the
  `grouper.atlassian.net` host, even though the pages are fetched from the
  atlassian.net API. Both hostnames are the same Confluence Cloud instance.
- **Idempotent.** URLs are de-duplicated and sorted, so an unchanged wiki
  produces a byte-identical file and a clean `git diff`.
- Regenerate just the sitemap (no markdown mirror) with:
  `python3 wikiToMarkdown.py --sitemap-only`

### Why it is hosted on grouperdemo, not on docs

Confluence Cloud is fully managed by Atlassian -- there is no way to serve a
static `/sitemap.xml` on `docs.grouper.internet2.edu`, its robots.txt is
autogenerated, and it publishes no sitemap. So the file is cross-hosted on
`grouperdemo.internet2.edu` (a plain Apache box Internet2 controls) and submitted
via Google's multi-site sitemap support: Google accepts the `docs...` URLs
because both hosts are verified in the same Search Console account.

### One-time Search Console setup

1. In Search Console, add a **URL-prefix** property for exactly
   `https://grouperdemo.internet2.edu/` (the site root -- NOT the sitemap path,
   and NOT a Domain property).
2. Verify it with the **HTML file** method: drop the `googleXXXX.html` file that
   Google gives you at `/var/www/html/` on **i2midev6** (the box that serves
   grouperdemo.internet2.edu).
3. After the sitemap is uploaded (below), submit `sitemap.xml` under the
   grouperdemo property in the Sitemaps report.
4. Optional: to keep grouperdemo's own content out of Google while still letting
   Google read the sitemap, put this at `/var/www/html/robots.txt` on i2midev6:

   ```
   User-agent: *
   Disallow: /
   Allow: /sitemap.xml

   Sitemap: https://grouperdemo.internet2.edu/sitemap.xml
   ```

### Per release (refresh the sitemap)

1. Regenerate: `python3 wikiToMarkdown.py --sitemap-only` (or a full mirror run,
   which also refreshes it). Review `git diff wikiMirror/sitemap.xml` and commit
   with the wiki sync (do not auto-push).
2. Replace the file on the server that hosts `grouperdemo.internet2.edu` --
   the Internet2 box **i2midev6**, at **`/var/www/html/sitemap.xml`**:
   `scp wikiMirror/sitemap.xml i2midev6:/var/www/html/sitemap.xml`
3. Confirm it serves as XML:
   `curl -sI https://grouperdemo.internet2.edu/sitemap.xml` (expect `200` and
   `content-type: ... xml`).
4. Google re-reads a submitted sitemap on its own schedule; no resubmit needed.
   To nudge a specific important page, use URL Inspection -> Request Indexing on
   the `docs.grouper.internet2.edu` property.
