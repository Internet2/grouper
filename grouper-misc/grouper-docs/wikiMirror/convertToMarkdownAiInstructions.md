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
