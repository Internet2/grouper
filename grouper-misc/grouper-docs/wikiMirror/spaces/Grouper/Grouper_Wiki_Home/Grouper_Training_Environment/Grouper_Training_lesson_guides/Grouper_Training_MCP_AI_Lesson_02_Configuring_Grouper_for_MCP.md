---
title: "Grouper Training - MCP AI - Lesson 02: Configuring Grouper for MCP"
space: Grouper
pageId: 314933250
version: 11
lastUpdated: 2026-11-27T11:58:26.910Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314933250/Grouper+Training+-+MCP+AI+-+Lesson+02+Configuring+Grouper+for+MCP
---

In this lesson you see how the GTE configures MCP, turn on LDAP searches over MCP, and give a person who is not a Grouper sysadmin access to LDAP over MCP.

Before you start: your GTE is running with its SSH tunnel open, and Claude Code is connected to `grouper-gte-local` as banderson, as in [Grouper Training - MCP AI - Lesson 00: Pre-work](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work).

> The GTE already turns on MCP. In `grouper.hibernate.properties`:
> 
> 
> ```
> grouper.is.mcp = true
> grouper.is.oauth = true
> ```
> 
> In `grouper.properties`:
> 
> 
> ```
> grouper.mcp.auth.oauth = true
> grouper.ws.url = https://localhost:8443/grouper-ws
> grouper.mcp.logClientErrors = true
> grouper.oauth.redrectUri.localhost.regex = ^http://localhost(:\\d+)?/.*$
> ```
> 
> The redirect URI regex allows OAuth clients that receive the login on `http://localhost`, which is what Claude Code does. Without a regex, no OAuth client can register. Note the key is spelled `redrectUri`, which is what the training environment (Grouper 7.9.0) reads. In Grouper 7.10.0 and later the key is spelled correctly, `grouper.oauth.redirectUri.<configId>.regex`; the old spelling is still read, and upgrade task 47 renames it in database configs.

Stay organized: name this lesson's conversations `Lesson 02: ...` and keep them in a sidebar section named `Lesson 02 Configuring`, as in Lesson 01.

## Step 1: turn on LDAP for MCP

1. In the Grouper UI (`https://localhost:8443/grouper`) as banderson, go to "Miscellaneous" -> "Configure" -> "Configuration files" -> "Import", and choose `grouper.properties`
2. Paste this in and import it:
  ```
  grouper.mcp.ldap.demo.baseDn = dc=internet2,dc=edu
  grouper.mcp.ldap.demo.documentationForAiClient = GTE OpenLDAP directory. People are in ou=people (uid, cn, givenName, sn, mail, employeeNumber, memberOf). Groups are in ou=groups (groupOfNames with member).
  grouper.mcp.ldap.demo.limitedAccessGroup = etc:mcp:mcpLdapDemoUsers
  ```

`demo` is the GTE LDAP connection (`ldap.demo.*` in `grouper-loader.properties`). Any `grouper.mcp.ldap.demo.*` setting makes it available to MCP. The limited access group is used in the extras, for a non-admin.

## Step 2: search LDAP as banderson

Log in again so you can grant the admin read-only scope. In the GTE, `etc:sysadmingroup` is already in `etc:mcp:mcpUsersAdminReadWrite`, and admin read-write includes the admin read-only tools, so banderson can use the LDAP tool.

Do this in Claude Code on the command line. If your GTE VM is new since the pre-work, its database is new too, so remove and add the MCP server first. In a terminal:

```bash
cd ~/grouperMcpTraining
claude mcp remove -s user grouper-gte-local
claude mcp add --transport http -s user grouper-gte-local https://localhost:8443/grouper-ws/mcp
claude
```

Type `/mcp`, select "grouper-gte-local", then select "Re-authenticate".

Log in as banderson, and on the consent page check "Read-only" and "Admin read-only". Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login.

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
search the demo LDAP for the person with uid
zparks and show all of their attributes
```

You should see zparks with employee number 800000789, and `memberOf` values if zparks is in an LDAP group.

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
how many members does the vpn_users group have in
the demo LDAP?  show me the LDAP filter you used
```

Expected: 1088 members.

If something does not work, copy and paste the error into Claude in the same conversation, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### See how MCP is configured

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
how is MCP configured in this Grouper?  who is in
each of the etc:mcp groups?
```

You should see the [etc:mcp](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=etc:mcp) groups. For example, `mcpUsersReadonly` has the HR staff group [basis:hr:employee:dept:10830:staff](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=basis:hr:employee:dept:10830:staff), and `mcpUsersReadwrite` has [etc:sysadmingroup](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:sysadmingroup).

### Check LDAP before it is turned on (do this before step 1)

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
search the demo LDAP for the person with uid
zparks
```

