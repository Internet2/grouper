---
title: "Grouper AI assistant in the UI"
space: Grouper
pageId: 271908865
version: 1
lastUpdated: 2026-10-02T18:25:53.787Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/271908865/Grouper+AI+assistant+in+the+UI
---

The AI assistant is a chat screen in the Grouper UI (Miscellaneous > AI assistant). People ask about groups, folders, members, privileges and attributes in plain language. The assistant looks things up with the same tools MCP clients use, as the person logged in, with their permissions. Before it changes anything, it shows the change and asks.

It is off by default, and nobody can use it until they are added to its group.

## How it works

- The assistant is a large language model from **Anthropic** (Messages API) or **OpenAI** (Responses API). Grouper calls the provider directly over HTTPS; an http endpoint is refused unless `grouper.ai.agent.allowInsecureEndpoint` is set, for development only. Provider errors are logged with the HTTP status, the provider's request id and its error type and code, never the text of the response, so conversations don't end up in the log.
- The model never touches Grouper. It asks for tools, and Grouper decides whether to run them:
  
  
  
  - **Look-ups run straight away**, as the user, with Grouper's normal security.
  - **Changes wait for the user.** Each change is shown in plain words, built by Grouper from the change itself, not written by the model. They are listed all checked; the user unchecks any they don't want, then approves or declines. Only the changes shown can run.
  - **Anything the session does not allow** is refused without asking.
- The assistant cannot change the groups that control access to it or to MCP: the assistant's own group, its limit override groups, the MCP groups in `etc:mcp`, and the sysadmin groups. MCP tools refuse to change them, so nobody can use the assistant to give themselves more access.
- Tool calls are recorded in `grouper_mcp_tool_log`, the same log as MCP, with `entry_path` = `ui`. A person's tool log on the MCP screen shows both, with a "From" column (MCP client or AI assistant).
- Changes the assistant makes are recorded in Grouper's audit log with the engine `grouperUiAiAgent`, shown as "AI assistant in the web user interface" on the audit screens. They are recorded as the logged-in person, who approved them, so a group's audit trail tells them apart from changes the person made themselves in the UI.
- If a tool fails, people only see the full error details (stack trace) if they are in `grouper.mcp.users.canSeeStackTraces`, as for MCP.

## Who can use it, and what it can do

Three things decide this, and each can only narrow the others:

1. **The assistant's group**, `grouper.ai.agent.users` (default `etc:aiAgent:aiAgentUsers`, created empty). Only members see the link or can use the screen. Grouper sysadmins are not let in automatically.
2. **The MCP groups** decide which kinds of tools the assistant can use for someone, exactly as for MCP. Someone in the assistant's group but in none of these doesn't see the link: the assistant couldn't do anything in Grouper for them.
  
  
  
  - Read-only: `grouper.mcp.users.readonly` (or `readwrite`, which includes it).
  - Read and write: `grouper.mcp.users.readwrite`.
  - SQL, admin read-only and admin read and write each have a full tier (`canRunSqlReadonly`, `adminReadonly`, `adminReadWrite`), which also requires being a Grouper sysadmin, and a limited tier (`canRunSqlReadonlyLimited`, `adminReadonlyLimited`, `adminReadWriteLimited`), which only gets what is opened to it with a `limitedAccessGroup`. See the MCP documentation.
3. **The session scope.** On the screen, each user chooses what the assistant may do in their session, among what their MCP groups allow: read-only, read and write, SQL, admin read-only, admin read and write. It starts as read-only. The choice lasts for the UI session, so each new login starts as read-only again.

Changes always wait for the person's approval. Look-ups, SQL queries included, run without asking. **Use a read-only database account for the SQL tools** (`grouper.mcp.sql.*`): the assistant runs queries without asking, and the SQL tools' check for read-only queries can't catch every SELECT that changes something.

Removing someone from a group, or turning the assistant off, takes effect within a minute. The assistant checks before every call to the model and every look-up, so a request it is already working on stops too, whether or not the person's screen is still open. Every tool call also checks the MCP groups as it runs.

## Setting it up

### 1. Create an external system for the provider

Create a **Web service bearer token** external system (`grouper.wsBearerToken.<configId>.*`) holding the provider's address and API key.

For Anthropic:

| Property | Value |
| --- | --- |
| `endpoint` | `https://api.anthropic.com/v1` |
| `accessTokenPassword` | the API key |
| `httpHeader` | `x-api-key` |
| `prependBearerTokenPrefix` | `false` |

