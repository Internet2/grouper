---
title: "Grouper provisioning SCIM for GitHub"
space: Grouper
pageId: 28564232
version: 3
lastUpdated: 2026-07-01T05:35:28.702Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564232/Grouper+provisioning+SCIM+for+GitHub
---

This is for v4+

[https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1](https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1)

## Github SCIM Provisioning

[https://docs.github.com/en/enterprise-cloud@latest/rest/scim?apiVersion=2022-11-28#provision-and-invite-a-scim-user](https://docs.github.com/en/enterprise-cloud@latest/rest/scim?apiVersion=2022-11-28#provision-and-invite-a-scim-user)

Github only supports SCIM for user operations. An organization must already exist for which members need to be managed. If you want to manage memberships of multiple organizations, configure a separate external system for each organization.

#### User fields and attributes

| Grouper name | Attribute or field | Type | Required? | Description |
| --- | --- | --- | --- | --- |
| id | field | String | required | UUID read from Github. Select only. |
| userName | attribute | String | required | User name |
| displayName | attribute | String | optional | Display name of the user |
| familyName | attribute | String | required | Family name (Last name) |
| givenName | attribute | String | required | Given name (First name) |
| externalId | attribute | String | optional | External id |
| formattedName | attribute | String | optional | Formatted name e.g Mr. John Smith, II |
| emailValue | attribute | String | required | Email value e.g. [test@example.com](mailto:test@example.com) |
| emailType | attribute | String | optional | Email type e.g. work |

## Configure SCIM settings in Github for development purposes

1. Go to Settings → Develop settings → Personal access tokens.
2. Generate a new token and keep it safe. You will need it when configuring the external system.
3. In your Github organization, you need SAML. For our testing we set up SAML integration between Github and Onelogin. You will need to set up an account on Onelogin. Github and Onelogin both offer trial versions for a few days.
4. The SCIM URL that you need to enter while configuring the external system would look like: [https://api.github.com/scim/v2/organizations/yourOrgName/](https://api.github.com/scim/v2/organizations/yourOrgName/)
5. Here is a video that shows how to integrate Onelogin with the Github organization (though you should integrate with your own saml).
