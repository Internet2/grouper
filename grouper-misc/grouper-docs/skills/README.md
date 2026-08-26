# Grouper AI skills

Shared, version-controlled AI **skills** for working on Grouper: reusable
instruction sets that tell an AI assistant (Claude, etc.) exactly how to perform
a Grouper task safely and consistently. Committing them here lets the whole
Grouper developer community use and improve the same skills instead of each
person keeping a private copy.

## What a skill is

A skill is a folder with a `SKILL.md` file. The `SKILL.md` has YAML front matter
(`name`, `description`) that tells the AI client when to use it, followed by the
instructions the assistant should follow. Clients that support skills (e.g.
Claude Code / Claude desktop) load the skill when the task matches its
description.

## Using a skill

Copy or symlink the skill folder into your AI client's skills directory. For
Claude Code / Claude desktop that is `~/.claude/skills/`, e.g.:

    ln -s "$PWD/grouper-wiki-edit" ~/.claude/skills/grouper-wiki-edit

Then the assistant can invoke it by name when a matching task comes up. Skills
that need credentials (API tokens) read them from a file **outside** git -- see
the individual skill for the expected path. Never commit tokens.

## Available skills

- **grouper-wiki-edit** -- edit pages on grouper.atlassian.net Confluence via the
  REST API with an API token (read via MCP, never write via MCP). Companion to
  the wiki page "Using AI to edit the Grouper wiki".
- **grouper-jira** -- create, transition, and resolve issues in the GRP project on
  grouper.atlassian.net via the Jira Cloud REST API with an API token (same
  cross-site token as grouper-wiki-edit). Holds the project/issue-type/transition
  ids and the resolution-field-not-on-screen gotcha.
- **grouper-gte-cert** -- fix the Grouper Training Environment (GTE) container's
  self-signed TLS certificate so https://localhost:8443 loads in an AI assistant's
  built-in browser pane. The shipped cert has no subjectAltName, which Chromium
  rejects outright and cannot be clicked past. Covers regenerating the cert inside
  the container, trusting it on macOS and Windows, and the one-line base/Dockerfile
  change that would stop it recurring on every fresh container.
- **grouper-release** -- cut and record a Grouper container release: bump the
  container Tomcat/base versions, confirm no DDL/upgrade-task or server.xml-patch
  surprises since the last release, resolve the shipped GRP issues and tag them
  with the new fixVersion, and add the release row to the "v7 Release Notes" wiki
  page. Uses grouper-jira and grouper-wiki-edit for the writes; holds the
  release-notes table contract (columns, status colours, layout).

- **cherry-pick** -- cherry-pick / backport commits between Grouper branches (v4,
  v6, v7, v9). Bounds the range by release tags (never past the source branch's
  last release), groups commits by JIRA, skips what is already applied, and holds
  the table of which features must not be backported (MCP, sync-back) versus which
  must (new provisioners). Walks the list in batches with per-JIRA approval and
  leaves all staging and committing to the user.

- **grouper-ddl** -- add a DDL change (index, table, column, comment, foreign key)
  to the Grouper codebase without missing a piece. Covers the full checklist: the
  per-database install SQL files (postgres/oracle/mysql), the GrouperDdl version
  class used by database compares, the upgrade task, and the DDL reference file.

- **grouper-mcp-edit** -- create and maintain Grouper MCP (Model Context Protocol)
  tools: the tool class and its tests, servlet registration, configuration
  properties, and the MCP wiki documentation. Holds the conventions the existing
  tools (group_find, group_add_member, folder_delete, ...) follow.

- **grouper-provisioner** -- build a new provisioner from scratch, listing every
  piece it needs: the Java classes, configuration, mock service handler, tests,
  base properties, externalized text, Hibernate mappings, and documentation.

## Contributing

Add a new skill as its own folder with a `SKILL.md`, keep instructions tight
(AI tends to over-write), and never hard-code credentials. Open a PR against the
`Internet2/grouper` repo like any other doc change.