For OpenAI:

| Property | Value |
| --- | --- |
| `endpoint` | `https://api.openai.com/v1` |
| `accessTokenPassword` | the API key |

Through a gateway such as LiteLLM, use the gateway's address (e.g. `https://litellm.example.edu/v1`) as the `endpoint` and the gateway's key as `accessTokenPassword`, with the default `Authorization: Bearer` header. Set `grouper.ai.agent.provider` to `anthropic` for Claude models (the gateway's `/v1/messages`) or `openai` for GPT models (its `/v1/responses`), and `grouper.ai.agent.model` to the name the gateway gives the model. Check the gateway key's per-minute token limit (see Using it).

### 2. Turn the assistant on (grouper.properties)

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.enabled` | `false` | Turns the assistant on |
| `grouper.ai.agent.provider` | `anthropic` | `anthropic` or `openai` |
| `grouper.ai.agent.externalSystemConfigId` |  | The external system from step 1 |
| `grouper.ai.agent.model` | none, required | The provider's id for the model, e.g. `claude-sonnet-5` (see below) |
| `grouper.ai.agent.users` | `etc:aiAgent:aiAgentUsers` | Who can use the assistant |

#### Choosing a model

There is no default model: providers release new models and retire old ones every few months, so any default would soon be out of date. Until `grouper.ai.agent.model` is set, people are told the assistant is not set up.

- Use the model id from the provider's current list: Anthropic's models overview, or OpenAI's models page.
- Larger models cost several times more per token than mid-size ones. For looking things up and making small changes, a mid-size model (for example Anthropic's Sonnet) is usually enough. Try a larger one if answers are not good enough.
- When the provider announces that a model is being retired, change the setting before the date. After it, every request fails.

### 3. Set a spend limit at the provider

Grouper's own limits (see below) only work while Grouper works as it should. A spend limit at the provider still holds if they fail, for example:

- usage cannot be recorded while the database is down (the assistant keeps working, but nothing is counted);
- a bug in Grouper;
- the API key is used somewhere other than Grouper.

So at the provider:

- Give the assistant **its own API key**, not one shared with other uses, so its spending can be seen and capped on its own.
- Set a **monthly spend limit**, and a **usage alert** somewhat below it. Anthropic and OpenAI both offer spend limits and alerts for an organization or project in their consoles.

When the spend limit is reached, the provider refuses requests, and the assistant shows people its general "Something went wrong" message until the limit resets or is raised. The error is in the Grouper UI log.

### 4. Add people

Add people to the assistant's group, and to the MCP groups for what the assistant should be able to do for them.

## Limits

The daily question limit and the conversation limit are required once the assistant is on: until both are set, people are told the assistant is not set up. The ceiling on what one person can cost (questions per day multiplied by the most one conversation can cost) only holds if both are capped, so no limit has to be chosen on purpose, with `-1`. Work out the values from the model's price and what you are willing to spend per person per day.

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.maxQuestionsPerUserPerDay` | none, required (`-1` for no limit) | Messages one person may send a day (see below) |
| `grouper.ai.agent.maxTokensPerUserPerDay` | no limit | Tokens one person may use a day (see below) |
| `grouper.ai.agent.maxTokensPerConversation` | none, required (`-1` for no limit) | Tokens one conversation may use (see below) |
| `grouper.ai.agent.maxStepsPerMessage` | `10` | Most calls to the model for one message; then the assistant stops and hands back |
| `grouper.ai.agent.maxConcurrentTurnsPerNode` | `10` | Most requests the assistant works on at once on one UI node; more are asked to try again. Needs a restart to change |
| `grouper.ai.agent.maxToolResultChars` | `20000` | Longer tool results are cut off before the model sees them |
| `grouper.ai.agent.maxLookupsPerStep` | `10` | Most look-ups run in one step; the model is told to ask for the rest again. Changes are not counted, since each waits for approval and returns a short result |
| `grouper.ai.agent.maxPageSize` | `200` | Most results a list tool gives the assistant in one call (see below) |
| `grouper.ai.agent.maxUserMessageChars` | `8000` | Longest message a person can send |
| `grouper.ai.agent.maxOutputTokens` | `16000` | Most the model may write in one call |
| `grouper.ai.agent.httpTimeoutMillis` | `300000` | How long to wait for the provider |
| `grouper.ai.agent.verbatimExchanges` | `6` | Latest exchanges kept word for word; older ones are summarized |

Tool calls also count toward the MCP rate limits (`grouper.mcp.throttle.*`), shared with the person's MCP clients.

Tools that return lists (finding groups and folders, members, memberships, audits and so on) give the assistant at most `grouper.ai.agent.maxPageSize` results per call (default 200). A single tool can have its own limit, `grouper.ai.agent.tool.<toolName>.maxPageSize`. When a result is capped, the assistant is told, so it asks for the next page or narrows the request rather than taking one page for the whole list.

MCP clients have a separate limit, `grouper.mcp.maxPageSize` (and `grouper.mcp.tool.<toolName>.maxPageSize`). It is blank by default, meaning no limit, since an MCP client's own AI account pays for what it asks for. Set it to protect the server from very large queries.

When a person reaches the question or daily token limit, the message tells them to contact their Grouper administrator for a higher limit. To point them at a help desk instead, override the text keys `aiAgentStatus_questionLimitReached` and `aiAgentStatus_limitReached`.

### Raising limits for a group

People who use the assistant all day, like staff, may need more than the defaults. An override gives the members of a group higher limits:

```
grouper.ai.agent.limitOverride.staff.groupName = ref:it:staff
grouper.ai.agent.limitOverride.staff.maxQuestionsPerUserPerDay = 200
grouper.ai.agent.limitOverride.staff.maxTokensPerUserPerDay = 5000000
grouper.ai.agent.limitOverride.staff.maxTokensPerConversation = 500000
```

- For each limit, the highest among the groups a person is in wins. An override can only raise a limit, never lower it.
- Leave a limit blank to keep the default, or set it to `-1` to remove that limit for the group.
- A value that is not a number is ignored and logged.
- Membership is checked when a message starts, and is cached for a minute.
- The screen shows each person the limits that apply to them.
- MCP tools cannot change these groups, so the assistant cannot be used to raise its own user's limits. That list is read when Grouper starts, so an override group added later outside `etc` is only protected after a restart. Manage who can change these groups like any access group.

### Daily question limit

This is the limit people are shown: "Questions used today: 18 / 50".

- Each message a person sends is a question. Approving or declining changes does not count.
- It counts questions rather than conversations, so the number people see matches what they do, and starting a new conversation doesn't reset anything. The conversation limit caps what any one conversation can cost. For people who work in long sessions, raise their limit with a group override.
- Checked when a message starts, never part way through.
- It resets at midnight in `grouper.ai.agent.timeZone` (for example `America/New_York`), or in the server's time zone if that is blank. Set it if your UI nodes might not all run in the same time zone.
- With the conversation limit, it bounds what one person can cost in a day: the question limit multiplied by the most one message can use. Set it from that number, not from what a typical question costs.

### Daily token limit

A second check, for someone who runs expensive conversations all day. Most people should never reach it. Its message is worded differently from the question limit's, so it is clear which one was hit.

- Counted as input tokens the provider did not read from its cache, plus output tokens. Cached input costs a fraction of the rest.
- The day is counted in `grouper.ai.agent.timeZone`, the same as the question limit, so it resets at midnight there.
- A value that is not a number counts as 0, so nobody can use the assistant until it is fixed. An error is logged.
- Checked when a message starts, never part way through. A message that takes a person over the limit finishes, and the next one is refused, so a person can go over by one message. The conversation stays on the page until they log out or their session ends; it is not kept for the next day.
- Someone using the assistant in several browsers at once can start a message in each at the same moment, and each passes the check, so they can go over by one message per extra browser. This is not worth locking over; the next message in any of them is refused as usual.
- Approving changes is not checked against the limits, so a task is never left half done. Each approval lets the assistant carry on with a fresh step allowance, so a message with several rounds of approvals can go further over. Every round needs the person to approve it, and each is bounded like a message by the step, look-up and tool result limits.
- When there is a limit, the screen shows the person how much of it they have used today.

### Conversation limit

- Counted the same way as the daily limit, including the summaries of older messages.
- Checked when a message starts, never part way through. A message that takes the conversation over its limit finishes, including any changes the person approves along the way, and the next one is refused until the person starts a new conversation.
- Together with the step, look-up and message size limits, it puts a ceiling on what one conversation can cost, give or take its last message. Choose the value from the model's price and the most you want one conversation to cost.
- When there is a limit, the screen shows how much of it this conversation has used.

## Usage table

`grouper_ai_agent_usage` has one row per person per day: messages sent, calls to the model, input tokens, cached input tokens and output tokens. It is shared by all UI nodes, and the database does the adding, so two nodes counting at once both count.

| Column | Meaning |
| --- | --- |
| `member_internal_id` | The person (`grouper_members.internal_id`). Part of the primary key |
| `usage_day` | The day, as `yyyymmdd` in `grouper.ai.agent.timeZone`. Part of the primary key, and indexed |
| `message_count` | Messages the person sent, which the daily question limit counts. Approving or declining changes is not counted |
| `model_call_count` | Calls made to the AI provider |
| `input_tokens` | Input tokens, including those read from the provider's cache |
| `cached_input_tokens` | Input tokens read from the provider's cache |
| `output_tokens` | Output tokens |
| `last_updated_micros` | When the row last changed, microseconds since 1970 |

If a daily limit is set and this table cannot be read, for example because upgrade task V45 has not run yet, the assistant refuses new messages and says it cannot check usage limits right now. The error is in the Grouper UI log. A limit is never silently skipped. Recording usage works the other way: if a row cannot be written, the error is logged and the assistant carries on, since that usage is already spent.

Rows older than `loader.retain.db.ai_agent_usage.days` (grouper-loader.properties, default `365`, `-1` for forever) are deleted by the daily `OTHER_JOB_cleanLogs` daemon.

### Usage report

Grouper sysadmins can see usage per person over a range of days in **Miscellaneous > Administration > AI assistant usage**. People are listed most first, with a total for everyone, and at most 1000 are shown. The link shows once the assistant has been set up (`grouper.ai.agent.enabled` is true or `grouper.ai.agent.externalSystemConfigId` is set), and stays after the assistant is turned off, so past usage can still be looked at.

The report shows tokens, not money. Token counts are exactly what the provider reported. A cost worked out in Grouper would go wrong as soon as prices or the model changed, and could not include cache-write charges or a gateway's discounts. For what the assistant actually costs, use the provider's console or the gateway's billing, where the spend limit is set too.

To query the table directly, for example for the heaviest users this month:

```sql
select m.subject_id, sum(u.input_tokens - u.cached_input_tokens + u.output_tokens) as tokens
from grouper_ai_agent_usage u join grouper_members m on m.internal_id = u.member_internal_id
where u.usage_day >= 20261001
group by m.subject_id order by tokens desc;
```

## Call log

`grouper_ai_agent_call_log` has one row per call to the AI provider, whether it worked or failed. Use it to match the provider's bill call by call, or to look into a failed or slow call. The usage table only has daily totals.

| Column | Meaning |
| --- | --- |
| `internal_id` | Primary key |
| `member_internal_id` | The person (`grouper_members.internal_id`) |
| `conversation_id` | The conversation in the person's UI session, to group the calls of one conversation |
| `provider` | `anthropic` or `openai` |
| `model` | The model asked for, from `grouper.ai.agent.model` |
| `call_type` | `turn` (answering the person or asking for tools) or `summary` (summarizing older messages) |
| `outcome` | `ok`, `error`, or `contextTooLong` (the provider said the conversation is too long for the model) |
| `stop_reason` | Why the model stopped: `endTurn`, `toolUse`, `maxTokens`, `refusal` or `other`. Empty if the call failed |
| `tool_call_count` | Tools the model asked to run. Empty if the call failed |
| `input_tokens` | Input tokens, including those read from or written to the cache. Empty if the call failed |
| `cached_input_tokens` | Input tokens read from the cache |
| `cache_write_input_tokens` | Input tokens written to the cache; 0 if the provider doesn't report them |
| `output_tokens` | Output tokens |
| `provider_request_id` | The id the provider's support, or a gateway's logs, can look the call up by (see below) |
| `error_summary` | What went wrong, with no text from the response. Empty if the call worked |
| `started_micros` | When the call started, microseconds since 1970. Indexed, and indexed with `member_internal_id` |
| `duration_micros` | How long the call took, in microseconds |

The request id is read from the first response header listed in `grouper.ai.agent.requestIdHeaders` that comes back. The default list is `request-id` (Anthropic), `x-request-id` (OpenAI), then `x-litellm-call-id`, a LiteLLM gateway's own id for when it doesn't pass the provider's on. Add the header of any other gateway you use.

No conversation text is kept. For a failed call, the row keeps the HTTP status and the provider's error type and code. A response that came back but could not be read keeps the provider's request id too, since the provider may have billed for it. For any other failure, such as a timeout, it keeps only the Java exception types. If a row cannot be written, the error is logged and the assistant carries on.

Rows older than `loader.retain.db.ai_agent_call_log.days` (grouper-loader.properties, default `365`, `-1` for forever) are deleted by the daily `OTHER_JOB_cleanLogs` daemon.

For example, the calls that failed since a given time:

```sql
select m.subject_id, c.provider, c.model, c.call_type, c.outcome, c.error_summary, c.provider_request_id
from grouper_ai_agent_call_log c join grouper_members m on m.internal_id = c.member_internal_id
where c.outcome <> 'ok' and c.started_micros >= 1790000000000000
order by c.started_micros desc;
```

`started_micros` is microseconds since 1970. Set the number to the start of the period you want.

## Data and privacy

- **Sent to the provider:** the conversation, including tool results such as group names, member lists and attribute values, and the person's own subject id, source and name, so the assistant knows who "me" is. Choose a provider and an account whose data terms suit this data.
- **OpenAI** is called with `store: false`, so OpenAI does not keep the conversation. Requests ask for the model's reasoning back in encrypted form, which a reasoning model needs to carry on after calling a tool. If a model without reasoning refuses requests because of this, set `grouper.ai.agent.openai.includeReasoningContent` to `false`.
- **Anthropic** reruns a request its model declines on a fallback model, unless `grouper.ai.agent.anthropic.serverSideFallback` is `false`.
- **Kept by Grouper:**
  
  
  
  - The conversation is kept in the person's UI session only, and is gone when they log out or the session ends.
  - Tool calls are kept in the tool log.
  - Daily totals are kept in the usage table.
  - Each call to the provider is kept in the call log, with its tokens, outcome and request id but no text.
  - Conversations themselves are not stored.
- The provider's API key is never written to logs.

## Using it

- Type a message and select **Send**. The assistant works in the background; **Stop** ends it after the current step.
- Each tool the assistant used can be expanded to show what it was asked and what came back.
- When it wants to make changes, they are listed for approval, all checked. Uncheck any you don't want, then select **Make N changes** (the button counts the checked ones), or **Decline all**. A single change just has **Make this change** and **Decline**. Sending a new message instead declines them.
- **New conversation** starts over.
- The conversation is kept in the person's UI session, so with more than one UI node the load balancer needs sticky sessions. The assistant works on the node that took the message. If that node goes down, the conversation is lost, and the person starts a new one.
- When a new message starts, long tool results from earlier messages are replaced with a short note (the assistant can look them up again), so only the latest message carries its full results. Long conversations are summarized as they go. If a summary can't be made because the model declines or the older messages are too long to summarize, the older messages are removed anyway, and the assistant is told some earlier messages are gone, so a long conversation stays bounded. If the provider fails for another reason, such as a timeout, nothing is removed and the summary is tried again at the next message. If a conversation still becomes too long for the model (for example, a few very large results in the latest messages), the provider refuses it and the assistant tells the person to start a new conversation, without calling the provider again for that conversation.
- If the provider, or a gateway in front of it such as LiteLLM, says it is over its rate limit, the assistant waits and tries again. It waits as long as the provider says, or 20 seconds at a time, for up to `grouper.ai.agent.rateLimitMaxWaitSeconds` (default 90) on each call. Meanwhile, the page says the AI service is over its rate limit and counts down to the next try. **Stop** ends the wait. If the limit still hasn't cleared, the assistant stops and says the AI service is busy. The conversation is kept, and sending another message later carries on from where it stopped.
- A gateway's per-minute token limit can be reached quickly, since every call sends the tool definitions, cached or not. Set it well above what one conversation uses; otherwise most questions spend a minute or more waiting.

## All settings

Every setting the assistant adds, in one place. The sections above explain them in more detail.

**grouper.properties: turning it on and the provider**

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.enabled` | `false` | Turns the assistant on |
| `grouper.ai.agent.users` | `etc:aiAgent:aiAgentUsers` | Who can use the assistant |
| `grouper.ai.agent.provider` | `anthropic` | `anthropic` (Messages API) or `openai` (Responses API) |
| `grouper.ai.agent.externalSystemConfigId` | none, required | The web service bearer token external system with the provider's address and key |
| `grouper.ai.agent.model` | none, required | The provider's id for the model, or the name a gateway such as LiteLLM gives it |
| `grouper.ai.agent.allowInsecureEndpoint` | `false` | Allow an `http://` endpoint. Development only |
| `grouper.ai.agent.httpTimeoutMillis` | `300000` | How long to wait for the provider |
| `grouper.ai.agent.anthropic.serverSideFallback` | `true` | Anthropic reruns a request its model declines on a fallback model |
| `grouper.ai.agent.openai.includeReasoningContent` | `true` | Ask OpenAI for the model's reasoning in encrypted form, which a reasoning model needs with `store: false`. Turn off if a model refuses requests because of it |
| `grouper.ai.agent.requestIdHeaders` | `request-id, x-request-id, x-litellm-call-id` | Response headers the id of each call is read from, first found wins. Add the header of any other gateway |
| `grouper.ai.agent.systemPrompt` | the built-in one | Replaces the built-in instructions to the model. A replacement does not get later improvements to the built-in one, so use it only if you must |

**grouper.properties: limits**

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.maxQuestionsPerUserPerDay` | none, required (`-1` for no limit) | Messages one person may send a day |
| `grouper.ai.agent.maxTokensPerConversation` | none, required (`-1` for no limit) | Tokens one conversation may use |
| `grouper.ai.agent.maxTokensPerUserPerDay` | no limit | Tokens one person may use a day |
| `grouper.ai.agent.limitOverride.<id>.groupName` |  | A group whose members get higher limits |
| `grouper.ai.agent.limitOverride.<id>.maxQuestionsPerUserPerDay`, `.maxTokensPerUserPerDay`, `.maxTokensPerConversation` | the default | That group's limits. They can only raise a limit; `-1` removes it |
| `grouper.ai.agent.timeZone` | the server's | The time zone days are counted in, e.g. `America/New_York` |

**grouper.properties: how a message runs**

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.maxStepsPerMessage` | `10` | Most calls to the model for one message or set of approvals |
| `grouper.ai.agent.maxLookupsPerStep` | `10` | Most look-ups run in one step. Changes are not counted |
| `grouper.ai.agent.maxToolResultChars` | `20000` | Longer tool results are cut off before the model sees them |
| `grouper.ai.agent.maxOutputTokens` | `16000` | Most the model may write in one call |
| `grouper.ai.agent.maxUserMessageChars` | `8000` | Longest message a person can send |
| `grouper.ai.agent.verbatimExchanges` | `6` | Latest exchanges kept word for word; older ones are summarized |
| `grouper.ai.agent.rateLimitMaxWaitSeconds` | `90` | Most seconds to wait, for each call, for a provider's or gateway's rate limit to clear. `0` means don't wait |
| `grouper.ai.agent.maxConcurrentTurnsPerNode` | `10` | Most requests worked on at once on one UI node. Needs a restart to change |

**grouper.properties: rows from list tools**

| Property | Default | Meaning |
| --- | --- | --- |
| `grouper.ai.agent.maxPageSize` | `200` | Most results a list tool gives the assistant in one call |
| `grouper.ai.agent.tool.<toolName>.maxPageSize` |  | The same for one tool |
| `grouper.mcp.maxPageSize` | no limit | The same for MCP clients |
| `grouper.mcp.tool.<toolName>.maxPageSize` |  | The same for one tool, for MCP clients |

**grouper-loader.properties: retention**

| Property | Default | Meaning |
| --- | --- | --- |
| `loader.retain.db.ai_agent_usage.days` | `365` | Days of daily usage rows kept; `-1` for forever |
| `loader.retain.db.ai_agent_call_log.days` | `365` | Days of call log rows kept; `-1` for forever |

**Web service bearer token external system (grouper-loader.properties)**

The assistant uses `grouper.wsBearerToken.<configId>.endpoint`, `accessTokenPassword`, and for Anthropic directly `httpHeader = x-api-key` with `prependBearerTokenPrefix = false`. See Setting it up.

## Upgrading

Upgrade task **V45** (7.7.0) creates `grouper_ai_agent_usage` and `grouper_ai_agent_call_log` with their indexes and column comments, and adds `grouper_mcp_tool_log.entry_path` (`mcp` or `ui`; empty for calls logged before it existed). New installs get all of this from the install DDL. The assistant's group, `etc:aiAgent:aiAgentUsers`, is created empty when Grouper starts. Add V45 to the [Grouper upgrade tasks](https://spaces.at.internet2.edu/spaces/Grouper/pages/318572008/Grouper+upgrade+tasks) page.
