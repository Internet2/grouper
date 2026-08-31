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

## 1. Refresh the grouper-docs mirrors FIRST

Do this BEFORE tagging, so the mirror refresh is part of the release commit rather
than a commit that lands after the tag and leaves the branch ahead of it.

Run them in the checkout for the branch being released -- a v6 release refreshes
v6's mirror. Note that `issueMirror` and `sitemap.xml` exist only on v7; older
branches carry `wikiMirror` alone, so there is just one script to run there.

Both mirrors are incremental, so this is cheap.

```bash
set -a && . ~/.secrets/grouper_confluence.env && set +a
cd grouper-misc/grouper-docs/wikiMirror  && python3 wikiToMarkdown.py
cd ../issueMirror                        && python3 jiraToMarkdown.py   # v7 only
```

**Dependency:** the scripts need `beautifulsoup4`, and it is often not installed
(`ModuleNotFoundError: No module named 'bs4'`). Homebrew's python is
externally-managed, so do not pip install into it -- use a throwaway venv:

```bash
python3 -m venv /tmp/wikivenv && /tmp/wikivenv/bin/pip install -q beautifulsoup4
/tmp/wikivenv/bin/python wikiToMarkdown.py
```

- The wiki run also regenerates `wikiMirror/sitemap.xml` (v7 only); copy it to
  `i2midev6:/var/www/html/sitemap.xml` (see the sitemap section of
  `wikiMirror/convertToMarkdownAiInstructions.md`).
- `jiraToMarkdown.py` deliberately skips `GRP-1 .. GRP-7143` -- the cloud
  metadata for many of those is misaligned by the CSV import, so the existing
  files are the accurate record. See
  `issueMirror/convertJiraToMarkdownAiInstructions.md`.
- Review both diffs and commit separately (`wiki: sync`, `issues: sync`);
  a human pushes.

**What this run will NOT capture:** the new release-notes row, because it does not
exist yet -- that row needs the published container digest, which needs the image,
which needs the tag. That is unavoidable, and it is fine: the row gets mirrored by
the NEXT release's run. Do not re-run the mirror after writing the row just to pick
it up, or you reintroduce the post-tag commit this ordering exists to avoid.

## 2. Push everything, THEN tag

The git tag is what Maven Central and the container build read. Tag a tree that is
not fully pushed and the release itself is still correct -- the tag carries the
commit -- but the BRANCH POINTER is left behind its own release tag, which is
invisible unless someone checks for it explicitly.

This is easy to do by accident, because pushing the tag and pushing the branch are
two separate operations and `git push --tags` does NOT push the branch. It happened
on 6.4.0: `GROUPER_RELEASE_6.4.0` was on origin pointing at the right commit while
`origin/GROUPER_6_BRANCH` still pointed one commit back.

Before tagging, confirm the working tree is clean and nothing is unpushed:

```bash
git status --short                                    # expect no tracked changes
git log --oneline origin/<BRANCH>..<BRANCH>           # expect NO output
```

If that second command prints anything, push it first:

```bash
git push origin <BRANCH>
```

Only then tag, and push the tag:

```bash
git tag GROUPER_RELEASE_<version>
git push origin GROUPER_RELEASE_<version>
```

Then prove the tag and the pushed branch agree. Do not skip this -- it is the only
check that catches the split, and it costs one command:

```bash
git fetch origin
git merge-base --is-ancestor GROUPER_RELEASE_<version>^{commit} origin/<BRANCH> \
  && echo "OK: tag is on the pushed branch" || echo "BAD: branch is behind the tag"
```

If it reports BAD, push the branch -- it is a plain fast-forward, since the branch
head is an ancestor of the tagged commit. Never force-push or move the tag to
"fix" this.

Remember the OTHER repos too. The container lives in a separate repo with a branch
per version, and the same split applies there.

## 3. Container prep (Dockerfile)

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

## 4. Confirm DDL / upgrade tasks since the last release

Determines the `Upgrade tasks` cell (usually `None` for a patch release).

