---
title: "Grouper MCP example: daemon error bug"
space: Grouper
pageId: 28554301
version: 5
lastUpdated: 2026-07-01T05:40:44.385Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554301/Grouper+MCP+example+daemon+error+bug
---

In testing of Grouper v7.0.1, I noticed an error when viewing certain daemons in the UI:

I paste that error into Claude code (which is opened in the directory of the Grouper git clone).

Claude goes into detail about how I have a legacy job and how Grouper should gracefully handle that and gives me a Java fix, which I could submit an a pull request if I weren't a committer, but I want to know about this legacy job

Then I ask claude code just to run those queries. The Grouper MCP can run readonly SQL against the Grouper database.

Then it uses the SQL tool in the MCP and tells me how to fix my DB. Note, it fumbles around a little until it figures out the exact structure of the tables, so ignore the errors. MCP resolves it own errors if it can.

I try the first delete and get an error

Paste that error in claude code. It tells me what to do

Now I enquire about the job.

This is not perfect. If I removed the grouper loader config from the database that would work but you should do this on UI. Since we are not getting perfect answers, I will edit the CLAUDE.md file to give claude context about this nuance:

Anyways, I remove the job, and now my UI works again!
