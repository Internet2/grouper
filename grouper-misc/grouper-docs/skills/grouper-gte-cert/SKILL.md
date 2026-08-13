---
name: grouper-gte-cert
description: |
  Fix the Grouper Training Environment (GTE) container's self-signed TLS certificate so
  https://localhost:8443 will load in Claude's internal browser pane (preview_start /
  navigate). Use this skill whenever the agent browser pane cannot open the GTE Grouper UI:
  navigation returns "denied or failed", the pane renders blank, get_page_text reports
  "URL: (non-http)", or a cert error like ERR_CERT_COMMON_NAME_INVALID appears. Also use it
  proactively right after a GTE container is created, recreated, or restarted for a new
  training day, since every fresh container reverts to the broken baked-in certificate.
  Trigger on phrases like "fix SSL again", "cert is broken", "browser pane won't load
  grouper", "new GTE container", "localhost:8443 won't open".
---

# GTE certificate fix (for the Claude browser pane)

The GTE container ships a self-signed cert that Chromium-based browsers reject outright.
Claude's internal browser pane is Chromium-based and has no "Proceed anyway" button, so
until the cert is replaced the pane cannot reach the Grouper UI at all.

Note the scope: human trainees in Chrome/Edge/Firefox do NOT need this. They get a
bypassable interstitial (Advanced -> Proceed) and can ignore the whole procedure. This
skill exists because the agent pane cannot click that button.

## Why the shipped cert fails

base/Dockerfile in the grouper_training repo generates the httpd cert with:

    -subj "/commonName=localhost.localdomain"

and no `-addext subjectAltName=...`. Chromium has required subjectAltName since Chrome 58
and ignores commonName entirely for hostname matching. So the cert is not merely untrusted,
it is invalid for every hostname. Trusting it in the OS keystore does NOT help; the cert
must be regenerated with a correct CN and a SAN.

## Step 0: confirm the container is actually up

A dead container looks like a cert problem. Check first:

```bash
echo | openssl s_client -connect localhost:8443 -servername localhost 2>/dev/null | openssl x509 -noout -subject -dates
```

- No output at all means nothing is listening. Start the container (`./gte <lessonId>` in
  the grouper_training repo) before doing anything else.
- `CN=localhost` with a SAN means the cert is already fixed; skip to Step 3 and verify trust.
- `CN=localhost.localdomain` means continue to Step 1.

Check the SAN explicitly, since the CN alone does not tell you the cert is usable:

```bash
echo | openssl s_client -connect localhost:8443 -servername localhost 2>/dev/null | openssl x509 -noout -text | grep -A2 "Subject Alternative Name"
```

## Step 1: regenerate the cert INSIDE the container

This is the step people get wrong. GTE usually runs in Docker on a remote VM, reached
through an SSH tunnel, so `localhost:8443` on the workstation is not the same machine as
the shell you are typing in. Signs you are on the VM host rather than in the container:
the prompt reads `student@ip-...` instead of `root@<hash>`, and `httpd` is not found.

Open a container shell using the repo's helper (it resolves the `tier/gte` container id
for you):

```bash
cd ~/git/grouper_training && ./gte-shell
```

Confirm the prompt changed to `root@<container-hash>`, then run:

```bash
openssl req -x509 -nodes -newkey rsa:2048 -days 1825 -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1' -keyout /etc/pki/tls/private/localhost.key -out /etc/pki/tls/certs/localhost.crt && httpd -k graceful
```

Two details that matter:

- Use `httpd -k graceful`, NOT `apachectl graceful`. On this RHEL-family image apachectl is
  a systemd wrapper, and the container has no systemd (PID 1 is not systemd), so it fails
  with "System has not been booted with systemd as init system". The cert regenerates fine
  but the running httpd keeps serving the old one, which looks like the fix did not work.
- No `sudo` inside the container; you are already root. If you find yourself typing sudo,
  you are probably on the VM host and about to overwrite the host's own
  /etc/pki/tls/certs/localhost.crt, which does nothing useful.

## Step 2: trust the new cert on the workstation

The pane consults the OS trust store of the machine Claude runs on, not the VM.

### macOS

If a previous GTE cert is already trusted, remove it first. The new cert shares its
`CN=localhost` subject but has a different key, and leaving both installed causes
duplicate-subject confusion:

```bash
security find-certificate -a -c localhost -Z ~/Library/Keychains/login.keychain-db | grep "SHA-1 hash"
```

```bash
security delete-certificate -Z <SHA-1-hash-from-above> ~/Library/Keychains/login.keychain-db
```

Export the new cert, then trust it (the add prompts for the user's password):

```bash
openssl s_client -connect localhost:8443 -servername localhost </dev/null 2>/dev/null | openssl x509 -out /tmp/gte.crt
```

```bash
security add-trusted-cert -r trustRoot -k ~/Library/Keychains/login.keychain-db /tmp/gte.crt
```

### Windows

The container-side work in Step 1 is identical; only the trust step differs. Claude's pane
is Chromium-based and Chromium on Windows reads the Windows certificate store, so importing
into the CurrentUser root store should be sufficient. This has not been verified on a
Windows install, so confirm once and correct this section if it behaves differently.

Getting the .crt file: Windows has no openssl by default. Git Bash ships one, so the
`openssl s_client` command above works there. Otherwise export the certificate from a
browser's certificate viewer.

Import into the per-user root store (no administrator rights required; a security warning
dialog appears and must be accepted):

```bash
certutil -addstore -user Root gte.crt
```

PowerShell equivalent:

```bash
Import-Certificate -FilePath gte.crt -CertStoreLocation Cert:\CurrentUser\Root
```

Restart the Claude app afterward; Chromium caches roots at startup.

## Step 3: verify before declaring success

Check the cert and the trust separately, because they fail independently:

```bash
echo | openssl s_client -connect localhost:8443 -servername localhost 2>/dev/null | openssl x509 -noout -subject -text | grep -E "Subject:|DNS:"
```

Expect `CN=localhost` and `DNS:localhost, IP Address:127.0.0.1`.

```bash
curl -s -o /dev/null -w "%{http_code}\n" https://localhost:8443/grouper
```

Note the absence of `-k`. A 302 means trust is working (the redirect goes to the Shibboleth
IdP login). 000 means the cert is still rejected. On macOS, curl consults the same keychain
the pane does, which makes this a reliable proxy.

Finally open the pane and confirm the login page renders:

    preview_start with url https://localhost:8443/grouper
    get_page_text

A working result shows the Shibboleth login form titled "InCommon Trusted Access Platform -
Grouper Training Environment". If the pane still reports "(non-http)" or navigation
"denied or failed" while curl returns 302, the app most likely needs a restart to pick up
the new root.

## This recurs on every fresh container

The fix lives in the container filesystem, so recreating the container reverts it. A fresh
container also means a fresh Grouper database (lesson work is gone) and a disconnected
grouper-gte-local MCP server that needs re-authorizing.

The permanent fix is one line in base/Dockerfile of the grouper_training repo: change the
subject to `/CN=localhost` and add
`-addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'` to the `openssl req` command. Tracked
upstream as GRP-7239. Until that ships in a rebuilt image, expect to repeat Steps 1 and 2
each time.
