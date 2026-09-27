---
title: "AI agent in the Grouper UI: sharing one tool layer with MCP"
space: GrIntDev
pageId: 228589569
version: 4
lastUpdated: 2026-09-21T14:21:04.784Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/228589569/AI+agent+in+the+Grouper+UI+sharing+one+tool+layer+with+MCP
---

*Loose design doc, for discussion. Nothing here is built yet. Measured against Grouper v7.*

## Goal

Run an AI agent inside the Grouper UI that uses the same tools the MCP endpoint exposes. Today the MCP tool classes and the web service logic they call live in grouper-ws, and the UI cannot reach them. One tool layer, two front doors.

The point is not code reuse for its own sake. Read/write classification, scope enforcement, and audit should exist in exactly one place. Two implementations of "is this tool a write" is how that becomes a security bug later.

## Proposal

Move the non-servlet logic, beans, and exceptions from grouper-ws into the grouper core project, **keeping the same package names**. Java packages do not have to match the Maven module, so `edu.internet2.middleware.grouper.ws.*` stays as-is and nothing left in grouper-ws needs an import change.

grouper-ws becomes a war that wires HTTP to logic living in core, which is arguably what it should have been all along.

## Is the move feasible

Yes, and it is smaller than it sounds. Of the 343 Java files in the main ws source tree, roughly 330 have no HTTP in them at all -- they are beans, logic, and exceptions. About a dozen classes are the actual HTTP edge: the `GrouperServiceJ2ee` filter, the Axis SOAP endpoint, the REST dispatcher, most of the security classes, and the four MCP servlets. Those stay in grouper-ws.

The dependencies that would normally block the move are already in place: `grouper/pom.xml` declares both `javax.servlet-api` and `jackson-databind`. grouper-ui depends on grouper and references zero ws classes today, so it picks all of this up with no new module dependency. The split has already started -- a few MCP support classes live in core while the ~40 tool classes sit in grouper-ws.

Not moving: the versioned trees `grouper-ws_v1_6` through `_v2_5` (about 700 files of WS contract compatibility, not shared logic) and the test tree.

Recommendation is to stop short of moving the servlets. Putting the SOAP endpoint, REST dispatcher, and OAuth servlets into grouper.jar means every GSH run and every daemon JVM carries the web service's HTTP surface.

## The one piece that is real work

`GrouperServiceJ2ee` is a `Filter`, 1265 lines, and thirty files outside it depend on it. But of its ~111 calls from elsewhere, only 3 touch a servlet type; 86 of them are just `retrieveDebugMap`. The rest is per-request context: logged-in subject, act-as subject, request start time. `GrouperServiceLogic` is 11,329 lines and touches `HttpServletRequest` exactly once.

So the split is: the ThreadLocal context and its ~7 accessors move to a context class in core; the Filter stays in grouper-ws and populates it. The debug-map calls are a mechanical find-replace.

> This is also what makes the UI path possible at all. The UI has no filter in that chain, so if the accessor stays welded to a Filter, the UI cannot populate it without pretending to be a web service request.

## Authentication and scope: two entry paths

OAuth does two jobs in the MCP path: it authenticates a caller Grouper has never seen, and it captures a consent grant from the user to a third-party client.

**The UI agent needs neither.** The user is already authenticated to the UI session, and the client is Grouper itself, so there is no third party to consent to. No redirect, no token, no consent screen.

What the UI still needs is a scope selection, but that is session state the user sets on the help/AI screen. It is a guardrail the user puts on their own assistant, not a trust boundary against a foreign app.

That should drive the defaults. MCP defaults narrow because it is protecting the user from a client they installed. The UI agent's risk is the assistant doing something unintended, so read-only by default with an explicit opt-up is reasonable, widenable mid-conversation since scope is checked at execution.

The abstraction the tool layer needs is small: logged-in subject, act-as subject, current scope. The OAuth servlet fills it from the token; the UI fills it from the session plus the screen. Neither `GrouperServiceLogic` nor the tool classes should know which.

