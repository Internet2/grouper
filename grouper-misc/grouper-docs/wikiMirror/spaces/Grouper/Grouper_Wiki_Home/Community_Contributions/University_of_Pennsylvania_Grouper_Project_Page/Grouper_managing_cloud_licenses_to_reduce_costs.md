---
title: "Grouper managing cloud licenses to reduce costs"
space: Grouper
pageId: 28544367
version: 5
lastUpdated: 2026-09-21T17:29:13.141Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544367/Grouper+managing+cloud+licenses+to+reduce+costs
---

Penn used Grouper to stop paying for Atlassian Cloud licenses that nobody was using: people claim their own license from a Grouper screen, and a license expires after 30 days without use. It went live on 2026-09-21 and cut the annual bill about 28%, or roughly $12,000.

 This page held the plan before it was built. It has been superseded by [Penn Atlassian licensing front door](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/234323970/Penn+Atlassian+licensing+front+door), which carries the design as implemented, the resulting numbers, and the Java source and configuration as attachments.

 One note from the original plan that the new page keeps: Atlassian's own access-denied message cannot be customised and its access-request queue collects tickets nobody reads, so a site that cannot intercept at the IdP needs a static HTML "jump page" linking to both the product and the claim screen.
