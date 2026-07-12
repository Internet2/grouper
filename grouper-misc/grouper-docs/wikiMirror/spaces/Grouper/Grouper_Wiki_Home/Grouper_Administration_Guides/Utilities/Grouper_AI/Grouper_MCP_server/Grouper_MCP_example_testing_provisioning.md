---
title: "Grouper MCP example: testing provisioning"
space: Grouper
pageId: 28554381
version: 5
lastUpdated: 2026-07-01T05:40:35.909Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554381/Grouper+MCP+example+testing+provisioning
---

This will be smoother in the near future when we add MCP tools to query LDAP, (un)mark groups/folders as provisionable, and delete groups. Right now its a coordination with agentic AI to manually do things that AI cannot do through MCP.

Note: in the full sync provisioning it told me what my largest provisionable groups are and i removed a bunch to make the testing quicker (test env).

This is the prompt

```
I want to test penngroups provisioning to active directory with MCP.

Only work in the penngroups folder test:ldapTesting2

The provisioner has config id: kiteTest. Only work with that provisioner in the following script

The incremental provisioner runs at the top of the minute, so to check changes, wait until then, and give it 10 more seconds to finish, or check the daemon logs for a successful incremental run for that provisioner.

Add a group in that folder which doesnt exist, mark it as provisionable. After the incremental, check the sync tables to see if that group in the target for that provisioner.

Then add a member to the group. After the incremental, see that the membership is in target

then remove the member from the group, after waiting, see it is not in target

then mark the group as not provisionable, see that it is not in target

then delete the group. create another group with two members, mark it as provisionable, see all that in target.

then delete that group, and see that it is not in target

note: there is no tool to mark something as (un) provisionable, so just tell me when i need to do that in the UI
```

This is how AI helped test provisioning:

- speeding up the process
- having a reproducible process
- more reliable process with fewer manual steps

Lets do the same thing to test full syncs

prompt

```
i temporarily disabled the incremental sync, 
lets do the same thing with full syncs.  
validate through the daemon logs that the 
full sync is not doing work it shouldnt

let me know when to run the full sync too

remind me to enabled the full sync when done
```
