---
title: "Jamf external system"
space: Grouper
pageId: 110952449
version: 1
lastUpdated: 2026-08-06T14:49:12.396Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/110952449/Jamf+external+system
---

See also: [Jamf Pro API client credentials](https://developer.jamf.com/jamf-pro/docs/client-credentials)

The Jamf provisioner authenticates to the Jamf Pro Classic API with an OAuth 2.0 client-credentials token. Credentials live in a Grouper `WsBearerToken` external system configured for the client-credentials grant; Grouper fetches, caches, and refreshes the short-lived bearer token automatically.

## Create the Jamf API client

In Jamf Pro: **Settings -> System -> API roles and clients**. Create an API role granting at least: Read Accounts, Read Account Groups, Create User, Update User, and Update Account Groups. Create an API client bound to that role and copy its `client_id` and `client_secret`.

## Configuration

Create a new WsBearerToken external system in the Grouper UI configured for the client-credentials grant:

| Setting | Value | Notes |
| --- | --- | --- |
| httpAuthnType | oauthClientCredentials | Grouper POSTs to the token endpoint and attaches the resulting bearer token. |
| endpoint | https://YOUR_TENANT.jamfcloud.com | Jamf Pro base URL. The provisioner appends `/JSSResource/accounts/...`. |
| tokenUrl | https://YOUR_TENANT.jamfcloud.com/api/oauth/token | OAuth token endpoint. |
| grantType | client_credentials |  |
| clientCredentialType | secret | Client id + secret (not public/private key). |
| clientId | the API client id |  |
| clientSecret | the API client secret | Encrypted by Grouper. |

**Important:** leave `sendParametersInBody` at its default (true) and `sendClientAuthorizationBasicHttpHeader` at its default (false). Jamf requires `client_id`/`client_secret` in the request *body*; sending them as a Basic auth header returns `401 invalid_client`.

## Example configuration properties

# External system config id: jamfProd grouper.wsBearerToken.jamfProd.httpAuthnType = oauthClientCredentials grouper.wsBearerToken.jamfProd.endpoint = https://YOUR_TENANT.jamfcloud.com grouper.wsBearerToken.jamfProd.tokenUrl = https://YOUR_TENANT.jamfcloud.com/api/oauth/token grouper.wsBearerToken.jamfProd.grantType = client_credentials grouper.wsBearerToken.jamfProd.clientCredentialType = secret grouper.wsBearerToken.jamfProd.clientId = your-client-id grouper.wsBearerToken.jamfProd.clientSecret = your-client-secret

## Verifying connectivity

Fetch a token, then list accounts:

curl -s -X POST "https://YOUR_TENANT.jamfcloud.com/api/oauth/token" \ -H "Content-Type: application/x-www-form-urlencoded" \ -d "client_id=YOUR_CLIENT_ID" -d "client_secret=YOUR_CLIENT_SECRET" -d "grant_type=client_credentials" curl -s "https://YOUR_TENANT.jamfcloud.com/JSSResource/accounts" \ -H "Accept: application/xml" -H "Authorization: Bearer YOUR_ACCESS_TOKEN"A successful token response returns `access_token`; the accounts call returns an `<accounts>` XML document with `<users>` and `<groups>`.
