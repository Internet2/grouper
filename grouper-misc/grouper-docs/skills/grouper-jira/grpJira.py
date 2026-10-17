#!/usr/bin/env python3
"""
Command line tool for the Grouper GRP Jira project (Jira Cloud at
grouper.atlassian.net, also shown as todos.grouper.internet2.edu).

Create, read, edit, comment on, assign, transition, and resolve GRP issues via
the Jira Cloud REST API v3 with an Atlassian API token.  Standard library only.

Credentials (never on the command line, never printed):
  ATLASSIAN_EMAIL and ATLASSIAN_API_TOKEN from the environment, or else read
  from the env file at $GRP_JIRA_ENV_FILE (default ~/.secrets/grouper_confluence.env),
  which holds lines like:
    ATLASSIAN_EMAIL=you@example.edu
    ATLASSIAN_API_TOKEN=xxxxxxxx

Descriptions and comments are plain text: blank lines separate paragraphs, and
they are converted to ADF (Atlassian Document Format) for you.  Pass text inline
or with --file (use - for stdin).

Examples:
  grpJira.py myself
  grpJira.py create --type improvement --summary "Dropbox provisioner: add persistentId" --file desc.txt
  grpJira.py get GRP-1234
  grpJira.py comment GRP-1234 "Fixed in v7"
  grpJira.py edit GRP-1234 --summary "New summary" --file newDesc.txt
  grpJira.py assign GRP-1234 --me
  grpJira.py assign GRP-1234 --email someone@example.edu
  grpJira.py transitions GRP-1234
  grpJira.py transition GRP-1234 "In Progress"
  grpJira.py resolve GRP-1234
"""

import argparse
import base64
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

API_BASE = "https://grouper.atlassian.net/rest/api/3"
DISPLAY_BASE = "https://todos.grouper.internet2.edu/browse/"
DEFAULT_ENV_FILE = "~/.secrets/grouper_confluence.env"

# GRP issue type ids (re-check with GET /project/GRP if creates start failing)
ISSUE_TYPES = {
    "improvement": "10044",
    "task": "10045",
    "subtask": "10046",
    "newfeature": "10047",
    "bug": "10048",
    "epic": "10000",
    "documentation": "10119",
}

# GRP "Resolved" transition. The resolution field is not on its screen, so it is
# sent with no fields (sending a resolution returns HTTP 400).
RESOLVE_TRANSITION_ID = "71"


# ---------------------------------------------------------------------------
# Credentials and HTTP
# ---------------------------------------------------------------------------

def load_credentials():
    """Return (email, token) from the environment or the env file."""
    email = os.environ.get("ATLASSIAN_EMAIL")
    token = os.environ.get("ATLASSIAN_API_TOKEN")
    if email and token:
        return email, token

    env_file = os.path.expanduser(os.environ.get("GRP_JIRA_ENV_FILE", DEFAULT_ENV_FILE))
    if not os.path.exists(env_file):
        sys.exit(f"Set ATLASSIAN_EMAIL and ATLASSIAN_API_TOKEN, or create {env_file}")

    # simple KEY=value parser; ignores comments, blank lines, and an optional "export "
    values = {}
    with open(env_file) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            if line.startswith("export "):
                line = line[len("export "):]
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip().strip("'\"")

    email = email or values.get("ATLASSIAN_EMAIL")
    token = token or values.get("ATLASSIAN_API_TOKEN")
    if not email or not token:
        sys.exit(f"ATLASSIAN_EMAIL / ATLASSIAN_API_TOKEN missing from {env_file}")
    return email, token


def call(method, path, body=None, expect_json=True):
    """Call the Jira REST API and return parsed JSON (or None for empty bodies)."""
    email, token = load_credentials()
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(API_BASE + path, data=data, method=method)
    req.add_header("Authorization", "Basic " + base64.b64encode(f"{email}:{token}".encode()).decode())
    req.add_header("Accept", "application/json")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            raw = resp.read()
    except urllib.error.HTTPError as e:
        # Jira error bodies explain what was wrong (bad field, missing permission, ...)
        sys.exit(f"HTTP {e.code} from {method} {path}: {e.read().decode('utf-8', 'replace')}")
    if not raw or not expect_json:
        return None
    return json.loads(raw)


