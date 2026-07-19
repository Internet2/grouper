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

## Contributing

Add a new skill as its own folder with a `SKILL.md`, keep instructions tight
(AI tends to over-write), and never hard-code credentials. Open a PR against the
`Internet2/grouper` repo like any other doc change.
