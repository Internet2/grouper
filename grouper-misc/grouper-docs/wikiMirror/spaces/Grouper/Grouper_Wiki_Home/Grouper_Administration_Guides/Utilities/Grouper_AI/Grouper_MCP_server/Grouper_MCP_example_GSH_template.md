---
title: "Grouper MCP example: GSH template"
space: Grouper
pageId: 28554602
version: 3
lastUpdated: 2026-07-01T05:40:04.303Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554602/Grouper+MCP+example+GSH+template
---

In Grouper v7.0.3 you can expose GSH templates to be used by MCP.

In this example, we have a simple GSH template with two inputs, to add a kerberos principal (i.e. service account) to our subject source (insert a row in a table), and add that principal to the groups to be able to query ldap the ws.

It looks like this:

So why do we want this exposed to MCP? Since we get tickets about this and they are few and far between, and people forget how to get to the screen etc.

We edit the template and enable for MCP. Note we do not set readonly true since it is changing state, it is read/write. This tells the MCP it is in the Grouper read/write scope.

Our input to the GSH template for kerberos principal should be the "subject scope" for the MCP so it has some guardrails. This will ensure the principal(s) in the Oauth scope are the only principals that can be added. Note: the principals are not subjects before the operation, but the Oauth allows that.

So now when we approve the MCP scope (when connecting AI to MCP) we would do this minimally like this. Note: for this ticket we need to put a folder in there too, but the MCP needs to tell us which folder, so we will adjust the scope a couple times throughout the example

The AI can now find that tool (with some prodding), and run it

The problem here is temporary, in future we wont need to do this, but i need to change the Oauth scope for the groups being added as subjects are in the subject scope. In the future if they are in the group scope you should be able to add as subject. Not a big deal, i will get a new scope.

Ok, lets carry on with the same conversation:
