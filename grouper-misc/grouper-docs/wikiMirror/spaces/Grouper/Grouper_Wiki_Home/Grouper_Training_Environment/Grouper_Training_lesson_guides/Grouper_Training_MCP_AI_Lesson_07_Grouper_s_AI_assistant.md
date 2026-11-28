---
title: "Grouper Training - MCP AI - Lesson 07: Grouper's AI assistant"
space: Grouper
pageId: 332300301
version: 11
lastUpdated: 2026-10-26T16:23:00.197Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/332300301/Grouper+Training+-+MCP+AI+-+Lesson+07+Grouper+s+AI+assistant
---

Grouper has an AI assistant built into the UI. It uses the same tools as MCP, as the logged-in user, and shows you every change to approve before it runs. In this lesson you set it up and use it to do [Use cases - Lesson 01: Basis and reference groups part 1](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1), the same exercise you did with a GSH script and through MCP.

Before you start: your GTE VM is running with its SSH tunnel open.

Stay organized: name this lesson's conversations `Lesson 07: ...` and keep them in a sidebar section named `Lesson 07 AI assistant`, as in Lesson 01.

## Step 1: reset your GTE

If you are still in GSH, type `:quit` first, so you see the `student@...` prompt.

In the SSH window on your GTE VM:

```bash
./gte 101.1.1
```

Wait a few minutes for Grouper to start, until [https://localhost:8443/grouper](https://localhost:8443/grouper) loads. Log in as banderson / password.

## Step 2: connect Grouper to the AI provider

The assistant calls the AI provider from the Grouper server, with an API key that the institution pays for. For the class, the instructors post a shared key in the Slack channel.

Go to "Miscellaneous" -> "Configure" -> "Configuration files" -> "Import", choose `grouper-loader.properties`, and import this, with the key from Slack on the last line:

```
grouper.wsBearerToken.anthropic.endpoint = https://api.anthropic.com/v1
grouper.wsBearerToken.anthropic.httpHeader = x-api-key
grouper.wsBearerToken.anthropic.prependBearerTokenPrefix = false
grouper.wsBearerToken.anthropic.accessTokenPassword = KEY_FROM_SLACK
```

> Do not paste the key anywhere else, and do not share it. The instructors revoke it after the class.

Do not worry if the "Test" button on the `anthropic` external system fails. The Anthropic API requires an `anthropic-version` header, which the external system test cannot send, and there is no config for it. The assistant sends that header itself, so the assistant still works.

## Step 3: turn on the assistant

Same import screen, this time choose `grouper.properties`:

```
grouper.ai.agent.enabled = true
grouper.ai.agent.provider = anthropic
grouper.ai.agent.externalSystemConfigId = anthropic
grouper.ai.agent.model = claude-sonnet-5-5
grouper.ai.agent.maxQuestionsPerUserPerDay = 30
grouper.ai.agent.maxTokensPerConversation = 200000
```

Grouper does not turn the assistant on until the model and both limits are set. The limits are how an institution controls what one person can cost in a day.

## Step 4: let banderson use it

In the UI, go to the group [etc:aiAgent:aiAgentUsers](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:aiAgent:aiAgentUsers) and add the member [etc:sysadmingroup](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:sysadmingroup). People also need to be in an MCP access group; in the GTE, [etc:sysadmingroup](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:sysadmingroup) is already in [etc:mcp:mcpUsersReadwrite](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=etc:mcp:mcpUsersReadwrite).

## Step 5: open the assistant

1. Go to "Miscellaneous" -> "AI assistant"
2. Under "What the assistant may do in this session", check "Read and write" and click "Save"

## Step 6: do the lesson with the assistant

The assistant cannot open web pages, so give it the lesson text:

1. Use the copy button to copy this prompt, and paste it into the assistant:
  ```
  Do this Grouper exercise for me, then answer
  its questions:
  ```
2. Open the [Use cases - Lesson 01: Basis and reference groups part 1](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1) page, copy the text from "Exercise: All Students Reference Group" to the end, and paste it into the assistant after the prompt
3. Click "Send"

The assistant looks things up, then shows the changes it wants to make. Read them, and click the button to make them, e.g. "Make 6 changes" (uncheck any you do not want, or click "Decline all"). Then it tells you how many students are in the group. Check [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students) in the UI. Compare the count with what you got through MCP and with Claude driving the UI.

If something does not work, copy and paste the error into the assistant or into Claude, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### See what it cost

Go to "Miscellaneous" -> "AI assistant usage". It shows the questions and tokens each person used. The top of the assistant page also shows your questions and tokens used today.
