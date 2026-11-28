---
title: "Grouper Training - MCP AI - Lesson 00: Pre-work"
space: Grouper
pageId: 314408978
version: 25
lastUpdated: 2026-10-26T16:22:43.055Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work
---

> Hands on is optional. If you do not want to do it, you do not need to do anything here: just come to class and look on with someone else.

 For people attending the AI MCP lesson in person who want to do the hands on. Bring a laptop with these steps done. They take about 30 to 45 minutes, most of it starting your GTE and connecting to it in step 7. If a step does not work, ask Claude Code first: paste in what you did and the error message. If you are still stuck, ask in the Slack channel, or come to class an hour early and an instructor will help.

 In the lesson you will connect Claude Code, an AI assistant that runs on your computer, to the Grouper training environment (GTE) through MCP (Model Context Protocol), so it can look up and change groups as the logged-in user.

 

## 1. Make sure you have GTE hours

 You need at least 10 hours of GTE VM time for the class. Check your remaining hours on the [GTE VM manager](https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=GTEManageAWSInstance&stemId=d0fcbfb223e74a288de5a388d42e6fca): it should show at least 10 hours. If it does not, ask in the Slack channel.

 Do not use up your hours before the class starts. The test in step 7 only needs about an hour.

 

## 2. Get a Claude license

 Claude Code needs a Claude license.

 

