---
name: cherry-pick
description: >
  Cherry-pick / merge git commits between Grouper branches (v4, v6, v7, v9).
  Lists commits by JIRA ID in the release-bounded range, checks which are already
  applied to the target branch, flags version-specific features that must not be
  backported, and cherry-picks in chronological order with per-JIRA approval. Use
  this skill whenever the user mentions cherry-picking, merging commits between
  branches, syncing branches, backporting, or bringing commits from one Grouper
  version to another. Also trigger on "merge commits", "cherry pick to v7",
  "sync v6 to v7", "backport to v4".
---

# Cherry-pick / merge git commits for Grouper

Cherry-pick commits from one Grouper branch to another, identifying what needs
to be merged by JIRA ticket and skipping already-applied or inapplicable changes.

Typical use is a **backport**: work lands on the newest branch (v7), and the
maintenance branch (v6, v4) picks up the subset that applies, usually right
before that branch's release.

## Branch and repo layout

Work with one checkout per branch, so no branch switching is needed. A common
layout:

- `$HOME/git/grouper_v4` -- GROUPER_4_BRANCH
- `$HOME/git/grouper_v6` -- GROUPER_6_BRANCH
- `$HOME/git/grouper_v7` -- GROUPER_7_BRANCH

**Do NOT switch branches in the current working directory.** Run the skill from
the TARGET branch's checkout. Read the source branch with `git log <branch>` --
all branches are in the same repo, so a fetched clone can read any of them.

Fetch before starting:

```bash
git fetch origin --tags
```

## Release tags

Release tags follow the format `GROUPER_RELEASE_a.b.c`, e.g.
`GROUPER_RELEASE_6.3.0`, `GROUPER_RELEASE_7.4.0`.

## Workflow

### 1. Identify source and target

Ask if not clear:

