---
name: security-review
description: Review staged/uncommitted Grouper changes for privilege and access-control security issues before committing
argument-hint: "[optional: path or area to focus on]"
allowed-tools:
  - read
  - grep
  - glob
  - exec
permissions:
  allow:
    - Exec(git diff)
    - Exec(git status)
    - Exec(git log)
---

You are performing a **security-focused code review of Grouper changes before they are committed**. Your single most important job is to make sure that **privileges are enforced tightly on the server/API side** so that users who lack the correct privilege cannot view or manage groups, stems, entities, attribute definitions, memberships, or any other objects.

The golden rule: **hiding a button, link, tab, or menu item in the UI is NOT security.** UI visibility only affects convenience. Real security must be enforced in the API/service layer, which is reachable directly via the UI backing beans, Web Services (WS/REST/SCIM), GSH, and the public Java API. Treat any change that gates access ONLY in a JSP, a UI container `canXxx()` getter, or client-side JavaScript as a potential vulnerability unless the underlying API also enforces the privilege.

Two more principles that carry equal weight:

- **A read/view role must never be accepted where a manage/write role is required.** Any feature that has separate "reader/viewer" and "editor/manager" (or "read" vs "admin/update") roles must gate *mutations* on the manage role, not merely on membership in the reader group or on the ability to view/reach the screen. "Can see it" or "can open the editor UI" must not imply "can change it."
- **Config that executes with elevated privileges is a privilege-escalation surface.** When a feature lets a user define something that later *runs as GrouperSystem/root* or otherwise with more power than the user has (rules, hooks, loader/SQL/LDAP jobs, GSH templates, scripts, workflows, provisioning logic, EL expressions, email/notification bodies), the ability to create or edit that config is effectively the ability to act with those elevated privileges. Such create/edit paths must be gated at the STRICTEST level and fail closed — reaching the editor is not authorization to save.
- **GET / read operations must have NO side effects.** Viewing, searching, browsing, loading a screen, autocomplete, "view" links, tooltips, and any HTTP GET must never mutate state — no creating/updating/deleting rows, no adding memberships, no privilege changes, no writes triggered as a side effect of rendering. Read paths are the least-guarded (a user only needs VIEW/READ, links get prefetched/crawled, GETs are retried and cached), so a mutation hidden in a read path both corrupts data and lets a low-privilege viewer trigger writes they were never authorized to make. Mutations belong only in explicit POST/action operations with their own privilege check. (Example: GRP-7392.)

## Step 1 — Gather the changes

1. Run `git status` and `git diff` (and `git diff --staged`). Review both staged and unstaged changes.
2. If the user named a path/area, focus there but still scan related enforcement paths.
3. Read the full changed files (not just the diff hunks) when you need context to judge enforcement — a diff can hide that a privilege check was removed elsewhere.

## Step 2 — Understand Grouper's privilege model

Grouper privileges are the security backbone. Know these before reviewing:

- **Access privileges (groups)** — `AccessPrivilege`: `VIEW`, `READ`, `UPDATE`, `ADMIN`, `OPTIN`, `OPTOUT`, `GROUP_ATTR_READ`, `GROUP_ATTR_UPDATE`. VIEW is required just to see a group exists; READ to see memberships; UPDATE to change memberships; ADMIN for full control.
- **Naming privileges (stems/folders)** — `NamingPrivilege`: `STEM`, `CREATE`, `STEM_ADMIN`, `STEM_ATTR_READ`, `STEM_ATTR_UPDATE`, and view. Folder privileges control creating/managing child objects and are inherited.
- **Attribute def privileges** — `AttributeDefPrivilege`: `ATTR_VIEW`, `ATTR_READ`, `ATTR_UPDATE`, `ATTR_ADMIN`, `ATTR_OPTIN`, `ATTR_OPTOUT`.
- **Enforcement helpers**:
  - `PrivilegeHelper.dispatch(session, group|stem|attributeDef, subject, privilege)` throws `InsufficientPrivilegeException` when the subject lacks the privilege. This is the canonical server-side gate.
  - `group.hasView/hasRead/hasUpdate/hasAdmin/hasOptin/hasOptout(subject)`, `stem.hasStem/hasCreate/hasStemAdmin(subject)` return booleans — fine for computing UI state, but a boolean check that is only used to hide UI and never enforced server-side is NOT security.
  - `PrivilegeHelper.isWheelOrRoot(subject)` and wheel/root variants — used to grant elevated access. Adding new bypasses here is high-risk.
- **Secure vs. insecure access — CRITICAL**:
  - Finders/queries that take a `GrouperSession` filter results by the acting subject's privileges (secure). Removing that filtering, or widening it, can leak objects.
  - **Root/system escalation** — `GrouperSession.startRootSession()`, `session.internal_getRootSession()`, and any `...AsGrouperSystem(...)` method (e.g. `GroupFinder.findByNameAsGrouperSystem`) **bypass all privilege checks**. These must only be used for genuinely internal operations, never to serve data or perform actions on behalf of an end user. Flag every new use of a root session or `AsGrouperSystem`/`asGrouperSystem` call that sits in a user-request code path.
  - `GrouperSession.callbackGrouperSession(...)` with a root session has the same escalation risk.

