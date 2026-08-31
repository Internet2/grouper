---
title: "Grouper external system - Web service - Oauth credential - Okta"
space: Grouper
pageId: 28547432
version: 4
lastUpdated: 2026-07-12T17:27:26.192Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta
---

This is an external system which uses Oauth credentials. i.e. there is an authentication endpoint, which gives a Bearer token to present to web services.

## Generate credentials in Okta

Private key is the one that begins with "-----BEGIN PRIVATE KEY-----" and ends with "-----END PRIVATE KEY-----".

## Configure external system

| Config | Example | Description |
| --- | --- | --- |
| Config id | okta  would be in config key:  grouper.wsBearerToken.okta.scopes | Used in configuration file grouper-loader.properties |
| Authentication type | oauthClientCredentials | Bearer token: just an Authentication header with a value (token can have a prefix, e.g. Bearer: )  Basic auth: Authentication header with basic auth standard  Oauth: Authenticate to the authorize URL and use the token for the services |
| Token URL | https://whatever.okta.com/oauth2/v1/token | URL for authorization to get a token |
| Service URL | https://whatever.okta.com/api/v1 | URL for the services |
| Client id | sdf6786sdaf876 | Oauth client id for token URL |
| Client credential type | publicPrivateKey | Public / private key |
| Public key id | abc123 | Oauth client secret for token URL |
| Private key |  | Private key is the one that begins with "-----BEGIN PRIVATE KEY-----" and ends with "-----END PRIVATE KEY-----". |
| Grant type | client_credentials | Oauth strategy |
| Scopes | okta.users.manage okta.groups.manage | Oauth scopes |
| Proxy URL | https://some.server.com:1234 | If you are using a proxy server (not reverse proxy), enter that URL |
| Proxy type | PROXY_HTTP, PROXY_SOCKS5 | Proxy protocol |
| Enabled | true \| false | If this is enabled and can be used |
| Test URL suffix | /groups/abc123xyz456 | Some service URL suffix to test when clicking the "test" button on the external system page |
| Test HTTP method | GET | HTTP method for test call |
| Test HTTP response code | 404 | Response code expected |
| Test response body regex | .*not.* | Run this regex on the response to see if it is valid |

## Use the external system

[Grouper Okta provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner)
