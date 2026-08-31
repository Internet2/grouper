---
title: "Grouper Box Provisioner"
space: Grouper
pageId: 28555350
version: 13
lastUpdated: 2026-07-12T15:27:12.137Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555350/Grouper+Box+Provisioner
---

> The info on this page applies to Grouper v4 and above.

## External System

[Grouper Box External System](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547924/Grouper+Box+External+System)

## Links

- [Box API reference](https://developer.box.com/reference/)
- [Box dev console](https://app.box.com/developers/console)

## Grouper development team testing

Set this in grouper.hibernate.properties (or set env var: GROUPER_MOCK_SERVICES=true)

```
grouper.is.mockServices = true
```

## Provisioning attributes

Advice

- Provisioning type is membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

#### Group attributes. [API](https://developer.box.com/reference/resources/group/)

| Grouper name | Type | Required? | Box API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | id | This is the id read from Box. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| name | String | required | name | This is the name of the group on the Box side. |
| description | String | optional | description | This is the description in Box |
| canInviteAsCollaborator | Boolean | optional | permissions.can_invite_as_collaborator | Select only |
| externalSyncIdentifier | String | optional | external_sync_identifier | An arbitrary identifier that can be used by external group sync tools to link this Box Group to an external group.  Example values of this field could be an **Active Directory Object ID** or a **Google Group ID**.  We recommend you use of this field in order to avoid issues when group names are updated in either Box or external systems. |
| groupType | String | optional | group_type | The type of the group. |
| invitabilityLevel | String | optional | invitability_level | Specifies who can invite the group to collaborate on folders. |
| memberViewabilityLevel | String | optional | member_viewability_level | Specifies who can see the members of the group. |
| provenance | String | optional | provenance | Keeps track of which external source this group is coming, for example `Active Directory`, or `Okta`. |
| type | String | optional | type | value is always "group" |

#### Entity attributes. [API](https://developer.box.com/reference/resources/user--full/)

| Grouper name | Type | Required? | Box API | Description |
| --- | --- | --- | --- | --- |
| id | String | required | id | This is the id read from Box. Select only. This should not be translated from Grouper, and the target attribute should be cached. |
| name | String | required | name | This is the name in Box. |
| login | String | mostly | login | The email address the user uses to log in. Required, unless `is_platform_access_only` is set to `true.` |
| canSeeManagedUsers | boolean | optional | can_see_managed_users | Whether the user can see other enterprise users in their contact list |
| isExemptFromDeviceLimits | boolean | optional | is_exempt_from_device_limits | Whether to exempt the user from enterprise device limits |
| isExemptFromLoginVerification | boolean | optional | is_exempt_from_login_verification | Whether the user must use two-factor authentication |
| isExternalCollabRestricted | boolean | optional | is_external_collab_restricited | Whether the user is allowed to collaborate with users outside their enterprise |
| isPlatformAccessOnly | boolean | optional | is_platform_access_only | Specifies that the user is an app user. |
| isSyncEnabled | boolean | optional | is_sync_enabled | Whether the user can use Box Sync |
| maxUploadSize | integer | optional | max_upload_size | The maximum individual file size in bytes the user can have |
| role | string | optional | role | The user’s enterprise role. Value is one of `coadmin`,`user` |
| spaceAmount | integer | optional | space_amount | The user’s total available space in bytes. Set this to `-1` to indicate unlimited storage. |
| spaceUsed | integer | optional | space_used | The amount of space in use by the user |
| status | string | optional | status | The user's account status  Value is one of `active`,`inactive`,`cannot_delete_edit`,`cannot_delete_edit_upload` |
| type | string | optional | type | value is always "user" |
