---
name: grouper-release
description: Cut a Grouper container release and record it -- bump the container
  Tomcat/base versions, confirm no DDL/upgrade-task or server.xml-patch surprises
  since the last release, resolve the GRP issues that shipped, tag them with the
  new fixVersion, and add the release row to the "v7 Release Notes" wiki page. Use
  whenever the user wants to do/record a Grouper release, cut a new container
  version (e.g. 7.3.2), update the release-notes table, mark a version
  STABLE/LATEST STABLE, or bump Tomcat/OS/Java in the container for a release.
---

# Grouper release

End-to-end steps to cut and record a Grouper container release. Companion
skills do the heavy lifting for Jira and the wiki -- this one is the checklist
and the release-notes-table contract.

- Jira writes (create version, resolve issues, set fixVersion): use **grouper-jira**.
- Wiki writes (edit the release-notes page): use **grouper-wiki-edit** (REST +
  token, NEVER the MCP write path -- it corrupts storage-format XML).

Work a new release newest-first: the new row goes ABOVE the current top row.

## Inputs to collect first

- New version, e.g. `7.3.2` (Grouper API version == container tag).
- Container base versions for the `Versions` cell: OS (e.g. Rocky 9.8), Tomcat
  (e.g. 9.0.120), Java Corretto (e.g. 17.0.19.10.1).
- The built image `sha256:` digest (from the container build/push pipeline).
- Release date (YYYY/MM/DD).
- Status for the new row (see Status values).

## 1. Container prep (Dockerfile)

The container Dockerfile pins `ARG TOMCAT_VERSION`, `ARG GROUPER_VERSION`, and
the OS/Java base. For a version/security bump:

- **Bump `TOMCAT_VERSION`** to the target (check https://tomcat.apache.org/security-9.html
  for open CVEs against the current pin; pick the lowest release that clears them).
- **server.xml patch still applies.** The container overlays a stock Tomcat
  `conf/server.xml` and patches it at runtime with `server.xml.grouper.patch`; a
  failed patch exits the container. Before bumping, diff stock `conf/server.xml`
  between the old and new Tomcat versions (fetch from the Apache git tags). If
  byte-identical, the patch is safe; if not, regenerate the patch. Confirm with a
  `patch --dry-run` against the new stock file.
- **Tarball is on the mirror.** The build downloads Tomcat from the Internet2
  software mirror (`software.internet2.edu/grouper/downloads/tools/`), not Apache.
  Confirm `apache-tomcat-<version>.tar.gz` is present there (use a ranged GET --
  the mirror may 404 on HEAD but serve 206 on GET).

## 2. Confirm DDL / upgrade tasks since the last release

Determines the `Upgrade tasks` cell (usually `None` for a patch release).

- The DDL version enum is `GrouperDdl.java` (a `V1..Vn` enum). Compare the highest
  `Vn` at the last release tag vs HEAD -- if unchanged and nothing under a `ddl/`
  package or `*upgradeTasks*` changed, there is no schema migration: `Upgrade
  tasks: None`.
- If DDL did change, the cell links the "Grouper upgrade tasks" wiki page with the
  new task numbers and `(DDL)`.

## 3. Resolve the GRP issues that shipped

- List GRP-#### keys from commits since the last release tag
  (`git log <TAG>..HEAD`).
- Any that are still Open / To Do but committed should be resolved (via
  grouper-jira). Container-only issues (e.g. a Tomcat bump with no code commit)
  count too if they ship in this image.

## 4. Create the version and tag fixVersion

- Create the new version in GRP if it does not exist (grouper-jira:
  `POST /rest/api/3/version` with `projectId` 10100). Match existing versions'
  shape (they are typically `released:false`).
- Set `fixVersion = <version>` on every resolved-since-last-release issue that has
  no fixVersion (`PUT /issue/KEY` with
  `{"update":{"fixVersions":[{"add":{"name":"<version>"}}]}}`). Leave issues that
  already carry a different fixVersion (e.g. a backport) alone unless told
  otherwise.
- Verify the count the release-notes link uses:
  `project = GRP AND status in (Resolved, Closed, "Ready for Release") AND
  fixVersion = <version>`.

## 5. Add the row to the release-notes wiki page

Page: **v7 Release Notes**, pageId `28549113`
(`/wiki/spaces/Grouper/pages/28549113`). Edit via grouper-wiki-edit (fetch
`body.storage`, edit the XHTML, PUT with `version.number + 1`, keep the top
Navigation include). Insert the new `<tr>` ABOVE the current first data row.

### Table columns (storage order)

1. **Date / Status** -- date in a `<p>`, then the status `<p>`(s) below. If the
   release has a known bug that a later release fixes, add a second `<p>` with the
   `GRP-####` link under the status (as 7.3.0 carries GRP-7119, 7.3.1 carries
   GRP-7132).
2. **Container tag (version)** -- `<p>i2incommon/grouper:<version></p>` then a `<p>`
   with `sha256:` and the full 64-char digest. **Do NOT put `<br>` in the hash** --
   a continuous run wraps to the column; hardcoded breaks double-wrap and look bad.
3. **Upgrade instructions / Upgrade tasks** -- one cell, bold labels:
   `<p><strong>Upgrade instructions:</strong></p>` + value, then
   `<p><strong>Upgrade tasks:</strong></p>` + value. Value is `None`, or a wiki
   link like `3 from 7.2.1` / `43 (DDL)`.
4. **Versions** -- `OS: <os>` / `Tomcat: <t>` / `Java Corretto: <j>` /
   `Grouper API: <version>` (one per line via `<br>`).
5. **Enhancements and bugs fixed / known issues** -- first `<p>` is the
   `<N> Jiras` link (the fixVersion JQL from step 4). Second `<p>` is the
   highlight list: **sentence case**, each linked to its GRP issue (or a wiki page
   when one exists), separated by `<br>`. Keep it curated -- not every issue.

### Status values and cell colour

Status text and the cell `data-highlight-colour` attribute (applied to every cell
in the row):

- `LATEST STABLE` -> `#fffae6` (yellow). **Exactly one row** may be LATEST STABLE;
  when promoting a new one, demote the previous LATEST STABLE to `STABLE`.
- `STABLE` -> no `data-highlight-colour` attribute (plain / no fill).
- `NOT STABLE` -> no fill; add the blocking `GRP-####` link under the status.
- `RELEASED` -> white (`#ffffff`); an interim state before a stability call.
- `EXPIRED` -> older superseded releases.

### Table layout

The table is `data-layout="full-width"` with an explicit `<colgroup>` of 5
`<col>` widths (roughly 12 / 19 / 15 / 19 / 35 percent) so columns do not
starve. Full-width + colgroup is what makes the Cloud table read like the old
on-prem one; without column widths Cloud squeezes every column and wraps tags
mid-token.

## 6. Verify

Re-GET the page and confirm: the new row is on top, exactly one `LATEST STABLE`,
the `<N> Jiras` link resolves to the expected count, hashes have no `<br>`, and
every row still has 5 cells. Give the user the page URL.