# ---------------------------------------------------------------------------
# ADF helpers
# ---------------------------------------------------------------------------

def to_adf(text):
    """Plain text -> ADF doc; blank lines split paragraphs, single newlines become hard breaks."""
    paragraphs = []
    for block in text.strip().split("\n\n"):
        block = block.strip("\n")
        if not block.strip():
            continue
        content = []
        for i, line in enumerate(block.split("\n")):
            if i > 0:
                content.append({"type": "hardBreak"})
            if line:
                content.append({"type": "text", "text": line})
        paragraphs.append({"type": "paragraph", "content": content})
    return {"type": "doc", "version": 1, "content": paragraphs}


def from_adf(node):
    """ADF doc -> readable plain text (enough for display, not a full renderer)."""
    if node is None:
        return ""
    if isinstance(node, list):
        return "".join(from_adf(n) for n in node)
    kind = node.get("type")
    if kind == "text":
        return node.get("text", "")
    if kind == "hardBreak":
        return "\n"
    inner = from_adf(node.get("content", []))
    if kind in ("paragraph", "heading", "codeBlock", "blockquote"):
        return inner + "\n\n"
    if kind == "listItem":
        return "* " + inner.strip() + "\n"
    return inner


def read_text(inline, file_arg):
    """Text from an inline argument or a file (- for stdin)."""
    if file_arg:
        if file_arg == "-":
            return sys.stdin.read()
        with open(os.path.expanduser(file_arg)) as f:
            return f.read()
    return inline


def account_id_for(me, email):
    """Resolve --me or --email to a Jira accountId."""
    if me:
        return call("GET", "/myself")["accountId"]
    users = call("GET", "/user/search?query=" + urllib.parse.quote(email))
    if not users:
        sys.exit(f"No Jira user found for {email}")
    return users[0]["accountId"]


# ---------------------------------------------------------------------------
# Commands
# ---------------------------------------------------------------------------

def cmd_myself(args):
    me = call("GET", "/myself")
    print(f"{me.get('displayName')}  {me.get('emailAddress', '')}  accountId={me.get('accountId')}")


def cmd_create(args):
    type_key = args.type.lower().replace("-", "").replace(" ", "")
    if type_key not in ISSUE_TYPES:
        sys.exit("Unknown type; use one of: " + ", ".join(sorted(ISSUE_TYPES)))
    fields = {
        "project": {"key": "GRP"},
        "issuetype": {"id": ISSUE_TYPES[type_key]},
        "summary": args.summary,
    }
    text = read_text(args.description, args.file)
    if text:
        fields["description"] = to_adf(text)
    # GRP convention: issues you create are assigned to you unless told otherwise
    if not args.unassigned:
        fields["assignee"] = {"accountId": account_id_for(True, None)}
    created = call("POST", "/issue", {"fields": fields})
    key = created["key"]
    print(f"{key}  {DISPLAY_BASE}{key}")
    if args.resolve:
        call("POST", f"/issue/{key}/transitions", {"transition": {"id": RESOLVE_TRANSITION_ID}}, expect_json=False)
        print(f"{key} resolved")


def cmd_get(args):
    issue = call("GET", f"/issue/{args.key}?fields=summary,status,resolution,assignee,issuetype,description,fixVersions")
    f = issue["fields"]
    print(f"{issue['key']}  {DISPLAY_BASE}{issue['key']}")
    print(f"Summary:    {f.get('summary')}")
    print(f"Type:       {(f.get('issuetype') or {}).get('name')}")
    print(f"Status:     {(f.get('status') or {}).get('name')}")
    print(f"Resolution: {(f.get('resolution') or {}).get('name', 'none')}")
    print(f"Assignee:   {(f.get('assignee') or {}).get('displayName', 'unassigned')}")
    versions = ", ".join(v.get("name", "") for v in f.get("fixVersions") or [])
    print(f"Fix versions: {versions or 'none'}")
    print()
    print(from_adf(f.get("description")).strip() or "(no description)")
    if args.comments:
        comments = call("GET", f"/issue/{args.key}/comment").get("comments", [])
        for c in comments:
            print(f"\n--- {c.get('author', {}).get('displayName')} {c.get('created', '')[:16]}")
            print(from_adf(c.get("body")).strip())


