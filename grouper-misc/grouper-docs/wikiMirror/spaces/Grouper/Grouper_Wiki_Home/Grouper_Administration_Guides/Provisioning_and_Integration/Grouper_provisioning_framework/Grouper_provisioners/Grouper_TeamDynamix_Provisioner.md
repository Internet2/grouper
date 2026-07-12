---
title: "Grouper TeamDynamix Provisioner"
space: Grouper
pageId: 28555382
version: 5
lastUpdated: 2026-07-12T15:27:12.591Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555382/Grouper+TeamDynamix+Provisioner
---

> The info on this page applies to Grouper v4 and above.

## External System

[Grouper TeamDynamix External System](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547992/Grouper+TeamDynamix+External+System)

## Links

- [TeamDynamix API reference](https://rollins.teamdynamix.com/TDWebApi/)

## Provisioning attributes

Advice

- Provisioning type is hardcoded to membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

#### Group attributes. [API](https://solutions.teamdynamix.com/TDWebApi/Home/type/TeamDynamix.Api.Users.Group)

| Grouper name | Type | Required? | TeamDynamix API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | ID | This is the id read from TeamDynamix. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| name | String | required | Name | This is the name of the group on the TeamDynamix side. |
| description | String | optional | Description | This is the description in TeamDynamix |

#### Entity attributes. [API](https://solutions.teamdynamix.com/TDWebApi/Home/type/TeamDynamix.Api.Users.NewUser)

| Grouper name | Type | Required? | TeamDynamix API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | UID | This is the id read from TeamDynamix. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| firstName | String | required | FirstName | This is the first name of the user in TeamDynamix. |
| lastName | String | required | LastName | This is the last name of the user in TeamDynamix. |
| externalId | String | required | ExternalId | External id of the user |
| primaryEmail | String | required | PrimaryEmail | The primary email of the user in TeamDynamix. |
| company | String | required | Company | The company of the user. |
| securityRoleId | String | required | SecurityRoleId | The UID of the global security role associated with the user. |
| userName | String | required | UserName | The username of the user, for use when using TeamDynamix authentication. |