## Step 3 — Review checklist

For every change, ask:

1. **Is there a new or changed code path that reads or mutates an object (group/stem/entity/member/attribute/privilege/config)?** If so, is there a corresponding privilege check on the SAME server-side path, using the acting subject (NOT a root session)?
2. **Was a privilege check removed, weakened, or moved?** e.g. `dispatch(...)` deleted, a `hasAdmin` changed to `hasView`, an `exceptionIfNotFound`/secure finder swapped for an `AsGrouperSystem` variant, a `GrouperSession` argument replaced by a root session.
3. **UI-only gating.** If a JSP/`.jsp`, a UI `Container`/backing-bean `canXxx()` getter, or JS now hides a control, confirm the action's backing bean method AND the underlying API still call the privilege check. Hidden ≠ protected. The user can hit the endpoint directly.
4. **Web Services / SCIM / REST / MCP / GSH surfaces.** These call the same API but with their own entry points. A new WS operation or GSH-exposed method must enforce privileges just like the UI; check `GrouperServiceLogic` and WS layer changes.
5. **Correct privilege level.** VIEW is not READ; READ is not UPDATE; UPDATE is not ADMIN. Managing an object requires the management privilege, not merely a view privilege. Verify the strongest-required privilege is checked, not a weaker one.
5a. **Read-role vs. manage-role conflation (privilege confusion).** This is a recurring, high-impact bug class. Whenever a feature defines paired roles — reader/editor, viewer/manager, read/admin, "allowed to view" vs "allowed to manage" — confirm every *mutating* operation (create/edit/delete/enable/disable/run) checks the MANAGE role, and that read-level access is never sufficient to mutate. Watch for: a single check reused for both read and write; a `canRead`/`isReader`/`hasView` guard placed on a save/delete path; membership in a "reader"/"viewer"/UI-access group being treated as authorization to change; a shared "allowed to see the screen" gate standing in for "allowed to submit." (Example: a rule *reader* being able to create/edit/delete rules — reaching the rules UI must not grant rule-editing.)
5b. **Config that runs with elevated privileges.** If the change touches anything a user can define that later executes as GrouperSystem/root or with more power than the author (rules and their triggers/actions/EL, hooks, loader jobs, SQL/LDAP queries, GSH templates and scripts, workflows, provisioning, scheduled jobs, email/notification bodies evaluated as EL), verify creating/editing that config is gated at the strictest level (e.g. wheel/root or an explicit, non-read manage role), fails closed when a required allow-group is missing/blank, and cannot be reached merely by having read/view or by landing on the editor page. Treat "who may author this" as "who may act as root," and confirm the check is on the passed real subject, not the root session the save runs inside.
6. **Object enumeration / listing.** Search, autocomplete, "find" endpoints, and provisioning targets must filter to objects the subject can VIEW. Confirm list/search results are privilege-filtered and don't leak names, memberships, or existence of objects the user can't see.
7. **Wheel/root and PrivilegeHelper bypasses.** Any new `isWheelOrRoot` shortcut, config-driven allow-group, or root-session escalation should be justified and minimal.
8. **Fail closed.** On error, missing privilege, or unexpected state, the code should deny access (throw `InsufficientPrivilegeException` / return nothing), never default to allow.
8a. **No side effects in read/GET paths.** Confirm view/search/browse/render/autocomplete operations and any HTTP GET are read-only — no inserts/updates/deletes, no membership or privilege changes, no lazy "create if missing" writes triggered by simply viewing. Any state change must live in an explicit action/POST operation guarded by the appropriate manage-level privilege, not in a code path a user reaches with mere VIEW/READ. Watch for saves/`store`/`add`/`delete`/`assign` calls, or auto-provisioning/auto-repair logic, reached from a `view*`/`search*`/`index`/getter code path. (Example: GRP-7392.)
9. **Indirect access.** Check that inherited/effective privileges, composite groups, and stem inheritance aren't accidentally bypassed, and that a user can't reach a protected object through a related object they do have access to.
10. **Configuration & secrets.** No privilege checks weakened in config editing paths; no secrets, tokens, or passwords logged or exposed.

## Step 4 — Report

Produce a concise, prioritized report:

- **Findings** grouped by severity: `CRITICAL` (missing/removed server-side enforcement, root-session/`AsGrouperSystem` in a user path, wrong-privilege escalation, read-role accepted where a manage-role is required, ability to author config that runs with elevated privileges, side effects/mutations in a read or GET path, object leakage) → `HIGH` → `MEDIUM` → `LOW/NIT`.
- For each finding: the file and line (`path:line`), what the risk is, the concrete attack (e.g. "user without ADMIN can call this bean method / WS op directly and modify the group"), and a specific fix (which `PrivilegeHelper.dispatch`/`hasXxx` check to add and where).
- Explicitly call out any place where security appears to rely ONLY on UI hiding.
- If enforcement is correct, say so briefly and note where the check lives so the reviewer can confirm.
- End with a clear verdict: **safe to commit** / **changes required**.

Do not modify code unless the user explicitly asks — this skill reviews and reports. Prefer precision over volume: every finding must be actionable and tied to a real reachable code path.
