---
title: "Grouper MCP configuration example"
space: Grouper
pageId: 28554803
version: 22
lastUpdated: 2026-07-01T05:39:35.842Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554803/Grouper+MCP+configuration+example
---

This wiki is an example of configuration. All the options are documented in the [admin guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide).

## Environment variable

Grouper MCP cannot run without this container env set in WS and UI

Docker example in Dockerfile:

```
ENV GROUPER_MCP=true
```

## Local environment variable for self-signed SSL

If you are connecting to a local Grouper (e.g. the Grouper Training Environment or a local Grouper container), you might need an env var for your AI vendor to allow self-signed SSL, e.g. for Claude code on Mac/Linux:

```
export NODE_TLS_REJECT_UNAUTHORIZED=0
```

## grouper.properties

Review the admin guide, but these settings will get you started

```
# base URL for WS
grouper.ws.url = https://grouperws-test.institution.edu/grouper-ws

# readonly external system (readonly replica or a database user which has readonly grants)
grouper.mcp.sqlGrouperExternalSystem = awsProdReadonly

# you dont need these to start, you can try to register, 
# look at the error in the WS logs, and add url regexes as needed. 
# here are some common ones
grouper.oauth.redrectUri.chatgpt.regex = ^https://chatgpt\.com/.*$
grouper.oauth.redrectUri.claudeAi.regex = ^https://claude\.(ai|com)/.*$
# some AIs use localhost and some use 127.0.0.1
grouper.oauth.redrectUri.localhost.regex = ^http\u003A//localhost(:[0-9]+)?/callback$
grouper.oauth.redrectUri.localhost2.regex = ^http\u003A//127.0.0.1(:[0-9]+)?/callback$

```

## Allow access to MCP from users to these "roles"

These roles are auto created to allow access to various tools. Only users in these roles can use MCP. Note, the access to the tools is "as the user" so if a user is not a Grouper admin and they can only manage a handful of folders or groups, that is all the MCP can do. For admin roles, they **must** also be a Grouper admin (must be both, have the role, and be a Grouper admin). For the SQL readonly, there is no row level security, so that should be given to trusted people who can READ the database.

## MCP in UI

See your MCP URL in the UI

## Register an MCP in AI

You can do this command line or in a UI. This is an example for Claude

## Log in with Oauth2

Note: the access token expiration if configurable, but generally you need to authenticate before an AI session. This is the screen in the Grouper UI that MCP users will go to. The options on this screen to approve correspond to the roles that the user has above (which groups they are in).

## Test the MCP

If it can list the tools, it is connected. Maybe also give a simple request.

Here is a simple tool run

Depending on your AI tool and settings it will ask you to approve tool usage