This should not work yet: LDAP is not turned on for MCP.

### More LDAP searches

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
find people in the demo LDAP whose last name
starts with Mill.  show uid, cn, mail and
employeeNumber in a table
```

Expected: 45 people (41 Miller and 4 Mills).

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
how many people in the demo LDAP are not in
vpn_users?  use memberOf, show the filter, and
give me 5 examples
```

Expected: 1912 people (3000 people minus 1088 members).

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
look up employee number 800000789 in the demo
LDAP, then find that person in Grouper and list
their Grouper groups
```

This uses LDAP and Grouper together: the person is zparks.

### Turn on LDAP provisioning

The GTE has a `groupOfNames` provisioner to the demo LDAP, but its jobs are paused. In the UI, go to "Miscellaneous" -> "Daemon jobs", filter for `groupOfNames`, and turn on both jobs from their "Job actions" menu:

- `OTHER_JOB_provisioner_full_groupOfNames` (full sync)
- `CHANGE_LOG_consumer_provisioner_incremental_groupOfNames` (incremental)

Do not mark any folder to provision. The next extra marks one group, and removes it again.

### Test provisioning with a new skill

A skill you can rerun after every upgrade. It changes groups in `test` and runs daemon jobs, so sign in again with more access. Do this in Claude Code on the command line. In a terminal:

```bash
cd ~/grouperMcpTraining
claude
```

1. Type `/mcp`, select "grouper-gte-local", then select "Re-authenticate"
2. Log in as banderson / password
3. On the consent page, check "Read-only", "Read-write", "Admin read-only", and "Admin read-write"
4. Under "Limit read-write scope", in "Folders", enter `test` and click "Add folder"
5. Approve
6. Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Create a new skill, gte-ldap-provisioning-test,
that checks the groupOfNames LDAP provisioner
still works, e.g. after a Grouper upgrade.  Use
the grouper-gte-local MCP tools for every step.
To run the provisioner: run the groupOfNames full
sync job, wait 5 seconds, run the incremental
job, and wait until it is done.
1. Create the group test:ldapProvisionTestXYZ,
   where XYZ is random, with 3 members from the
   eduLDAP source
2. Run the provisioner.  Use the ldap tool on the
   demo LDAP to check the group is not in
   ou=groups,dc=internet2,dc=edu, since it is not
   marked to provision yet
3. Mark the group to provision to groupOfNames,
   wait 5 seconds, and run the provisioner
4. Check the demo LDAP: the group is there with
   the right member values
5. Delete the group, run the provisioner, and
   check it is gone from the demo LDAP
6. Report pass or fail, with what you checked
Then run the skill.
```

Expected: the group shows up in the demo LDAP only after it is marked to provision, is gone after it is deleted, and the skill reports pass. Next time, type `/gte-ldap-provisioning-test` to run it again.

### Give a non-admin LDAP access

zparks is HR staff, so zparks can use MCP read-only, but is not a Grouper sysadmin. In the UI as banderson:

1. In the folder [etc:mcp](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=etc:mcp), create the group `mcpLdapDemoUsers`. It must be in [etc:mcp](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=etc:mcp), where MCP cannot change it, or it is ignored
2. Add zparks to [etc:mcp:mcpLdapDemoUsers](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpLdapDemoUsers)
3. Add zparks to [etc:mcp:mcpUsersAdminReadonlyLimited](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpUsersAdminReadonlyLimited)

zparks now has the LDAP tool for the demo LDAP only, and no other admin tools.

### Search LDAP as zparks

Do this in Claude Code on the command line. In a terminal, start Claude Code in the training folder:

```bash
cd ~/grouperMcpTraining
claude
```

Type `/mcp`, select "grouper-gte-local", then select "Re-authenticate".

Copy the login URL that Claude shows into a private (incognito) browser window, so you do not log in as banderson. Log in as zparks / password, and check "Read-only" and "Admin read-only". Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login.

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
how many members does the vpn_users group have in
the demo LDAP?
```

This should work: 1088 members.

Use the copy button to copy this prompt into the Claude app, in the same conversation:

```
use the admin_daemon_logs tool to show me recent
loader errors
```

This should be refused: zparks only has the LDAP tool opened.

**Clean up:**

Log back in as banderson for the next lessons: in Claude Code on the command line, `/mcp` -> "grouper-gte-local" -> "Re-authenticate", and log in as banderson in a normal browser window. Type `/exit`, then quit the Claude app completely and open it again, so it uses the new login.
