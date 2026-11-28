---
title: "Grouper web services - authentication - OAuth / OIDC"
space: Grouper
pageId: 28555869
version: 8
lastUpdated: 2026-11-20T21:00:06.401Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555869/Grouper+web+services+-+authentication+-+OAuth+OIDC
---

You can have Grouper use an OIDC external system to authenticate web service calls and if it is not OIDC fallback to another mechanism.

An example is a ChatGPT action can authenticate the user (a second time if already using SAML) with OIDC, and pass the access token in the Authorization header to Grouper WS. Grouper WS will act like the user is calling the web service.

## Manage users

Users using the OIDC need to be WS users and have the appropriate access (e.g. ability to run GSH template web services).

## Make an OIDC external system

## Configure

Note the file locations in the container are listed in the [v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation). These actions can be done on the Grouper UI if configuration is stored in the database

| File | Config | Value | Description |
| --- | --- | --- | --- |
| grouper-ws.properties | ws.security.non-   rampart.authentication.class | edu.internet2.middleware.grouper.ws.   security.WsGrouperOauthAuthentication | Use this as the first authenticator |
| ws.security.authn.oauth.fallback.class | edu.internet2.middleware.grouper.ws.   security.WsGrouperKerberosAuthentication | If the request is not OAuth / OIDC, then fallback to this authenticator for other traffic (the previous authenticator) |
| ws.security.authn.oauth.   clientVersion.X.Y.Z.grouperOidcConfigId | someOidcConfigId (external system) | If using WsGrouperOauthAuthentication, then this version from the WS client will use this grouperOidcConfigId for oidc authentication. Note, this can instead be set in the HTTP request with the parameter: grouperOidcConfigId    (The HTTP parameter is the preferred option if available from OIDC relying party, then the version can be the actual version)  Either way, the named OIDC external system must be enabled, have useForWs = true and claimSource = userInfoEndpoint, and set wsAccessTokenValidation (see the grouper.properties rows below), or WS access tokens are refused. |
| ws.logRestRequestDebugInfo | false \| true | If set to false, retain existing behavior up to v4.4.0, in which WsRestGshTemplateExecRequest returned success even though the GSH script had a non-success status (by explicitly setting status, a non-zero GrouperUtil.gshReturn(int code), or adding output lines of type error) |
| ws.security.authn.oauth.debugAccessTokens | false \| true | Deprecated. As of v7.9.1 and v6.5.1 this setting is ignored: access tokens are never written to the logs. Debug logging shows a SHA-256 fingerprint of the token instead, which is enough to tell tokens apart. If it is set to true, a warning is logged once. |
| grouper.properties | grouper.oidcExternalSystem.<configId>.   useForWs | true | Required. The OIDC external system must be marked for WS use. |
| grouper.oidcExternalSystem.<configId>.   wsAccessTokenValidation | jwt \| introspection \| userInfoOnly | Required (no default; blank refuses every WS access token). How Grouper checks that the access token was issued for Grouper WS. jwt: also set wsAccessTokenIssuer and wsAccessTokenJwksUri. introspection: also set wsAccessTokenIntrospectionUri (the config's clientId and clientSecret are used). For both, set wsAccessTokenAudiences and/or wsAccessTokenAllowedClientIds. userInfoOnly is the older, weaker check (any token the userinfo endpoint accepts) and logs a warning; use it only as a temporary step. |

As of v7.9.1 and v6.5.1, these grouper.properties settings are required. On a patched release where the external system screen does not show the new keys, set them in grouper.properties or the database configuration.

## Debug

Note, if you want to debug this, put this in the /slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalLoggers.xml.txt:

```
        <Logger name="edu.internet2.middleware.grouper.ws.security.WsGrouperOauthAuthentication" level="debug" additivity="false">
            <!--<AppenderRef ref="logpipe_grouper_error"/>-->
            <AppenderRef ref="file_grouper_error"/>
            <!--<AppenderRef ref="stderr"/>-->
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.ws.j2ee.ServletFilterLogger" level="debug" additivity="false">
            <!--<AppenderRef ref="logpipe_grouper_error"/>-->
            <AppenderRef ref="file_grouper_error"/>
            <!--<AppenderRef ref="stderr"/>-->
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet" level="debug" additivity="false">
            <!--<AppenderRef ref="logpipe_grouper_error"/>-->
            <AppenderRef ref="file_grouper_error"/>
            <!--<AppenderRef ref="stderr"/>-->
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.ws.util.GrouperWsLog" level="debug" additivity="false">
          <!--<AppenderRef ref="logpipe_grouper_ws"/>-->
          <AppenderRef ref="file_grouper_ws"/>
          <!--<AppenderRef ref="stderr"/>-->
        </Logger>
```
