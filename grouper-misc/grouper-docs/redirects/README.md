# Old wiki -> Cloud docs redirects (one-time migration)

One-time artifacts to redirect the OLD on-prem Grouper wiki
(`spaces.at.internet2.edu`, Confluence Server 9.x) to the NEW Cloud docs site
(`docs.grouper.internet2.edu`). The old wiki is frozen (its content was migrated
to Cloud), so this map is built once and handed to the Internet2 infra team to
deploy. It is NOT a per-release task -- contrast with the sitemap in
`../wikiMirror/`, which is refreshed every release.

## Why

The old space is still indexed by Google and duplicates the new docs site. A 301
redirect takes the old URLs out of Google, forwards users/bookmarks to the right
new page, and consolidates ranking signals onto the new site -- while the old
server stays up.

## Files

- `build_redirects.py` -- generator. Lists both wikis' Grouper-space pages and
  matches them by title: old numeric pageId -> new Cloud URL. Read-only against
  both servers; changes nothing. Needs `ATLASSIAN_EMAIL` / `ATLASSIAN_API_TOKEN`
  (for the Cloud API; the old wiki is read anonymously with a browser UA).
- `grouper_moved.map` -- Apache RewriteMap: `<oldPageId> <newCloudUrl>`, 988
  entries, sorted by id (idempotent).
- `grouper_redirects.conf` -- Apache `mod_rewrite` rules that use the map.
- `redirect_report.txt` -- match summary: 988 matched 1:1, 9 old pages
  renamed/dropped in migration (fall back to the space home), 21 new-only.

## Regenerate (only if the old wiki changed, which it should not)

    export ATLASSIAN_EMAIL='you@example.edu'
    export ATLASSIAN_API_TOKEN='...'
    python3 build_redirects.py

## Deploy (Internet2 infra team)

See the header comments in `grouper_redirects.conf`. Two must-dos:

1. An AWS ELB/WAF in front of Apache currently returns 403 to Googlebot. A 301
   that Googlebot never receives will NOT deindex the old pages -- either allow
   Googlebot through the WAF, or implement the redirects at the ELB/CloudFront
   layer.
2. Place the rules in the vhost BEFORE the ProxyPass to Confluence/Tomcat, or the
   requests get proxied and never redirect.

Verify one page after deploy:

    curl -s -o /dev/null -w '%{http_code} -> %{redirect_url}\n' \
      'https://spaces.at.internet2.edu/spaces/Grouper/pages/14517693/Grouper+glossary'
    # expect: 301 -> https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28541893/Grouper+glossary

## Alternate / obfuscated old URL forms

All resolve to Cloud; see the NOTE block at the end of `grouper_redirects.conf`.
Canonical `/spaces/.../pages/<id>` and `/pages/viewpage.action?pageId=<id>` are
direct single 301s; tiny links `/x/<id>` and `/display/Grouper/<Title>` ride
Confluence's own 302 to the canonical URL first (works while Tomcat is running).
