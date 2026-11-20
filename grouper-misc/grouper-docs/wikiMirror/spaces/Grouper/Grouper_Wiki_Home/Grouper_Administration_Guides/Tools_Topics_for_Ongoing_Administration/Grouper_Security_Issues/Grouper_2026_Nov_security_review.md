---
title: "Grouper 2026-Nov security review"
space: Grouper
pageId: 392593432
version: 5
lastUpdated: 2026-11-18T20:17:59.146Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/392593432/Grouper+2026-Nov+security+review
---

# Grouper 2026-Nov security review

*Disclosed 2026/11/18. Fixed containers v7.9.1, v6.5.1 and v4.26.1. Temporary patches are available for releases that cannot upgrade yet.*

> **Short version:** A security review of Grouper in October and November 2026 found and fixed issues across the UI, web services, rules, GSH templates, expressions, provisioning and the MCP server. They are grouped below into 19 advisories (3 critical, 8 high, 7 medium, 1 low) plus MCP. **Upgrade to v7.9.1, v6.5.1 or v4.26.1**, and read the upgrade instructions first: six of the fixes change behavior that some sites rely on.

## Timeline

- 2026/10 - 2026/11 security review of Grouper v7, with each fix applied to every supported release
- 2026/11/16 patches for each release shared with the Grouper community under embargo
- 2026/11/18 fixed container v7.9.1 released (see [v7 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549113/v7+Release+Notes))
- 2026/11/18 fixed containers v6.5.1 and v4.26.1 (see [v6 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547614/v6+Release+Notes) and [v4 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes))
- 2026/11/18 public disclosure (this page)

## Affected versions

All Grouper versions before v7.9.1, v6.5.1 and v4.26.1 are affected by some of these advisories. Older releases have fewer features, so some advisories do not apply to them. The MCP advisory applies only to v7 (MCP is new in v7.0.0).

| Release line | Fixed in |
| --- | --- |
| v7 | v7.9.1 |
| v6 | v6.5.1 (no MCP) |
| v4 | v4.26.1 (no MCP) |
| v5, v2.6, v2.5 | Out of support: upgrade. Patches are available for v5.13.5, v2.6.19 and v2.5.68 (see below). |

## Advisories

Severity: **Critical** means someone with no special role (an outsider, an outsider plus a click from a signed-in user, or any user who can sign in) could get full Grouper admin, read any data, or run code, in a feature that is on by default. **High** means a delegated role (for example a group or folder admin) could get full Grouper admin, any signed-in user could get real extra access or data, or it would be critical but is in a rarely used optional feature. **Medium** means a delegated role could reach beyond what it should, or an optional feature or site setting is needed first. **Low** covers audit gaps, hardening and small disclosures.

| Advisory | Severity | Area | Summary |
| --- | --- | --- | --- |
| C1 | Critical | UI actions run from a link, and other cross-site requests | The UI could run an action named in a URL opened from outside (for example a link in an email), with the signed-in user's session. The WS ran writes sent with HTTP GET, JSONP accepted any callback, and the UI OIDC callback was not tied to the browser that started the login. |
| C2 | Critical | Query injection through sort fields and lookups | The WS sortString parameter was put into the database query as text, so a crafted value could read data the caller should not see. Two harder-to-reach spots (configuration history filter, custom composite lookups) had the same issue. |
| C3 | Critical | GSH template file uploads and downloads | Template forms saved an uploaded file before checking the user could run the template, and used the uploaded file name as given. Download links could serve a file replaced by a later run. |
| H1 | High | Workflow | Group admins and workflow editors could build a workflow whose approval adds people to groups they cannot update, and workflow form HTML could carry script. Only sites with workflow turned on. |
| H2 | High | Expressions and scripted groups run with wheel (sysadmin) rights | Expressions that delegated users can write (rules, permission limits, external subject descriptions, UI labels, scripted groups) could reach Java classes and run code with wheel rights. |
| H3 | High | Rules written by delegated users | Some rule features trusted the delegated user who saved the rule too much, for example send-email rule fields could call Grouper helper methods that ran with wheel rights. |
| H4 | High | GSH templates and daemons outside their security settings | Some template paths ran template code before checking who was allowed, delegated daemon admins could edit daemons that carry code, and a disabled compiled hook still ran. |
| H5 | High | Subject and entity data beyond privacy settings | Several screens and lookups showed subject and entity data past the settings that protect it (privacy realms, requireGroupNameForView, subject customizers). |
| H6 | High | Output escaping (cross-site scripting) | Some screens showed stored or request values without escaping them, so a value could carry script that ran in the viewer's browser. |
| H7 | High | Authentication and admission (WS OAuth, JWT, OIDC, act-as) | WS OAuth accepted access tokens issued for other applications, plus smaller gaps in JWT, act-as, VOOT, UI OIDC routes and the optional WS LDAP password check (blank passwords). Mostly optional features. |
| H8 | High | Messaging and the ESB HTTP listener | The optional ESB HTTP listener accepted membership changes without authentication, and WS messaging operations could use Grouper's own broker credentials. Only sites that run these features. |
| M1 | Medium | Provisioning screens, metadata and logs | Delegated provisioner viewers could see or change more than intended, credentials could appear in logs, and some external system secrets were stored in clear. |
| M2 | Medium | Permissions, roles and attribute definitions | Some role, permission and attribute definition changes needed less privilege than the UI implies, and bulk WS queries showed privilege holders to callers with only READ. |
| M3 | Medium | Group and folder administration | A few group and folder features let a delegated admin go past their privileges, and membership requirements were not applied to subfolders. |
| M4 | Medium | SCIM | SCIM membership deletes needed less than UPDATE, responses skipped the subject customizer, and conflict recovery could link to an account someone else created. |
| M5 | Medium | Attestation | Group updaters could change attestation settings meant for admins, and viewing attestation wrote to the database. |
| M6 | Medium | Deprovisioning | The deprovisioning screen and reports acted on whatever ids the request sent, not only the rows they showed. |
| M7 | Medium | WS request handling and external subjects | WS REST had no request size limit, and external subject operations could run without the right to edit external subjects. |
| L1 | Low | Visibility of folders, favorites and counts | The folder tree, favorites, recent activity, visualization counts and WS hasMember could show names, existence or counts the user should not see. |
| MCP | Medium | MCP server (new in v7) | Several MCP scope checks (attribute assignments, subject sources, institutional tools, renamed objects, replace-all memberships) were incomplete. None lets a user exceed their own Grouper privileges. |

