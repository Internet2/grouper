#!/usr/bin/env python3
"""
wikiToMarkdown.py -- mirror the Grouper Confluence wiki (grouper.atlassian.net)
to Markdown, one file per page, under misc/wiki/<SPACE>/.

This is the reference implementation of convertToMarkdownAiInstructions.md --
read that file for the full spec and rationale. In short:

  * Source of truth is the Confluence "storage" (XHTML) body, NOT rendered HTML.
  * Content only: navigation menus (the `include` macro), TOC, children lists,
    images and attachments are stripped.
  * Internal page links are resolved to absolute, pageId-stable cloud URLs.
  * Each file gets YAML frontmatter (title/space/pageId/version/lastUpdated/url).
  * Incremental: only pages whose Confluence `version` changed are re-fetched and
    rewritten; pages that no longer exist (or moved) get their stale file pruned.

Usage:
    export ATLASSIAN_EMAIL='you@example.com'
    export ATLASSIAN_API_TOKEN='...'                # id.atlassian.com API token
    python3 wikiToMarkdown.py --out misc/wiki       # from the repo root
    python3 wikiToMarkdown.py --out misc/wiki --full # rewrite every page

Requires: Python 3, beautifulsoup4 (pip install beautifulsoup4). Uses only the
standard library otherwise.
"""

import os
import re
import sys
import json
import base64
import argparse
import subprocess
import urllib.parse
import urllib.request

from bs4 import BeautifulSoup, NavigableString, Tag

SITE = "https://grouper.atlassian.net/wiki"
API = SITE + "/rest/api"
SPACES = ["Grouper", "GrIntDev"]

# Macros that are navigation / dynamic / non-content -- strip them entirely.
STRIP_MACROS = {
    "include", "toc", "children", "anchor", "recently-updated",
    "recently-updated-dashboard", "livesearch", "search-box", "contentbylabel",
    "gliffy", "view-file", "align", "unmigrated-wiki-markup",
    "unmigrated-inline-wiki-markup", "auitabspage", "auibutton", "auimessage",
    "auitabs",
}
# Macros rendered as a Markdown blockquote callout.
CALLOUT_MACROS = {"info", "note", "warning", "tip", "panel"}


# --------------------------------------------------------------------------- #
# Confluence REST helpers
# --------------------------------------------------------------------------- #
def _auth_header():
    email = os.environ.get("ATLASSIAN_EMAIL")
    token = os.environ.get("ATLASSIAN_API_TOKEN")
    if not (email and token):
        sys.exit("Set ATLASSIAN_EMAIL and ATLASSIAN_API_TOKEN in the environment.")
    return "Basic " + base64.b64encode(f"{email}:{token}".encode()).decode()


def _get(url, auth):
    req = urllib.request.Request(url, headers={"Authorization": auth})
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.load(r)


def list_pages(space, auth):
    """All current pages in a space, without bodies (cheap). Returns list of dicts
    with id, title, version, ancestors, lastUpdated, webui."""
    out, start = [], 0
    expand = "version,ancestors,history.lastUpdated"
    while True:
        url = (f"{API}/content?spaceKey={space}&type=page&status=current"
               f"&limit=100&start={start}&expand={expand}")
        d = _get(url, auth)
        for r in d["results"]:
            out.append({
                "id": r["id"],
                "title": r["title"],
                "space": space,
                "version": r["version"]["number"],
                "ancestors": [a["title"] for a in r.get("ancestors", [])],
                "lastUpdated": r.get("history", {}).get("lastUpdated", {}).get("when", ""),
                "webui": r["_links"]["webui"],
            })
        start += 100
        if len(d["results"]) < 100:
            break
    return out


def fetch_storage(page_id, auth):
    d = _get(f"{API}/content/{page_id}?expand=body.storage", auth)
    return d["body"]["storage"]["value"]


# --------------------------------------------------------------------------- #
# Link resolution -- title -> absolute, pageId-stable URL
# --------------------------------------------------------------------------- #
class LinkResolver:
    def __init__(self, pages):
        # (space, title) -> (pageId, webui) ; also title -> first match for fallback
        self.by_space = {}
        self.by_title = {}
        for p in pages:
            self.by_space[(p["space"], p["title"])] = (p["id"], p["webui"])
            self.by_title.setdefault(p["title"], (p["id"], p["webui"]))

    def url(self, title, space_key, src_space):
        hit = (self.by_space.get((space_key or src_space, title))
               or self.by_space.get((src_space, title))
               or self.by_title.get(title))
        return SITE + hit[1] if hit else None


# --------------------------------------------------------------------------- #
# storage XHTML -> Markdown
# --------------------------------------------------------------------------- #
def _text(node):
    return node.get_text() if isinstance(node, Tag) else str(node)


