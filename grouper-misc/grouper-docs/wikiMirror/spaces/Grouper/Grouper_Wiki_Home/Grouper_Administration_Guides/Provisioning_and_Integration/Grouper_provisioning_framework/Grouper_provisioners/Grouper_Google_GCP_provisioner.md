---
title: "Grouper Google GCP provisioner"
space: Grouper
pageId: 28554610
version: 30
lastUpdated: 2026-07-01T05:39:59.927Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554610/Grouper+Google+GCP+provisioner
---

## External system

[Setup the external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548243/Grouper+Google+external+system)

## Movie

[Setup the google provisioner managing groups and entities (and memberships)](https://www.youtube.com/watch?v=Mdh644gOGxY)

## Provisioner

Grouper uses directory APIs to manage groups. Group fields and attributes are below. [Documentation](https://developers.google.com/admin-sdk/directory/v1/guides). [API](https://developers.google.com/admin-sdk/directory/reference/rest). [Settings.](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups)

Advice

- Provisioning type is membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

[Additional settings](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource)

| **Group attributes. [Documentation](https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups). [API](https://developers.google.com/admin-sdk/directory/reference/rest/v1/groups).** |
| --- |
| Grouper name | Type | Required? | Description |
| id | String | required | UUID read from GCP. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| name | String | required for create | Name of the group in GCP. Note: required for group create. If not creating groups you need name or email. |
| email | String | required for create | Unique email address of the group. Note: required for group create. If not creating groups you need name or email. |
| description | String | optional | Description of the group |
| whoCanAdd | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| whoCanJoin | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| whoCanViewMembership | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| whoCanViewGroup | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| whoCanInvite | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| allowExternalMembers | Boolean | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| whoCanPostMessage | String | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |
| allowWebPosting | Boolean | optional | Valid values are listed at [https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource](https://developers.google.com/admin-sdk/groups-settings/v1/reference/groups#resource) |

| **Entity attributes**. [Documentation](https://developers.google.com/admin-sdk/directory/v1/guides/manage-users). [API](https://developers.google.com/admin-sdk/directory/v1/guides/manage-users).**** |
| --- |
| Grouper name | Type | Required? | Description |
| id | String | required | UUID read from GCP. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| email | String | required | email address of the user. In GCP, it's called primaryEmail. |
| familyName | String | required for create | Family name (Last name). Note: required for entity create. If not creating entities you need the email and id. |
| givenName | String | required for create | Given name (First name). Note: required for entity create. If not creating entities you need the email and id. |
