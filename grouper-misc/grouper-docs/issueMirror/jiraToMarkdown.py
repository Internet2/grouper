#!/usr/bin/env python3
"""
jiraToMarkdown.py -- mirror the GRP Jira project (grouper.atlassian.net) to
Markdown, one file per issue, under issueMirror/spaces/Grouper/GRP-<n>.md.

This is the companion of wikiMirror/wikiToMarkdown.py, for issues instead of
wiki pages. Read convertJiraToMarkdownAiInstructions.md for the full spec and
rationale. In short:

  * Source of truth is Jira Cloud REST **v2** (/rest/api/2), because v2 renders
    bodies as wiki markup text. v3 returns ADF (a JSON tree) which would have to
    be re-serialized and would not match the existing mirror.
  * The original mirror (GRP-1 .. GRP-7143) was rendered from the frozen on-prem
    Jira export by jira_migration/migrate.py `markdown`. This script continues
    that mirror from the cloud, keeping the exact same file/frontmatter shape.
  * Migration boilerplate the CSV import stamped onto migrated issues (the
    "Original reporter/assignee" header in the description, and the
    "_[originally by X]_" prefix on comments) is stripped again here, and is
    used to recover the real reporter/assignee/comment author -- so a re-render
    of a migrated issue stays faithful to the on-prem original.
  * Incremental: an issue is re-fetched and rewritten only when its cloud
    `updated` timestamp differs from the `updated` in its file's frontmatter
    (or the file is missing). The 2026-07-18 migration bulk-touched every
    issue, so those timestamps are ignored -- see MIGRATION_TOUCH below.
  * **Migrated issues (GRP-1 .. GRP-7143) are NOT re-rendered from the cloud by
    default.** The CSV import misaligned the summary/created/fixVersions of
    1181 of them by one to three rows (cloud GRP-1009 carries on-prem
    GRP-1006's summary, etc.); the later REST reformat pass fixed descriptions
    by key but not the CSV-carried fields. So for those issues the existing
    on-prem-derived Markdown is the accurate record and the cloud is not.
    `--include-migrated` overrides this -- do not use it until the cloud
    summaries have been repaired from the on-prem export.
  * Email addresses are redacted to <localpart>@example.com, same convention as
    the wiki mirror, because this repo is public.

Usage:
    export ATLASSIAN_EMAIL='you@example.com'
    export ATLASSIAN_API_TOKEN='...'         # id.atlassian.com API token
    python3 jiraToMarkdown.py                # incremental refresh
    python3 jiraToMarkdown.py --full         # re-render every issue
    python3 jiraToMarkdown.py --dry-run      # list what would change, write nothing
    python3 jiraToMarkdown.py --only GRP-7245,GRP-7250

Standard library only (no requests / no bs4).
"""

import os
import re
import sys
import json
import base64
import argparse
import urllib.error
import urllib.parse
import urllib.request

SITE = "https://grouper.atlassian.net"
API = SITE + "/rest/api/2"
PROJECT = "GRP"

# The frozen on-prem Jira the mirror originally came from. Issues that predate
# the cloud migration keep an onprem_url; cloud-native issues have none.
ONPREM = "https://todos.internet2.edu"

# The cloud import (2026-07-18) rewrote every migrated issue, so every issue
# carries an `updated` of that date or later even when nothing really changed.
# Anything updated on or before this instant is treated as "migration noise"
# and does not by itself trigger a re-render. Real post-migration edits are
# 2026-07-19 or later.
MIGRATION_TOUCH = "2026-07-19"

# Highest key that came over from the on-prem box. Anything above this was
# created natively in the cloud and is safe to render from the cloud; anything
# at or below it is only rendered with --include-migrated (see the module
# docstring for why).
MIGRATED_MAX_KEY = 7143

DEFAULT_OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                           "spaces", "Grouper")

# Fields the mirror needs. `comment` and `attachment` are only available on the
# single-issue GET, not on search, which is why changed issues are fetched one
# at a time.
ISSUE_FIELDS = ("summary,description,issuetype,status,resolution,priority,"
                "reporter,assignee,created,updated,resolutiondate,components,"
                "fixVersions,labels,issuelinks,attachment,comment")


# --------------------------------------------------------------------------- #
# Email redaction -- identical convention to wikiMirror/wikiToMarkdown.py
# --------------------------------------------------------------------------- #
EMAIL_RE = re.compile(
    r"(?<![\w.%+\-@])([A-Za-z0-9][A-Za-z0-9._%+-]*)@"
    r"([A-Za-z0-9](?:[A-Za-z0-9.-]*[A-Za-z0-9])?\.[A-Za-z]{2,})"
)


