---
title: "OIDC authentication to Grouper Web Service"
space: Grouper
pageId: 28547847
version: 11
lastUpdated: 2026-07-01T05:46:02.889Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547847/OIDC+authentication+to+Grouper+Web+Service
---

> The Grouper Web Service can authenticate a caller with an OIDC bearer token. The caller passes an OIDC authorization code in the `Authorization` header; Grouper exchanges it for an access token, reads the user's claims, and resolves the Grouper subject. OIDC authentication to the Web Service was available in **v2.6.0+** (introduced 2021). It is configured through the same Grouper OIDC external system used for OIDC authentication to the Grouper UI, with `useForWs = true`.

 > Configuring the OIDC external system requires editing `grouper.properties` (server-side file access plus a restart) or the Grouper configuration UI, which a Grouper sysadmin (wheel or root) can use. This is an authentication mechanism, not a Grouper privilege: a successful call authenticates the Web Service as the subject resolved from the token, and that subject's own privileges then apply.

 

## How it works

 When a Web Service request arrives with an OIDC bearer token, Grouper:

 

- extracts the OIDC authorization code from the `Authorization` header (see below);
- calls the token endpoint with an HTTP POST and the client id / client secret (basic authentication) to exchange the code for an access token;
- calls the userinfo endpoint with the access token to get the user's claims (by default; the claims can alternatively come from the id token, depending on `claimSource`);
- resolves the Grouper subject from the configured claim.

 

## Configure the OIDC external system

 Add the OIDC external system in `grouper.properties` and set `useForWs = true` to enable Web Service authentication. The same external system can serve the UI (`useForUi`) and the Web Service (`useForWs`).

 
```text
############################################
## OIDC external system (grouper.properties)
############################################

# enable Web Service authentication for this external system (default false)
grouper.oidcExternalSystem.myOidcConfigId.useForWs = true

# whether this connector is enabled (default true)
grouper.oidcExternalSystem.myOidcConfigId.enabled = true

# exchange the OIDC code for an access token (required)
grouper.oidcExternalSystem.myOidcConfigId.tokenEndpointUri = https://idp.institution.edu/idp/profile/oidc/token

# read the user's claims from the access token (required when claimSource = userInfoEndpoint, the default)
grouper.oidcExternalSystem.myOidcConfigId.userInfoUri = https://idp.institution.edu/idp/profile/oidc/userinfo

# OAuth client credentials (required)
grouper.oidcExternalSystem.myOidcConfigId.clientId =
grouper.oidcExternalSystem.myOidcConfigId.clientSecret =

# scope to retrieve from OIDC, e.g. openid email profile (required)
grouper.oidcExternalSystem.myOidcConfigId.scope = openid email profile

# how to resolve the Grouper subject from the claims:
#   subjectId type is subjectId, subjectIdentifier, or subjectIdOrIdentifier (required)
grouper.oidcExternalSystem.myOidcConfigId.subjectIdType = subjectId
#   claim that holds the subject id / identifier (default preferred_username), e.g. employeeId
grouper.oidcExternalSystem.myOidcConfigId.subjectIdClaimName = employeeId
#   optional: restrict resolution to one or more subject sources
grouper.oidcExternalSystem.myOidcConfigId.subjectSourceIds =

# optional redirect uri, needed for the oidcWithRedirectUri token form below, e.g. https://my.app/someUrlBackFromIdp
grouper.oidcExternalSystem.myOidcConfigId.redirectUri =
```

 

## Make a web service call

 Pass the OIDC authorization code as a bearer token in the `Authorization` header. Grouper decodes the claim using the named external system config id and authenticates the Web Service as the resolved subject.

 
```text
Authorization: Bearer oidc_configId_abc123def456

-or-

Authorization: Bearer oidcWithRedirectUri_configId_lmn432rew987_abc123def456
```

 Here `configId` is the external system config id, `abc123def456` is the OIDC authorization code, and (in the second form) `lmn432rew987` is the base64-encoded redirect uri.

 

## Developers: test against a local OIDC provider

  For local development you can stand up a disposable OIDC provider instead of pointing at a real IdP. Sample users ship in the Grouper source at:

 
```text
grouper/misc/oidc_container_users.json
```

 Run the local OIDC docker container, binding that users file (replace the source path with wherever you saved it):

 
```bash
docker run --rm -d -p 9000:9000 \
  --mount type=bind,src=/path/to/oidc_container_users.json,dst=/tmp/users.json \
  -e "REDIRECTS=http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc" \
  -e "USERS_FILE=/tmp/users.json" \
  qlik/simple-oidc-provider
```

 Then start the login flow at:

 
```text
http://localhost:9000/auth?response_type=code&scope=openid+email+profile&client_id=foo&redirect_uri=http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc
```