- The DDL version enum is `GrouperDdl.java` (a `V1..Vn` enum). Compare the highest
  `Vn` at the last release tag vs HEAD -- if unchanged and nothing under a `ddl/`
  package or `*upgradeTasks*` changed, there is no schema migration: `Upgrade
  tasks: None`.
- If DDL did change, the cell links the "Grouper upgrade tasks" wiki page with the
  new task numbers and `(DDL)`.

## 5. Resolve the GRP issues that shipped

- List GRP-#### keys from commits since the last release tag
  (`git log <TAG>..HEAD`).
- Any that are still Open / To Do but committed should be resolved (via
  grouper-jira). Container-only issues (e.g. a Tomcat bump with no code commit)
  count too if they ship in this image. Ask before transitioning -- an issue may
  be deliberately open because follow-on work is planned, even though code for it
  is committed.
- Transition with the bare `Resolved` transition and do NOT set a `resolution`
  value. The project convention is Resolved with an empty resolution; setting
  `Fixed` makes the issue an outlier.
- An unresolved issue can still carry the fixVersion -- it just will not appear in
  the release-notes count, which filters on status. Subtract it from the count.

## 6. Create the version and tag fixVersion

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
  fixVersion = <version>`. `GET /rest/api/3/search/jql` no longer returns a
  `total`; use `POST /rest/api/3/search/approximate-count` with `{"jql": "..."}`.
  Sanity-check the endpoint by running the same JQL for the PREVIOUS version and
  confirming it matches the number already on that release-notes row.

## 7. Smoke-test the published image with a local quickstart

Run the published container locally before writing the release-notes row. This is
not optional polish -- two of the row's cells can only be read out of the running
image, and this is the cheapest way to catch a broken build before the demo server
and the announcement.

### Wait for the image to actually publish

Pushing the container branch only triggers the Jenkins build; the tag 404s for a
while afterwards. Do not conclude the build failed from one early check. Poll the
registry without pulling anything:

```bash
TOKEN=$(curl -s "https://auth.docker.io/token?service=registry.docker.io&scope=repository:i2incommon/grouper:pull" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://registry-1.docker.io/v2/i2incommon/grouper/tags/list" \
  | python3 -c "import sys,json;print(sorted(json.load(sys.stdin)['tags'])[-8:])"
```

The same token gets the **sha256 digest** for the notes, again without pulling:

```bash
curl -s -o /dev/null -w 'HTTP %{http_code}  digest: %header{docker-content-digest}\n' \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.oci.image.index.v1+json,application/vnd.docker.distribution.manifest.list.v2+json" \
  -I "https://registry-1.docker.io/v2/i2incommon/grouper/manifests/<version>"
```

### Compose file

Works with docker or podman. Use a real random password, not this one.

```yaml
services:
  postgres:
    image: "postgres:14"
    restart: always
    ports:
      - '5433:5432'
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=BlKIS9FeyezRnAw7gW7K
  grouper:
    image: "i2incommon/grouper:<version>"
    restart: always
    ports:
      - '8443:8443'
    command:
      - quickstart
    environment:
      - GROUPERSYSTEM_QUICKSTART_PASS=BlKIS9FeyezRnAw7gW7K
      - GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234
      - GROUPER_DATABASE_PASSWORD=BlKIS9FeyezRnAw7gW7K
      - GROUPER_DATABASE_USERNAME=postgres
      - GROUPER_DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
```

Note there is NO `GROUPER_AUTO_DDL_UPTOVERSION` line. prep_quickstart sets its own
default (`v6.*.*` on v6, `v4.*.*` on v4 -- note the `v` prefix) and only when the
variable is unset, so anything you pass OVERRIDES the correct value. Leave it out.

```bash
docker compose up --detach          # first start builds the schema, several minutes
docker compose logs -f grouper
```

Then **https://localhost:8443/grouper** as `GrouperSystem` with the quickstart
password. Self-signed cert, so expect the browser warning.

#### The port differs by branch -- v4 is NOT the same as v6/v7

**v6 and v7** dropped Apache: Tomcat serves HTTPS directly on **8443**, so
`'8443:8443'` above is right.

**v4 still bundles Apache and Shibboleth.** Its quickstart sets
`GROUPER_RUN_APACHE=true` and configures only the AJP and HTTP connectors -- there
is no HTTPS connector, so **nothing listens on 8443**. Apache terminates TLS on 443
and proxies to Tomcat over AJP. Publishing `'8443:8443'` on v4 maps to a closed
port and the browser just hangs. On v4 use:

```yaml
    ports:
      - '8443:443'          # host 8443 -> Apache 443 inside the container
```

The URL is unchanged (`https://localhost:8443/grouper/`); only the container-side
port differs. Host-side 8443 keeps it non-privileged, which matters for rootless
podman.

Confirm what is actually listening rather than guessing:

```bash
docker compose exec grouper netstat -ltnp     # or: ss -ltnp
```

On v4 expect 443 and 80 (httpd), 8080 and 8009 (Tomcat HTTP/AJP), 8005 shutdown --
and no 8443. On v6/v7 expect 8443.

Mapping `'8443:8080'` also reaches v4 and skips the cert warning, but it bypasses
Apache, so it does not exercise the path a real v4 deployment uses. Prefer 443.

### Five mistakes that cost real time

- **Do not set `GROUPER_AUTO_DDL_UPTOVERSION` at all.** prep_quickstart sets the
  right value itself (`v4.*.*` / `v6.*.*`, with a `v` prefix) and only when unset,
  so passing your own overrides it. The wiki quickstart lesson still shows `5.*.*`,
  which is both the wrong major and the wrong format -- do not copy it.
- **v4 has no HTTPS on 8443** -- see the port section above. This one looks like a
  hung container or a broken build and is neither.
- **Never put the published host port in `GROUPER_DATABASE_URL`.** `'5433:5432'`
  publishes the container's 5432 as 5433 *on the host only*; container-to-container
  it is still `postgres:5432`. The host port exists so a human can attach with
  `psql -h localhost -p 5433 -U postgres`, nothing more.
- **The database name is lowercase `postgres`.** Postgres preserves the case of a
  name sent over the wire, so `/Postgres` fails with `database "Postgres" does not
  exist`.
- **The webapp is at `/grouper`, not the root.** A 404 from Tomcat at `/` is
  normal and says nothing about whether the app deployed.

A database connection failure looks far worse than it is: Grouper reads its config
FROM the database, so a refused connection surfaces as `Problem reading config:
'database:grouper'` inside a static initializer, failing
`CommonServletContainerInitializer` and then the whole context. Read to the LAST
`Caused by` -- that one names the actual port or database problem.

### Read the two image-derived values for the notes

The Rocky minor can no longer be read off the Dockerfile or the image labels: the
base is pinned to a **floating major** (`ARG ROCKY_VERSION=9`), and the label
reports only `version: 9`. Both values have to come from the running image:

```bash
docker compose exec grouper cat /etc/rocky-release
docker compose exec grouper java -version
```

Ignore any `ImageOS: centos7` label -- stale metadata inherited from the TIER base,
wrong for years.

### Tear down

```bash
docker compose stop            # keep containers and data, restart with `start`
docker compose down            # remove containers, KEEP the data volume
docker compose down --volumes  # remove the data too
```

Use `down --volumes` when re-testing against a genuinely fresh schema. Keep the
volume when you only changed a Grouper env var -- wiping costs another full DDL
build for nothing, and `up --detach --force-recreate` already recreates any
container whose config changed.

## 8. Add the row to the release-notes wiki page

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
   `<p><strong>Upgrade tasks:</strong></p>` + value. The two lines mean different
   things -- do not use the same wording for both:
   - **Upgrade instructions** is `None` unless one of the shipped Jiras needs
     operators to be aware of or do something. When there is something, it is a
     COUNT of instructions linking the upgrade-instructions page, e.g.
     `3 from 7.2.1`, `1 from v7.0.2`. Most releases are `None`.
   - **Upgrade tasks** is the upgrade task NUMBER(s), not a count, linking the
     "Grouper upgrade tasks" page (pageId `28549372`). One task is just `44`; two
     are `41, 42`. Append ` (DDL)` only when the task carries a schema change --
     compare `GrouperDdl.java` between the tag and HEAD to decide. `None` when no
     task was added.
4. **Versions** -- `OS: <os>` / `Tomcat: <t>` / `Java Corretto: <j>` /
   `Grouper API: <version>` (one per line via `<br>`).
5. **Enhancements and bugs fixed / known issues** -- first `<p>` is the
   `<N> Jiras` link (the fixVersion JQL from step 6). Second `<p>` is the
   highlight list: **sentence case**, each linked to its GRP issue (or a wiki page
   when one exists), separated by `<br>`. Keep it curated -- not every issue.
   **Security items go FIRST** -- vulnerability fixes, then third-party jar
   upgrades -- so a site deciding whether to upgrade sees them without reading the
   whole cell. New features and bug fixes follow. Related items can share one line
   (`Security improvements: upgrade <a>jsoup</a>, <a>c3p0</a>`).

### Status values and cell colour

A new release is **never** stable on release day. It goes out as `RELEASED`, and
is promoted only after it has been out about **a week** with no issues reported --
a judgment call by the Grouper team, not an automatic date. Until then the
PREVIOUS release keeps `LATEST STABLE`. Promoting is a separate, later edit to the
same page: set the new row to `LATEST STABLE` with the yellow fill, and demote the
old one to `STABLE` by removing its `data-highlight-colour` attributes.

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

## 9. Verify

Re-GET the page and confirm: the new row is on top, exactly one `LATEST STABLE`,
the `<N> Jiras` link resolves to the expected count, hashes have no `<br>`, and
every row still has 5 cells. Give the user the page URL.

Re-run the tag/branch check from step 2 as well, for every repo the release touched
(grouper, and the container repo). Commits often land after tagging -- a late fix,
a doc tweak -- so a branch that was in sync at tag time can be behind by the end:

```bash
git fetch origin
git log --oneline origin/<BRANCH>..<BRANCH>    # expect NO output
git merge-base --is-ancestor GROUPER_RELEASE_<version>^{commit} origin/<BRANCH> \
  && echo "OK" || echo "BAD: branch is behind the tag"
```

## 10. Slack announcement

Two things close out a release: the demo server is upgraded to the new container
tag, and the release is announced on Slack.

The release is not finished until the user has something to paste into Slack.
Once the demo server is up, ALWAYS produce the announcement without being asked --
it is the last deliverable of every release.

**Output it as RENDERED markdown, never inside a code fence.** The whole point is
that the links survive the copy/paste into Slack. A fenced block pastes as literal
markdown source and every link is lost, which means redoing it by hand.

Everything in it comes from the release-notes row you just wrote -- do not
re-derive it, or the wiki and the announcement will disagree:

- Version from the row.
- The upgrade-instructions sentence from the `Upgrade instructions` cell: `None`
  becomes "There are no upgrade instructions from v<previous>." When the cell has
  a count, say so instead and link the upgrade-instructions page.
- The `<N> Jiras` link, verbatim from cell 5 (same JQL URL).
- The highlight lines, verbatim from cell 5 -- same text, same order (security
  first), same links, one per line.

Template:

> @channel
>
> We are proud to announce the release of **Grouper v7.4.0**.  There are no
> upgrade instructions from v7.3.2.
>
> See the [v7 release notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549113/v7+Release+Notes)
>
> [73 Jiras](<fixVersion JQL url>)
> OAuth security fixes: [open redirect on authorize](...), [client secret verification](...), [signing key encrypted at rest](...)
> Security improvements: upgrade [PostgreSQL JDBC](...), [jsoup](...), [c3p0](...), [ldaptive](...)
> [New Jamf Pro provisioner](...)
> ... one highlight per line, in cell-5 order ...

Note the highlights are a flat list of lines, not a bulleted list -- Slack renders
the plain lines the way the release-notes cell reads.
