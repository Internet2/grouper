#!/usr/bin/env python3
"""
Build a 1:1 redirect map from the OLD on-prem Grouper wiki
(spaces.at.internet2.edu, Confluence Server 9.x) to the NEW Cloud docs site
(docs.grouper.internet2.edu). Pages are matched by title; the old numeric pageId
is the redirect key, the new cloud URL is the target.

Outputs (in the same dir as this script):
  grouper_moved.map        Apache RewriteMap txt: "<oldId> <newUrl>" per line
  grouper_redirects.conf   Apache config snippet using that map
  redirect_report.txt      matched / unmatched-old / new-only, for review

Read-only against both wikis. Makes NO changes to either server.
"""
import os, sys, json, base64, subprocess

OLD = "https://spaces.at.internet2.edu"          # Confluence Server 9.x (no /wiki)
NEW_API = "https://grouper.atlassian.net/wiki"   # Cloud API host
DOCS = "https://docs.grouper.internet2.edu/wiki"  # public target host
UA = ("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
HERE = os.path.dirname(os.path.abspath(__file__))


def curl_json(url, auth=None):
    cmd = ["curl", "-s", "--fail", "-A", UA, "-H", "Accept: application/json"]
    if auth:
        cmd += ["-u", auth]
    cmd.append(url)
    out = subprocess.run(cmd, capture_output=True, text=True, timeout=40)
    if out.returncode != 0:
        raise RuntimeError(f"curl {out.returncode} for {url}")
    return json.loads(out.stdout)


def list_pages(api, space, auth, page_size):
    pages, start = [], 0
    while True:
        d = curl_json(f"{api}/rest/api/content?spaceKey={space}&type=page"
                      f"&status=current&limit={page_size}&start={start}", auth)
        for r in d.get("results", []):
            pages.append({"id": r["id"], "title": r["title"],
                          "webui": r["_links"]["webui"]})
        if len(d.get("results", [])) < page_size:
            break
        start += page_size
    return pages


def main():
    email = os.environ.get("ATLASSIAN_EMAIL")
    token = os.environ.get("ATLASSIAN_API_TOKEN")
    if not (email and token):
        sys.exit("set ATLASSIAN_EMAIL / ATLASSIAN_API_TOKEN for the new (cloud) wiki")
    new_auth = f"{email}:{token}"

    sys.stderr.write("listing OLD (spaces.at.internet2.edu) ...\n")
    old = list_pages(OLD, "Grouper", None, 200)          # anonymous, browser UA
    sys.stderr.write(f"  old pages: {len(old)}\n")
    sys.stderr.write("listing NEW (grouper.atlassian.net) ...\n")
    new = list_pages(NEW_API, "Grouper", new_auth, 250)  # basic auth
    sys.stderr.write(f"  new pages: {len(new)}\n")

    # index new pages by title (report collisions)
    new_by_title, new_dups = {}, set()
    for p in new:
        if p["title"] in new_by_title:
            new_dups.add(p["title"])
        else:
            new_by_title[p["title"]] = p

    matched, unmatched_old = [], []
    for o in old:
        n = new_by_title.get(o["title"])
        if n:
            matched.append((o["id"], DOCS + n["webui"], o["title"]))
        else:
            unmatched_old.append(o)
    matched_titles = {t for _, _, t in matched}
    new_only = [p for p in new if p["title"] not in {o["title"] for o in old}]

    # sort by old id for a stable, idempotent map file
    matched.sort(key=lambda x: int(x[0]))

    with open(os.path.join(HERE, "grouper_moved.map"), "w") as f:
        for oid, url, _title in matched:
            f.write(f"{oid} {url}\n")

    with open(os.path.join(HERE, "grouper_redirects.conf"), "w") as f:
        f.write(
'''# Redirect the OLD on-prem Grouper wiki space to the NEW Cloud docs site.
# Old server: spaces.at.internet2.edu (Confluence Server 9.x, Apache in front of
# Tomcat). Drop grouper_moved.map next to this file and fix the RewriteMap path.
#
# PLACEMENT: put these rules in the vhost BEFORE the ProxyPass to Confluence, or
# the requests get proxied to Tomcat and never redirect.
#
# IMPORTANT (SEO): an AWS ELB/WAF in front of Apache currently returns 403 to
# Googlebot. A 301 that Googlebot never receives will NOT deindex the old pages.
# Either allow Googlebot through the WAF so it reaches these 301s, or implement
# the same redirects at the ELB/CloudFront layer.

RewriteEngine On
RewriteMap grouper_moved "txt:/etc/httpd/conf.d/grouper_moved.map"

# 1:1 canonical page URL: /spaces/Grouper/pages/<oldId>/<slug> -> new cloud page.
RewriteCond ${grouper_moved:$1|NOTFOUND} !^NOTFOUND$
RewriteRule ^/spaces/Grouper/pages/(\\d+)(?:/[^?]*)?$ ${grouper_moved:$1} [R=301,L,QSD]

# Legacy view URL that carries the pageId: /pages/viewpage.action?pageId=<oldId>.
# Map it directly so it is a single 301 (else Confluence 302s it to the canonical
# URL first). Unknown ids fall back to the space home.
RewriteCond %{QUERY_STRING} (?:^|&)pageId=(\\d+)
RewriteRule ^/pages/viewpage\\.action$ ${grouper_moved:%1|https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview} [R=301,L,QSD]

# Everything else in the space -- space landing, the renamed/dropped pages, and
# legacy /display/Grouper/<Title> -> new space home.
RewriteRule ^/spaces/Grouper(/.*)?$ https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview [R=301,L,QSD]
RewriteRule ^/display/Grouper(/.*)?$ https://docs.grouper.internet2.edu/wiki/spaces/Grouper/overview [R=301,L,QSD]

# NOTE on obfuscated / short URLs (no static pageId to map):
#   * tiny link  /x/<id>       -> Confluence 302 -> /pages/tinyurl.action -> canonical /spaces/... -> (rule 1) 301 -> cloud
#   * /display/Grouper/<Title> -> Confluence 302 -> canonical /spaces/...                          -> (rule 1) 301 -> cloud
# They resolve via Confluence's own 302 to the canonical URL, which rule 1 then
# 301s to cloud. This chain works ONLY while Confluence/Tomcat is still running to
# perform that internal 302.
''')

    with open(os.path.join(HERE, "redirect_report.txt"), "w") as f:
        f.write(f"OLD pages: {len(old)}\nNEW pages: {len(new)}\n")
        f.write(f"1:1 matched (title): {len(matched)}\n")
        f.write(f"OLD with no new match (fall back to space home): {len(unmatched_old)}\n")
        f.write(f"NEW-only (exist in cloud, not old): {len(new_only)}\n")
        if new_dups:
            f.write(f"\nDUPLICATE TITLES in NEW (ambiguous, took first): {len(new_dups)}\n")
            for t in sorted(new_dups):
                f.write(f"  {t}\n")
        f.write("\n--- OLD pages with NO new match (will hit fallback) ---\n")
        for o in sorted(unmatched_old, key=lambda x: x["title"]):
            f.write(f"  {o['id']}  {o['title']}\n")

    print(f"old={len(old)} new={len(new)} matched={len(matched)} "
          f"unmatched_old={len(unmatched_old)} new_only={len(new_only)} "
          f"dup_titles={len(new_dups)}")


if __name__ == "__main__":
    main()