def redact_emails(md):
    """Replace real email addresses with <localpart>@example.com."""
    def repl(m):
        if m.group(2).lower().startswith("odata."):
            return m.group(0)  # leave OData annotations untouched
        return m.group(1) + "@example.com"
    return EMAIL_RE.sub(repl, md)


# --------------------------------------------------------------------------- #
# REST helpers
# --------------------------------------------------------------------------- #
def auth_header():
    email = os.environ.get("ATLASSIAN_EMAIL")
    token = os.environ.get("ATLASSIAN_API_TOKEN")
    if not (email and token):
        sys.exit("Set ATLASSIAN_EMAIL and ATLASSIAN_API_TOKEN in the environment.")
    return "Basic " + base64.b64encode(("%s:%s" % (email, token)).encode()).decode()


def get(url, auth):
    req = urllib.request.Request(url, headers={"Authorization": auth,
                                               "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.load(r)


def list_issues(auth):
    """Every GRP issue with just its key + updated timestamp (cheap). Uses the
    token-paginated /search/jql endpoint -- the old /search with startAt was
    removed by Atlassian and now returns 410."""
    out, token = [], None
    while True:
        q = {"jql": "project=%s ORDER BY key ASC" % PROJECT,
             "maxResults": 100, "fields": "updated"}
        if token:
            q["nextPageToken"] = token
        d = get(API + "/search/jql?" + urllib.parse.urlencode(q), auth)
        for i in d.get("issues", []):
            out.append((i["key"], i["fields"]["updated"]))
        token = d.get("nextPageToken")
        if not token or d.get("isLast"):
            break
    return out


# --------------------------------------------------------------------------- #
# Un-stamping the migration boilerplate
#
# The CSV import could not set the real reporter/assignee/comment author, so
# migrate.py stamped them into the text instead. The mirror is the faithful
# record, so we peel that back off and put the real people in the frontmatter.
# --------------------------------------------------------------------------- #
MIGRATION_HEADER_RE = re.compile(
    r"\A-{4,}\s*\n+Original reporter:\s*(?P<reporter>.*?)\s*\n+"
    r"Original assignee:\s*(?P<assignee>.*?)\s*\n+-{4,}\s*\n+",
    re.S)

ORIGINALLY_BY_RE = re.compile(r"\A_\[originally by (?P<who>[^\]]+)\]_\s*\n+")


def strip_migration_header(desc):
    """-> (description without the header, original reporter, original assignee).
    Reporter/assignee are '' when the issue is cloud-native (no header)."""
    m = MIGRATION_HEADER_RE.match(desc or "")
    if not m:
        return desc or "", "", ""
    return desc[m.end():], m.group("reporter"), m.group("assignee")


def strip_originally_by(body):
    """-> (comment body without the '_[originally by X]_' line, X or '')."""
    m = ORIGINALLY_BY_RE.match(body or "")
    if not m:
        return body or "", ""
    return body[m.end():], m.group("who")


# --------------------------------------------------------------------------- #
# Rendering -- must match migrate.py phase_markdown byte for byte
# --------------------------------------------------------------------------- #
def _yaml_list(items):
    return "[" + ", ".join(items) + "]"


def _nm(d):
    return (d or {}).get("name") or (d or {}).get("displayName") or ""


def _person(d):
    if not d:
        return ""
    return "%s <%s>" % (d.get("displayName", "?"),
                        d.get("emailAddress") or d.get("name") or "?")


def render(issue):
    f = issue["fields"]
    key = issue["key"]

    desc, orig_reporter, orig_assignee = strip_migration_header(f.get("description"))
    migrated = bool(orig_reporter or orig_assignee)

    comps = [c["name"] for c in f.get("components", []) or []]
    fixv = [v["name"] for v in f.get("fixVersions", []) or []]
    labels = f.get("labels", []) or []
    links = []
    for l in f.get("issuelinks", []) or []:
        other = l.get("outwardIssue") or l.get("inwardIssue") or {}
        rel = l["type"].get("outward" if "outwardIssue" in l else "inward",
                            l["type"]["name"])
        links.append("%s %s" % (rel, other.get("key", "?")))

    out = [
        "---",
        "key: %s" % key,
        "cloud_key: %s" % "",
        # Cloud-native issues never existed on the on-prem box, so no link.
        "onprem_url: %s" % ("%s/browse/%s" % (ONPREM, key) if migrated else ""),
        "type: %s" % _nm(f.get("issuetype")),
        "status: %s" % _nm(f.get("status")),
        "resolution: %s" % (_nm(f.get("resolution")) or "Unresolved"),
        "priority: %s" % _nm(f.get("priority")),
        # For migrated issues the real people are in the stamped header; for
        # cloud-native ones the Jira fields are already correct.
        "reporter: %s" % (orig_reporter or _person(f.get("reporter"))),
        "assignee: %s" % (orig_assignee or _person(f.get("assignee"))),
        "created: %s" % f.get("created", ""),
        "updated: %s" % f.get("updated", ""),
        "resolved: %s" % (f.get("resolutiondate") or ""),
        "components: %s" % _yaml_list(comps),
        "fixVersions: %s" % _yaml_list(fixv),
        "labels: %s" % _yaml_list(labels),
        "links: %s" % _yaml_list(links),
        "---",
        "",
        "# %s  %s" % (key, f.get("summary") or ""),
        "",
        desc or "_(no description)_",
        "",
    ]

    comments = (f.get("comment", {}) or {}).get("comments", [])
    if comments:
        out.append("## Comments")
        for c in comments:
            body, orig_author = strip_originally_by(c.get("body"))
            author = orig_author or (c.get("author") or {}).get("displayName", "unknown")
            out.append("")
            out.append("### %s - %s" % (author, c.get("created", "?")))
            out.append("")
            out.append(body)

    atts = f.get("attachment", []) or []
    if atts:
        out.append("")
        out.append("## Attachments")
        for a in atts:
            out.append("- %s (%s bytes) - by %s on %s" % (
                a["filename"], a.get("size", "?"),
                (a.get("author") or {}).get("displayName", "?"), a.get("created", "?")))

    return redact_emails("\n".join(out))


# --------------------------------------------------------------------------- #
# Incremental bookkeeping
# --------------------------------------------------------------------------- #
FM_UPDATED_RE = re.compile(r"^updated:\s*(.*)$", re.M)


def file_updated(path):
    """The `updated` stamp recorded in an existing mirror file, or None."""
    try:
        with open(path, encoding="utf-8") as fh:
            head = fh.read(2048)
    except OSError:
        return None
    m = FM_UPDATED_RE.search(head)
    return m.group(1).strip() if m else None


def main():
    ap = argparse.ArgumentParser(description="Mirror the GRP Jira project to Markdown.")
    ap.add_argument("--out", default=DEFAULT_OUT,
                    help="output dir (default: issueMirror/spaces/Grouper)")
    ap.add_argument("--full", action="store_true",
                    help="re-render every issue, not just changed ones")
    ap.add_argument("--dry-run", action="store_true",
                    help="report what would change; write nothing")
    ap.add_argument("--only", help="comma-separated issue keys to refresh")
    ap.add_argument("--include-migrated", action="store_true",
                    help="also render GRP-1..GRP-%d from the cloud -- UNSAFE, the "
                         "cloud summaries of 1181 of them are misaligned by the "
                         "CSV import (see the module docstring)" % MIGRATED_MAX_KEY)
    args = ap.parse_args()

    auth = auth_header()
    out = args.out
    if not args.dry_run:
        os.makedirs(out, exist_ok=True)

    if args.only:
        wanted = [(k.strip(), None) for k in args.only.split(",") if k.strip()]
    else:
        print("listing %s issues ..." % PROJECT)
        wanted = list_issues(auth)
        print("  %d issue(s)" % len(wanted))

    todo = []
    skipped_migrated = 0
    for key, updated in wanted:
        # Migrated issues: the local file beats the cloud (misaligned CSV
        # fields), so leave it alone unless explicitly asked.
        if (not args.only and not args.include_migrated
                and int(key.split("-")[1]) <= MIGRATED_MAX_KEY):
            skipped_migrated += 1
            continue
        path = os.path.join(out, key + ".md")
        have = file_updated(path)
        if args.full or args.only or have is None:
            todo.append((key, have is None))
            continue
        # Unchanged since the mirror was written -> skip.
        if updated == have:
            continue
        # The 2026-07-18 import touched every migrated issue without changing
        # its content; only a genuine later edit is worth re-rendering.
        if updated and updated[:10] < MIGRATION_TOUCH:
            continue
        todo.append((key, False))

    print("%d issue(s) to render" % len(todo))
    new = upd = 0
    for key, is_new in todo:
        try:
            issue = get("%s/issue/%s?fields=%s" % (API, key, ISSUE_FIELDS), auth)
        except urllib.error.HTTPError as e:
            print("  %-10s FETCH FAILED %s" % (key, e))
            continue
        md = render(issue)
        path = os.path.join(out, key + ".md")
        print("  %-4s %s" % ("NEW" if is_new else "UPD", path))
        if not args.dry_run:
            with open(path, "w", encoding="utf-8") as fh:
                fh.write(md)
        new, upd = (new + 1, upd) if is_new else (new, upd + 1)

    print("\nnew=%d updated=%d unchanged=%d migrated-not-rendered=%d%s"
          % (new, upd, len(wanted) - len(todo) - skipped_migrated, skipped_migrated,
             "  (dry run)" if args.dry_run else ""))
    print("Review `git diff` and commit with an 'issues: sync' message "
          "(do not auto-push).")


if __name__ == "__main__":
    main()
