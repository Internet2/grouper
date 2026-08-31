---
title: "Grouper web services - authentication - OAuth / OIDC"
space: Grouper
pageId: 28555869
version: 6
lastUpdated: 2026-07-01T05:37:13.103Z
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
| ws.security.authn.oauth.   clientVersion.X.Y.Z.grouperOidcConfigId | someOidcConfigId (external system) | If using WsGrouperOauthAuthentication, then this version from the WS client will use this grouperOidcConfigId for oidc authentication. Note, this can instead be set in the HTTP request with the parameter: grouperOidcConfigId    (The HTTP parameter is the preferred option if available from OIDC relying party, then the version can be the actual version) |
| ws.logRestRequestDebugInfo | false \| true | If set to false, retain existing behavior up to v4.4.0, in which WsRestGshTemplateExecRequest returned success even though the GSH script had a non-success status (by explicitly setting status, a non-zero GrouperUtil.gshReturn(int code), or adding output lines of type error) |
| ws.security.authn.oauth.debugAccessTokens | false \| true | if using WsGrouperOauthAuthentication, and you want to see the access tokens in the logs, set this to true generally this should always be false unless you are debugging something in which can set to true temporarily |

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
