#!/usr/bin/env bash
#
# test_redirects.sh -- verify the old-wiki -> Cloud-docs redirects.
#
# Runs a representative case for every rule in grouper_redirects.conf and prints
# PASS/FAIL. Re-run after each Apache config tweak. Read-only (HEAD/GET only).
#
# Usage:
#   ./test_redirects.sh                       # defaults to the dev host
#   ./test_redirects.sh spaces.at.internet2.edu   # test prod
#   ./test_redirects.sh https://spaces.dev.at.internet2.edu
#
# Notes:
#  * A browser User-Agent + Accept-Language is sent because the ELB/WAF in front
#    of these hosts returns 403 to bare bots/curl. This does NOT test Googlebot's
#    path -- see the reminder printed at the end.
#  * Only the FIRST hop is checked. A correct config 301s straight to docs; any
#    302 that stays on the source host is Confluence's own redirect (a FAIL here).

set -u

HOST="${1:-spaces.dev.at.internet2.edu}"
HOST="${HOST#http://}"; HOST="${HOST#https://}"; HOST="${HOST%/}"
BASE="https://$HOST"
DOCS="https://docs.grouper.internet2.edu/wiki"
OVERVIEW="$DOCS/spaces/Grouper/overview"
UA='Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'

# Each case: "<request path>|<expected 301 target>"
CASES=(
  # Rule 1 -- canonical page URL -> its specific new page (map lookup)
  "/spaces/Grouper/pages/14517693/Grouper+glossary|$DOCS/spaces/Grouper/pages/28541893/Grouper+glossary"
  "/spaces/Grouper/pages/14517699/Grouper+Web+Services|$DOCS/spaces/Grouper/pages/28544233/Grouper+Web+Services"
  "/spaces/Grouper/pages/14517677/Subject+sources|$DOCS/spaces/Grouper/pages/28544786/Subject+sources"
  # Rule 2 -- viewpage.action?pageId= -> its specific new page (direct)
  "/pages/viewpage.action?pageId=14517681|$DOCS/spaces/Grouper/pages/28541872/Contact+Information"
  # Rule 3 -- fallback -> new space home
  "/spaces/Grouper/overview|$OVERVIEW"
  "/spaces/Grouper/pages/93651007/Grouper+Duo+integration|$OVERVIEW"   # unmatched page
  "/display/Grouper/Grouper+glossary|$OVERVIEW"                        # legacy /display
)

echo "Testing redirects on: $BASE"
echo "-------------------------------------------------------------"
pass=0; fail=0
for entry in "${CASES[@]}"; do
  path="${entry%%|*}"
  want="${entry#*|}"
  read -r code loc < <(curl -s -A "$UA" -H 'Accept-Language: en-US,en;q=0.9' \
      -o /dev/null -w '%{http_code} %{redirect_url}' "$BASE$path"; echo)
  if [ "$code" = "301" ] && [ "$loc" = "$want" ]; then
    echo "PASS  $path"
    pass=$((pass+1))
  else
    echo "FAIL  $path"
    echo "        got : $code ${loc:-<no redirect>}"
    echo "        want: 301 $want"
    fail=$((fail+1))
  fi
done
echo "-------------------------------------------------------------"
echo "PASS=$pass  FAIL=$fail"
echo
echo "Reminder: this uses a browser User-Agent to get past the WAF. Even when all"
echo "cases PASS, confirm the WAF also lets GOOGLEBOT reach these 301s (or that the"
echo "redirect runs at the ELB/CloudFront layer) -- otherwise Google never sees them."

[ "$fail" -eq 0 ]
