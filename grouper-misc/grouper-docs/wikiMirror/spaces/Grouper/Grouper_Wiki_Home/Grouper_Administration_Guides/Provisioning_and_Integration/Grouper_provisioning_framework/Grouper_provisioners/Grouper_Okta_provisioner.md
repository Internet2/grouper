---
title: "Grouper Okta provisioner"
space: Grouper
pageId: 28554255
version: 7
lastUpdated: 2026-07-01T05:40:50.328Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554255/Grouper+Okta+provisioner
---

## Links

- (Log in) [https://trial-8031936-admin.okta.com/admin/dashboard](https://trial-8031936-admin.okta.com/admin/dashboard) (change to your own instance)
- (Docs) [https://developer.okta.com/docs/api/](https://developer.okta.com/docs/api/)

## External system

[Okta external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta)

## Provisioning general

Provisioning type is membershipObjects

## Provisioning groups

You can search by name or id. You should cache the name and id.

#### [API documentation](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/)

| Grouper name | Type | Required? | Okta API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | id | This is the id read from Okta. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| name | String | required | profile.name | This is the name of the group on the Okta side. |
| description | String | not required | profile.description | This is the description of the group on the Okta side. |

## Provisioning users

You should cache the login and id.

#### [API documentation](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/)

| Grouper name | Type | Required? | Okta API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | id | This is the id read from Okta. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| email | String | required | profile.email | Email address |
| firstName | String | required | profile.firstName | First name of the user |
| lastName | String | required | profile.lastName | Last name of the user |
| login | String | required | profile.login | Login username (email format) of the user |

Okta Provisioner also supports custom attributes for users. The name of the custom attribute must begin with "profile." The next three screenshots below show how to configure a custom attribute:

## Sample config

Carefully review these settings if you are making a similar provisioner

## Developer notes

[Developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792584/Grouper+Okta+provisioner+developer+notes)
