---
title: "Grouper provisioning SCIM for Atlassian example with local entities"
space: Grouper
pageId: 28565516
version: 5
lastUpdated: 2026-07-01T05:35:18.348Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28565516/Grouper+provisioning+SCIM+for+Atlassian+example+with+local+entities
---

This provisioner works with Grouper v4.4.0+

## External system

[Setup the external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547461/Grouper+Atlassian+cloud+SCIM2+external+system)

## Movie

[Setup the Atlassian provisioner managing groups and entities (and memberships) with local entities modeled as external subjects](https://www.youtube.com/watch?v=WrWiFaA3B8w)

## Provisioner

Grouper uses directory APIs to manage groups. Group fields and attributes are below. [Documentation](https://support.atlassian.com/provisioning-users/docs/understand-user-provisioning/). [API](https://developer.atlassian.com/cloud/admin/user-provisioning/rest/intro/#auth).

Advice

- Provisioning type is membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

| **Group attributes. [API](https://developer.atlassian.com/cloud/admin/user-provisioning/rest/api-group-groups/#api-group-groups).** |
| --- |
| Grouper name | Type | Required? | Description |
| id | String | required | UUID read from Atlassian. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| displayName | String | required | This is how the group shows up in atlassian. Recommended to map from extension (if all groups in one folder for uniqueness)   or name. Do not map from the Grouper displayName since that can change. |

| **Entity attributes**. [API](https://developer.atlassian.com/cloud/admin/user-provisioning/rest/api-group-users/#api-group-users).**** |
| --- |
| Grouper name | Type | Required? | Description |
| id | String | required | UUID read from Atlassian. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| userName | String | required | Username of the user logging in |
| emailValue | String | required | Email of the user (can be same as userName) |

## Example provisioner with local entities modeled as external subjects
