---
title: "GPT AI calling Grouper web service with basic auth"
space: Grouper
pageId: 28555861
version: 2
lastUpdated: 2026-07-01T05:37:14.069Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555861/GPT+AI+calling+Grouper+web+service+with+basic+auth
---

This example uses OpenAI GPTs but could be from any AI vendor that allows web service actions.

GPT's can call Grouper web services (WS) with basic auth. This can be very useful when you would like to GPT to look up specific data, use low level WS calls to perform logic, or to perform actions.

## Using the GPT

This called two web services, one to look up "chris hyzer", and since there was one result, another to see if that Penn ID is in a group. If there are multiple results it would ask you to pick a result.

The conversation can continue.

This time it knew the Penn ID and queried another group

## Architecture

1. A browser logs in to ChatGPT with SSO
2. The GPT has actions that call web services (WS)
  
  1. The GPT knows to use the web services for certain reasons and what the inputs are
3. The result of the WS will be interpreted by the GPT and communicated to the user

## Security

The GPT is protected by WebLogin, and only certain people are invited to use the GPT. GPTs with web services generally need the security of invite-only security. OpenAI can be provisioned by Grouper and can provision groups. GPTs can be protected by group. This automatically deprovisions users who leave the institution from using the tools.

The web services are authenticated by bearer tokens and are able to be called in any fashion by any user. Do not assume some WS operations have any higher clearance than any others.

Users can see the requests and responses of the web services, so do not rely on the GPT hiding any of the implementation.

Any actions done with basic auth are not logged as happening by the user. So if you need auditing for actions, you need to use Oauth WS.

## How to configure

1. Identify a web service
2. Generate the OpenAPI config
3. Test the service
4. Instruct the agent how to use the service and when

### Identify a web service

Identify a JSON / REST web service which authenticates with credentials (bearer token, or basic auth). Note that all users in the GPT can use the credential as they wish (do not trust the agent to have discretion).

Obtain the:

1. Endpoint
2. Credentials
3. Sample input
4. Sample output

In ChatGPT there can be multiple actions, and each action can have multiple operations. Each operation in the same action must use the same endpoint and credentials.

### Generate the OpenAPI config

The credentials in basic auth are the Authorization header value of the user and password. Note: do not use a website to base64 encode those, you can use a program or your browser or a trusted postman type app.

You can paste the sanitized request and response (NOT CREDENTIALS) in the GPT configuration assistant to generate the OpenAPI config.

Click "[Get help from 'Actions GPT'](https://chatgpt.com/g/g-TYEliDU6A-actionsgpt)"

Copy and paste that spec in to the schema section of the action. Edit the schema as you see fit.

Add the consequential flag to false if you do not want the user to be prompted to allow the WS call each time (might only be applicable for OIDC for non-GETs)

```
summary: Search for subjects by name and source.
"x-openai-isConsequential": false
parameters:
```

If there are schema errors, ask AI how to fix them in another window. Make sure the dynamic parts of the WS are extracted as parameters.

### Test the service

Click the test button next to each action, it will ask you for sample inputs, and see the successful web service call:

### Train the agent how to use the WS

You can use the "Create" tab or the "Configure" tab to instruct the GPT how and when to call the agent. Sometimes it is useful to give a sample request and response (even though its in the schema), for various use cases. You can let it know which values are hard coded and which are dynamic. It should understand when to ask the user for information or how to chain together the services into multiple calls.

In this example, this is the training information in the instructions text:

```
If the user wants to do a person lookup or status, ask the user for the name or search 
criteria.  Then call the person search for that criteria.  If there are a small number 
of results, then check some groups for that user using the hasMember action using the 
subject id from the first call.  Here are some useful groups:

- ISC staff: penn:isc:staff:iscstaffNotContractorsNotTempFullTime
- Penn workforce: penn:community:employeeOrContractorIncludingUphs
- Member of Penn: penn:community:activeNonAlumniWithPennname
- Penn affiliate: penn:community:affiliateMember
- Recent affiliate: penn:community:recentAffiliate
- Two step (MFA): penn:community:authentication:twoStepUsers
```