The fixes are tracked by category in [GRP-7572](https://grouper.atlassian.net/browse/GRP-7572), [GRP-7573](https://grouper.atlassian.net/browse/GRP-7573), [GRP-7574](https://grouper.atlassian.net/browse/GRP-7574), [GRP-7575](https://grouper.atlassian.net/browse/GRP-7575), [GRP-7576](https://grouper.atlassian.net/browse/GRP-7576), [GRP-7577](https://grouper.atlassian.net/browse/GRP-7577), [GRP-7578](https://grouper.atlassian.net/browse/GRP-7578), [GRP-7579](https://grouper.atlassian.net/browse/GRP-7579), [GRP-7580](https://grouper.atlassian.net/browse/GRP-7580), [GRP-7581](https://grouper.atlassian.net/browse/GRP-7581), [GRP-7582](https://grouper.atlassian.net/browse/GRP-7582), [GRP-7583](https://grouper.atlassian.net/browse/GRP-7583), [GRP-7584](https://grouper.atlassian.net/browse/GRP-7584), [GRP-7585](https://grouper.atlassian.net/browse/GRP-7585) ([all 7.9.1 Jiras](https://grouper.atlassian.net/issues/?jql=project%20%3D%20GRP%20AND%20fixVersion%20%3D%207.9.1)).

## Patches for releases you cannot upgrade yet

If you cannot upgrade right away, a security patch is available for your release. Each patch has the same fixes as the fixed containers for that release (MCP fixes excepted, see below), as whole-file drop-ins.

Patches are built for these releases: v7.9.0, v7.8.0, v7.7.0, v7.5.1, v7.5.0, v7.2.1, v6.5.0, v6.4.1, v6.3.0, v6.2.0, v6.1.0, v5.13.5, v4.26.0, v4.25.0, v4.24.0, v4.22.0, v4.21.2, v4.21.1, v4.18.2, v4.17.7, v4.17.3, v4.16.0, v2.6.19 and v2.5.68. For another release, ask and one can be built.

Each patch contains:

- a README with the required steps and install instructions, and a manifest describing each advisory as it applies to that release
- a container patch (compiled classes and changed files to copy into the webapp) and a source patch with the tests
- scripts to check the files before installing and to verify the patch is installed

To get the patch for your version, Slack Chris Hyzer (mchyzer in the Internet2 Slack) with your Grouper version.

**Patches are a temporary solution.** We will not be able to provide patches for future security issues on top of these patches: future fixes will be released only in new Grouper versions. Plan to upgrade soon. Once patched, upgrade only to a release that includes these fixes (v7.9.1, v6.5.1, v4.26.1 or later), since any other version brings the issues back.

The MCP fixes are only in v7.9.1, not in patches. Until you upgrade: with MCP off (the default) there is nothing to do; with MCP on for a few trusted staff it can stay on; with MCP open to a wide or untrusted audience, turn it off.

## What you should do

1. Read the upgrade instructions for your release: [v7 Upgrade instructions from v7](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549096/v7+Upgrade+instructions+from+v7) (v7.9.1), [v6 Upgrade instructions from v6](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549643/v6+Upgrade+instructions+from+v6) (v6.5.1) or [v4 Upgrade instructions from v4](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549792/v4+Upgrade+instructions+from+v4) (v4.26.1; no WS OAuth step). In short: workflow and external subjects are now off by default; WS writes sent with HTTP GET are refused; WS OAuth needs `useForWs` and `wsAccessTokenValidation`; the ESB HTTP listener needs a Basic auth realm file; send-email rules beyond the predefined variables are sysadmin only; and membership requirements now apply to subfolders.
2. Upgrade to v7.9.1, v6.5.1 or v4.26.1. If you cannot upgrade yet, install the patch for your release (see above), then upgrade.
3. If you run the MCP server (`grouper.is.mcp`) for a wide or untrusted audience and cannot upgrade soon, turn it off until you upgrade.

See [Grouper Security Issues](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544332/Grouper+Security+Issues) for the full list of Grouper security advisories.