Two constraints worth stating explicitly:

- **The grant is an intersection, never a grant of new authority.** Effective permission is what the user can do in Grouper AND what they approved for the session. If it is computed from the consent record alone, the consent screen becomes a privilege-escalation surface.
- **Audit should distinguish the paths.** "via UI AI agent" and "via MCP client X" are different enough that someone reading the audit trail later will want to tell them apart.

## Runtime loop

*Agentic loop in the Grouper UI. SVG source is attached to this page as agentic-loop.svg.*

1. User sends a message to the Grouper UI.
2. UI sends conversation history plus tool definitions to the LLM.
3. LLM replies with a structured tool call, not prose.
4. UI classifies it. Writes and deletes go to a confirmation gate; reads bypass it.
5. On approval the UI executes local code. Session scope is enforced here, at execution, so a change on the screen takes effect on the next call.
6. The result is appended to history as a tool result.
7. UI re-enters the loop with updated history, up to a step cap.
8. When the LLM returns text with no tool call, that text is the answer.

The LLM is isolated throughout. It never touches Grouper, the database, or any API. It reads text and decides what should happen next; the UI is the only thing that runs code.

Supporting pieces:

- **Confirmation gate.** Every mutating call pauses for the user. Read/write classification is a property of the registered tool, not inferred from arguments the model produced.
- **History store.** Recent turns verbatim, older ones collapsed to a rolling summary. Compaction lives on the UI side. Approval and scope state are never carried in a summary, since a summary is model-authored text.
- **Tool results carry errors.** A denied scope check or a failed call comes back as a tool result the model can explain, not an exception.

## Steps to get there

The package move is step 2 of 8. Steps 1 through 4 are refactoring with no user-visible change and can land on their own; steps 5 through 8 build the agent.

| Step | What | Done when |
| --- | --- | --- |
| 1 | Extract the per-request context from `GrouperServiceJ2ee` into a context class in core: logged-in subject, act-as subject, request start, debug map. The filter stays in grouper-ws and populates it. | ws builds and passes its tests with the filter only populating, not owning, the context. |
| 2 | Move the non-servlet logic, beans, and exceptions into grouper core, same package names. Update the Maven modules, build.xml, and container assembly. | grouper.jar carries the logic, the ws war does not double-package it, and SOAP/REST/SCIM/MCP all still answer. |
| 3 | Define the tool layer contract in core: a registry where each tool declares its name, schema, read/write classification, and required scope. Execution checks scope against the context and writes audit naming the entry path. | A tool can be invoked from plain Java with no HTTP anywhere in the call stack. |
| 4 | Repoint the MCP servlets at the core registry. No behavior change -- this is the regression gate. | Existing MCP clients see identical responses. |
| 5 | Build the UI entry path: populate the same context from the UI session plus a session scope, no OAuth, no token. | A tool runs from a UI request, as the logged-in user, scope enforced. |
| 6 | Build the agent loop: LLM call, tool dispatch, confirmation gate on writes, history store with compaction, step cap. | The eight-step loop above runs end to end. |
| 7 | Build the UI screen: chat, scope selection, confirmation prompts, and tool calls and results shown rather than hidden. | A user can complete a real task and see exactly what was run. |
| 8 | Audit, config, and docs: which LLM, where the key lives, how to turn the whole feature off. | An institution can enable or disable it deliberately. |

Steps 1 and 2 are the ones that need community agreement before anyone writes code, since they change where core classes live.

## Build and cleanup notes

- Maven modules, build.xml, and the container assembly all need updating.
- The ws war must not double-package classes now arriving via grouper.jar.
- Verify the axis/wsdl generation still finds coresoap beans across the module boundary.
- `grouper-ws/grouper-ws/webapp/WEB-INF/classes/edu/internet2/middleware/grouper/ws/mcp` is stale compiled output that could shadow the new location.
- grouper.jar grows. Mostly bean classes, but it changes the core artifact's public surface and should be called out.

## Cost and Usage Limits