def convert(storage, src_space, resolver):
    soup = BeautifulSoup(storage or "", "html.parser")

    def walk(node):
        if isinstance(node, NavigableString):
            return re.sub(r"\s+", " ", str(node))
        if not isinstance(node, Tag):
            return ""
        n = node.name

        def kids():
            return "".join(walk(c) for c in node.children)

        # block / inline text
        if n in ("p", "div"):
            return kids().strip() + "\n\n"
        if n == "br":
            return "  \n"
        if re.fullmatch(r"h[1-6]", n or ""):
            return "\n\n" + "#" * int(n[1]) + " " + kids().strip() + "\n\n"
        if n in ("strong", "b"):
            return "**" + kids().strip() + "**"
        if n in ("em", "i"):
            return "*" + kids().strip() + "*"
        if n == "code":
            return "`" + kids().strip() + "`"
        if n == "blockquote":
            inner = kids().strip()
            return "\n".join("> " + l for l in inner.split("\n")) + "\n\n"

        # lists
        if n in ("ul", "ol"):
            items = [c for c in node.children if getattr(c, "name", None) == "li"]
            lines = []
            for i, li in enumerate(items):
                mark = "- " if n == "ul" else f"{i + 1}. "
                body = walk(li).strip()
                body = "\n".join(("  " + l if j else l)
                                 for j, l in enumerate(body.split("\n")))
                lines.append(mark + body)
            return "\n\n" + "\n".join(lines) + "\n\n"
        if n == "li":
            return "".join(walk(c) for c in node.children).strip()

        # tables (GitHub markdown)
        if n == "table":
            rows = node.find_all("tr")
            md = []
            for ri, tr in enumerate(rows):
                cells = [walk(c).strip().replace("\n", " ")
                         for c in tr.find_all(["td", "th"])]
                md.append("| " + " | ".join(c.replace("|", "\\|") for c in cells) + " |")
                if ri == 0:
                    md.append("| " + " | ".join("---" for _ in cells) + " |")
            return "\n\n" + "\n".join(md) + "\n\n"

        # links
        if n == "a":
            href = node.get("href", "")
            txt = kids().strip() or href
            return f"[{txt}]({href})" if href else txt
        if n == "ac:link":
            rp = node.find("ri:page")
            body = node.find(["ac:link-body", "ac:plain-text-link-body"])
            txt = (walk(body).strip() if body else "")
            if rp is not None:
                title = rp.get("ri:content-title", "")
                txt = txt or title
                url = resolver.url(title, rp.get("ri:space-key"), src_space)
                return f"[{txt}]({url})" if url else txt
            return txt  # attachment/user link -> text only

        # code / noformat macros -> fenced block
        if n == "ac:structured-macro":
            mac = node.get("ac:name", "")
            if mac in ("code", "noformat"):
                body = node.find("ac:plain-text-body")
                langp = node.find("ac:parameter", {"ac:name": "language"})
                lang = langp.get_text().strip() if langp else ""
                code = body.get_text() if body else ""
                return f"\n```{lang}\n{code}\n```\n\n"
            if mac in CALLOUT_MACROS:
                body = node.find("ac:rich-text-body")
                inner = walk(body).strip() if body else ""
                return "\n".join("> " + l for l in inner.split("\n")) + "\n\n"
            if mac == "expand":
                body = node.find("ac:rich-text-body")
                return walk(body) if body else ""
            if mac == "jira":
                k = node.find("ac:parameter", {"ac:name": "key"})
                return f"[JIRA {k.get_text().strip()}]" if k else ""
            if mac in STRIP_MACROS:
                return ""
            # unknown macro -> emit its rich-text body if any, else drop
            body = node.find("ac:rich-text-body")
            return walk(body) if body else ""

        # strip images/attachments/user refs entirely
        if n in ("ac:image", "ri:page", "ri:attachment", "ri:user", "ri:space",
                 "ac:parameter"):
            return ""
        # transparent wrappers
        if n in ("ac:layout", "ac:layout-section", "ac:layout-cell", "span",
                 "col", "colgroup", "tbody", "thead", "time",
                 "ac:link-body", "ac:plain-text-link-body", "ac:rich-text-body",
                 "ac:plain-text-body"):
            return kids()
        return kids()

    md = walk(soup)
    md = md.replace("\xa0", " ")
    md = re.sub(r"\n{3,}", "\n\n", md)
    return md.strip() + "\n"


# --------------------------------------------------------------------------- #
# File layout / frontmatter
# --------------------------------------------------------------------------- #
def sanitize(name):
    """Folder/file-safe segment: every run of non-alphanumeric characters becomes a
    single underscore; leading/trailing underscores trimmed; case preserved.
    Raises if the result is empty (fix the offending title upstream in the wiki)."""
    s = re.sub(r"[^A-Za-z0-9]+", "_", name).strip("_")
    if not s:
        raise ValueError(f"segment sanitizes to empty: {name!r}")
    return s


def target_path(out_dir, page):
    parts = [page["space"]] + [sanitize(a) for a in page["ancestors"]]
    d = os.path.join(out_dir, *parts)
    return os.path.join(d, sanitize(page["title"]) + ".md")


