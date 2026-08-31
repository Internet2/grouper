---
title: "Grouper AI agentic with Claude code"
space: Grouper
pageId: 28547554
version: 3
lastUpdated: 2026-07-01T05:46:33.578Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547554/Grouper+AI+agentic+with+Claude+code
---

## Using agentic AI with Grouper

The best way to use AI with Grouper currently (as of March 2026) is agentic AI.

A known best pattern is Claude Code with Opus. This seems expensive but it provides valuable time savings in working with and troubleshooting Grouper.

You can use any agentic tool with Opus and it will be similar. Or if you do not have access to Opus your mileage may vary.

## How to use Claude Code with Grouper

You can set this up however you want, but at a high level you want the source code of Grouper (all modules) in the version that you use, and your local code/views/etc.

See what version of Grouper you want Claude to use

Go to a new directory.

Might as well clone Grouper so you can easily switch tags as you upgrade. Switch to the tag of your version

```
mchyzer@Chriss-MacBook-Pro-6 /tmp % mkdir myGrouper
mchyzer@Chriss-MacBook-Pro-6 /tmp % cd myGrouper
mchyzer@Chriss-MacBook-Pro-6 myGrouper % git clone https://github.com/Internet2/grouper.git
mchyzer@Chriss-MacBook-Pro-6 myGrouper % cd grouper
mchyzer@Chriss-MacBook-Pro-6 grouper % git fetch --tags
mchyzer@Chriss-MacBook-Pro-6 grouper % git checkout tags/GROUPER_RELEASE_5.22.1
```

Start claude (after installing, enrolling, paying, etc)

Add in your institution's folder of source control for GSH templates, SQL views, documentation for institution-specific Grouper stuff, etc

Then your Grouper turbo jets are on! Note, it works well to just edit files in your institution's folder. Edit GSH templates, daemons, views, reports, etc.
