---
title: "Grouper external system - Web service - Oauth credential - Adobe"
space: Grouper
pageId: 28547372
version: 7
lastUpdated: 2026-07-12T17:27:25.715Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547372/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Adobe
---

This is an external system which uses Oauth credentials. i.e. there is an authentication endpoint, which gives a Bearer token to present to web services.

| Config | Example | Description |
| --- | --- | --- |
| Config id | adobe  would be in config key:  grouper.wsBearerToken.adobe.scopes | Used in configuration file grouper-loader.properties |
| Authentication type | bearerToken, basicAuth, oauthClientCredentials | Bearer token: just an Authentication header with a value (token can have a prefix, e.g. Bearer: )  Basic auth: Authentication header with basic auth standard  Oauth: Authenticate to the authorize URL and use the token for the services |
| Token URL | https://ims-na1.adobelogin.com/ims/token/v2 | URL for authorization to get a token |
| Service URL | https://usermanagement.adobe.io/v2/usermanagement | URL for the services |
| Client id | sdf6786sdaf876 | Oauth client id for token URL |
| Client secret | sdf79asdf897as | Oauth client secret for token URL |
| Grant type | client_credentials | Oauth strategy |
| Scopes | openid,AdobeID,user_management_sdk | Oauth scopes |
| API key header name | Some-name | Some HTTP header name to include in services (optional) |
| API key password | fd76asdf876 | Value of an HTTP header to include in services (optional) |
| Proxy URL | https://some.server.com:1234 | If you are using a proxy server (not reverse proxy), enter that URL |
| Proxy type | PROXY_HTTP, PROXY_SOCKS5 | Proxy protocol |
| Enabled | true \| false | If this is enabled and can be used |
| Test URL suffix | /groups/whatever | Some service URL suffix to test when clicking the "test" button on the external system page |
| Test HTTP method | GET | HTTP method for test call |
| Test HTTP response code | 200 | Response code expected |
| Test response body regex | .*adminGroupName.* | Run this regex on the response to see if it is valid |

## Use the external system

[Grouper Adobe provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner)