- With MCP the institution just runs the endpoint. Users connect their own client and pay for their own usage. The UI agent works the other way: every call goes through the institution's API key, so the institution pays, and the bill grows as more people use it. What you get for that is reach -- people with no shell account and no AI subscription can use it -- plus a say in which vendor is used, what the contract says, and how long data is kept. That last part is also the answer to where membership data ends up, which is something nobody controls today under MCP.
- Every step of the loop re-sends the system prompt, all the tool definitions, and the whole conversation so far. That makes big tool results expensive twice over: if `get_members` returns 50,000 rows, those rows sit in the history and get re-sent on every step after that. Each tool needs a row limit and a way to tell the model its result was cut short. Put that in the tool registry in step 3, or it has to be added to all forty tools later.
- Controls worth having, most useful first:
  
  
  
  - Only let people in a particular Grouper group use it, rather than everyone with a login.
  - Row limits on tool results.
  - A token budget per conversation, on top of the step cap.
  - A per-user quota.
  - A spend limit on the API key, set at the vendor, in case something in Grouper does not work the way it should.
- Quota is easier to explain if it counts conversations instead of tokens. A conversation already has a step cap and a token budget, so there is a limit to what one can cost. Counting tokens means a user could be stopped at their twelfth question when the screen said they had fifty, which just looks broken.
  
  
  
  - This only works if a conversation really is capped. Three things can make one expensive: a user pasting a large amount of text into the box, a question that turns into twenty tool calls, and a conversation that runs for forty turns. Those need a limit on message size, the step and row limits, and the conversation token budget. With all three, the most a user can cost is their quota multiplied by the most expensive conversation possible. Set the quota from that number, not from what a typical conversation costs.
  - When a conversation runs out of budget it ends, and starting a new one uses up another conversation from the quota. So a long run of expensive questions draws the quota down faster, and nobody has to explain tokens to anyone.
  - Add a daily token limit as a second check, for the person who runs expensive conversations all day. Most people will never reach it. Word its message differently from the conversation-quota message so it is obvious which limit was hit.
  - Keep usage in a table by user and day, with token counts alongside for reporting. The UI runs on more than one node, so a counter held in memory would let every node hand out the full limit. Read the count when a conversation starts and record real usage from the LLM response as each turn finishes. If two nodes start at the same moment a user might get one extra conversation, which is not worth locking over.
  - Use calendar days in the institution's timezone. A rolling window is more accurate but harder to show on a screen, and "resets at midnight" is easy to understand.
  - Set the default limit in config and let group membership raise it. Staff who use it all day need more than someone who uses it twice a week, and this is the same mechanism that grants access in the first place, so there is nothing new to build.
  - Do not stop a conversation in the middle. If someone reaches the limit on their last question, let it finish, since most of the cost is already spent, and block the next one instead. Say when the limit resets and who to ask for more, or it turns into a ticket.
- Put token counts in the tool log and build an admin report of usage by user, so a problem shows up before the bill does. Show users something simpler, like "18 of 50 questions used today," so running out is never a surprise. Leave dollar figures in the admin report -- someone doing their job should not have to decide whether a question was worth three cents.
- Keep the system prompt and tool definitions exactly the same for every user, so they share a cached prefix instead of being charged in full on every call. Worth writing down, because it rules out customizing the prompt per user later.

## Open questions

- Move `GrouperServiceJ2ee` whole, or extract the context class? Moving compiles, since servlet-api is already in core, but it puts a Filter in grouper.jar and leaves the UI still needing a way around the filter.
- Does anything depend on the ws beans being absent from grouper.jar today?
- Should the tool registry be a core service with a registration API, so institutions can add their own tools to both surfaces?
- Where does the conversation history live: memory, session, or a table?
- Is the confirmation gate per-call, or does it support "approve this kind of call for the rest of the session"?

## Not in scope

- Moving the servlets themselves.
- Moving the versioned WS bean trees.
- Changing the WS contract in any way. This is a relocation, not a redesign.