def cmd_edit(args):
    fields = {}
    if args.summary:
        fields["summary"] = args.summary
    text = read_text(args.description, args.file)
    if text:
        fields["description"] = to_adf(text)
    if not fields:
        sys.exit("Nothing to edit; pass --summary and/or --description/--file")
    call("PUT", f"/issue/{args.key}", {"fields": fields}, expect_json=False)
    print(f"{args.key} updated: {', '.join(fields)}")


def cmd_comment(args):
    text = read_text(args.text, args.file)
    if not text:
        sys.exit("Comment text required (inline or --file)")
    call("POST", f"/issue/{args.key}/comment", {"body": to_adf(text)})
    print(f"{args.key} comment added")


def cmd_assign(args):
    if not args.me and not args.email:
        sys.exit("Pass --me or --email")
    account_id = account_id_for(args.me, args.email)
    call("PUT", f"/issue/{args.key}/assignee", {"accountId": account_id}, expect_json=False)
    print(f"{args.key} assigned")


def cmd_transitions(args):
    for t in call("GET", f"/issue/{args.key}/transitions").get("transitions", []):
        print(f"  {t['id']:>4}  {t['name']}  -> {t.get('to', {}).get('name', '')}")


def cmd_transition(args):
    # accept a transition id or a (case-insensitive) transition name
    target = args.transition
    if not target.isdigit():
        available = call("GET", f"/issue/{args.key}/transitions").get("transitions", [])
        match = [t for t in available if t["name"].lower() == target.lower()]
        if not match:
            sys.exit(f"No transition named {target}; available: " + ", ".join(t["name"] for t in available))
        target = match[0]["id"]
    call("POST", f"/issue/{args.key}/transitions", {"transition": {"id": target}}, expect_json=False)
    print(f"{args.key} transitioned ({target})")


def cmd_resolve(args):
    if args.comment:
        call("POST", f"/issue/{args.key}/comment", {"body": to_adf(args.comment)})
    call("POST", f"/issue/{args.key}/transitions", {"transition": {"id": RESOLVE_TRANSITION_ID}}, expect_json=False)
    print(f"{args.key} resolved")


def main():
    parser = argparse.ArgumentParser(description="Grouper GRP Jira command line tool")
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("myself", help="show the token owner").set_defaults(func=cmd_myself)

    p = sub.add_parser("create", help="create a GRP issue (assigned to you)")
    p.add_argument("--type", default="improvement", help="improvement (default), bug, task, newfeature, documentation, ...")
    p.add_argument("--summary", required=True)
    p.add_argument("--description", help="plain text; blank lines separate paragraphs")
    p.add_argument("--file", help="read the description from a file (- for stdin)")
    p.add_argument("--unassigned", action="store_true", help="do not assign to yourself")
    p.add_argument("--resolve", action="store_true", help="resolve right after creating")
    p.set_defaults(func=cmd_create)

    p = sub.add_parser("get", help="show an issue")
    p.add_argument("key")
    p.add_argument("--comments", action="store_true", help="also show comments")
    p.set_defaults(func=cmd_get)

    p = sub.add_parser("edit", help="change summary and/or description")
    p.add_argument("key")
    p.add_argument("--summary")
    p.add_argument("--description")
    p.add_argument("--file", help="read the new description from a file (- for stdin)")
    p.set_defaults(func=cmd_edit)

    p = sub.add_parser("comment", help="add a comment")
    p.add_argument("key")
    p.add_argument("text", nargs="?")
    p.add_argument("--file", help="read the comment from a file (- for stdin)")
    p.set_defaults(func=cmd_comment)

    p = sub.add_parser("assign", help="assign to yourself or a user by email")
    p.add_argument("key")
    p.add_argument("--me", action="store_true")
    p.add_argument("--email")
    p.set_defaults(func=cmd_assign)

    p = sub.add_parser("transitions", help="list available transitions")
    p.add_argument("key")
    p.set_defaults(func=cmd_transitions)

    p = sub.add_parser("transition", help="move to a status by transition id or name")
    p.add_argument("key")
    p.add_argument("transition")
    p.set_defaults(func=cmd_transition)

    p = sub.add_parser("resolve", help="resolve an issue")
    p.add_argument("key")
    p.add_argument("--comment", help="optional comment to add first")
    p.set_defaults(func=cmd_resolve)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