- **Source branch**: where commits are coming FROM (e.g. GROUPER_7_BRANCH)
- **Target branch**: where commits are going TO (the current checkout's branch)

### 2. Find the release tags that bound the range

Two different tags matter -- do not confuse them:

- **Lower bound: the last release tag on the TARGET branch.** This is where the
  target left off, so it is the starting point for what still needs to come over.
- **Upper bound: the last release tag on the SOURCE branch.** See step 3 -- the
  range STOPS here, it does not run to the source branch HEAD.

```bash
# last release on the target (lower bound), e.g. GROUPER_RELEASE_6.3.0
git tag --sort=-creatordate | grep "GROUPER_RELEASE_<target_prefix>" | head -1

# last release on the source (upper bound), e.g. GROUPER_RELEASE_7.4.0
git tag --sort=-creatordate | grep "GROUPER_RELEASE_<source_prefix>" | head -1
```

### 3. List commits in range -- ALWAYS cap at the SOURCE branch's last release

**Never list to the source branch HEAD.** Commits made on the source branch after
its most recent release tag have not shipped in any release yet -- they are
unreleased work still baking. Pulling them into the target would ship code in the
target's release that the source has never released, which is backwards. Cap the
range at the source branch's last release tag.

Use the tag itself as the revision argument (`git log ... <SOURCE_TAG>`), NOT
`<branch>`. This also avoids a subtle trap: `--after` filters on AUTHOR date, and
some commits land on the branch after the tag while carrying an earlier author
date, so a date-only filter against the branch silently pulls post-release
commits in. The tag argument bounds by ancestry, which is what you actually want.

For the lower bound, take the date of the TARGET's last release tag and subtract
about a week. This catches commits made just before that release but not
cherry-picked into it.

Include the commit timestamp and author in the output so they can be shown in the
tables.

```bash
# target's release tag date (lower bound)
git log -1 --format=%ai GROUPER_RELEASE_<target_version>

# source release tag and its commit (upper bound)
git log -1 --format="%h %ai %an%n%s" GROUPER_RELEASE_<source_version>

# commits, oldest first, bounded ABOVE by the source release tag
git log --reverse --after="<target tag date minus 7 days>" \
    --format="%h|%ai|%an|%s" GROUPER_RELEASE_<source_version>
```

Show the user the source release tag and its commit before listing, and state
how many commits sit above the cap and are therefore excluded:

```bash
git log --reverse --format="%h|%ai|%an|%s" GROUPER_RELEASE_<source_version>..<source_branch>
```

If the user genuinely wants unreleased source-branch work too, they will say so
-- ask before including it, never default to it.

### 4. Group by JIRA ID

Parse commit messages for JIRA IDs (`GRP-XXXX`) and group commits by ticket.
Commits with no JIRA ID go in a separate list for review (they are usually
mechanical, e.g. "wiki: sync", and usually skipped).

Multiple commits often share one JIRA ID (e.g. "GRP-1234 (commit 2)"). Keep them
together and cherry-pick them in order.

### 5. Check which are already applied

For each JIRA ID, check whether it already exists on the target branch. Match by
JIRA ID in the commit message, NOT by commit hash -- cherry-picks create new
hashes.

```bash
git log --oneline <target_branch> | grep "GRP-XXXX"
```

Mark each JIRA as:

- **Already applied** -- skip. Note that some of these were done on the target
  branch FIRST and forward-ported to the source, which is normal.
- **Not yet applied** -- candidate.
- **Partially applied** -- some commits for this JIRA exist on target but not all;
  flag for review.

### 6. Flag version-specific features

Some features exist only on newer branches. Flag them so the user knows they are
being skipped, but still list them.

| Feature | v4 | v6 | v7 | v9 |
|---|---|---|---|---|
| Provisioning | yes | yes | yes | yes |
| Data field subjects/sources | no | yes | yes | yes |
| Data fields for ABAC | no | yes | yes | yes |
| User lifecycle events | no | yes | yes | yes |
| MCP / OAuth for MCP | no | no | yes | yes |
| Sync-back / native-sync capture | no | no | yes | yes |
| Compiled GSH templates (`templateMode=compiled`) | no | no | yes | yes |
| Composite-in-place conversion (GRP-7187) | no | no | yes | yes |

Identify features by keywords in commit messages and changed file paths:

- **MCP**: "mcp", "MCP", or files in MCP packages
- **Data fields / ABAC**: "data field", "abac", "dataField"
- **User lifecycle**: "lifecycle", "user lifecycle"
- **OAuth for MCP**: "oauth", "OAuth"
- **Sync-back**: "sync-back", "sync back", "syncBack", "native sync", or files
  named `*ProvisioningTargetNativeSync.java`
- **Compiled GSH templates**: "compiled", "compiledJava", "templateMode",
  `GshTemplateMode`, `GshTemplateCompile*`, or config comments gated on
  `showEl: "${scriptType == 'compiledJava'}"`

#### Compiled GSH templates are v7+ only

v6 and v4 have the interpreted `GshTemplateV2` API but NOT compiled mode. A
commit that touches compiled daemon/change-log templates cannot be backported --
the `templateMode` / `compiledJava` config values and the `GshTemplateMode` enum
do not exist on the older branches, so even a config-comment-only change edits a
line that is not there. Skip it.

This one is easy to misjudge, because the GSH template classes with the same
names DO exist on the target -- it is only compiled mode that is missing. Check
the capability, not the class names:

```bash
# each returns 0 on a branch without compiled mode
for t in templateMode compiledJava GshTemplateCompile "TemplateMode.compiled"; do
  echo "$t: $(git grep -il "$t" <target_branch> -- 'grouper/src' 'grouper/conf' | wc -l)"
done
```

#### Composite-in-place conversion is v7+ only

Converting a group that already has members into a composite in place, without
change log / PIT churn (GRP-7187: `CompositeInPlaceConverter`, plus the
`CompositeSave` and `Group` changes that back it), stays on v7 and later. Do not
backport it to v6 or v4, and do not treat it as an oversight -- it is a
deliberate decision to keep that path off the maintenance branches, because it
reaches into core `Group` / `CompositeSave` membership handling and bypasses the
normal change log. Skip it and any follow-up commit that builds on it.

#### Sync-back is v7+ only -- ALWAYS skip it on v6 and earlier

**Sync-back (the native-sync capture layer) does not exist on v6 or v4.** Skip
every sync-back commit when the target is v6 or earlier -- do not ask, just skip
and say why. Porting it is a feature port, not a cherry-pick: it would mean
creating the whole `*ProvisioningTargetNativeSync.java` family plus its
`grouper-loader.base.properties` keys, and it conflicts heavily.

Verify against the repo rather than trusting this table -- both commands return
nothing on a branch without the feature:

```bash
git ls-tree -r --name-only <target_branch> | grep -iE "nativesync|syncback"
git grep -il -E "captureMembershipsFromCache|ProvisioningTargetNativeSync|syncBackCache|fullSyncFromSyncBack" \
    <target_branch> -- 'grouper/src'
```

This was a deliberate decision, not an oversight -- the v6 branch carries an
explicit divergence commit recording it:

```
GRP-7062: (commit 2) v4/v6 only: remove sync-back captureUserInsertFromCurrentProvisioner
call on the 409-link path; the native-sync capture layer does not exist on v6
```

Watch for sync-back riding along inside a commit that is otherwise applicable
(e.g. a provisioner series where one commit adds sync-back support). Skip just
that commit and keep the rest of the JIRA's commits.

#### New provisioners and external systems ARE backported

A brand-new provisioner or external system is **cherry-picked back to v4 and v6**,
not treated as a v7-only feature. Do not auto-skip one just because every file it
touches is new on the target -- "all files missing" is the normal shape of a new
provisioner, and it is the one case where that heuristic gives the wrong answer.
Recommend picking it.

The catch: a provisioner written on v7 often has sync-back woven into the very
commit that creates it, so there is no clean commit to drop. Bring the provisioner
over and then **strip the sync-back parts** on the target:

- Delete the `*ProvisioningTargetNativeSync.java` file the commit adds.
- Remove `captureUserInsert*` / `captureMembership*` / `captureGroup*` calls and
  the native-sync imports from the target DAO.
- Remove the provisioner's `syncBack` / `nativeSync` keys from
  `grouper-loader.base.properties` and any matching externalized text.
- Drop any native-sync test class, and any sync-back cases inside a kept test.

This is avoidable at authoring time -- see the "How to commit this work" section
of the grouper-provisioner skill, which says to keep sync-back in its own commit
so the backport is "skip commit N" instead. CCure did that and applied clean to
v4 and v6; Jamf did not and needed the surgery below on both branches.

Do this as a modified pick: `git cherry-pick -n <hash>` applies without
committing; make the removals in the working tree, then stage and commit it
yourself. Say plainly that it is a modified pick and exactly what was stripped,
so the divergence is deliberate and recorded -- the way GRP-7062 (commit 2) did.

#### Always inspect the changed files before asking

Even when a commit is not auto-flagged above, the code it touches may simply not
exist on the target. For each candidate:

1. `git show <hash> --stat` to see what files it touches.
2. For non-trivial commits, grep the target branch for the key classes, methods,
   JSPs, properties keys, or UI components the commit modifies.
3. If the target does not have that code at all, auto-skip and say so -- do not
   ask. Wasted confirmations on inapplicable commits erode trust.
4. Only ask when the change genuinely applies.

A single JIRA can be **half-applicable**: some hunks touch shared code and some
touch a class that exists only on the source branch. Symptom is a cherry-pick
conflict spanning hundreds or thousands of lines, because git could not place the
hunks and fell back to offering the whole region. Do not try to resolve that block
by hand. Instead:

1. Reset the conflicted file to the target's version: `git show HEAD:<path> > <path>`
2. Re-read the source diff hunk by hunk (`git show <hash> -- <path>`).
3. Apply only the hunks whose surrounding code exists on the target, checking that
   every variable they reference is in scope there.
4. Tell the user exactly which half you kept and which you dropped, and why.

Also check dependency versions when a commit uses a new API: a fix may reference a
class added in a newer version of a library (e.g. jexl3 3.2's `JexlPermissions`)
that the target branch's dependency version does not have. Present the
dependency-upgrade trade-off rather than letting it fail silently.

### 7. Present the list

**Commits to cherry-pick (oldest first):**

| # | JIRA | Commit(s) | Date/Time | Author | Description |
|---|------|-----------|-----------|--------|-------------|

**Already applied (skipping):**

| JIRA | Source commit | Date/Time | Author | Target commit | Description |
|------|---------------|-----------|--------|---------------|-------------|

**Feature not on target branch (skipping):**

| JIRA | Feature | Date/Time | Author | Description |
|------|---------|-----------|--------|-------------|

**Partially applied (needs review):**

| JIRA | Applied | Missing | Date/Time | Author | Description |
|------|---------|---------|-----------|--------|-------------|

For a long list, walk it in small batches (about 5 JIRAs) rather than dumping all
of them, and give a recommendation with a reason for each.

### 8. Cherry-pick, with approval

Go through JIRAs in chronological order, oldest first. For each, show the JIRA ID,
commit hash(es), date/time, author, and description, then ask "Cherry-pick? (y/n)"
as plain text. Do not use a multiple-choice prompt tool. On approval, pick all of
that JIRA's commits in order.

**Describe the batch, then STOP and wait for the answer.** Do not print the
descriptions and start picking in the same turn -- the user cannot approve what
they have not read yet, and an approval given to a bare list of JIRA numbers is
not informed consent. In particular:

- Naming the next batch at the end of the previous one ("batch 5 is GRP-7175,
  7177, ...") is a preview, NOT a description. A "yes" to that is not approval to
  proceed; describe what each one actually does, then ask again.
- Every JIRA in the batch needs a plain description of what it changes and why it
  matters, not just its summary line. The user is deciding whether it is worth the
  risk on a maintenance branch.
- Never write "picking these now" in the same message as the table. End on the
  question and wait.

Re-confirm mid-batch whenever the situation changes from what was approved:

- A conflict resolution that changes BEHAVIOUR rather than just placing code (see
  the conflict rules below).
- A commit that turns out to need more than the JIRA describes -- e.g. bumping a
  dependency in a second pom the source branch no longer has.
- A pick that turns out difficult, so the necessary/difficult trade-off applies.

#### Difficult AND unnecessary -- revert or skip, do not push through

Weigh every hard pick against how much it actually buys the target branch. When a
commit is BOTH difficult and not necessary, skip it (or revert it if already
applied) rather than spending effort forcing it in. Difficulty is a real signal:
it usually means the branches diverged in that area, so a forced resolution is
where silent breakage gets introduced.

Rough guide, but ASK rather than deciding alone:

- **Necessary** -- crashes, data corruption, security fixes, provisioner runs
  aborting, anything a site would hit in production.
- **Not necessary** -- string externalization, styling and contrast tweaks,
  monitoring and progress labels, cosmetic UI work, new conveniences.

Signs a pick is difficult enough to trigger the question: more than a handful of
conflict blocks; conflicts spanning whole functions rather than adjacent lines; a
resolution that needs an API the target branch does not have; or a file that has
diverged so far that git offers the whole region.

Say plainly what was skipped and why, and keep a running skip list so the release
notes and the summary agree.

**Do not use `git cherry-pick -x`.** The Grouper history does not carry
`(cherry picked from commit ...)` trailers -- the backported commit keeps the
original one-line message unchanged. Verify on any branch with:

```bash
git log -400 --format="%B" <target_branch> | grep -c "cherry picked from"
```

If a conflict occurs:

1. Stop and show the conflict to the user.
2. Edit the conflicted files to resolve it (remove markers, choose the correct
   code).
3. **commons-lang3 import fix**: when resolving conflicts in v4 files, if the
   conflict involves imports, update `org.apache.commons.lang.` (commons-lang 2)
   to `org.apache.commons.lang3.` to match v6+. Apply to all commons-lang classes
   (`StringUtils`, `ObjectUtils`, `BooleanUtils`, ...) across the whole file, not
   just the conflicted section.
4. **Resolve it, stage it, and complete the pick.** Staging is part of resolving a
   conflict, not a separate decision to defer -- a cherry-pick left half-resolved
   in the working tree still reads as an unresolved conflict to the IDE (Eclipse
   shows the file conflicted until it is staged), so handing it back unstaged
   leaves the user's workspace in a broken-looking state. Stage with the CRLF-safe
   form below, then `git cherry-pick --continue --no-edit`. Afterwards tell the
   user what you did: show the resulting `git diff HEAD~1` and say which parts of
   the source commit you dropped and why. Stop and ask only when you genuinely
   cannot tell which side is correct, or when the resolution would change
   behaviour rather than just place code.
5. Some Grouper source files are stored with CRLF line endings while
   `core.autocrlf=input` strips CR on add, which silently rewrites the whole file.
   When a resolved file is CRLF (`grep -c $'\r' <path>` equals its line count),
   tell the user to stage it with:
   `git -c core.autocrlf=false add <path>`
6. When the user says they have committed, review before moving on: run
   `git show HEAD`, compare against the source commit, and check for leftover
   conflict markers, missing code, or accidental deletions. Flag anything wrong,
   otherwise confirm it looks right.

Push periodically rather than at the very end, so a long run is not one giant
push. The user does the pushing.

### 9. Run unit tests after merging

A backport is not done when the last commit lands. Cherry-picks apply by context, and
a modified pick (sync-back stripped, a v7-only half dropped) can leave code that
compiles but no longer behaves. Run tests before handing the branch over.

Do NOT run the full suite -- it is far too slow for this. Build a **throwaway suite**
that covers what the backport actually touched, one test class per area:

1. List the test classes the backport modified:
   `git diff --name-only <TARGET_TAG>..HEAD | grep -E 'src/test/.*Test\.java$'`
2. Pick roughly 8-12 of them, one per distinct area (config, caching, HTTP, loader,
   hooks, ABAC, rules, export, ...). Enough to be meaningful, few enough to finish.
3. Write it as a JUnit 3 suite named so nobody mistakes it for permanent -- e.g.
   `TrashV6BackportTests` -- with a class comment saying to delete it, and a
   `// GRP-####:` comment on each entry naming what it covers.
4. Tell the user to delete it once the release is verified. Never add it to `AllTests`
   and never let it reach a release branch.

Two traps when choosing classes:

- **Module classpath.** `addTestSuite` only compiles against classes on the same
  module's test classpath. A test under `grouper-misc/grouperClient/src/test/java`
  cannot be referenced from a suite in `grouper/src/test` -- it must be run from its
  own module. Verify each candidate resolves under the suite's own module before
  including it.
- **Tests needing the mock service Tomcat.** Provisioner tests gate on
  `tomcatRunTests()` and need the mock service Tomcat in a separate JVM; they do not
  belong in a quick verification run. List them for the user as a separate run, and
  note that the Tomcat must have the daemon OFF or the changelog daemon races
  `fullProvision`.

Report which areas the suite covers and which it deliberately does not, so the user
knows what was and was not exercised.

### 10. Summary

When done, report:

- How many commits were cherry-picked
- Which JIRAs the user declined
- Which JIRAs were auto-skipped and why
- Any conflicts resolved, and how
- Any modified picks, and exactly what was stripped
- Which JIRAs were skipped or reverted as difficult-and-unnecessary, and which
  were applied then reverted (name the revert commit)

## Important notes

- Resolving, staging, and completing picks is yours. **Pushing is the user's** --
  never push. Report every resolution so nothing lands silently.
- Always cherry-pick in chronological order, oldest first.
- Describe a batch and then WAIT. Approval of a bare list of JIRA numbers is not
  approval to proceed; never present the descriptions and start picking in the
  same turn.
- Never force-push or rewrite history.
- If `git cherry-pick` fails with "empty commit", the change is already applied --
  skip it with `git cherry-pick --skip`.
