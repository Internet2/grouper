---
title: "Using Playwright to capture Confluence documents"
space: Grouper
pageId: 28543143
version: 2
lastUpdated: 2026-07-01T05:50:19.223Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543143/Using+Playwright+to+capture+Confluence+documents
---

Playwright is in Grouper so browser activities can be automated e.g. for testing during upgrades. It is also useful for other activities like indexing Grouper documentation (central or institution specific) for AI knowledge bases.

This document is an example of using Playwright to harvest a Confluence space and convert it into markdown for AI.

The first task is making markdown and putting this in a JSON file

(e.g. to manually put in a GPT where there is no programmatic interface). The second shows how to programmatically post the knowledge files to an OpenAI assistant so there is no manual process.