- **If you already have one** (Pro, Max, Education, or Enterprise), you can use it, as long as it is allowed to connect to arbitrary MCP servers. Some Education and Enterprise accounts do not allow this. Step 7 tests it: if your account cannot connect, get a Pro account
- **Otherwise**, sign up for a Pro account at [https://claude.ai](https://claude.ai) ($20, billed monthly). You only need it for one month: do not forget to cancel it if you do not want to keep it

 

## 3. Trust the GTE certificate

The GTE uses its own (self-signed) certificate for `https://localhost:8443`. Claude Code, the Claude app, and the app's built-in browser only connect to it if your computer trusts that certificate, so do this before you install them.

1. Download the attachment  to your `Downloads` folder. If Confluence says "Something went wrong. We couldn't generate a preview for this file.", ignore it and click the download button
2. Mac: open the "Terminal" app (in "Applications" -> "Utilities") and run this. It asks for your password:
  ```bash
  security add-trusted-cert -r trustRoot -k ~/Library/Keychains/login.keychain-db ~/Downloads/gte-localhost.crt
  ```
3. Windows: double click the file, click "Install Certificate", choose "Current User", choose "Place all certificates in the following store", pick "Trusted Root Certification Authorities", and finish

> Every GTE uses this same certificate, and its private key is in the public GTE image. Trust it only for the training, and remove it afterward (see the end of this page).

If the GTE image is updated, its certificate changes. You can always get the current one from a running GTE, with its SSH tunnel open (Mac or Linux):

```bash
echo | openssl s_client -connect localhost:8443 2>/dev/null | openssl x509 > ~/Downloads/gte-localhost.crt
```

## 4. Install the Claude app and Claude Code

 Install both. The command line is needed to add and log in to the Grouper MCP server. The app is an easier place to work, and its "Code" tab uses the same MCP servers.

 

1. Download and install the Claude app for Mac or Windows from [https://claude.ai/download](https://claude.ai/download), and sign in with your Claude account
2. Install the Claude Code command line (below)

 Mac or Linux, in a terminal:

 
```bash
curl -fsSL https://claude.ai/install.sh | bash
```

 Windows, in PowerShell:

 
```powershell
irm https://claude.ai/install.ps1 | iex
```

 

## 5. Approvals

Claude Code asks before it does things on your computer. Do **not** allow:

- Cookies or sign-ins from your web browser
- Folders outside `~/grouperMcpTraining`
- Access to other apps

Generally fine to allow:

- Scripts Claude Code writes and runs for the task, and read operations
- Write operations, e.g. changes in Grouper over MCP, after a quick review of what they will change

## 6. Sign in and test

 

1. In a terminal, make the training folder, go into it, and run `claude`Mac or Linux:
  
  
  ```bash
  mkdir -p ~/grouperMcpTraining
  cd ~/grouperMcpTraining
  claude
  ```
  
  Windows PowerShell:
  
  
  ```powershell
  mkdir $HOME\grouperMcpTraining -Force; cd $HOME\grouperMcpTraining; claude
  ```
2. Sign in with your Claude account
3. Ask it something simple and confirm you get an answer. Use the copy button to copy this prompt into Claude Code:
  ```
  what is Grouper?
  ```
4. Type `/exit` to quit

 

## 7. Connect Claude Code to a GTE

 This checks that everything works before class, including that your Claude account can connect to an MCP server.

 

1. Start your GTE VM for 1 hour only, and connect to it with SSH, as described in [Connecting to the Grouper Training Environment VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM). Leave that SSH window open: Claude Code reaches Grouper through its tunnel. If the SSH window shows errors like `setsockopt TCP_NODELAY: Invalid argument`, press Enter and ignore them
2. Confirm [https://localhost:8443/grouper](https://localhost:8443/grouper) loads in your browser (accept the security warning)
3. Download the Grouper v7 source code into the training folder, in a folder named grouper (about 225 MB unzipped). In class, Claude Code can read it to answer questions about how Grouper works

 Mac or Linux:

 
```bash
cd ~/grouperMcpTraining && curl -L -o grouper.zip https://github.com/Internet2/grouper/archive/refs/tags/GROUPER_RELEASE_7.9.0.zip && unzip -q grouper.zip && mv grouper-GROUPER_RELEASE_7.9.0 grouper && rm grouper.zip
```

 Windows PowerShell (unzipping can take several minutes):

 
```powershell
cd $HOME\grouperMcpTraining; Invoke-WebRequest -Uri https://github.com/Internet2/grouper/archive/refs/tags/GROUPER_RELEASE_7.9.0.zip -OutFile grouper.zip; Expand-Archive grouper.zip -DestinationPath .; Rename-Item grouper-GROUPER_RELEASE_7.9.0 grouper; Remove-Item grouper.zip
```

 

1. Add the GTE MCP server for your user, so it works in every folder and in the Claude app Code tab:

 
```bash
claude mcp add --transport http -s user grouper-gte-local https://localhost:8443/grouper-ws/mcp
```

 Start Claude Code in the training folder. Mac or Linux:

```bash
cd ~/grouperMcpTraining
claude
```

Windows PowerShell:

```powershell
cd $HOME\grouperMcpTraining; claude
```

The first time, Claude Code asks you to sign in with your Claude account, then asks if you trust this folder. Sign in, and choose yes for the folder.

1. In Claude Code, type `/mcp`, select "grouper-gte-local", and select "Authenticate"
2. A browser opens. Accept the security warning, log in as `banderson` / `password`, and approve read-only access
3. Back in Claude Code, use the copy button to copy this prompt:
  ```
  what groups is banderson a member of in Grouper?
  ```
  
  You should see a list of groups from the GTE
4. Make Claude always ask before it changes Grouper. Use the copy button to copy this prompt into Claude Code in the same conversation:
  ```
  Add this to my user memory, ~/.claude/CLAUDE.md,
  so it applies in every folder: before any write
  operation with the grouper-gte-local MCP server
  (adding or removing members, saving or deleting
  groups and folders, assigning privileges or
  attributes, running daemon jobs), tell me exactly
  what you will change and wait for my approval.
  ```
5. Let the read-only Grouper tools run without asking each time. Write tools still ask. Use the copy button to copy this prompt into Claude Code in the same conversation:
  ```
  In my user settings, ~/.claude/settings.json, add
  the read-only grouper-gte-local MCP tools to the
  permissions allow list, so they run without asking
  me: doc_search, attribute_def_name_find,
  group_find, folder_find, attribute_assignment_get,
  audit_get, privilege_get, entity_get_groups,
  group_get_members, memberships_get, entity_get,
  group_has_member, recipe, sql_get_schema,
  sql_select, admin_external_system_get,
  admin_daemon_job_message, admin_daemon_logs,
  admin_config_search, admin_daemon_names, and ldap.
  Tool names look like
  mcp__grouper-gte-local__group_find.  Do not add
  any write tools.  Show me the result.
  ```
6. Type `/exit` to quit Claude Code. If the Claude app is open, quit it completely (Mac: Cmd+Q; Windows: right-click it in the taskbar and close it) so it picks up the MCP server and settings when it opens again

 Then check the Claude app, since most of the class works there:

 You do all MCP setup and authentication in Claude Code on the command line. The Claude app uses the same MCP servers, even if it does not list `grouper-gte-local` with its connectors.

1. Open the Claude app and click "Code" at the top
2. Start a new chat. In the row under the prompt box, click the folder control, click "Open folder...", and choose the `grouperMcpTraining` folder in your home folder. If the app asks to save it as an environment, say yes
3. In the model picker, select Opus 5.5 and "Medium" effort. If you set the model, effort, and folder first thing in a new conversation, they stick for your new conversations.
4. In the permission mode menu under the prompt box, select "Auto". Claude then runs safe steps without asking you, and still asks before risky ones. You do not need "Accept edits": that only skips the question for file edits
5. If Claude asks "Allow this read outside the working directories?", choose "No, and block reads outside the working directories from now on". Claude only needs the training folder
6. Ask the same question. Use the copy button to copy this prompt into the Claude app:
  ```
  what groups is banderson a member of in Grouper?
  ```
  
  You should see the same list. If it cannot reach `grouper-gte-local`, ask in the Slack channel

> If the Claude app shows an error that `grouper-gte-local` could not start for Cowork and Code sessions, it has its own copy of the server in its config file, often added by an earlier setup prompt, and that copy cannot sign in to Grouper. The Code tab does not need it: it uses the server you added with `claude mcp add`. Use the copy button to copy this prompt into Claude Code:
> 
> 
> ```
> Check the Claude app config file,
> claude_desktop_config.json.  If it has a
> grouper-gte-local MCP server, remove it, and show
> me the result.  Do not change my Claude Code MCP
> servers.
> ```
> 
> Then quit the Claude app completely and open it again.

 When it works, terminate your VM in the [GTE VM manager](https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=GTEManageAWSInstance&stemId=d0fcbfb223e74a288de5a388d42e6fca) so you keep your hours for class. If you cannot get this working, come to class an hour early and an instructor will help.

 

## On the day of the training

A Claude Pro plan has a usage limit that resets every 5 hours, counted from your first message. The Claude app and Claude Code share the same limit. Time it so your limit resets during the class. All times are Central time (Minneapolis time) on Monday, October 26.

| Time | What to do |
| --- | --- |
| 5am to 10am | Do not use Claude Code or the Claude app. A message in this window would start your 5 hours early, and your limit would not reset during the class |
| 10am | Start Claude Code and send it any prompt, e.g. hello. This starts your 5 hours |
| 3pm | Your limit resets, during the class, so you are less likely to run out during the hands on |

## After the training: keep experimenting

On your own time, you may run out of your 10 GTE hours in AWS. You can also run the GTE on your own computer, without the AWS VM:

1. Install [Podman Desktop](https://podman-desktop.io) or [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Follow [Run locally, on the Grouper Training Environment page](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment#Run-locally)

The GTE is then at [https://localhost:8443/grouper](https://localhost:8443/grouper) as before, so the same MCP setup works. If your local GTE has a different certificate, get it from the running GTE with the command at the end of step 3, and trust it the same way.

## After the training: remove the certificate

Mac, in the "Terminal" app:

```bash
FINGERPRINT=$(openssl x509 -in ~/Downloads/gte-localhost.crt -noout -fingerprint -sha1 | cut -d= -f2 | tr -d :)
security delete-certificate -Z $FINGERPRINT ~/Library/Keychains/login.keychain-db
```

Windows: open "Manage user certificates", go to "Trusted Root Certification Authorities" -> "Certificates", and delete the one issued to `localhost`.

## After the training: remove the GTE MCP server

When you no longer use the GTE, remove its MCP server. In a terminal:

```bash
claude mcp remove grouper-gte-local -s user
```

If it says the server is not found, it was saved only for the training folder: run the same command without `-s user` from `~/grouperMcpTraining`. Then clean up the settings from step 7. Start `claude` and use the copy button to copy this prompt into Claude Code:

```
Remove the grouper-gte-local MCP tools from the
permissions allow list in ~/.claude/settings.json,
and remove the grouper-gte-local approval rule
from ~/.claude/CLAUDE.md.  Show me the result.
```
