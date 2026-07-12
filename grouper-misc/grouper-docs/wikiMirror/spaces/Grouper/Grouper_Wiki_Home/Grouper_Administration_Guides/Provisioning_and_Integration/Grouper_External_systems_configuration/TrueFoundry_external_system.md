---
title: "TrueFoundry external system"
space: Grouper
pageId: 28549505
version: 5
lastUpdated: 2026-07-01T05:41:53.284Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549505/TrueFoundry+external+system
---

See also: [TrueFoundry SDK documentation](https://www.truefoundry.com/docs/truefoundry_sdk)

TrueFoundry uses two separate tokens for authentication: a native API token for all standard REST API operations, and a SCIM token for display name updates via the SCIM v2 endpoint. Both tokens are stored in the Grouper WsBearerToken external system's `accessTokenPassword` field as a JSON string.

## Configuration

Create a new WsBearerToken external system in the Grouper UI and configure the following settings:

| Setting | Value | Notes |
| --- | --- | --- |
| httpAuthnType | bearerToken | Default value, select from dropdown |
| endpoint | [https://app.truefoundry.com](https://app.truefoundry.com) | The TrueFoundry API endpoint (always this URL regardless of vanity domain) |
| accessTokenPassword | `{"apiToken": "...", "scimToken": "..."}` | JSON string containing both tokens (see "Getting your tokens" below). This value will be encrypted by Grouper. |

**Note:** The "test connection" button in the Grouper UI will not work for this external system because the accessTokenPassword is a JSON string, not a plain bearer token. Use the curl commands below to verify connectivity instead.

## Token JSON format

The `accessTokenPassword` field must contain a JSON string with two keys:

{"apiToken": "your-native-api-token", "scimToken": "your-scim-bearer-token"}

| Key | Required? | Description |
| --- | --- | --- |
| `apiToken` | Yes | Bearer token for all native REST API operations (users, teams, roles). Obtained from a virtual account credential. |
| `scimToken` | Only if SCIM display name updates are enabled | Bearer token for the SCIM v2 endpoint. Obtained from Settings → SSO → SCIM configuration. If SCIM is not configured on the provisioner (`trueFoundryScimTenantName` and `trueFoundryScimSsoId` are blank), this key can be omitted. |

## Example configuration properties

# External system config id: trueFoundryProd grouper.wsBearerToken.trueFoundryProd.endpoint = https://app.truefoundry.com grouper.wsBearerToken.trueFoundryProd.accessTokenPassword = {"apiToken": "abc123", "scimToken": "def456"}Note: in practice the accessTokenPassword value would be encrypted. The above shows the plaintext for illustration.

## Verifying connectivity

To verify the native API token, use curl to call the subjects endpoint:

curl -H "Authorization: Bearer YOUR_API_TOKEN" \ "https://app.truefoundry.com/api/svc/v1/subjects?query=&limit=1&offset=0&showInvalidUsers=true"A successful response (HTTP 200) with a JSON body containing a `users` key confirms that the API token and endpoint are valid.

To verify the SCIM token, use curl to call the SCIM Users endpoint:

curl -H "Authorization: Bearer YOUR_SCIM_TOKEN" \ "https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId}/Users"A successful response (HTTP 200) with a SCIM ListResponse confirms the SCIM token is valid.

## Getting your tokens

### Native API token

Create a virtual account and bearer token credential in the TrueFoundry dashboard:

1. Navigate to **Access > Access management > Virtual accounts**.
2. Create a new virtual account and assign it the **tenant admin** role. This is required so that the provisioner can manage teams and team memberships across the tenant.
3. On the virtual account, create a new **credential** (bearer token). Copy the token value — this is the `apiToken` value.

### SCIM token

The SCIM token is obtained from the TrueFoundry SSO/SCIM configuration:

1. Navigate to **Settings → SSO** in the TrueFoundry UI.
2. Locate the SCIM configuration section. The SCIM URL has the form `[https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId](https://app.truefoundry.com/api/svc/v1/scim/v2/{tenantName}/{ssoId)}`.
3. Copy the SCIM bearer token — this is the `scimToken` value.
4. The `{tenantName}` and `{ssoId}` segments from the SCIM URL are configured on the provisioner as `trueFoundryScimTenantName` and `trueFoundryScimSsoId`.

See your TrueFoundry documentation for details on virtual accounts, credential management, and SCIM configuration.
