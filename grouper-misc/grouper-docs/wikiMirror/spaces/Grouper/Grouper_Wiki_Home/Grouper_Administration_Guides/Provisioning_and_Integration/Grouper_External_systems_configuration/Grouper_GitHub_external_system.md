---
title: "Grouper GitHub external system"
space: Grouper
pageId: 171835422
version: 1
lastUpdated: 2026-08-30T06:37:00.937Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/171835422/Grouper+GitHub+external+system
---

## GitHub external system

See also: [Grouper GitHub provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/171868186/Grouper+GitHub+provisioner) | [GitHub provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/171769876/Grouper+GitHub+provisioner+developer+notes)

GitHub authenticates with a single bearer token, so the standard WsBearerToken external system is used directly: the token goes in the accessTokenPassword field and the provisioner attaches it as an Authorization: Bearer header on every request (plus the two standard GitHub headers).

## Configuration

Create a WsBearerToken external system with the following settings:

| Setting | Value |
| --- | --- |
| httpAuthnType | bearerToken |
| endpoint | [https://api.github.com](https://api.github.com) |
| accessTokenPassword | the GitHub token (see below) |
| pageSize | optional; defaults to 100 (the GitHub max) |

## accessTokenPassword (the token)

The accessTokenPassword is the raw GitHub token. It is encrypted at rest by Grouper morphString encryption. It may be a fine-grained personal access token, a classic personal access token, or a GitHub App installation token.

ghp_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXThe provisioner attaches these headers to every REST request:

Authorization: Bearer <token> Accept: application/vnd.github+json X-GitHub-Api-Version: 2022-11-28

## Token permissions

- The token identity must be an **organization owner** of every managed org, so it can invite people, read members, and manage team membership.
- If SAML identity resolution is used (the provisioner githubEnterpriseSlug is set), the token must also be an **enterprise owner**, so it can read the enterprise SAML external-identities map (nameId to login) via GraphQL.
- The same token and endpoint serve both the REST calls and the GraphQL call ([https://api.github.com/graphql](https://api.github.com/graphql)).
