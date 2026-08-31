---
title: "Dropbox external system"
space: Grouper
pageId: 28547380
version: 2
lastUpdated: 2026-07-01T05:46:58.074Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547380/Dropbox+external+system
---

See also: [Dropbox Team (Business) HTTP API documentation](https://www.dropbox.com/developers/documentation/http/teams)

Dropbox uses OAuth2 for all Team API operations. The credential is stored in the Grouper WsBearerToken external system's `accessTokenPassword` field, and the provisioner attaches a bearer access token as an `Authorization: Bearer` header on every Team API request.

The `accessTokenPassword` accepts **two formats** (see "Token format" below):

- **OAuth2 refresh-token config (recommended for production)** -- a JSON object `{"appKey": "...", "appSecret": "...", "refreshToken": "..."}`. The provisioner exchanges the refresh token for a short-lived access token at runtime and caches it, refreshing automatically before it expires. This is the durable option for modern scoped apps, whose access tokens expire after 4 hours.
- **Plain bearer token** -- a single token string, used verbatim. Only durable if it is a legacy long-lived token; a token generated from the App Console for a scoped app is short-lived (4 hours) and is suitable only for quick testing.

## Configuration

Create a new WsBearerToken external system in the Grouper UI and configure the following settings:

| Setting | Value | Notes |
| --- | --- | --- |
| httpAuthnType | bearerToken | Default value, select from dropdown |
| endpoint | [https://api.dropboxapi.com](https://api.dropboxapi.com) | The Dropbox Business Team API endpoint. In tests this points at the mock service path (.../mockServices/dropbox) instead. |
| accessTokenPassword | `{"appKey": "...", "appSecret": "...", "refreshToken": "..."}`  or  `sl.AbCdEf...your-team-token...` | Either the OAuth2 refresh-token JSON config (recommended) or a single plain bearer token (see "Token format" and "Getting your token" below). This value is encrypted by Grouper. |

**Note:** the "test connection" button in the Grouper UI sends the `accessTokenPassword` as a literal bearer token, so it only works when a **plain token** is configured. When the refresh-token JSON config is used, verify instead with a provisioner diagnostic / full sync (or the curl flow below), since the value is a config, not a usable bearer token.

## Token format

The `accessTokenPassword` field is entered as a single string in the encrypted password field of the WsBearerToken external system. It accepts either of the following:

**1. OAuth2 refresh-token config (recommended).** A single-line JSON object with the app key, app secret, and a refresh token. The provisioner exchanges the refresh token at `{endpoint}/oauth2/token` for a short-lived access token, caches it, and refreshes it automatically about 5 minutes before its (4-hour) expiry. No `Bearer` prefix; the provisioner builds the Authorization header from the minted access token.

{"appKey": "abc123appkey", "appSecret": "xyz789appsecret", "refreshToken": "sl.u.AF...long-refresh-token..."}**2. Plain bearer token.** The token string verbatim, with no JSON wrapper and no `Bearer` prefix (the provisioner adds the prefix). Only durable if it is a legacy long-lived token; a scoped-app token from the App Console expires after 4 hours.

sl.AbCdEfGhIjKlMnOpQrStUvWxYz0123456789-your-team-token

## Example configuration properties

# External system config id: dropboxProd grouper.wsBearerToken.dropboxProd.endpoint = https://api.dropboxapi.com grouper.wsBearerToken.dropboxProd.accessTokenPassword = sl.AbCdEf...your-team-token...Note: in practice the accessTokenPassword value would be encrypted. The above shows the plaintext for illustration.

## Verifying connectivity

To verify the token, use curl to call the team groups list endpoint. All Dropbox Team API calls are POST with a JSON body (use `{}` or a minimal body for list calls):

curl -X POST "https://api.dropboxapi.com/2/team/groups/list" \ -H "Authorization: Bearer YOUR_TEAM_TOKEN" \ -H "Content-Type: application/json" \ --data '{"limit": 100}'A successful response (HTTP 200) with a JSON body containing a `groups` array confirms that the token and endpoint are valid. A 401 indicates the token is missing the required scopes or is invalid.

## Getting your token

Create a scoped Dropbox Business team app and generate a team access token in the Dropbox App Console:

1. Sign in to the [Dropbox App Console](https://www.dropbox.com/developers/apps) as a team admin.
2. Click **Create app**. Choose the **Scoped access** API. For access type, choose a **team** app (the "Team" / "Dropbox Business" option) so the token can manage team members and team groups, not just a single user's files.
3. On the new app's **Permissions** tab, enable the team member and team group scopes the provisioner needs:
  
  - `members.read` -- list and look up team members
  - `members.write` -- add, update, suspend, and remove team members
  - `members.delete` -- remove members from the team (if separated from members.write in your console)
  - `groups.read` -- list groups and read group membership
  - `groups.write` -- create, update, delete groups and change group membership
  - **Member admin** scope (e.g. `team_info.read` plus the admin-management scope that backs `members/set_admin_permissions_v2`) -- only needed if the admin-role overlay is used.
  
  Click **Submit** to save the scopes. Note: if you change scopes after a token was generated, you must generate a new token for the new scopes to take effect.
4. **App key and secret** are on the **Settings** tab under "App key" / "App secret".

**For quick testing**, on the **Settings** tab under **OAuth 2** click **Generate** next to "Generated access token" and use that value as a plain `accessTokenPassword`. Note this is a short-lived (4-hour) token for a scoped app -- it will stop working, so it is for testing only.

**For production, obtain a refresh token** (a one-time, team-admin authorization). Run the OAuth2 authorization-code flow with offline access:

1. In a browser, visit the authorize URL with your app key and `token_access_type=offline`:  
  `[https://www.dropbox.com/oauth2/authorize?client_id=YOUR_APP_KEY&token_access_type=offline&response_type=code](https://www.dropbox.com/oauth2/authorize?client_id=YOUR_APP_KEY&token_access_type=offline&response_type=code)`
2. Sign in as a **team admin** and approve. Dropbox shows an authorization **code**.
3. Exchange the code for tokens (this returns the long-lived `refresh_token`):curl https://api.dropboxapi.com/oauth2/token \ -d code=THE_CODE \ -d grant_type=authorization_code \ -d client_id=YOUR_APP_KEY \ -d client_secret=YOUR_APP_SECRET
4. Take the `refresh_token` from the response and assemble the `accessTokenPassword` JSON config: `{"appKey":"YOUR_APP_KEY","appSecret":"YOUR_APP_SECRET","refreshToken":"THE_REFRESH_TOKEN"}`. The provisioner mints and refreshes 4-hour access tokens from it automatically.

Treat the app secret and refresh token as secrets; Grouper stores the whole value encrypted. The refresh token is the durable proof of the team admin's consent -- the app key and secret alone cannot mint a team token (Dropbox has no client-credentials grant for the Team API). See your Dropbox documentation for details on scoped team apps, OAuth scopes, and token lifetimes.