def read_meta(path):
    """(pageId, version) from an existing file's frontmatter, else (None, None)."""
    try:
        with open(path, encoding="utf-8") as f:
            head = f.read(1000)
    except OSError:
        return (None, None)
    pid = re.search(r"^pageId:\s*(\d+)\s*$", head, re.M)
    ver = re.search(r"^version:\s*(\d+)\s*$", head, re.M)
    return (pid.group(1) if pid else None, int(ver.group(1)) if ver else None)


def scan_existing(out_dir, spaces):
    """Map pageId -> current abspath for files we previously generated, so a page
    that was renamed/moved can be `git mv`d instead of rewritten at a new path."""
    m = {}
    for sp in spaces:
        base = os.path.join(out_dir, sp)
        if not os.path.isdir(base):
            continue
        for root, _dirs, files in os.walk(base):
            for fn in files:
                if fn.endswith(".md"):
                    pid, _ = read_meta(os.path.join(root, fn))
                    if pid:
                        m[pid] = os.path.abspath(os.path.join(root, fn))
    return m


def _git(out_dir, *args):
    """Run `git -C out_dir ...`; True on success (False if not a git repo, etc.)."""
    try:
        return subprocess.run(["git", "-C", out_dir, *args],
                              capture_output=True, text=True).returncode == 0
    except OSError:
        return False


def git_mv(out_dir, old, new):
    """Rename preserving git history; fall back to a plain move for untracked files."""
    os.makedirs(os.path.dirname(new), exist_ok=True)
    if not _git(out_dir, "mv", old, new):
        os.replace(old, new)


def git_rm(out_dir, path):
    if not _git(out_dir, "rm", "-q", path):
        try:
            os.remove(path)
        except OSError:
            pass


def frontmatter(page):
    url = SITE + page["webui"]
    def q(s):
        return '"' + s.replace('"', '\\"') + '"'
    return ("---\n"
            f"title: {q(page['title'])}\n"
            f"space: {page['space']}\n"
            f"pageId: {page['id']}\n"
            f"version: {page['version']}\n"
            f"lastUpdated: {page['lastUpdated']}\n"
            f"url: {url}\n"
            "---\n\n")


# --------------------------------------------------------------------------- #
# main
# --------------------------------------------------------------------------- #
def main():
    ap = argparse.ArgumentParser(description="Mirror Grouper Confluence to Markdown.")
    default_out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "spaces")
    ap.add_argument("--out", default=default_out,
                    help="output base dir (default: ./spaces next to this script)")
    ap.add_argument("--spaces", default=",".join(SPACES),
                    help="comma-separated space keys")
    ap.add_argument("--full", action="store_true",
                    help="rewrite every page (ignore version cache)")
    ap.add_argument("--no-prune", action="store_true",
                    help="do not delete stale/moved files")
    args = ap.parse_args()
    auth = _auth_header()
    spaces = [s.strip() for s in args.spaces.split(",") if s.strip()]
    out = args.out

    # 1. list every current page (cheap; also feeds link resolution)
    pages = []
    for sp in spaces:
        print(f"listing {sp} ...", flush=True)
        pages += list_pages(sp, auth)
    print(f"  {len(pages)} pages", flush=True)
    resolver = LinkResolver(pages)

    # 2. compute target paths; FAIL on any collision (fix the title upstream)
    targets = {}  # abspath -> page
    for p in pages:
        path = os.path.abspath(target_path(out, p))
        if path in targets:
            o = targets[path]
            sys.exit(f"CONFLICT: '{o['title']}' (id {o['id']}) and "
                     f"'{p['title']}' (id {p['id']}) both map to\n  {path}\n"
                     "Rename one page in the wiki, then re-run.")
        targets[path] = p

    # 3. where each pageId's file currently lives (for rename/move detection)
    existing = scan_existing(out, spaces)

    def write(path, p):
        storage = fetch_storage(p["id"], auth)
        md = frontmatter(p) + convert(storage, p["space"], resolver)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            f.write(md)

    new = updated = skipped = moved = 0
    for path, p in targets.items():
        old = existing.get(p["id"])
        if old and old != path:            # renamed or moved -> git mv (keeps history)
            git_mv(out, old, path)
            write(path, p)
            moved += 1
            print(f"  MOVE {old} -> {path}", flush=True)
        elif old == path:
            _, ver = read_meta(path)
            if not args.full and ver == p["version"]:
                skipped += 1
                continue
            write(path, p)
            updated += 1
            print(f"  UPD  {path}", flush=True)
        else:
            write(path, p)
            new += 1
            print(f"  NEW  {path}", flush=True)

    # 4. prune files for pages that no longer exist (git rm)
    pruned = 0
    if not args.no_prune:
        current = {p["id"] for p in pages}
        for pid, old in existing.items():
            if pid not in current and os.path.exists(old):
                git_rm(out, old)
                pruned += 1
                print(f"  PRUNE {old}", flush=True)

    print(f"\nnew={new} updated={updated} moved={moved} "
          f"skipped={skipped} pruned={pruned}")
    print("Review `git diff` and commit with a 'wiki: sync' message (do not auto-push).")


if __name__ == "__main__":
    main()
