---
title: "Grouper Adobe provisioner"
space: Grouper
pageId: 28555458
version: 14
lastUpdated: 2026-07-01T05:38:10.357Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555458/Grouper+Adobe+provisioner
---

Initial release was in v4.16.0. However you are always encouraged to use the most recent "LATEST STABLE" release.

Adobe has strict rate limiting so you should set thread count to be 1

In the external system set sleep seconds to 6000 ([since max 10 calls per minute per client](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html#actionThrottle))

Advice

- User userName as an entity attribute
- Case insensitive compare on username and email
- Do not provision if null (username and email)
- Do not update (in CRUD) (username)
- Username is EPPN in FederatedID log in
- Search attribute is email
- Match attributes are: userName, ID, email

## Links

- (Log in) [https://adminconsole.adobe.com/](https://adminconsole.adobe.com/)
- (Docs) [https://adobe-apiplatform.github.io/umapi-documentation/en/](https://adobe-apiplatform.github.io/umapi-documentation/en/)

## External system

[Use an Oauth Web service external system for Adobe](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547372/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Adobe)

| Config | Example | Description |
| --- | --- | --- |
| Config id | adobe  would be in config key:  grouper.wsBearerToken.adobe.scopes | Used in configuration file grouper-loader.properties |
| Authentication type | oauthClientCredentials | Bearer token: just an Authentication header with a value (token can have a prefix, e.g. Bearer: )  Basic auth: Authentication header with basic auth standard |
| Token URL | [https://ims-na1.adobelogin.com/ims/token/v2](https://ims-na1.adobelogin.com/ims/token/v2) | URL for authorization to get a token |
| Service URL | [https://usermanagement.adobe.io/v2/usermanagement](https://usermanagement.adobe.io/v2/usermanagement) | URL for the services |
| Client id | sdf6786sdaf876 | Oauth client id for token URL |
| Client secret | sdf79asdf897as | Oauth client secret for token URL |
| Grant type | client_credentials | Oauth strategy |
| Scopes | openid,AdobeID,user_management_sdk | Oauth scopes |
| API key header name | x-api-key | Adobe key |
| API key password | fd76asdf876 | Adobe key |
| Proxy URL | [https://some.server.com:1234](https://some.server.com:1234) | If you are using a proxy server (not reverse proxy), enter that URL |
| Proxy type | PROXY_HTTP, PROXY_SOCKS5 | Proxy protocol |
| Enabled | true \| false | If this is enabled and can be used |
| Test URL suffix | /groups/5DE01@AdobeOrg/0 | Gets the first page of groups, put your org ID in there |
| Test HTTP method | GET | HTTP method for test call |
| Test HTTP response code | 200 | Response code expected |
| Test response body regex | .*adminGroupName.* | Run this regex on the response to see if it is valid |

## Provisioning general

Input the Org ID and optionally the user type on create.

Provisioning type is membershipObjects

## Provisioning groups

You can search by name or id. You should cache the name and id.

#### [API documentation](https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html)

| Grouper name | Type | Required? | Adobe API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | groupId | This is the id read from Adobe. Select only. This should not be translated from Grouper, and the target attribute should be cached.  Note: this is a number in JSON but it is a String type in the provisioner |
| name | String | required | name | This is the name of the group on the Adobe side. |

## Provisioning users

You can search by email only. You should cache the email and id.

#### [API documentation](https://adobe-apiplatform.github.io/umapi-documentation/en/api/getUser.html)

| Grouper name | Type | Required? | Adobe API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | groupId | This is the id read from Adobe. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| email | String | required | name | Email which will be the username too |
| firstname | String | required usually | firstname | [Docs](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#user-information) |
| lastname | String | required usually | lastname | [Docs](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#user-information) |
| country | String | required usually | country | e.g. US. You can hard code if you want in the provisioner translation |
| emailsForLookup   v5.21.4+ | String | no | N/A | If email does not match userName, this can be multi-valued, or single-valued comma separated, and will look up the user based on those emails (and will use the email attribute too) |

## Loading

You can load groups, users and memberships from the target.

You need to Select all groups and users from target on full sync. And load entities

You will see users, groups, and memberships in the database. You can report on them, load then into groups (once or ongoing), etc

## Sample config

Carefully review these settings if you are making a similar provisioner

## Developer notes

[Developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792592/Grouper+Adobe+provisioner+developer+notes)
